<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Orders</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/userOrders.css">
</head>
<body>

<header>
    <h1>Customer Orders</h1>
    <nav>
        <ul>
            <li><a href="profile">Back to Profile</a></li>
            <li><a href="loadInventory">Back to Products</a></li>
        </ul>
    </nav>
</header>

<main>
    <%
      List<Map<String, Object>> orders = (List<Map<String, Object>>) request.getAttribute("orders");
    %>

    <% if (orders == null || orders.isEmpty()) { %>
    <div class="info-box">
      <p>No orders found for this customer.</p>
    </div>
    <% } else { %>
    <table class="orders-table">
      <thead>
      <tr>
        <th>Order ID</th>
        <th>Order Date</th>
        <th>Status</th>
        <th>Total Amount</th>
      </tr>
      </thead>
      <tbody>
      <% for (Map<String, Object> order : orders) { %>
      <tr>
        <td><%= ((Number)order.get("order_id")).intValue() %></td>
        <td><%= order.get("order_date") %></td>
        <td><%= order.get("status") %></td>
        <td>$<%= order.get("total_amount") %></td>
      </tr>
      <% } %>
      </tbody>
    </table>
    <% } %>
</main>

</body>
</html>
