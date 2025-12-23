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
    <h1>Order Confirmation</h1>
    <nav>
        <ul>
            <li><a href="loadInventory">Back to products</a></li>
        </ul>
    </nav>
</header>

<main>
    <div class="success-message">
        <h2>Your order has been placed successfully!</h2>
    </div>

    <div class="order-details">
        <%
          Map<String, Object> orderResponse =
                  (Map<String, Object>) request.getAttribute("order");

          Map<String, Object> order =
                  (Map<String, Object>) orderResponse.get("order");

          List<Map<String, Object>> products =
                  (List<Map<String, Object>>) order.get("products");
        %>

        <p><strong>Customer:</strong> <%= order.get("customer_name") %></p>
        <p><strong>Status:</strong> <%= order.get("status") %></p>
        <p><strong>Total Amount:</strong> $<%= String.format("%.2f", Double.parseDouble(order.get("total_amount").toString())) %></p>

        <h3>Ordered Products</h3>

        <table>
          <tr>
            <th>Product</th>
            <th>Quantity</th>
            <th>Total Price</th>
          </tr>

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
            <td colspan="3">No products found</td>
          </tr>
          <%
            }
          %>
        </table>
    </div>
</main>

<footer>
    <a href="loadInventory" class="btn">Back to Products</a>
</footer>

</body>
</html>
