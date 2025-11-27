<%--
  Created by IntelliJ IDEA.
  User: moham
  Date: 11/27/2025
  Time: 9:31 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Pricing Calculator</title>
    <style>
        body { font-family: sans-serif; padding: 20px; }
        .form-group { margin-bottom: 15px; border: 1px solid #ddd; padding: 10px; }
    </style>
</head>
<body>
<h1>Calculate Order Price</h1>
<p>Enter products to calculate total with tax and discounts.</p>

<form action="pricing" method="post">

    <!-- Region for Tax -->
    <label>Region (State):</label>
    <select name="region">
        <option value="EG">Egypt (EG)</option>
    </select>
    <br><br>

    <!-- Product 1 -->
    <div class="form-group">
        <strong>Item 1:</strong><br>
        Product ID: <input type="number" name="product_id_1" value="1"><br>
        Quantity: <input type="number" name="quantity_1" value="5">
        <em>(Hint: 5+ items gets discount)</em>
    </div>

    <!-- Product 2 -->
    <div class="form-group">
        <strong>Item 2:</strong><br>
        Product ID: <input type="number" name="product_id_2" value="2"><br>
        Quantity: <input type="number" name="quantity_2" value="1">
    </div>

    <button type="submit" style="padding: 10px 20px; background: #28a745; color: white;">Calculate Total</button>
</form>
</body>
</html>
