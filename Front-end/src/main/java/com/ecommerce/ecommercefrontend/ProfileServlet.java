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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String CUSTOMERS_URL;

    public ProfileServlet() {
        String customerBase = System.getenv("CUSTOMER_SERVICE_URL") != null
                ? System.getenv("CUSTOMER_SERVICE_URL")
                : "http://localhost:5002";
        CUSTOMERS_URL = String.format("%s/api/customers", customerBase);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpRequest customersRequest = HttpRequest.newBuilder()
                    .uri(URI.create(CUSTOMERS_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> customersResponse = httpClient.send(customersRequest, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> customers = gson.fromJson(
                    customersResponse.body(),
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());

            request.setAttribute("customers", customers);

            String customerIdParam = request.getParameter("customer_id");
            if (customerIdParam != null && !customerIdParam.isEmpty()) {
                try {
                    int customerId = (int) Double.parseDouble(customerIdParam);

                    HttpRequest customerRequest = HttpRequest.newBuilder()
                            .uri(URI.create(CUSTOMERS_URL + "/" + customerId))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<String> customerResponse = httpClient.send(customerRequest,
                            HttpResponse.BodyHandlers.ofString());

                    if (customerResponse.statusCode() == 200) {
                        Map<String, Object> selectedCustomer = gson.fromJson(
                                customerResponse.body(),
                                new TypeToken<Map<String, Object>>() {
                                }.getType());
                        request.setAttribute("selectedCustomer", selectedCustomer);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            request.getRequestDispatcher("profile.jsp").forward(request, response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request interrupted");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
