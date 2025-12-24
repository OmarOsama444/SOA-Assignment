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
  private final String ORDERS_URL;

  public UserOrdersServlet() {
    String base = System.getenv("CUSTOMER_SERVICE_URL") != null
        ? System.getenv("CUSTOMER_SERVICE_URL")
        : "http://localhost:5002";
    CUSTOMERS_URL = base + "/api/customers";

    String orderBase = System.getenv("ORDER_SERVICE_URL") != null
        ? System.getenv("ORDER_SERVICE_URL")
        : "http://localhost:5001";
    ORDERS_URL = orderBase + "/api/orders"; // Will append /{id}
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String customerIdParam = request.getParameter("customer_id");
    if (customerIdParam == null || customerIdParam.isEmpty()) {
      // Fetch customers for selection
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
      // 1. Get List of Orders
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

      // 2. Enrich with Details (Scenario 3 Requirement)
      List<Map<String, Object>> detailedOrders = new java.util.ArrayList<>();

      if (ordersSummary != null) {
        for (Map<String, Object> summary : ordersSummary) {
          int orderId = (int) Double.parseDouble(String.valueOf(summary.get("order_id")));

          HttpRequest orderRequest = HttpRequest.newBuilder()
              .uri(URI.create(ORDERS_URL + "/" + orderId))
              .header("Accept", "application/json")
              .GET()
              .build();

          HttpResponse<String> orderResponse = httpClient.send(orderRequest, HttpResponse.BodyHandlers.ofString());

          if (orderResponse.statusCode() == 200) {
            Map<String, Object> orderResponseMap = gson.fromJson(
                orderResponse.body(),
                new TypeToken<Map<String, Object>>() {
                }.getType());

            if (orderResponseMap.containsKey("order")) {
              detailedOrders.add((Map<String, Object>) orderResponseMap.get("order"));
            } else {
              detailedOrders.add(orderResponseMap);
            }
          } else {
            detailedOrders.add(summary);
          }
        }
      }

      // 3. Fetch Inventory to Map Product Names
      String INVENTORY_URL;
      String inventoryBase = System.getenv("INVENTORY_SERVICE_URL") != null
          ? System.getenv("INVENTORY_SERVICE_URL")
          : "http://localhost:5003";
      INVENTORY_URL = String.format("%s/api/inventory/products", inventoryBase);

      HttpRequest inventoryRequest = HttpRequest.newBuilder()
          .uri(URI.create(INVENTORY_URL))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> inventoryResponse = httpClient.send(inventoryRequest, HttpResponse.BodyHandlers.ofString());
      List<Map<String, Object>> inventory = gson.fromJson(
          inventoryResponse.body(),
          new TypeToken<List<Map<String, Object>>>() {
          }.getType());

      Map<Integer, String> productIdToName = new java.util.HashMap<>();
      if (inventory != null) {
        for (Map<String, Object> p : inventory) {
          int pid = (int) Double.parseDouble(String.valueOf(p.get("product_id")));
          productIdToName.put(pid, (String) p.get("product_name"));
        }
      }

      // 4. Enrich Orders with Product Names
      for (Map<String, Object> order : detailedOrders) {
        List<Map<String, Object>> products = (List<Map<String, Object>>) order.get("products");
        if (products != null) {
          for (Map<String, Object> p : products) {
            // Some services return "productID", others "product_id". Handling standard
            // "product_id".
            if (p.containsKey("product_id")) {
              int pid = (int) Double.parseDouble(String.valueOf(p.get("product_id")));
              p.put("product_name", productIdToName.getOrDefault(pid, "Unknown Product (" + pid + ")"));
            }
          }
        }
      }
      request.setAttribute("orders", detailedOrders);
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
