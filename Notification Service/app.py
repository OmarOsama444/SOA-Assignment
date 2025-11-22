from flask import Flask , render_template
from flask import request , jsonify
from marshmallow import ValidationError 
from request_schemas import *
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

    return {"message": "Notification sent successfully"}, 200

if __name__ == "__main__":
    app.run(debug=True,host="0.0.0.0", port=5000)
