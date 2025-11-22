from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class Inventory(db.Model):
    __tablename__ = 'inventory'

    product_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    product_name = db.Column(db.String(100), nullable=False)
    quantity_available = db.Column(db.Integer, nullable=False)
    unit_price = db.Column(db.Numeric(10, 2), nullable=False)
    last_updated = db.Column(db.TIMESTAMP, server_default=db.func.current_timestamp())

class PricingRule(db.Model):
    __tablename__ = 'pricing_rules'

    rule_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    product_id = db.Column(db.Integer, db.ForeignKey('inventory.product_id'))
    min_quantity = db.Column(db.Integer)
    discount_percentage = db.Column(db.Numeric(5, 2))

class TaxRate(db.Model):
    __tablename__ = 'tax_rates'

    region = db.Column(db.String(50), primary_key=True)
    tax_rate = db.Column(db.Numeric(5, 2))

class Customer(db.Model):
    __tablename__ = 'customers'

    customer_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(100), unique=True, nullable=False)
    phone = db.Column(db.String(20))
    loyalty_points = db.Column(db.Integer, default=0)
    created_at = db.Column(db.TIMESTAMP, server_default=db.func.current_timestamp())

class NotificationLog(db.Model):
    __tablename__ = 'notification_log'

    notification_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    order_id = db.Column(db.Integer, nullable=False)
    customer_id = db.Column(db.Integer, db.ForeignKey('customers.customer_id'), nullable=False)
    notification_type = db.Column(db.String(50))
    message = db.Column(db.Text)
    sent_at = db.Column(db.TIMESTAMP, server_default=db.func.current_timestamp())
