<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html>
<head>
    <title>E-Commerce Store - Products</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <header>
        <h1>Welcome to E-Commerce Store</h1>
    </header>

    <main>
        <h2>Available Products</h2>

        <div class="product-grid">
            <%
                List<Map<String, Object>> products =
                        (List<Map<String, Object>>) request.getAttribute("products");

                if (products != null) {
                    for (Map<String, Object> product : products) {
            %>
            <div class="product-card">
                <h3><%= product.get("product_name") %></h3>
                <p class="price">$<%= product.get("unit_price") %></p>
                <p class="stock">In Stock: <%= product.get("quantity_available") %></p>
            </div>
            <%
                    }
                }
            %>
        </div>

        <div>
            <a href="checkout" class="submit-btn">Place Order</a>
        </div>

    </main>
</div>
</body>
</html>
