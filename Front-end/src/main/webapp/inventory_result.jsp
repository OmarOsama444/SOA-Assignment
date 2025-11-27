<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head><title>Inventory Result</title></head>
<body>
<h2>Result:</h2>
<div style="background:#eee; padding:10px;">
    <%= request.getAttribute("jsonData") %>
</div>
<br><a href="inventory_check.jsp">Back</a>
</body>
</html>