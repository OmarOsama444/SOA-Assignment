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
  private final String CUSTOMER_URL; // Base for customers
  private final String INVENTORY_URL;
  private final String NOTIFICATION_URL;

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

    String notificationBase = System.getenv("NOTIFICATION_SERVICE_URL") != null
        ? System.getenv("NOTIFICATION_SERVICE_URL")
        : "http://localhost:5005";
    NOTIFICATION_URL = String.format("%s/api/notifications/send", notificationBase);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      int customerId = (int) Double.parseDouble(request.getParameter("customer_id"));
      String[] productIds = request.getParameterValues("product_id[]");
      String[] quantities = request.getParameterValues("quantity[]");
      String totalAmountStr = request.getParameter("total_amount");
      double totalAmount = (totalAmountStr != null && !totalAmountStr.isEmpty()) ? Double.parseDouble(totalAmountStr)
          : 0.0;

      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put("customer_id", customerId);
      jsonRequest.put("total_amount", totalAmount);

      JSONArray productsArray = new JSONArray();
      if (productIds != null) {
        for (int i = 0; i < productIds.length; i++) {
          int quantity = (int) Double.parseDouble(quantities[i]);
          int productId = (int) Double.parseDouble(productIds[i]);

          JSONObject product = new JSONObject();
          product.put("product_id", productId);
          product.put("quantity", quantity);
          productsArray.put(product);
        }
      }
      jsonRequest.put("products", productsArray);

      // 1. Create Order
      HttpRequest orderRequest = HttpRequest.newBuilder()
          .uri(URI.create(ORDER_URL))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString(), StandardCharsets.UTF_8))
          .build();

      HttpResponse<String> orderResponse = httpClient.send(orderRequest, HttpResponse.BodyHandlers.ofString());

      if (orderResponse.statusCode() >= 400) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Order Service Failed: " + orderResponse.body());
        return;
      }

      JSONObject jsonResponse = new JSONObject(orderResponse.body());

      JSONObject orderObj = jsonResponse.getJSONObject("order");
      int orderId = -1;
      if (orderObj.has("order_id")) {
        // Handle both integer and double cases just in case
        Object oid = orderObj.get("order_id");
        orderId = (int) Double.parseDouble(String.valueOf(oid));
      }

      // 3. Send Notification
      // JSONObject notificationJson = new JSONObject();
      // notificationJson.put("order_id", orderId);
      // notificationJson.put("customer_id", customerId);
      // notificationJson.put("message",
      //     "Your order #" + orderId + " has been placed successfully. Total: $" + totalAmount);

      // HttpRequest notificationRequest = HttpRequest.newBuilder()
      //     .uri(URI.create(NOTIFICATION_URL))
      //     .header("Content-Type", "application/json")
      //     .POST(HttpRequest.BodyPublishers.ofString(notificationJson.toString(), StandardCharsets.UTF_8))
      //     .build();
      // httpClient.send(notificationRequest, HttpResponse.BodyHandlers.discarding());

      // Fetch Customer Name
      HttpRequest customerInfoRequest = HttpRequest.newBuilder()
          .uri(URI.create(CUSTOMER_URL + "/" + customerId))
          .header("Accept", "application/json")
          .GET()
          .build();
      HttpResponse<String> customerInfoResponse = httpClient.send(customerInfoRequest,
          HttpResponse.BodyHandlers.ofString());
      // Assuming returns the customer object directly
      JSONObject customerObj = new JSONObject(customerInfoResponse.body());
      String customerName = customerObj.optString("name", "Unknown Customer");

      // Fetch Product Names (Inventory)
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

      Map<Integer, String> productIdToName = new HashMap<>();
      for (Map<String, Object> p : inventory) {
        int pid = (int) Double.parseDouble(String.valueOf(p.get("product_id")));
        productIdToName.put(pid, (String) p.get("product_name"));
      }

      // Enrich Order Object for Display
      Map<String, Object> orderMap = orderObj.toMap();
      List<Map<String, Object>> orderProducts = (List<Map<String, Object>>) orderMap.get("products");
      if (orderProducts != null) {
        for (Map<String, Object> p : orderProducts) {
          if (p.containsKey("product_id")) {
            int pid = (int) Double.parseDouble(String.valueOf(p.get("product_id")));
            p.put("product_name", productIdToName.getOrDefault(pid, "Unknown Product"));
          }
        }
      }
      orderMap.put("customer_name", customerName);
      // Re-wrap in the structure expected by confirmation.jsp
      Map<String, Object> responseData = new HashMap<>();
      responseData.put("order", orderMap);

      request.setAttribute("order", responseData);
      request.getRequestDispatcher("confirmation.jsp").forward(request, response);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
