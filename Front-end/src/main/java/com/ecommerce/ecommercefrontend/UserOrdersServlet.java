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

@WebServlet("/userOrders")
public class UserOrdersServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String CUSTOMERS_URL;

    public UserOrdersServlet() {
        String base = System.getenv("CUSTOMER_SERVICE_URL") != null
                ? System.getenv("CUSTOMER_SERVICE_URL")
                : "http://localhost:5002";
        CUSTOMERS_URL = base + "/api/customers";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String customerIdParam = request.getParameter("customer_id");
        if (customerIdParam == null || customerIdParam.isEmpty()) {
            HttpRequest customersRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CUSTOMERS_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            try {
                HttpResponse<String> customersResponse = httpClient.send(customersRequest,
                        HttpResponse.BodyHandlers.ofString());
                List<Map<String, Object>> customers = gson.fromJson(
                        customersResponse.body(),
                        new TypeToken<List<Map<String, Object>>>() {
                        }.getType());

                request.setAttribute("customers", customers);
                request.getRequestDispatcher("userOrders.jsp").forward(request, response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServletException(e);
            }
            return;
        }

        int customerId = (int) Double.parseDouble(customerIdParam);

        try {
            HttpRequest apiRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CUSTOMERS_URL + "/" + customerId + "/orders"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());

            if (apiResponse.statusCode() != 200) {
                request.setAttribute("error", "Failed to fetch orders.");
                request.getRequestDispatcher("userOrders.jsp").forward(request, response);
                return;
            }

            List<Map<String, Object>> ordersSummary = gson.fromJson(
                    apiResponse.body(),
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());

            request.setAttribute("orders", ordersSummary);
            request.setAttribute("customer_id", customerId);
            request.getRequestDispatcher("userOrders.jsp").forward(request, response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request interrupted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
