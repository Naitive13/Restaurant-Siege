package com.restaurant.siege.model.rest;

import com.restaurant.siege.model.entities.SoldDish;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SoldDishRest {
    private LocalDateTime updatedAt;
    private List<SoldDish> sales;
}
