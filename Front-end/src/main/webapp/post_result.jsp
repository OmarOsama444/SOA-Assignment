<%--
  Created by IntelliJ IDEA.
  User: moham
  Date: 11/27/2025
  Time: 8:56 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Order Result</title>
</head>
<body>
<h2>Response from Python Flask Service:</h2>

<div style="background-color: #f4f4f4; padding: 15px; border: 1px solid #ddd;">
    <%= request.getAttribute("jsonData") %>
</div>

<br>
<a href="create_order.jsp">Go Back</a>
</body>
</html>