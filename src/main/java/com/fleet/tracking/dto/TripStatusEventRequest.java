package com.fleet.tracking.dto;

import com.fleet.tracking.entity.TripStatusEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripStatusEventRequest {
    @NotNull
    private TripStatusEventType eventType;

    private String note;
}
