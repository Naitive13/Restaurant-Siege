package com.restaurant.siege.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.restaurant.siege.model.DurationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessingTime {
    private String salesPoint;
    private String dish;
    private Long preparationDuration;
    private DurationType durationUnit;
    @JsonIgnore
    private DishOrder dishOrder;
}
