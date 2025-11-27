<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Create Order</title>
</head>
<body>
<h2>Create New Order</h2>

<form action="order" method="post">
    <label>Customer ID:</label>
    <input type="number" name="customer_id" required value="101"><br><br>

    <h3>Product Details</h3>
    <label>Product ID:</label>
    <input type="number" name="product_id" required value="5"><br>

    <label>Quantity:</label>
    <input type="number" name="quantity" required value="2"><br><br>

    <label>Total Amount:</label>
    <input type="number" step="0.01" name="total_amount" required value="50.00"><br><br>

    <button type="submit">Send to Flask Service</button>
</form>

<hr>

<h2>Check Order Status</h2>
<form action="order" method="get">
    <label>Enter Order ID:</label>
    <input type="number" name="id" required>
    <button type="submit">Search</button>
</form>
</body>
</html>