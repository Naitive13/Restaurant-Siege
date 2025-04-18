package com.restaurant.siege.controller.endpoint;

import com.restaurant.siege.model.DurationType;
import com.restaurant.siege.model.ProcessingTimeType;
import com.restaurant.siege.model.entities.ProcessingTime;
import com.restaurant.siege.model.entities.SoldDish;
import com.restaurant.siege.service.DashboardService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardRestController {
  private static final Logger log = LoggerFactory.getLogger(DashboardRestController.class);
  private final DashboardService dashboardService;

  @GetMapping("/bestSales")
  public ResponseEntity<Object> getBestSales(
      @RequestParam LocalDateTime dateMin,
      @RequestParam LocalDateTime dateMax,
      @RequestParam int size) {
    try {
      List<SoldDish> soldDishes = dashboardService.getBestSales(dateMin, dateMax);
      List<SoldDish> body = new ArrayList<>();
      while (body.size() != size) {
        body.add(soldDishes.removeFirst());
      }
      return ResponseEntity.ok().body(body);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/dishes/{id}/processingTime")
  public ResponseEntity<Object> getProcessingTime(
      @PathVariable Long id,
      @RequestParam(defaultValue = "SECOND", required = false) String durationType,
      @RequestParam(defaultValue = "AVERAGE", required = false) String processingTimeType) {

    try {
      log.info(durationType);
      log.info(processingTimeType);
      ProcessingTime processingTime =
          dashboardService.getProcessingTimeFor(
              id,
              ProcessingTimeType.valueOf(processingTimeType),
              DurationType.valueOf(durationType));
      return ResponseEntity.ok().body(processingTime);

    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }
}
