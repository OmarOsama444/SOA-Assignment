package com.ecommerce.ecommercefrontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {
    private String FLASK_URL = null;

    public InventoryServlet() {
        String baseUrl = System.getenv("INVENTORY_SERVICE_URL") != null ? System.getenv("INVENTORY_SERVICE_URL")
                : "http://localhost:5003";
        FLASK_URL = String.format("%s/api/inventory", baseUrl);
    }

    // 1. Handle GET Request (Check Stock)
    // Matches Flask: @app.route("/api/inventory/check/<int:product_id>",
    // methods=["GET"])
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.getenv("ORDER_SERVICE_URL");
        String productId = request.getParameter("product_id");
        String resultJson = "";

        if (productId != null && !productId.isEmpty()) {
            try {
                URL url = new URL(FLASK_URL + "/check/" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int status = conn.getResponseCode();
                // Read response (Success or Error)
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (status < 300) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                resultJson = sb.toString();
                conn.disconnect();

            } catch (Exception e) {
                resultJson = "{\"error\": \"Java Connection Error: " + e.getMessage() + "\"}";
            }
        } else {
            resultJson = "{\"error\": \"Product ID is required\"}";
        }

        request.setAttribute("jsonData", resultJson);
        request.getRequestDispatcher("/inventory_result.jsp").forward(request, response);
    }

    // 2. Handle POST Request (Update/Adjust Inventory)
    // Matches Flask: @app.route("/api/inventory/update", methods=["POST"])
    // Expects payload: { "products": [ { "product_id": 1, "quantity": 10 } ] }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String prodId = request.getParameter("product_id");
        String qty = request.getParameter("quantity");
        String resultJson = "";

        // Manually construct the JSON structure your Python code requires
        // Structure: {"products": [{"product_id": X, "quantity": Y}]}
        String jsonPayload = String.format(
                "{\"products\": [{\"product_id\": %s, \"quantity\": %s}]}",
                prodId, qty);

        try {
            URL url = new URL(FLASK_URL + "/update");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Send JSON
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get Response
            int status = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (status < 300) ? conn.getInputStream() : conn.getErrorStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            resultJson = sb.toString();

        } catch (Exception e) {
            resultJson = "{\"error\": \"Java Connection Error: " + e.getMessage() + "\"}";
        }

        request.setAttribute("jsonData", resultJson);
        request.getRequestDispatcher("/inventory_result.jsp").forward(request, response);
    }
}