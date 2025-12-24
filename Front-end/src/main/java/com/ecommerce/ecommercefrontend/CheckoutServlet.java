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

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final Gson gson = new Gson();
  private final String INVENTORY_URL;
  private final String CUSTOMERS_URL;
  private final String PRICING_URL;

  public CheckoutServlet() {
    String inventoryBase = System.getenv("INVENTORY_SERVICE_URL") != null
        ? System.getenv("INVENTORY_SERVICE_URL")
        : "http://localhost:5003";
    INVENTORY_URL = String.format("%s/api/inventory", inventoryBase);

    String customerBase = System.getenv("CUSTOMER_SERVICE_URL") != null
        ? System.getenv("CUSTOMER_SERVICE_URL")
        : "http://localhost:5002";
    CUSTOMERS_URL = String.format("%s/api/customers", customerBase);

    String pricingBase = System.getenv("PRICING_SERVICE_URL") != null
        ? System.getenv("PRICING_SERVICE_URL")
        : "http://localhost:5005";
    PRICING_URL = String.format("%s/api/pricing/calculate", pricingBase);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Redirect to main page if accessed using GET
    response.sendRedirect("loadInventory");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      String[] selectedProductsIds = request.getParameterValues("selected_product");

      if (selectedProductsIds == null || selectedProductsIds.length == 0) {
        response.sendRedirect("loadInventory?error=No products selected");
        return;
      }

      // Prepare products list for Pricing Service and Display
      List<Map<String, Object>> selectedProducts = new java.util.ArrayList<>();

      // Pricing Service Payload
      org.json.JSONObject pricingRequestJson = new org.json.JSONObject();
      org.json.JSONArray productsArray = new org.json.JSONArray();

      for (String pidStr : selectedProductsIds) {
        int pid = (int) Double.parseDouble(pidStr);
        String selectedQuantityStr = request.getParameter("quantity_" + pid);
        int selectedQuantity = (selectedQuantityStr != null && !selectedQuantityStr.isEmpty()) ?
                (int) Double.parseDouble(selectedQuantityStr)
                : 0;

        if (selectedQuantity <= 0)
          continue; // Skip invalid quantities

        String checkUrl = String.format("%s/check/%d", INVENTORY_URL, pid);
        HttpRequest checkRequest = HttpRequest.newBuilder()
            .uri(URI.create(checkUrl))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> checkResponse = httpClient.send(checkRequest, HttpResponse.BodyHandlers.ofString());

        if (checkResponse.statusCode() == 200) {
          Map<String, Object> product = gson.fromJson(
              checkResponse.body(),
              new TypeToken<Map<String, Object>>() {
              }.getType());

          int available = (int) Double.parseDouble(String.valueOf(product.get("quantity")));

          if (selectedQuantity > available) {
            response.sendRedirect(
                "loadInventory?error=Quantity for " + product.get("product_name") + " exceeds availability.");
            return;
          }

          product.put("selected_quantity", selectedQuantity);

          selectedProducts.add(product);

          org.json.JSONObject item = new org.json.JSONObject();
          item.put("product_id", pid);
          item.put("quantity", selectedQuantity);
          productsArray.put(item);
        } else {
          response.sendRedirect("loadInventory?error=Failed to verify product " + pid);
          return;
        }
      }

      if (selectedProducts.isEmpty()) {
        response.sendRedirect("loadInventory?error=Please select quantity for selected products");
        return;
      }

      pricingRequestJson.put("products", productsArray);

      // 2. Call Pricing Service
      HttpRequest pricingRequest = HttpRequest.newBuilder()
          .uri(URI.create(PRICING_URL))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(pricingRequestJson.toString(),
              java.nio.charset.StandardCharsets.UTF_8))
          .build();

      HttpResponse<String> pricingResponse = httpClient.send(pricingRequest, HttpResponse.BodyHandlers.ofString());

      double totalAmount = 0.0;
      if (pricingResponse.statusCode() == 200) {
        org.json.JSONObject pricingResult = new org.json.JSONObject(pricingResponse.body());

        totalAmount = pricingResult.getDouble("total_amount");

        // Update selectedProducts with pricing details.
        org.json.JSONArray pricedProducts = pricingResult.getJSONArray("products");
        for (int i = 0; i < pricedProducts.length(); i++) {
          org.json.JSONObject pricedItem = pricedProducts.getJSONObject(i);
          int pId = pricedItem.getInt("product_id");

          // Find matching product in selectedProducts and update it
          for (Map<String, Object> product : selectedProducts) {
            int selectedPId = (int) Double.parseDouble(String.valueOf(product.get("product_id")));
            if (selectedPId == pId) {
              product.put("discount_applied", pricedItem.getDouble("discount_applied"));
              product.put("total_price_after_discount", pricedItem.getDouble("total_price_after_discount"));
              break;
            }
          }
        }
      }

      // 3. Fetch Customers for Selection
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

      // 4. Pass data to JSP
      request.setAttribute("selectedProducts", selectedProducts);
      request.setAttribute("totalAmount", totalAmount);
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
