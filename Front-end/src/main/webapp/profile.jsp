<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customers Profiles</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/profile.css">
</head>
<body>

<header>
    <div class="container nav-container">
        <nav>
            <h1>Customers Profiles</h1>
            <ul class="nav-links">
                <li><a href="loadInventory" class="nav-link">← Back to Products</a></li>
            </ul>
        </nav>
    </div>
</header>

<main class="container fade-in">
    <div class="card mb-4 customer-select-card">
        <h3 class="text-center">Select a Customer</h3>
        <form action="profile" method="get" class="form-section mt-4">
            <div class="flex-between gap-4">
                <select name="customer_id" required>
                    <option value="">-- Choose Customer --</option>
                    <%
                        List<Map<String, Object>> customers =
                                (List<Map<String, Object>>) request.getAttribute("customers");
                        if (customers != null) {
                            for (Map<String, Object> c : customers) {
                    %>
                    <option value="<%= c.get("customer_id") %>"
                            <%= request.getParameter("customer_id") != null &&
                                    request.getParameter("customer_id").equals(String.valueOf(c.get("customer_id"))) ? "selected" : "" %>>
                        <%= c.get("name") %> - <%= c.get("email") %>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>
                <button type="submit" class="btn">View</button>
            </div>
        </form>
    </div>

    <%
        Map<String, Object> selectedCustomer =
                (Map<String, Object>) request.getAttribute("selectedCustomer");
        if (selectedCustomer != null) {
    %>
    <div class="card profile-card fade-in">
        <div class="profile-header">
            <span class="profile-avatar">👤</span>
            <h2 class="mt-4"><%= selectedCustomer.get("name") %></h2>
            <p class="text-muted"><%= selectedCustomer.get("email") %></p>
        </div>
        
        <div class="profile-stats">
            <div class="stat-item">
                <p class="stat-label">PHONE</p>
                <p class="stat-value"><%= selectedCustomer.get("phone") %></p>
            </div>
            <div class="stat-item">
                <p class="stat-label">LOYALTY POINTS</p>
                <p class="stat-value"><%= selectedCustomer.get("loyalty_points") %> ⭐</p>
            </div>
        </div>

        <% if (selectedCustomer != null) { 
               int custId = (int) Double.parseDouble(String.valueOf(selectedCustomer.get("customer_id")));
        %>
        <div class="actions text-center mt-4">
            <a href="userOrders?customer_id=<%= custId %>" class="btn btn-secondary w-full">
                View Order History
            </a>
        </div>
        <% } %>
    </div>
    <% } %>
</main>

</body>
</html>
