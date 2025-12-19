from marshmallow import Schema, fields, validate

class ProductSchema(Schema):
    product_id = fields.Int(required=True)
    quantity = fields.Int(required=True, validate=validate.Range(min=1))

class OrderSchema(Schema):
    customer_id = fields.Int(required=True)
    products = fields.List(
        fields.Nested(ProductSchema),
        required=True,
        validate=validate.Length(min=1)
    )
    total_amount = fields.Float(required=True, validate=validate.Range(min=0.01))

class StatusUpdateSchema(Schema):
    status = fields.Str(
        required=True,
        validate=validate.OneOf(
            ["Approved", "Shipped", "Delivered"],
            error="Status must be one of: pending, approved, shipped, delivered"
        )
    )