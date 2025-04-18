package com.restaurant.siege.model.entities;

import java.time.LocalDateTime;

import com.restaurant.siege.model.enums.StatusType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DishOrderStatus {
  private Long id;
  private Long dishOrderId;
  private StatusType status;
  private LocalDateTime creationDate;
}
