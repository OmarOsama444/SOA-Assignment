package com.ecommerce.ecommercefrontend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/loadInventory")
public class InventoryServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String INVENTORY_URL;

    // Set baseURL
    public InventoryServlet() {
        String baseUrl = System.getenv("INVENTORY_SERVICE_URL") != null
                ? System.getenv("INVENTORY_SERVICE_URL")
                : "http://localhost:5003";
        INVENTORY_URL = String.format("%s/api/inventory/products", baseUrl);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Call inventory service
            HttpRequest productsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(INVENTORY_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
                        
            HttpResponse<String> productsResponse
                    = httpClient.send(productsRequest, HttpResponse.BodyHandlers.ofString());

            // Parse response
            List<Map<String, Object>> products = gson.fromJson(
                    productsResponse.body(),
                    new TypeToken<List<Map<String, Object>>>() {}.getType());

            // Set attribute and forward
            request.setAttribute("products", products);
            request.getRequestDispatcher("index.jsp").forward(request, response);

        } catch (InterruptedException e) {
        }
    }
}
