from flask import Flask , request , jsonify
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
    return jsonify({"message": "Order created successfully", "order": data}), 201

@app.route("/api/orders/<int:order_id>" , methods=["GET"])
def get_order(order_id):
    try :
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        );
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM orders WHERE order_id = %s", (order_id,))
        order = cursor.fetchone()
    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500
    finally:
        cursor.close()
        conn.close()
    if not order:
        return jsonify({"message": "Order not found"}), 404
    else:
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

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
