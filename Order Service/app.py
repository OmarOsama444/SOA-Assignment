from flask import Flask , request , jsonify
import requests
from flask_sqlalchemy import SQLAlchemy
import mysql.connector
from mysql.connector import Error
from request_schemas import *
from marshmallow import ValidationError
import os
app = Flask(__name__)

db_user = os.getenv("MYSQL_USER" , "myuser")
db_password = os.getenv("MYSQL_PASSWORD", "mypassword")
db_host = os.getenv("MYSQL_HOST", "localhost")
db_port = os.getenv("MYSQL_PORT", "3306")
db_name = os.getenv("MYSQL_DATABASE", "ecommerce_system")

order_service_url=os.getenv("ORDER_SERVICE_URL" , "http://localhost:5001")
customer_service_url=os.getenv("CUSTOMER_SERVICE_URL" , "http://localhost:5002")
inventory_service_url=os.getenv("INVENTORY_SERVICE_URL" , "http://localhost:5003")
notification_service_url=os.getenv("NOTIFICATION_SERVICE_URL" , "http://localhost:5004")
pricing_service_url=os.getenv("PRICING_SERVICE_URL","http://localhost:5005")



@app.route("/api/orders/create", methods=["POST"])
def create_order():
    json_data = request.get_json()
    try:
        data = OrderSchema().load(json_data)
    except ValidationError as err:
        return jsonify({"errors": err.messages}), 400
    customer = requests.get(f"{customer_service_url}/api/customers/{data['customer_id']}", timeout=2)
    if customer.status_code != 200:
        return jsonify({"message": "Customer not found"}), 404
    # Notify Inventory Service
    try:
        r = requests.post(f"{inventory_service_url}/api/inventory/update", json={ "products" : data["products"] }, timeout=2)
        if r.status_code == 404:
            return jsonify({"message": "One or more products not found in inventory"}), 404
        elif r.status_code == 400:
            return jsonify({"message": "Insufficient inventory for one or more products"}), 400
        elif r.status_code == 200:
            price_request = {
                "products": data["products"],
            }
            r2 = requests.post(f"{pricing_service_url}/api/pricing/calculate", json=price_request, timeout=2)
            if r2.status_code == 200:
                pricing_info = r2.json()
                response = {
                    "customer_id": data["customer_id"],
                    "products": pricing_info["products"],
                    "total_amount": pricing_info["total_amount"],
                    "status": "Pending"
                }
                orderid = save_order_to_db(response)
                requests.put(f"{customer_service_url}/api/customers/{data['customer_id']}/loyalty")
                requests.post(f"{notification_service_url}/api/notifications/send", 
                        json={ "order_id": orderid, "customer_id": data["customer_id"] }, timeout=2)
                return jsonify({"message": "Order created successfully", "order": response}), 201
            else:
                return jsonify({"message": "Failed to calculate price"}), 500
    except Exception as e:
        app.logger.warning(f"Failed to create order: {e}")
        return jsonify({"message": "Failed to create order due to inventory service error"}), 500

    

@app.route("/api/orders/<int:order_id>" , methods=["GET"])
def get_order(order_id):
    try :
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM orders WHERE order_id = %s", (order_id,))
        order = cursor.fetchone()
        if not order :
            return jsonify({"message": "Order not found"}), 404
    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500
    finally:
        cursor.close()
        conn.close()
        return jsonify(order), 200
    
@app.route("/api/orders/customer/<int:customer_id>" , methods=["GET"])
def get_orders_by_customer(customer_id):
    try :
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM orders WHERE customer_id = %s", (customer_id,))
        orders = cursor.fetchall()
    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500
    finally:
        cursor.close()
        conn.close()
    return jsonify(orders), 200


def save_order_to_db(order_data):
    try:
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )
        cursor = conn.cursor()
        insert_query = """
                INSERT INTO orders (customer_id, total_amount, status)
                VALUES (%s, %s, %s)
                """
        cursor.execute(insert_query, (
            order_data["customer_id"],
            order_data["total_amount"],
            order_data["status"]
        ))
        order_id = cursor.lastrowid
        conn.commit()
    except mysql.connector.Error as err:
        app.logger.error(f"Database error: {err}")
        raise
    finally:
        cursor.close()
        conn.close()
    return order_id



@app.route("/api/orders/<int:order_id>/status", methods=["PUT"])
def update_order_status(order_id):
    json_data = request.get_json()
    try:
        data = StatusUpdateSchema().load(json_data)
    except ValidationError as err:
        return jsonify({"errors": err.messages}), 400
    # Notify Inventory Service
    try:
        status_dict = {
            "Pending" : 0 ,
            "Approved" : 1 ,
            "Shipped" : 2 ,
            "Delivered" : 3
        } 
        status = data["status"]
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM orders WHERE order_id = %s", (order_id,))
        order = cursor.fetchone()
        if not order :
            return jsonify({"message": "Order not found"}), 404
        old_status = order["status"]
        if ( status_dict[old_status] + 1 ) != status_dict[status] :
            return jsonify({"message": f"Cant change status from {old_status} to {status}"}), 400
        cursor.execute(
            "UPDATE orders SET status = %s WHERE order_id = %s",
            (status, order_id)
        )
        conn.commit()
        return jsonify({
            "message": "Order status updated successfully",
            "old_status": old_status,
            "new_status": status
        }), 200
    except Exception as e:
        app.logger.warning(f"Failed to update order: {e}")
        return jsonify({"message": "Failed to update order status"}), 500
    
if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)



