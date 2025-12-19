package com.ecommerce.ordersystem.servlets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.*;

@WebServlet("/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    private final String CREATE_ORDER_API;

    public PlaceOrderServlet() {
        String baseUrl = System.getenv("ORDER_SERVICE_URL") != null
                ? System.getenv("ORDER_SERVICE_URL")
                : "http://localhost:5001";
        CREATE_ORDER_API = String.format("%s/api/orders/create", baseUrl);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int customerId = (int) Double.parseDouble(request.getParameter("customer_id"));
        String[] productIds = request.getParameterValues("product_id[]");
        String[] quantities = request.getParameterValues("quantity[]");

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("customer_id", customerId);
        jsonRequest.put("total_amount", 1); // You may calculate total amount dynamically later

        JSONArray productsArray = new JSONArray();
        for (int i = 0; i < productIds.length; i++) {
            int productId = (int) Double.parseDouble(productIds[i]);
            int quantity  = (int) Double.parseDouble(quantities[i]);

            JSONObject product = new JSONObject();
            product.put("product_id", productId);
            product.put("quantity", quantity);

            productsArray.put(product);
        }

        jsonRequest.put("products", productsArray);

        URL url = new URL(CREATE_ORDER_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();

        if (status != 200 && status != 201) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                throw new RuntimeException("Order API error (" + status + "): " + error);
            }
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        }

        JSONObject jsonResponse = new JSONObject(result.toString());
        request.setAttribute("order", jsonResponse.toMap());
        request.getRequestDispatcher("confirmation.jsp").forward(request, response);
    }
}
