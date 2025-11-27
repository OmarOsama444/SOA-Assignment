<%--
  Created by IntelliJ IDEA.
  User: moham
  Date: 11/27/2025
  Time: 9:31 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Pricing Breakdown</title>
</head>
<body>
<h2>Price Calculation Result</h2>

<div style="background-color: #f8f9fa; border: 1px solid #ced4da; padding: 20px; border-radius: 5px;">
    <!-- Display raw JSON for verification -->
    <pre><%= request.getAttribute("jsonData") %></pre>
</div>

<br>
<a href="pricing_check.jsp">Calculate Another</a>
</body>
</html>
