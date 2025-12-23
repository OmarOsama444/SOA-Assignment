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
    <h1>Customers Profiles</h1>
    <nav>
        <ul>
            <li><a href="loadInventory">Back to products</a></li>
        </ul>
    </nav>
</header>

<main>
    <div class="form-section">
        <h3>Select a Customer</h3>
        <form action="profile" method="get">
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
            <button type="submit">View Profile</button>
        </form>
    </div>

    <%
        Map<String, Object> selectedCustomer =
                (Map<String, Object>) request.getAttribute("selectedCustomer");
        if (selectedCustomer != null) {
    %>
    <div class="customer-profile">
        <h3>Profile Details</h3>
        <p><strong>Name:</strong> <%= selectedCustomer.get("name") %></p>
        <p><strong>Email:</strong> <%= selectedCustomer.get("email") %></p>
        <p><strong>Phone:</strong> <%= selectedCustomer.get("phone") %></p>
        <p><strong>Loyalty Points:</strong> <%= selectedCustomer.get("loyalty_points") %></p>
    </div>
    <% } %>

    <% if (selectedCustomer != null) { %>
    <div class="actions">
        <a href="userOrders?customer_id=<%= ((Number)selectedCustomer.get("customer_id")).intValue() %>" class="btn">
            View Orders
        </a>
    </div>
    <% } %>
</main>

</body>
</html>
