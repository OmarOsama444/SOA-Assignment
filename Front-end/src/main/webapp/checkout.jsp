<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/checkout.css">
</head>
<body>

<header>
    <h1>Place Your Order</h1>
    <nav>
        <ul>
            <li><a href="loadInventory">Back to Products</a></li>
        </ul>
    </nav>
</header>

<main>
    <%
        List<Map<String, Object>> customers = (List<Map<String, Object>>) request.getAttribute("customers");
        List<Map<String, Object>> products = (List<Map<String, Object>>) request.getAttribute("products");
        String error = (String) request.getAttribute("error");
    %>

    <% if (error != null) { %>
    <div class="error"><%= error %></div>
    <% } %>

    <% if (customers == null || products == null || customers.isEmpty() || products.isEmpty()) { %>
    <div class="info-box">
        <p>Products or customers are not loaded. Please go back to <a href="loadInventory">Products Page</a>.</p>
    </div>
    <% } else { %>

    <form action="placeOrder" method="post" class="checkout-form">
        <div class="form-section">
            <h3>Select Customer</h3>
            <label for="customer_id">Customer:</label>
            <select name="customer_id" id="customer_id" required>
                <option value="">-- Choose Customer --</option>
                <% for (Map<String, Object> customer : customers) { %>
                <option value="<%= customer.get("customer_id") %>">
                    <%= customer.get("name") %> - <%= customer.get("email") %>
                </option>
                <% } %>
            </select>
        </div>

        <div class="form-section">
            <h3>Select Quantities for Products</h3>
            <% for (Map<String, Object> product : products) { %>
            <div class="product-row">
                <input type="hidden" name="product_id[]" value="<%= product.get("product_id") %>">
                <div class="product-info">
                    <span><%= product.get("product_name") %> - $<%= product.get("unit_price") %> (Stock: <%= product.get("quantity_available") %>)</span>
                </div>
                <div class="quantity-group">
                    <label>Quantity:</label>
                    <input type="number" name="quantity[]" min="0" value="0">
                </div>
            </div>
            <% } %>
        </div>

        <button type="submit" class="submit-btn">Place Order</button>
    </form>

    <% } %>
</main>

</body>
</html>