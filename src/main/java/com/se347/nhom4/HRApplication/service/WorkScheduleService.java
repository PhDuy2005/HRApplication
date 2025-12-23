package com.se347.nhom4.HRApplication.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.ShiftRepository;
import com.se347.nhom4.HRApplication.repository.WorkScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    /**
     * Get all work schedules.
     */
    public List<WorkSchedule> findAll() {
        return workScheduleRepository.findAll();
    }

    /**
     * Find work schedule by ID.
     */
    public Optional<WorkSchedule> findById(Long id) {
        return workScheduleRepository.findById(id);
    }

    /**
     * Get work schedules by employee ID.
     */
    public List<WorkSchedule> findByEmployeeId(Long employeeId) {
        return workScheduleRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get work schedules by shift ID.
     */
    public List<WorkSchedule> findByShiftId(Long shiftId) {
        return workScheduleRepository.findByShiftId(shiftId);
    }

    /**
     * Get work schedules by work date.
     */
    public List<WorkSchedule> findByWorkDate(LocalDate workDate) {
        return workScheduleRepository.findByWorkDate(workDate);
    }

    /**
     * Get work schedules by employee ID and work date.
     */
    public List<WorkSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate) {
        return workScheduleRepository.findByEmployeeIdAndWorkDate(employeeId, workDate);
    }

    /**
     * Get active work schedules by employee ID and date range.
     */
    public List<WorkSchedule> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate,
            LocalDate endDate) {
        List<WorkSchedule> workSchedules = workScheduleRepository.findByEmployeeIdAndWorkDateBetween(employeeId,
                startDate, endDate);
        List<WorkSchedule> activeWorkSchedules = new ArrayList<>();
        for (WorkSchedule ws : workSchedules) {
            if (ws.getShift().getIsActive() == true) {
                activeWorkSchedules.add(ws);
            }
        }
        return activeWorkSchedules;
    }

    /**
     * Create new work schedule.
     */
    public WorkSchedule createWorkSchedule(WorkSchedule workSchedule) {
        return workScheduleRepository.save(workSchedule);
    }

    /**
     * Update work schedule.
     */
    public WorkSchedule updateWorkSchedule(Long id, WorkSchedule workSchedule) {
        return workScheduleRepository.save(workSchedule);
    }

    /**
     * Delete work schedule by ID.
     */
    public void deleteById(Long id) {
        workScheduleRepository.deleteById(id);
    }

    /**
     * Check if work schedule exists by employee, shift and work date.
     */
    public boolean existsByEmployeeIdAndShiftIdAndWorkDate(Long employeeId, Long shiftId, LocalDate workDate) {
        return workScheduleRepository.existsByEmployeeIdAndShiftIdAndWorkDate(employeeId, shiftId, workDate);
    }
}