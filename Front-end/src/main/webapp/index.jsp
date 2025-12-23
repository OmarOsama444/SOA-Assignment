<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<%
    Map<String, String> productIcons = new HashMap<>();
    productIcons.put("Laptop", "💻");
    productIcons.put("Mouse", "🖱️");
    productIcons.put("Keyboard", "⌨️");
    productIcons.put("Monitor", "🖥️");
    productIcons.put("Headphones", "🎧");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>E-Commerce Store - Products</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/index.css">
</head>
<body>

<header>
    <h1>Welcome to our E-Commerce Store</h1>
    <nav>
        <ul>
            <li><a href="profile">👤 Profiles</a></li>
        </ul>
    </nav>
</header>

<main>
    <section class="products-section">
        <h2>Our Products</h2>

        <div class="products-container">
            <%
                List<Map<String, Object>> products = (List<Map<String, Object>>) request.getAttribute("products");

                if (products != null) {
                    for (Map<String, Object> product : products) {

                        String productName = product.get("product_name").toString();
                        String productIcon = productIcons.getOrDefault(productName, "📦");
            %>

            <div class="product-container">
                <div class="product-img"><%= productIcon %></div>
                <h3 class="product-title"><%= productName %></h3>
                <p class="product-price">$<%= product.get("unit_price") %></p>
                <p class="product-stock">
                    In Stock: <%= product.get("quantity_available") %>
                </p>
            </div>

            <%
                    }
                }
            %>
        </div>
    </section>
</main>

<footer>
    <a href="checkout">
        <button class="checkout-btn">Proceed to Checkout</button>
    </a>
</footer>

</body>
</html>
