<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmation</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/confirmation.css">
</head>
<body>

<header>
    <div class="container nav-container">
        <nav>
            <h1>Order Confirmation</h1>
            <ul class="nav-links">
                <li><a href="loadInventory" class="nav-link">← Back to Store</a></li>
            </ul>
        </nav>
    </div>
</header>

<main class="container fade-in">
    <div class="order-success">
        <span class="checkmark">✅</span>
        <h2 class="mb-4">Order Placed Successfully!</h2>
        <p class="text-muted">Thank you for shopping with us.</p>
    </div>

    <div class="card confirmation-card-container">
        <%
          Map<String, Object> orderResponse =
                  (Map<String, Object>) request.getAttribute("order");

          Map<String, Object> order =
                  (Map<String, Object>) orderResponse.get("order");

          List<Map<String, Object>> products =
                  (List<Map<String, Object>>) order.get("products");
        %>

        <div class="flex-between mb-4 order-summary-header">
            <div>
                <p class="text-muted text-sm">CUSTOMER</p>
                <p><strong><%= order.get("customer_name") %></strong></p>
            </div>
             <div>
                <p class="text-muted text-sm">STATUS</p>
                <span class="status-badge <%= order.get("status").toString().toLowerCase() %>">
                    <%= order.get("status") %>
                </span>
            </div>
            <div>
                 <p class="text-muted text-sm">TOTAL</p>
                 <p class="price-tag">$<%= String.format("%.2f", Double.parseDouble(order.get("total_amount").toString())) %></p>
            </div>
        </div>

        <h3 class="mt-4">Ordered Products</h3>

        <div class="table-container mt-4">
            <table>
              <thead>
                  <tr>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Total Price</th>
                  </tr>
              </thead>
              <tbody>
              <%
                if (products != null) {
                  for (Map<String, Object> p : products) {
              %>
              <tr>
                <td><%= p.get("product_name") %></td>
                <td><%= p.get("quantity") %></td>
                <td>$<%= String.format("%.2f", Double.parseDouble(p.get("total_price_after_discount").toString())) %></td>
              </tr>
              <%
                }
              } else {
              %>
              <tr>
                <td colspan="3" class="text-center text-muted">No products found</td>
              </tr>
              <%
                }
              %>
              </tbody>
            </table>
        </div>
        
        <div class="text-center mt-4">
             <a href="loadInventory" class="btn">Continue Shopping</a>
        </div>
    </div>
</main>

<footer class="site-footer">
    <p>&copy; 2025 E-Commerce Store</p>
</footer>

</body>
</html>
