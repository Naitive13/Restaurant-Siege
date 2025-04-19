package com.restaurant.siege.service.mapper;

import com.restaurant.siege.model.entities.DishOrder;
import com.restaurant.siege.model.entities.ProcessingTime;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

import static com.restaurant.siege.model.enums.StatusType.DONE;
import static com.restaurant.siege.model.enums.StatusType.IN_PROGRESS;

@Component
public class ProcessingTimeMapper implements Function<DishOrder, ProcessingTime> {
  @Override
  public ProcessingTime apply(DishOrder dishOrder) {
    LocalDateTime inProgressDate =
        dishOrder.getStatusList().stream()
            .filter(dishOrderStatus -> dishOrderStatus.getStatus().equals(IN_PROGRESS))
            .toList()
            .getFirst()
            .getCreationDate();
    LocalDateTime doneDate =
        dishOrder.getStatusList().stream()
            .filter(dishOrderStatus -> dishOrderStatus.getStatus().equals(DONE))
            .toList()
            .getFirst()
            .getCreationDate();
    Duration duration = Duration.between(inProgressDate, doneDate);

    ProcessingTime processingTime = new ProcessingTime();
    processingTime.setDishOrder(dishOrder);
    processingTime.setSalesPoint(dishOrder.getSalesPoint());
    processingTime.setDish(dishOrder.getDishName());
    processingTime.setRawDuration(duration);

    return processingTime;
  }
}
