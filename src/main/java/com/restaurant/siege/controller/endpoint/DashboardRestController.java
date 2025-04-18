package com.restaurant.siege.controller.endpoint;

import com.restaurant.siege.model.DurationType;
import com.restaurant.siege.model.ProcessingTimeType;
import com.restaurant.siege.model.entities.ProcessingTime;
import com.restaurant.siege.model.entities.SoldDish;
import com.restaurant.siege.model.rest.SoldDishRest;
import com.restaurant.siege.service.DashboardService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DashboardRestController {
  private static final Logger log = LoggerFactory.getLogger(DashboardRestController.class);
  private final DashboardService dashboardService;

  @GetMapping("/bestSales")
  public ResponseEntity<Object> getBestSales(
      @RequestParam(required = false) LocalDateTime dateMin,
      @RequestParam(required = false) LocalDateTime dateMax,
      @RequestParam int top) {
    try {
      List<SoldDish> soldDishes = dashboardService.getBestSales(dateMin, dateMax);
      while (true) {
        if (soldDishes.size() > top) {
          soldDishes.removeLast();
        } else {
          break;
        }
      }

      SoldDishRest soldDishRest = new SoldDishRest();
      soldDishRest.setUpdatedAt(LocalDateTime.now());
      soldDishRest.setSales(soldDishes);
      return ResponseEntity.ok().body(soldDishRest);
    } catch (Exception e) {
      log.info(e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/dishes/{id}/bestProcessingTime")
  public ResponseEntity<Object> getProcessingTime(
      @PathVariable Long id,
      @RequestParam(defaultValue = "SECOND", required = false) String durationUnit,
      @RequestParam(defaultValue = "AVERAGE", required = false) String calculationMode) {

    try {
      log.info(durationUnit);
      log.info(calculationMode);
      ProcessingTime processingTime =
          dashboardService.getProcessingTimeFor(
              id,
              ProcessingTimeType.valueOf(calculationMode),
              DurationType.valueOf(durationUnit));
      return ResponseEntity.ok().body(processingTime);

    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/syncrhonization")
  public ResponseEntity<Object> synchronization() {
    try {
      dashboardService.sync();
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error(e.getMessage());
      return ResponseEntity.internalServerError().body("Failed to sync");
    }
  }
}
