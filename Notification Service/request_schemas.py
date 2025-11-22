from marshmallow import Schema, fields, validate

class NotificationSchema(Schema):
    order_id = fields.Int(required=True)

