package com.restaurant.siege.model.entities;

import java.util.List;

import com.restaurant.siege.model.enums.StatusType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DishOrder {
    private Long dishOrderId;
    private String salesPoint;
    private Long dishId;
    private String dishName;
    private int quantity;
    private Long totalAmount;
    private List<DishOrderStatus> statusList;
    private StatusType actualStatus;
}
