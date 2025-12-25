from flask import Flask , render_template
from flask import request , jsonify
from marshmallow import ValidationError 
from request_schemas import *
import mysql.connector
import os
import requests
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

app = Flask(__name__)

@app.route("/")
def home():
    return render_template("index.html")

@app.route("/api/notifications/send", methods=["POST"])
def send_notification():
    json_data = request.get_json()
    try:
        data = NotificationSchema().load(json_data)
    except ValidationError as err:
        return jsonify({"errors": err.messages}), 400
    order_id = data["order_id"]
    customer_id = data["customer_id"]
    customer = requests.get(f"{customer_service_url}/api/customers/{customer_id}", timeout=2)
    if not customer:
        return jsonify({"message": "Customer not found"}), 404
    customer_info = customer.json()
    stockreq = requests.get(f"{order_service_url}/api/orders/{order_id}", timeout=2)
    stock = stockreq.json()
    status = stock.get("status", "Unknown")
    print(f"Email sent to {customer_info['email']}")
    print(f"Subject: Order {order_id} Confirmed")
    print(f"Body: Order status is {status}")
    conn = mysql.connector.connect(
        host=db_host,
        port=db_port,
        user=db_user,
        password=db_password,
        database=db_name
    )
    cursor = conn.cursor()
    cursor.execute("INSERT INTO notification_log (order_id, customer_id, notification_type, message) VALUES (%s, %s, %s, %s)",
                   (order_id, customer_id, "email", f"Order status is {status}"))
    conn.commit()
    cursor.close()
    conn.close()
    return {"message": "Notification sent successfully"}, 200

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
