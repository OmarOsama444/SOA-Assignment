<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html>
<head>
    <title>Checkout - Place Order</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

<!DOCTYPE html>
<html>
<head>
    <title>Checkout - Place Order</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <header>
        <h1>Place Your Order</h1>
        <nav><a href="loadInventory">Back to Products</a></nav>
    </header>

    <main>
        <%
            List<Map<String, Object>> customers = (List<Map<String, Object>>) request.getAttribute("customers");
            List<Map<String, Object>> products = (List<Map<String, Object>>) request.getAttribute("products");
            String error = (String) request.getAttribute("error");
        %>

        <% if (error != null) { %>
        <div class="error"><%= error %></div>
        <% } %>

        <% if (customers == null || products == null || customers.isEmpty() || products.isEmpty()) { %>
        <div class="info-box">
            <p>Products or customers are not loaded. Please go back to <a href="loadInventory">Products Page</a>.</p>
        </div>
        <% } else { %>

        <form action="placeOrder" method="post" class="checkout-form">
            <div class="form-section">
                <h3>Select Customer</h3>
                <label for="customer_id">Customer:</label>
                <select name="customer_id" id="customer_id" required>
                    <option value="">-- Choose Customer --</option>
                    <% for (Map<String, Object> customer : customers) { %>
                    <option value="<%= customer.get("customer_id") %>">
                        <%= customer.get("name") %> - <%= customer.get("email") %>
                    </option>
                    <% } %>
                </select>
            </div>

            <div class="form-section">
                <h3>Select Products</h3>
                <div id="products-container">
                    <div class="product-row">
                        <div class="product-select-group">
                            <label>Product:</label>
                            <select name="product_id[]" class="product-dropdown" required onchange="updatePrice(this)">
                                <option value="">-- Select Product --</option>
                                <% for (Map<String, Object> product : products) { %>
                                <option value="<%= product.get("product_id") %>"
                                        data-price="<%= product.get("unit_price") %>"
                                        data-stock="<%= product.get("quantity_available") %>">
                                    <%= product.get("product_name") %> - $<%= product.get("unit_price") %> (Stock: <%= product.get("quantity_available") %>)
                                </option>
                                <% } %>
                            </select>
                        </div>
                        <div class="quantity-group">
                            <label>Quantity:</label>
                            <input type="number" name="quantity[]" class="quantity-input" required min="1" value="1" onchange="calculateTotal()">
                        </div>
                        <div class="price-display">
                            <span class="item-total">$0.00</span>
                        </div>
                    </div>
                </div>
                <button type="button" onclick="addProductRow()">+ Add Another Product</button>
            </div>

            <div class="order-summary">
                <h3>Order Summary</h3>
                <div class="summary-row">
                    <span>Estimated Total:</span>
                    <span id="orderTotal" class="total-amount">$0.00</span>
                </div>
            </div>

            <button type="submit" class="submit-btn">Place Order</button>
        </form>

        <% } %>
    </main>
</div>

<script>
    // Store products data for JS
    const productsData = {};
    <% for (Map<String, Object> product : products) { %>
    productsData["<%= product.get("product_id") %>"] = {
        name: "<%= product.get("product_name") %>",
        price: <%= product.get("unit_price") %>,
        stock: <%= product.get("quantity_available") %>
    };
    <% } %>

    function addProductRow() {
        const container = document.getElementById('products-container');
        const newRow = document.createElement('div');
        newRow.className = 'product-row';
        newRow.innerHTML = `
            <div class="product-select-group">
                <label>Product:</label>
                <select name="product_id[]" class="product-dropdown" required onchange="updatePrice(this)">
                    <option value="">-- Select Product --</option>
                    <% for (Map<String, Object> product : products) { %>
                        <option value="<%= product.get("product_id") %>"
                                data-price="<%= product.get("unit_price") %>"
                                data-stock="<%= product.get("quantity_available") %>">
                            <%= product.get("product_name") %> - $<%= product.get("unit_price") %> (Stock: <%= product.get("quantity_available") %>)
                        </option>
                    <% } %>
                </select>
            </div>
            <div class="quantity-group">
                <label>Quantity:</label>
                <input type="number" name="quantity[]" class="quantity-input" required min="1" value="1" onchange="calculateTotal()">
            </div>
            <div class="price-display">
                <span class="item-total">$0.00</span>
            </div>
            <button type="button" onclick="removeProductRow(this)" class="remove-btn">Remove</button>
        `;
        container.appendChild(newRow);
    }

    function removeProductRow(button) {
        button.parentElement.remove();
        calculateTotal();
    }

    function updatePrice(selectElement) {
        const row = selectElement.closest('.product-row');
        const quantityInput = row.querySelector('.quantity-input');
        const priceDisplay = row.querySelector('.item-total');
        const selectedOption = selectElement.options[selectElement.selectedIndex];
        const price = parseFloat(selectedOption.dataset.price) || 0;
        const stock = parseInt(selectedOption.dataset.stock) || 0;
        quantityInput.max = stock;
        const quantity = parseInt(quantityInput.value) || 0;
        priceDisplay.textContent = '$' + (price * quantity).toFixed(2);
        calculateTotal();
    }

    function calculateTotal() {
        let total = 0;
        const rows = document.querySelectorAll('.product-row');
        rows.forEach(row => {
            const select = row.querySelector('.product-dropdown');
            const quantityInput = row.querySelector('.quantity-input');
            if (select.value) {
                const price = parseFloat(select.options[select.selectedIndex].dataset.price) || 0;
                const quantity = parseInt(quantityInput.value) || 0;
                row.querySelector('.item-total').textContent = '$' + (price * quantity).toFixed(2);
                total += price * quantity;
            }
        });
        document.getElementById('orderTotal').textContent = '$' + total.toFixed(2);
    }

    document.addEventListener('DOMContentLoaded', () => {
        calculateTotal();
    });
</script>
</body>
</html>