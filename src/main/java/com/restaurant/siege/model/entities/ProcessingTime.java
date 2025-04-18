package com.restaurant.siege.model.entities;

import com.restaurant.siege.model.DurationType;
import com.restaurant.siege.model.ProcessingTimeType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessingTime {
    private ProcessingTimeType processingTimeType;
    private DurationType durationType;
    private Long value;
}
