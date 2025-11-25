import os
from flask import Flask , render_template, request, jsonify
import mysql.connector
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

@app.route("/")
def home():
    return render_template("index.html")

@app.route("/api/inventory/check/<int:product_id>", methods=["GET"])
def check_inventory(product_id):
    # Connect to the database
    try:
        conn = mysql.connector.connect(
                host=db_host,
                port=db_port,
                user=db_user,
                password=db_password,
                database=db_name
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT quantity_available FROM inventory WHERE product_id = %s", (product_id,))
        result = cursor.fetchone()
        cursor.close()
        conn.close()
    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500
    if result:
        quantity = result.get("quantity_available", 0)
        return jsonify({"product_id": product_id, "quantity": quantity}), 200
    else:
        return jsonify({"error": "Product not found"}), 404


@app.route("/api/inventory/update", methods=["POST"])
def adjust_inventory_from_order():
    order = request.get_json()

    if not order or "products" not in order:
        return jsonify({"error": "Invalid order payload"}), 400

    conn = None
    cursor = None
    try:
        conn = mysql.connector.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_password,
            database=db_name
        )

        cursor = conn.cursor(dictionary=True)

        for p in order["products"]:
            product_id = p.get("product_id")
            qty = p.get("quantity", 0)

            cursor.execute(
                "SELECT quantity_available FROM inventory WHERE product_id = %s",
                (product_id,)
            )
            row = cursor.fetchone()

            if row is None:
                return jsonify({"message": f"Product with product_id {product_id} not found"}), 404

            current = row.get("quantity_available", 0)
            if current - qty < 0:
                return jsonify({"message": f"Insufficient inventory for product_id {product_id}"}), 400

            new_qty = current - qty
            cursor.execute(
                "UPDATE inventory SET quantity_available = %s WHERE product_id = %s",
                (new_qty, product_id)
            )

        conn.commit()
        return jsonify({"message": "Inventory updated"}), 200

    except mysql.connector.Error as err:
        return jsonify({"message": "Database error", "error": str(err)}), 500

    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()


if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
