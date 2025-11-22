from flask import Flask , request , jsonify
from flask_sqlalchemy import SQLAlchemy
from request_schemas import *
from marshmallow import ValidationError
import os
app = Flask(__name__)

# db_user = os.getenv("MYSQL_USER")
# db_password = os.getenv("MYSQL_PASSWORD")
# db_host = os.getenv("MYSQL_HOST")
# db_port = os.getenv("MYSQL_PORT")
# db_name = os.getenv("MYSQL_DATABASE")

# app.config['SQLALCHEMY_DATABASE_URI'] = f"mysql+mysqlconnector://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"
# app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

@app.route("/api/orders/create", methods=["POST"])
def create_order():
    json_data = request.get_json()
    print("=== RAW JSON RECEIVED ===")
    print(json_data)

    try:
        data = OrderSchema().load(json_data)
    except ValidationError as err:
        return jsonify({"errors": err.messages}), 400  

    return jsonify({"message": "Order created successfully", "order": data}), 201

@app.route("/api/orders/<int:order_id>" , methods=["GET"])
def get_order(order_id):
    dummy_order = {
        "order_id": order_id,
        "customer_id": 123,
        "products": [
            {"product_id": 1, "quantity": 2},
            {"product_id": 2, "quantity": 1}
        ],
        "total_amount": 59.99
    }
    return jsonify(dummy_order), 200

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
