package com.fleet.tracking.dto;

import com.fleet.tracking.entity.Truck;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TruckDTO {
    private Long id;
    private String registrationNumber;
    private String model;
    private boolean active;

    public static TruckDTO from(Truck t) {
        return new TruckDTO(t.getId(), t.getRegistrationNumber(), t.getModel(), t.isActive());
    }
}
