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
    <div class="container nav-container">
        <nav>
            <h1>E-Commerce Store</h1>
            <ul class="nav-links">
                <li><a href="profile" class="nav-link">👤 Profiles</a></li>
                <li><a href="userOrders" class="nav-link">📜 Orders History</a></li>
            </ul>
        </nav>
    </div>
</header>

<main class="container fade-in">
    <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-error">
            <%= request.getParameter("error") %>
        </div>
    <% } %>

    <section class="products-section">
        <div class="flex-between">
            <h2>Our Products</h2>
        </div>

        <form action="checkout" method="post">
            <div class="product-grid">
                <%
                    List<Map<String, Object>> products = (List<Map<String, Object>>) request.getAttribute("products");

                    if (products != null) {
                        for (Map<String, Object> product : products) {
                                // Filter products with quantity > 0
                                Object quantityObj = product.get("quantity_available");
                                int quantityAvailable = (int) Double.parseDouble(String.valueOf(quantityObj));

                                if (quantityAvailable > 0) {
                                    String productName = product.get("product_name").toString();
                                    String productIcon = productIcons.getOrDefault(productName, "📦");

                                    Object productIdObj = product.get("product_id");
                                    int productId = (int) Double.parseDouble(String.valueOf(productIdObj));
                %>

                <div class="card product-card">
                    <span class="product-image"><%= productIcon %></span>
                    <h3 class="product-title text-center"><%= productName %></h3>
                    <div class="flex-between mb-4 mt-4">
                        <span class="price-tag">$<%= product.get("unit_price") %></span>
                        <span class="status-badge confirmed">Stock: <%= quantityAvailable %></span>
                    </div>

                    <div class="product-actions">
                        <label>
                            <input type="checkbox" name="selected_product" value="<%= productId %>"> Select to order
                        </label>
                        <div>
                            <label class="text-muted">Quantity:</label>
                            <input type="number" name="quantity_<%= productId %>" min="0" max="<%= quantityAvailable %>" value="0">
                        </div>
                    </div>
                </div>

                <%
                            }
                        }
                    }
                %>
            </div>

            <div class="action-bar">
                <button type="submit" class="btn">Make Order</button>
            </div>
        </form>
    </section>
</main>

<footer class="site-footer">
    <p>&copy; 2025 E-Commerce Store</p>
</footer>

</body>
</html>
