from flask import Flask , request , jsonify, render_template
import requests
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


@app.route("/api/pricing/calculate", methods = ["POST"])
def calculate_price():
    order = request.get_json()
    if not order or "products" not in order:
        return jsonify({"error": "Invalid order payload"}), 400
    conn = mysql.connector.connect(
                    host=db_host,
                    port=db_port,
                    user=db_user,
                    password=db_password,
                    database=db_name
                )
    cursor = conn.cursor()
    total_price = 0.0
    response = {
        "products" : [],
        "total_amount": 0.0
    }
    for p in order["products"]:
        product_id = p.get("product_id")
        qty = p.get("quantity", 0)
        try:
            r = requests.get(f"{inventory_service_url}/api/inventory/check/{product_id}", timeout=2)
            if r.status_code == 200:
                product_info = r.json()
                unit_price = float(product_info.get("unit_price"))
                total_price += unit_price * qty
                cursor.execute(
                    "select discount_percentage, min_quantity from pricing_rules where product_id = %s", (product_id,))
                rules = cursor.fetchall()
                if rules is None or len(rules) < 1 :
                    response["products"].append({
                            "product_id": product_id,
                            "unit_price": unit_price,
                            "quantity": qty,
                            "discount_applied": 0,
                            "total_price_after_discount": (unit_price * qty)
                        })
                    continue
                rule = rules[0]
                discount_percentage, min_quantity = rule
                if qty >= min_quantity:
                    discount_amount = (unit_price * qty) * (float(discount_percentage) / 100)
                    total_price -= discount_amount
                    response["products"].append({
                        "product_id": product_id,
                        "unit_price": unit_price,
                        "quantity": qty,
                        "discount_applied": float(discount_percentage),
                        "total_price_after_discount": (unit_price * qty) - discount_amount
                    })
                else:
                    response["products"].append({
                                "product_id": product_id,
                                "unit_price": unit_price,
                                "quantity": qty,
                                "discount_applied": 0,
                                "total_price_after_discount": (unit_price * qty)
                            }) 
            else:
                return jsonify({"error": f"Product {product_id} not found"}), 404
        except Exception as e:
            app.logger.warning(f"Failed to fetch product info for {product_id}: {e}")
            return jsonify({"message": "Failed to calculate price due to inventory service error"}), 500
    cursor.close()
    conn.close()
    response["total_amount"] = total_price
    return jsonify(response), 200

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
