<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Inventory Service</title>
</head>
<body>
<h1>Inventory Service Management</h1>

<div style="border:1px solid #ccc; padding:10px; margin-bottom:20px;">
    <h3>Check Product Stock</h3>
    <form action="inventory" method="get">
        <label>Product ID:</label>
        <input type="number" name="product_id" required>
        <button type="submit">Check Availability</button>
    </form>
</div>

<div style="border:1px solid #ccc; padding:10px;">
    <h3>Update Stock (Simulate Order Deduction)</h3>
    <form action="inventory" method="post">
        <label>Product ID:</label>
        <input type="number" name="product_id" required><br><br>

        <label>Quantity to Deduct:</label>
        <input type="number" name="quantity" required><br><br>

        <button type="submit">Update Inventory</button>
    </form>
</div>
</body>
</html>