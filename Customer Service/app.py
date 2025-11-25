from flask import Flask , render_template , jsonify
import requests;
import mysql.connector
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

@app.route("/api/customers/<int:customer_id>" , methods=["GET"])
def GetCustomerProfile(customer_id):
    try :
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM customers WHERE customer_id = %s", (customer_id,))
        customer = cursor.fetchone()
    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500
    finally:
        cursor.close()
        conn.close()
    if customer:
        return jsonify(customer), 200
    else:
        return jsonify({"message": "Customer not found"}), 404
    
@app.route("/api/customers/<int:customer_id>/orders" , methods=["GET"])
def GetCustomerOrders(customer_id):
    response = requests.get(f"{order_service_url}/api/orders/customer/{customer_id}")
    if response.status_code != 200:
        return jsonify({"message": "Failed to fetch orders from Order Service"}), 500
    dummy_orders = response.json()
    return jsonify(dummy_orders), 200

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
