package com.ecommerce.ordersystem.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Dynamic service URLs
    private final String INVENTORY_URL;
    private final String CUSTOMER_URL;

    public CheckoutServlet() {
        String inventoryBase = System.getenv("INVENTORY_SERVICE_URL") != null
                ? System.getenv("INVENTORY_SERVICE_URL")
                : "http://localhost:5003";
        INVENTORY_URL = String.format("%s/api/inventory/products", inventoryBase);

        String customerBase = System.getenv("CUSTOMER_SERVICE_URL") != null
                ? System.getenv("CUSTOMER_SERVICE_URL")
                : "http://localhost:5002";
        CUSTOMER_URL = String.format("%s/api/customers", customerBase);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Load products from Inventory Service
            HttpRequest productsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(INVENTORY_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> productsResponse =
                    httpClient.send(productsRequest, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> products = gson.fromJson(
                    productsResponse.body(),
                    new TypeToken<List<Map<String, Object>>>(){}.getType());

            // Load customers from Customer Service
            HttpRequest customersRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CUSTOMER_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> customersResponse =
                    httpClient.send(customersRequest, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> customers = gson.fromJson(
                    customersResponse.body(),
                    new TypeToken<List<Map<String, Object>>>(){}.getType());

            request.setAttribute("products", products);
            request.setAttribute("customers", customers);
            request.getRequestDispatcher("checkout.jsp").forward(request, response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request interrupted");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
