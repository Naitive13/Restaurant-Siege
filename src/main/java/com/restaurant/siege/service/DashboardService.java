package com.restaurant.siege.service;

import static com.restaurant.siege.model.enums.StatusType.DONE;
import static com.restaurant.siege.model.enums.StatusType.IN_PROGRESS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurant.siege.model.DurationType;
import com.restaurant.siege.model.ProcessingTimeType;
import com.restaurant.siege.model.entities.DishOrder;
import com.restaurant.siege.model.entities.ProcessingTime;
import com.restaurant.siege.model.entities.SoldDish;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.restaurant.siege.repository.dao.DishOrderDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DishOrderDAO dishOrderDAO;

  public List<SoldDish> getBestSales(LocalDateTime dateMin, LocalDateTime dateMax) {
    //    List<DishOrder> dishOrders = dishOrderDAO.getAll(1, 100);
    throw new UnsupportedOperationException("not done yet");
    //    try (HttpClient client = HttpClient.newHttpClient()) {
    //      HttpRequest request = HttpRequest.newBuilder().uri(new
    // URI("http://localhost:8081")).build();
    //      HttpResponse<String> response = client.send(request,
    // HttpResponse.BodyHandlers.ofString());
    //      ObjectMapper objectMapper = new ObjectMapper();
    //
    //      List<DishOrder> dishOrders =
    //          objectMapper.readValue(response.body(), new TypeReference<List<DishOrder>>() {});
    //
    //      List<DishOrder> finished =
    //          dishOrders.stream()
    //              .filter(
    //                  dishOrder ->
    //                      dishOrder.getStatusList().stream()
    //                          .anyMatch(dishOrderStatus ->
    // dishOrderStatus.getStatus().equals(DONE)))
    //              .toList();
    //      List<DishOrder> dishOrdersBetweenInterval =
    //          finished.stream()
    //              .filter(
    //                  dishOrder ->
    //                      (dishOrder.getActualStatus().getCreationDate().isAfter(dateMin)
    //                          && dishOrder.getActualStatus().getCreationDate().isBefore(dateMax)))
    //              .toList();
    //
    //      List<SoldDish> soldDishes = new ArrayList<>();
    //      dishOrdersBetweenInterval.forEach(
    //          dishOrder -> {
    //            if (soldDishes.stream()
    //                .anyMatch(soldDish -> soldDish.getDishName().equals(dishOrder.getDishName())))
    // {
    //              for (int i = 0; i < soldDishes.size(); i++) {
    //                if (soldDishes.get(i).getDishName().equals(dishOrder.getDishName())) {
    //                  Double profit = soldDishes.get(i).getTotalProfit();
    //                  soldDishes.get(i).setTotalProfit(profit + dishOrder.getTotalAmount());
    //                  Long quantity = soldDishes.get(i).getTotalQuantities();
    //                  soldDishes.get(i).setTotalQuantities(quantity + dishOrder.getQuantity());
    //                }
    //              }
    //            } else {
    //              SoldDish soldDish = new SoldDish();
    //
    //              soldDish.setDishName(dishOrder.getDishName());
    //              soldDish.setTotalProfit(dishOrder.getTotalAmount().doubleValue());
    //              soldDish.setTotalQuantities((long) dishOrder.getQuantity());
    //
    //              soldDishes.add(soldDish);
    //            }
    //          });
    //
    //      return soldDishes.stream()
    //          .sorted(Comparator.comparing(SoldDish::getTotalQuantities,
    // Comparator.naturalOrder()))
    //          .toList();
    //
    //    } catch (Exception e) {
    //      throw new RuntimeException(e);
    //    }
  }

  public ProcessingTime getProcessingTimeFor(
      Long dishId, ProcessingTimeType processingTimeType, DurationType durationType) {
    //    List<DishOrder> dishOrders = dishOrderDAO.getByDishId(dishId);
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI("http://localhost:8081/dishOrders?page=1&pageSize=10"))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      log.info(response.body());

      List<DishOrder> dishOrders =
          objectMapper.readValue(response.body(), new TypeReference<List<DishOrder>>() {});

      List<Duration> durations =
          dishOrders.stream()
              .map(
                  dishOrder -> {
                    LocalDateTime inProgressDate =
                        dishOrder.getStatusList().stream()
                            .filter(
                                dishOrderStatus -> dishOrderStatus.getStatus().equals(IN_PROGRESS))
                            .toList()
                            .getFirst()
                            .getCreationDate();
                    LocalDateTime doneDate =
                        dishOrder.getStatusList().stream()
                            .filter(dishOrderStatus -> dishOrderStatus.getStatus().equals(DONE))
                            .toList()
                            .getFirst()
                            .getCreationDate();
                    return Duration.between(inProgressDate, doneDate);
                  })
              .toList();

      ProcessingTime result = new ProcessingTime();
      Duration duration = null;
      result.setProcessingTimeType(processingTimeType);
      result.setDurationType(durationType);
      switch (processingTimeType) {
        case MINIMUM -> {
          duration = durations.stream().min(Duration::compareTo).get();
        }

        case MAXIMUM -> {
          duration = durations.stream().max(Duration::compareTo).get();
        }

        case AVERAGE -> {
          duration = durations.stream().reduce((Duration::plus)).get().dividedBy(durations.size());
        }
      }

      switch (durationType) {
        case SECOND -> result.setValue(duration.toSeconds());
        case MINUTE -> result.setValue(duration.toMinutes());
        case HOUR -> result.setValue(duration.toHours());
      }

      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sync() {
    String urls = System.getenv("SALES_POINT_URLS");
    List<String> salesPoints = Arrays.asList(urls.split(","));
    log.info(salesPoints.toString());
    salesPoints.forEach(url -> {
      log.info(url);
      List<DishOrder> dishOrders = fetchFromRestaurant(url);
      dishOrderDAO.saveAll(dishOrders);
    });
  }

  private List<DishOrder> fetchFromRestaurant(String restaurantURL) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(restaurantURL + "/dishOrders?page=1&pageSize=10"))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      log.debug(response.body());

      return objectMapper.readValue(response.body(), new TypeReference<List<DishOrder>>() {});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
