<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<html>
<head>
  <title>Order Confirmation</title>
  <link rel="stylesheet" href="css/style.css">
  <style>

  </style>
</head>
<body>
<div class="container">
  <header>
    <h1>Order Confirmation</h1>
    <nav><a href="loadInventory">Back to products</a></nav>
  </header>

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

    <p><strong>Customer ID:</strong> <%= order.get("customer_id") %></p>
    <p><strong>Status:</strong> <%= order.get("status") %></p>
    <p><strong>Total Amount:</strong> $<%= order.get("total_amount") %></p>

    <h3>Ordered Products</h3>

    <table>
      <tr>
        <th>Product ID</th>
        <th>Quantity</th>
      </tr>

      <%
        if (products != null) {
          for (Map<String, Object> p : products) {
      %>
      <tr>
        <td><%= p.get("product_id") %></td>
        <td><%= p.get("quantity") %></td>
      </tr>
      <%
        }
      } else {
      %>
      <tr>
        <td colspan="2">No products found</td>
      </tr>
      <%
        }
      %>
    </table>
  </div>

  <div class="actions">
    <a href="loadInventory" class="btn">Back to Products</a>
  </div>
</div>
</body>
</html>
