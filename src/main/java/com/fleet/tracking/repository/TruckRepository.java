package com.fleet.tracking.repository;

import com.fleet.tracking.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    List<Truck> findByCompanyIdAndActiveTrue(Long companyId);
}
