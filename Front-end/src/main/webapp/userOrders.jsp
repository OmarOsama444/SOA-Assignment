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
    <div class="container nav-container">
        <nav>
            <h1>Customer Orders</h1>
            <ul class="nav-links">
                <li><a href="profile" class="nav-link">Back to Profile</a></li>
                <li><a href="loadInventory" class="nav-link">Back to Products</a></li>
            </ul>
        </nav>
    </div>
</header>

<main class="container fade-in">
    <%
      List<Map<String, Object>> orders = (List<Map<String, Object>>) request.getAttribute("orders");
      List<Map<String, Object>> customers = (List<Map<String, Object>>) request.getAttribute("customers");
    %>

    <% if (customers != null) { %>
    <div class="card mb-4 customer-select-card" style="max-width: 600px; margin: 0 auto;">
        <h3 class="text-center">Select Customer to View Orders</h3>
        <form action="userOrders" method="get" class="form-section mt-4">
            <div class="flex-between gap-4">
                <select name="customer_id" required>
                    <option value="">-- Choose Customer --</option>
                    <% for (Map<String, Object> c : customers) { %>
                    <option value="<%= c.get("customer_id") %>">
                        <%= c.get("name") %> - <%= c.get("email") %>
                    </option>
                    <% } %>
                </select>
                <button type="submit" class="btn">View Orders</button>
            </div>
        </form>
    </div>
    <% } else { %>
    
    <% if (orders == null || orders.isEmpty()) { %>
    <div class="card text-center">
      <p>No orders found for this customer.</p>
    </div>
    <% } else { %>
      <div class="card">
          <div class="table-container">
              <table class="orders-table">
                  <thead>
                  <tr>
                    <th>Order ID</th>
                    <th>Order Date</th>
                    <th>Total Amount</th>
                    <th>Status</th>
                  </tr>
                  </thead>
                  <tbody>
                  <% for (Map<String, Object> order : orders) { 
                      int orderId = (int) Double.parseDouble(String.valueOf(order.get("order_id")));
                  %>
                  <tr>
                    <td><strong>#<%= orderId %></strong></td>
                    <td><%= order.get("order_date") %></td>
                    <td>$<%= String.format("%.2f", Double.parseDouble(String.valueOf(order.get("total_amount")))) %></td>
                    <td>
                        <span class="status-badge <%= order.get("status").toString().toLowerCase() %>">
                            <%= order.get("status") %>
                        </span>
                    </td>
                  </tr>
                  <% } %>
                  </tbody>
              </table>
          </div>
      </div>
    <% } %>

    <% } %>
</main>

</body>
</html>
