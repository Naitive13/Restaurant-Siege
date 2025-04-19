package com.restaurant.siege.service;

import static com.restaurant.siege.model.enums.StatusType.DONE;
import static java.util.Comparator.naturalOrder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurant.siege.model.DurationType;
import com.restaurant.siege.model.ProcessingTimeType;
import com.restaurant.siege.model.entities.DishOrder;
import com.restaurant.siege.model.entities.DishOrderStatus;
import com.restaurant.siege.model.entities.ProcessingTime;
import com.restaurant.siege.model.entities.SoldDish;
import com.restaurant.siege.repository.dao.DishOrderDAO;
import com.restaurant.siege.service.mapper.ProcessingTimeMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
  private final DishOrderDAO dishOrderDAO;
  private final ProcessingTimeMapper processingTimeMapper;

  public List<SoldDish> getBestSales(LocalDateTime dateMin, LocalDateTime dateMax) {
    List<DishOrder> dishOrders = dishOrderDAO.getAll(1, 100);
    List<DishOrder> finished =
        dishOrders.stream()
            .filter(
                dishOrder ->
                    dishOrder.getStatusList().stream()
                        .anyMatch(dishOrderStatus -> dishOrderStatus.getStatus().equals(DONE)))
            .toList();
    List<DishOrder> dishOrdersBetweenInterval =
        finished.stream()
            .filter(
                dishOrder -> {
                  if (dateMax != null && dateMin != null) {
                    return filterDishOrder(dateMin, dateMax, dishOrder);
                  }
                  return true;
                })
            .toList();

    List<SoldDish> soldDishes = new ArrayList<>();
    dishOrdersBetweenInterval.forEach(
        dishOrder -> {
          if (soldDishes.stream()
              .anyMatch(soldDish -> soldDish.getDishName().equals(dishOrder.getDishName()))) {
            for (int i = 0; i < soldDishes.size(); i++) {
              if (soldDishes.get(i).getDishName().equals(dishOrder.getDishName())) {
                Double profit = soldDishes.get(i).getTotalProfit();
                soldDishes.get(i).setTotalProfit(profit + dishOrder.getTotalAmount());
                Long quantity = soldDishes.get(i).getTotalQuantities();
                soldDishes.get(i).setTotalQuantities(quantity + dishOrder.getQuantity());
              }
            }
          } else {
            SoldDish soldDish = new SoldDish();

            soldDish.setSalesPoint(dishOrder.getSalesPoint());
            soldDish.setDishName(dishOrder.getDishName());
            soldDish.setTotalProfit(dishOrder.getTotalAmount().doubleValue());
            soldDish.setTotalQuantities((long) dishOrder.getQuantity());

            soldDishes.add(soldDish);
          }
        });

    return new ArrayList<>(
        soldDishes.stream()
            .sorted(Comparator.comparing(SoldDish::getTotalQuantities, naturalOrder()))
            .toList());
  }

  public List<ProcessingTime> getProcessingTimeFor(
      Long dishId, ProcessingTimeType processingTimeType, DurationType durationType) {
    List<DishOrder> dishOrders = dishOrderDAO.getByDishId(dishId);
    List<ProcessingTime> processingTimes = new ArrayList<>(dishOrders.stream().map(processingTimeMapper).toList());

    switch (processingTimeType) {
      case MINIMUM -> {
        processingTimes.sort(Comparator.comparing(ProcessingTime::getRawDuration,naturalOrder()));
      }

      case MAXIMUM -> {
        processingTimes.sort(Comparator.comparing(ProcessingTime::getRawDuration,naturalOrder()).reversed());
      }

      case AVERAGE -> {
        processingTimes.forEach(processingTime -> {
          processingTime.setRawDuration(processingTime.getRawDuration().dividedBy(processingTime.getDishOrder().getQuantity()));
        });
        processingTimes.sort(Comparator.comparing(ProcessingTime::getRawDuration,naturalOrder()));
      }
    }

    switch (durationType) {
      case SECOND -> {
        processingTimes.forEach(processingTime -> {
          processingTime.setDurationUnit(durationType);
          processingTime.setPreparationDuration(processingTime.getRawDuration().toSeconds());
        });
      }
      case MINUTE -> {
        processingTimes.forEach(processingTime -> {
          processingTime.setDurationUnit(durationType);
          processingTime.setPreparationDuration(processingTime.getRawDuration().toMinutes());
        });
      }
      case HOUR -> {
        processingTimes.forEach(processingTime -> {
          processingTime.setDurationUnit(durationType);
          processingTime.setPreparationDuration(processingTime.getRawDuration().toHours());
        });
      }
    }
    return processingTimes;
  }

  public void sync() {
    String urls = System.getenv("SALES_POINT_URLS");
    List<String> salesPoints = Arrays.asList(urls.split(","));
    log.info(salesPoints.toString());
    salesPoints.forEach(
        url -> {
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

  private boolean filterDishOrder(
      LocalDateTime dateMin, LocalDateTime dateMax, DishOrder dishOrder) {
    DishOrderStatus actualStatus =
        dishOrder.getStatusList().stream()
            .filter(
                dishOrderStatus -> dishOrder.getActualStatus().equals(dishOrderStatus.getStatus()))
            .findFirst()
            .get();

    return actualStatus.getCreationDate().isAfter(dateMin)
        && actualStatus.getCreationDate().isBefore(dateMax);
  }
}
