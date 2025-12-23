package com.ecommerce.ecommercefrontend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String ORDER_URL;
    private final String CUSTOMER_URL;
    private final String INVENTORY_URL;

    public PlaceOrderServlet() {
        String orderBase = System.getenv("ORDER_SERVICE_URL") != null
                ? System.getenv("ORDER_SERVICE_URL")
                : "http://localhost:5001";
        ORDER_URL = String.format("%s/api/orders/create", orderBase);

        String customerBase = System.getenv("CUSTOMER_SERVICE_URL") != null
                ? System.getenv("CUSTOMER_SERVICE_URL")
                : "http://localhost:5002";
        CUSTOMER_URL = String.format("%s/api/customers", customerBase);

        String inventoryBase = System.getenv("INVENTORY_SERVICE_URL") != null
                ? System.getenv("INVENTORY_SERVICE_URL")
                : "http://localhost:5003";
        INVENTORY_URL = String.format("%s/api/inventory/products", inventoryBase);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int customerId = (int) Double.parseDouble(request.getParameter("customer_id"));
            String[] productIds = request.getParameterValues("product_id[]");
            String[] quantities = request.getParameterValues("quantity[]");

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("customer_id", customerId);
            jsonRequest.put("total_amount", 1);

            JSONArray productsArray = new JSONArray();
            int totalQuantity = 0;
            for (int i = 0; i < productIds.length; i++) {
                if (productIds[i] != null && !productIds[i].trim().isEmpty() &&
                    quantities[i] != null && !quantities[i].trim().isEmpty()) {
                    int quantity = (int) Double.parseDouble(quantities[i]);
                    totalQuantity += quantity;
                    if (quantity > 0) {
                        int productId = (int) Double.parseDouble(productIds[i]);

                        JSONObject product = new JSONObject();
                        product.put("product_id", productId);
                        product.put("quantity", quantity);

                        productsArray.put(product);
                    }
                }
            }

            if (totalQuantity == 0) {
                response.sendRedirect("checkout?error=Please select at least one product with quantity greater than 0.");
                return;
            }
            jsonRequest.put("products", productsArray);

            // Send POST to Order API
            HttpRequest orderRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ORDER_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> orderResponse = httpClient.send(orderRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonResponse = new JSONObject(orderResponse.body());

            // 1. Load customers from Customer Service
            HttpRequest customersRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CUSTOMER_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> customersResponse = httpClient.send(customersRequest, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> customers = gson.fromJson(
                    customersResponse.body(),
                    new TypeToken<List<Map<String, Object>>>(){}.getType());

            // Find the customer name
            String customerName = customers.stream()
                    .filter(c -> ((Number)c.get("customer_id")).intValue() == customerId)
                    .map(c -> (String)c.get("name"))
                    .findFirst()
                    .orElse("Unknown Customer");

            // 2. Load products from Inventory Service
            HttpRequest inventoryRequest = HttpRequest.newBuilder()
                    .uri(URI.create(INVENTORY_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> inventoryResponse = httpClient.send(inventoryRequest, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> inventory = gson.fromJson(
                    inventoryResponse.body(),
                    new TypeToken<List<Map<String, Object>>>(){}.getType());

            Map<Integer, String> productIdToName = new HashMap<>();
            for (Map<String, Object> p : inventory) {
                productIdToName.put(((Number)p.get("product_id")).intValue(), (String)p.get("product_name"));
            }

            // Replace product IDs with names
            Map<String, Object> orderMap = jsonResponse.getJSONObject("order").toMap();
            List<Map<String, Object>> orderProducts = (List<Map<String, Object>>) orderMap.get("products");
            for (Map<String, Object> p : orderProducts) {
                int pid = ((Number)p.get("product_id")).intValue();
                p.put("product_name", productIdToName.getOrDefault(pid, "Unknown Product"));
            }

            orderMap.put("customer_name", customerName);
            jsonResponse.put("order", new JSONObject(orderMap));

            request.setAttribute("order", jsonResponse.toMap());
            request.getRequestDispatcher("confirmation.jsp").forward(request, response);

        } catch (InterruptedException e) {
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
