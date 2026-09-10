package com.fleet.tracking.dto;

import com.fleet.tracking.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;

// Never expose the Driver entity directly over JSON: it carries passwordHash
// and a lazy `company` association that Jackson can't serialize outside a
// Hibernate session. This is the safe, flat shape returned by the API instead.
@Data
@AllArgsConstructor
public class DriverDTO {
    private Long id;
    private String name;
    private String phone;
    private String status;

    public static DriverDTO from(Driver d) {
        return new DriverDTO(d.getId(), d.getName(), d.getPhone(), d.getStatus().name());
    }
}
