package com.fleet.tracking.repository;

import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByPhone(String phone);
    List<Driver> findByCompanyId(Long companyId);
    List<Driver> findByCompanyIdAndStatus(Long companyId, DriverStatus status);
}
