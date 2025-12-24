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
    <div class="container nav-container">
        <nav>
            <h1>Place Your Order</h1>
            <ul class="nav-links">
                <li><a href="loadInventory" class="nav-link">← Back to Products</a></li>
            </ul>
        </nav>
    </div>
</header>

<main class="container fade-in">
    <%
        List<Map<String, Object>> customers = (List<Map<String, Object>>) request.getAttribute("customers");
        List<Map<String, Object>> selectedProducts = (List<Map<String, Object>>) request.getAttribute("selectedProducts");
        Double totalAmount = (Double) request.getAttribute("totalAmount");
        String error = (String) request.getAttribute("error");
    %>

    <% if (error != null) { %>
    <div class="alert alert-error"><%= error %></div>
    <% } %>

    <form action="placeOrder" method="post" class="card">
        <input type="hidden" name="total_amount" value="<%= totalAmount %>">
        
        <% for (Map<String, Object> product : selectedProducts) { %>
           <input type="hidden" name="product_id[]" value="<%= product.get("product_id") %>">
           <input type="hidden" name="quantity[]" value="<%= product.get("selected_quantity") %>">
        <% } %>

        <div class="customer-fields mb-4">
            <h3>Customer Details</h3>
            <label for="customer_id">Select Customer:</label>
            <select name="customer_id" id="customer_id" required class="mt-4">
                <option value="">-- Choose Customer --</option>
                <% for (Map<String, Object> customer : customers) { %>
                <option value="<%= customer.get("customer_id") %>">
                    <%= customer.get("name") %> - <%= customer.get("email") %>
                </option>
                <% } %>
            </select>
        </div>

        <div class="order-summary-container mb-4">
            <h3>Order Summary</h3>
            <div class="table-container checkout-summary">
                <table>
                    <thead>
                    <tr>
                        <th>Product</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (Map<String, Object> product : selectedProducts) { %>
                    <tr>
                        <td><%= product.get("product_name") %></td>
                        <td>$<%= String.format("%.2f", Double.parseDouble(String.valueOf(product.get("unit_price")))) %></td>
                        <td><%= product.get("selected_quantity") %></td>
                        <td>$<%= String.format("%.2f", Double.parseDouble(String.valueOf(product.get("total_price_after_discount")))) %></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <div class="checkout-total">
                <p class="price-tag">Total: $<%= String.format("%.2f", totalAmount) %></p>
            </div>
        </div>

        <div class="flex-between">
            <a href="loadInventory" class="btn btn-secondary">Cancel</a>
            <button type="submit" class="btn">Confirm Order</button>
        </div>
    </form>
</main>

</body>
</html>