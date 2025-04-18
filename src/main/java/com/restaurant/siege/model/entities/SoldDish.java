package com.restaurant.siege.model.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SoldDish {
    private String dishName;
    private Long totalQuantities;
    private Double totalProfit;
}
