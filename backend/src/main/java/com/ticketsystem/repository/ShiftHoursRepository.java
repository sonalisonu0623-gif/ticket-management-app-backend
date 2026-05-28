package com.ticketsystem.repository;

import com.ticketsystem.entity.ShiftHours;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftHoursRepository extends JpaRepository<ShiftHours, Long> {
    List<ShiftHours> findByIsActiveTrue();
}
