package com.restaurant.siege.model.rest;

import com.restaurant.siege.model.entities.ProcessingTime;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ProcessingTimeRest {
    private LocalDateTime updatedAt;
    private List<ProcessingTime> bestProcessingTimes;
}
