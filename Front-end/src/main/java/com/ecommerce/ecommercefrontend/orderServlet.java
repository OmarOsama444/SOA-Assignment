package com.ecommerce.ecommercefrontend;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import jakarta.servlet.ServletException;

@WebServlet("/order")
public class orderServlet extends HttpServlet {

    private String FLASK_URL = null;

    public orderServlet() {
        String baseUrl = System.getenv("ORDER_SERVICE_URL") != null ? System.getenv("ORDER_SERVICE_URL")
                : "http://localhost:5001";
        FLASK_URL = String.format("%s/api/orders", baseUrl);
    }

    // Handle GET request (Retrieve Order Details)
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String orderId = request.getParameter("id");
        String resultJson = "";

        if (orderId != null) {
            try {
                URL url = new URL(FLASK_URL + "/" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    resultJson = br.readLine(); // Reads the JSON response from Flask
                } else {
                    resultJson = "Error: Order not found (Status " + conn.getResponseCode() + ")";
                }
                conn.disconnect();
            } catch (Exception e) {
                resultJson = "Error connecting to service: " + e.getMessage();
            }
        }

        request.setAttribute("jsonData", resultJson);
        request.getRequestDispatcher("post_result.jsp").forward(request, response);
    }

    // Handle POST request (Create New Order)
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get parameters from JSP
        String custId = request.getParameter("customer_id");
        String prodId = request.getParameter("product_id");
        String quantity = request.getParameter("quantity");
        String total = request.getParameter("total_amount");

        // 2. Construct JSON String manually (Or use a library like Gson/Jackson)
        // Format: {"customer_id": 1, "total_amount": 100, "products": [{"product_id":
        // 1, "quantity": 1}]}
        String jsonInputString = String.format(
                "{\"customer_id\": %s, \"total_amount\": %s, \"products\": [{\"product_id\": %s, \"quantity\": %s}]}",
                custId, total, prodId, quantity);

        String resultJson = "";

        // 3. Send POST to Flask
        try {
            URL url = new URL(FLASK_URL + "/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Write JSON to stream
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read Response
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                                    ? conn.getInputStream()
                                    : conn.getErrorStream()));

            StringBuilder responseStr = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                responseStr.append(responseLine.trim());
            }
            resultJson = responseStr.toString();

        } catch (Exception e) {
            resultJson = "Error: " + e.getMessage();
        }

        request.setAttribute("jsonData", resultJson);
        request.getRequestDispatcher("post_result.jsp").forward(request, response);
    }
}