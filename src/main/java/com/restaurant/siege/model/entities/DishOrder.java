package com.restaurant.siege.model.entities;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DishOrder {
    private Long dishOrderId;
    private Long dishId;
    private String dishName;
    private int quantity;
    private Long totalAmount;
    private List<DishOrderStatus> statusList;
    private DishOrderStatus actualStatus;
}
