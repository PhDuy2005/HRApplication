package com.se347.nhom4.HRApplication.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.domain.table.WorkSite;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.ShiftRepository;
import com.se347.nhom4.HRApplication.repository.WorkScheduleRepository;
import com.se347.nhom4.HRApplication.repository.WorkSiteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final WorkSiteRepository workSiteRepository;

    // gán WorkSite cho WorkSchedule (phục vụ GPS chấm công)
    @Transactional
    public WorkSchedule assignWorkSite(Long workScheduleId, Long workSiteId) {
        WorkSchedule ws = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found with id: " + workScheduleId));

        WorkSite site = workSiteRepository.findById(workSiteId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSite not found with id: " + workSiteId));

        if (site.getActive() != null && !site.getActive()) {
            throw new IllegalArgumentException("WorkSite đang inactive, không thể gán");
        }

        ws.setWorkSite(site);
        return workScheduleRepository.save(ws);
    }

    // bỏ WorkSite khỏi WorkSchedule
    @Transactional
    public WorkSchedule removeWorkSite(Long workScheduleId) {
        WorkSchedule ws = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found with id: " + workScheduleId));

        ws.setWorkSite(null);
        return workScheduleRepository.save(ws);
    }

    /**
     * Get all work schedules.
     */
    public List<WorkSchedule> findAll() {
        System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch all work schedules in WS Service");
        return workScheduleRepository.findAll();
    }

    /**
     * Find work schedule by ID.
     */
    public Optional<WorkSchedule> findById(Long id) {
        System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedule by ID in WS Service: " + id);
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
     * Get active work schedules by shift ID and date range.
     */
    public List<WorkSchedule> findByShiftIdAndWorkDateBetween(Long shiftId, LocalDate startDate, LocalDate endDate) {
        List<WorkSchedule> workSchedules = workScheduleRepository.findByShiftIdAndWorkDateBetween(shiftId, startDate,
                endDate);
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
        // kiểm tra worksite id
        if (workSchedule.getWorkSite() == null || workSchedule.getWorkSite().getId() == null)
            throw new IllegalArgumentException("workSite.id is required");
        var site = workSiteRepository.findById(workSchedule.getWorkSite().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSite not found: " + workSchedule.getWorkSite().getId()));
        if (site.getActive() != null && !site.getActive())
            throw new IllegalArgumentException("WorkSite inactive");
        workSchedule.setWorkSite(site);

        return workScheduleRepository.save(workSchedule);
    }

    /**
     * Update work schedule.
     */
    @Transactional
public WorkSchedule updateWorkSchedule(Long id, WorkSchedule req) {
    WorkSchedule existing = workScheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found: " + id));

    // 1) WorkDate
    if (req.getWorkDate() != null) {
        existing.setWorkDate(req.getWorkDate());
    }

    // hoặc:
    // if (req.getDescription() != null) existing.setDescription(req.getDescription());

    // 3) Employee (nếu cho phép đổi nhân viên)
    if (req.getEmployee() != null && req.getEmployee().getId() != null) {
        var emp = employeeRepository.findById(req.getEmployee().getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + req.getEmployee().getId()));
        // nếu có active:
        // if (Boolean.FALSE.equals(emp.getActive())) throw new IllegalArgumentException("Employee inactive");
        existing.setEmployee(emp);
    }

    // 4) Shift (nếu cho phép đổi ca)
    if (req.getShift() != null && req.getShift().getId() != null) {
        var shift = shiftRepository.findById(req.getShift().getId())
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + req.getShift().getId()));
        // nếu shift có active:
        // if (Boolean.FALSE.equals(shift.getIsActive())) throw new IllegalArgumentException("Shift inactive");
        existing.setShift(shift);
    }

    // 5) WorkSite (đoạn bạn đã làm, giữ lại)
    if (req.getWorkSite() != null && req.getWorkSite().getId() != null) {
        var site = workSiteRepository.findById(req.getWorkSite().getId())
                .orElseThrow(() -> new IllegalArgumentException("WorkSite not found: " + req.getWorkSite().getId()));
        if (Boolean.FALSE.equals(site.getActive()))
            throw new IllegalArgumentException("WorkSite inactive");
        existing.setWorkSite(site);
    }

   
    return workScheduleRepository.save(existing);
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