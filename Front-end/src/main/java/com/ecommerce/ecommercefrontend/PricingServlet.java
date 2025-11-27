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

@WebServlet("/pricing")
public class PricingServlet extends HttpServlet {

    // Python Pricing Service URL (Port 5005)
    private static final String PRICING_URL = "http://pricing-service:5000/api/pricing/calculate";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get Parameters
        String prodId1 = request.getParameter("product_id_1");
        String qty1 = request.getParameter("quantity_1");

        String prodId2 = request.getParameter("product_id_2");
        String qty2 = request.getParameter("quantity_2");

        String region = request.getParameter("region");

        // 2. Build JSON Payload manually
        // We handle a simple case of up to 2 products for this demo
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"region\": \"" + region + "\",");
        jsonBuilder.append("\"products\": [");

        if (prodId1 != null && !prodId1.isEmpty()) {
            jsonBuilder.append(String.format("{\"product_id\": %s, \"quantity\": %s}", prodId1, qty1));
        }

        if (prodId2 != null && !prodId2.isEmpty()) {
            if (!jsonBuilder.toString().endsWith("["))
                jsonBuilder.append(",");
            jsonBuilder.append(String.format("{\"product_id\": %s, \"quantity\": %s}", prodId2, qty2));
        }

        jsonBuilder.append("]");
        jsonBuilder.append("}");

        String resultJson = "";

        // 3. Call Python Service
        try {
            URL url = new URL(PRICING_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBuilder.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

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
            resultJson = "{\"error\": \"Connection Error: " + e.getMessage() + "\"}";
        }

        request.setAttribute("jsonData", resultJson);
        request.getRequestDispatcher("/pricing_result.jsp").forward(request, response);
    }
}