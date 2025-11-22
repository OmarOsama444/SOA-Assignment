-- Create database
CREATE DATABASE IF NOT EXISTS ecommerce_system;
USE ecommerce_system;

-- Inventory table
CREATE TABLE inventory (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    quantity_available INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'Pending'
);

-- Pricing rules
CREATE TABLE pricing_rules (
    rule_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    min_quantity INT,
    discount_percentage DECIMAL(5,2)
);

-- Tax rates
CREATE TABLE tax_rates (
    region VARCHAR(50) PRIMARY KEY,
    tax_rate DECIMAL(5,2)
);

-- Customers
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    loyalty_points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification logs
CREATE TABLE notification_log (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    notification_type VARCHAR(50),
    message TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert inventory
INSERT INTO inventory (product_name, quantity_available, unit_price) VALUES
('Laptop', 50, 999.99),
('Mouse', 200, 29.99),
('Keyboard', 150, 79.99),
('Monitor', 75, 299.99),
('Headphones', 100, 149.99);

-- Insert customers
INSERT INTO customers (name, email, phone, loyalty_points) VALUES
('Ahmed Hassan', 'ahmed@example.com', '01012345678', 100),
('Sara Ahmed', 'sara@example.com', '01098765432', 250),
('Omar Ali', 'omar@example.com', '01055555555', 50);

-- Insert pricing rules
INSERT INTO pricing_rules (product_id, min_quantity, discount_percentage) VALUES
(1, 5, 10.00),
(2, 10, 15.00),
(3, 10, 12.00);

INSERT INTO tax_rates (region, tax_rate) VALUES
('EG', 14.00);