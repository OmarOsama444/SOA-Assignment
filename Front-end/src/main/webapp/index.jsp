<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World!" %></h1>
<br/>
<a href="${pageContext.request.contextPath}/hello-servlet">Hello Servlet</a>
<a href="${pageContext.request.contextPath}/servlet2">Hello Servlet</a>
<a href="${pageContext.request.contextPath}/servlet3">Hello Servlet</a>



<h3>JSP Page Links:</h3>
<ul>
    <li><a href="create_order.jsp">Create Order</a></li>
    <li><a href="inventory_check.jsp">Check inventory</a></li>
    <li><a href="pricing_check.jsp">Pricing</a></li>
    <li><a href="inventory_check.jsp">View Page 2</a></li>
    <li><a href="page3.jsp">View Page 3</a></li>
</ul>
</body>
</html>

