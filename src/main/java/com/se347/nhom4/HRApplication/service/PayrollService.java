package com.se347.nhom4.HRApplication.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.Payroll;
import com.se347.nhom4.HRApplication.domain.table.Shift;
import com.se347.nhom4.HRApplication.domain.table.ShiftOtRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftSpecialRate;
import com.se347.nhom4.HRApplication.repository.AttendanceRepository;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.PayrollRepository;
import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.PayrollStatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final DayTypeService dayTypeService;

    /**
     * Lấy tất cả bảng lương
     */
    public List<Payroll> findAll() {
        return payrollRepository.findAll();
    }

    /**
     * Lấy bảng lương theo ID
     */
    public Optional<Payroll> findById(Long id) {
        return payrollRepository.findById(id);
    }

    /**
     * Lấy bảng lương theo nhân viên
     */
    public List<Payroll> findByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployee_Id(employeeId);
    }

    /**
     * Lấy bảng lương theo tháng/năm
     */
    public List<Payroll> findByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }

    /**
     * Lấy bảng lương của nhân viên theo tháng/năm
     */
    public Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year) {
        return payrollRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year);
    }

    /**
     * Tính lương cho toàn bộ nhân viên trong tháng/năm
     */
    @Transactional
    public List<Payroll> calculateSalaryForAll(Integer month, Integer year) {
        // Lấy tất cả nhân viên
        List<Employee> employees = employeeRepository.findAll();

        // Tính lương cho từng nhân viên
        return employees.stream()
                .map(employee -> calculateSalary(employee.getId(), month, year))
                .toList();
    }

    /**
     * Tính toán lại lương cho nhân viên (xóa bảng lương cũ và tính lại)
     */
    @Transactional
    public Payroll recalculateSalary(Long employeeId, Integer month, Integer year) {
        // Xóa bảng lương cũ nếu có
        Optional<Payroll> existingPayroll = payrollRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year);
        existingPayroll.ifPresent(payroll -> payrollRepository.delete(payroll));

        // Tính lương mới từ đầu
        return calculateSalaryFromScratch(employeeId, month, year);
    }

    /**
     * Tính toán lại lương cho toàn bộ nhân viên trong tháng/năm
     */
    @Transactional
    public List<Payroll> recalculateSalaryForAll(Integer month, Integer year) {
        // Lấy tất cả nhân viên
        List<Employee> employees = employeeRepository.findAll();

        // Tính toán lại lương cho từng nhân viên
        return employees.stream()
                .map(employee -> recalculateSalary(employee.getId(), month, year))
                .toList();
    }

    /**
     * Tính lương cho nhân viên (nếu đã có thì trả về, nếu chưa thì tính mới)
     */
    @Transactional
    public Payroll calculateSalary(Long employeeId, Integer month, Integer year) {
        // 1. Kiểm tra nếu đã có bảng lương cho nhân viên trong tháng/năm này, nếu có
        // thì trả về bảng lương đó
        Optional<Payroll> existingPayroll = payrollRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year);
        if (existingPayroll.isPresent()) {
            return existingPayroll.get();
        }

        // 2. Nếu chưa có thì tính mới
        return calculateSalaryFromScratch(employeeId, month, year);
    }

    /**
     * Tính lương cho nhân viên từ đầu (không kiểm tra bảng lương cũ)
     */
    @Transactional
    public Payroll calculateSalaryFromScratch(Long employeeId, Integer month, Integer year) {
        // Lấy thông tin nhân viên
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // 1. Lấy thông tin List<chấm công> từ tháng/năm
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Attendance> attendances = attendanceRepository.findByEmployee_IdAndWorkDateBetween(employeeId, startDate,
                endDate);

        // Khởi tạo các biến tính toán
        long totalHour = 0;
        long totalOtHour = 0;
        long baseSalary = 0;
        long shiftSalary = 0;
        long otSalary = 0;

        Instant now = Instant.now();

        // 2. Với mỗi chấm công, tính toán lương
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() == null || attendance.getCheckOut() == null) {
                continue; // Bỏ qua nếu chưa hoàn thành check-in/check-out
            }

            // Kiểm tra có workSchedule và shift không
            if (attendance.getWorkSchedule() == null || attendance.getWorkSchedule().getShift() == null) {
                continue; // Bỏ qua nếu không có thông tin ca làm việc
            }

            Shift shift = attendance.getWorkSchedule().getShift();

            // Xác định loại ngày (DayTypeEnum) của ca đó
            DayTypeEnum dayType = dayTypeService.getDayType(attendance.getWorkDate());

            // Lấy ShiftRate tương ứng: Ưu tiên ShiftSpecialRate, nếu không có thì dùng
            // ShiftBaseRate
            ShiftRate shiftRate = findApplicableShiftRate(employee, shift, dayType, now);

            if (shiftRate == null) {
                continue; // Bỏ qua nếu không tìm thấy shift rate
            }

            // 3. Tính số giờ làm việc (totalWorkTime đang tính bằng phút)
            Integer workTimeMinutes = attendance.getTotalWorkTime() != null ? attendance.getTotalWorkTime() : 0;
            double workHours = workTimeMinutes / 60.0;

            // 4. Tính lương ca làm việc
            long hourlyRate = shiftRate.getBaseRate();
            BigDecimal rateMultiplier = shiftRate.getRateMultiplier();
            long shiftAmount = (long) (hourlyRate * rateMultiplier.doubleValue() * workHours);

            totalHour += workTimeMinutes;
            shiftSalary += shiftAmount;

            // 5. Nếu có làm thêm giờ, tính lương OT
            Integer overtimeMinutes = attendance.getOvertime() != null ? attendance.getOvertime() : 0;
            if (overtimeMinutes > 0) {
                double otHours = overtimeMinutes / 60.0;
                totalOtHour += overtimeMinutes;

                // Lấy ShiftOtRate (nếu không có thì hệ số mặc định là 1.0)
                ShiftOtRate otRate = findActiveShiftOtRate(employee, now);
                BigDecimal otMultiplier = otRate != null ? otRate.getRateMultiplier() : BigDecimal.ONE;

                long otAmount = (long) (hourlyRate * otMultiplier.doubleValue() * otHours);
                otSalary += otAmount;
            }
        }

        // Chuyển đổi từ phút sang giờ cho totalHour và totalOtHour
        totalHour = totalHour / 60;
        totalOtHour = totalOtHour / 60;

        // 6. Lấy allowance từ employee (phụ cấp cố định hàng tháng)
        long allowance = employee.getAllowance() != null ? employee.getAllowance() : 0L;

        // 7. Tính final salary = shift salary + OT salary + allowance - penalty
        long finalSalary = shiftSalary + otSalary + allowance;

        // 8. Lưu bảng lương mới vào database
        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .totalHour(totalHour)
                .totalOtHour(totalOtHour)
                .baseSalary(baseSalary)
                .shiftSalary(shiftSalary)
                .otSalary(otSalary)
                .penaltyTotal(0L) // TODO: Implement penalty calculation
                .finalSalary(finalSalary)
                .status(PayrollStatusEnum.APPROVED)
                .build();

        return payrollRepository.save(payroll);
    }

    /**
     * Tìm ShiftRate phù hợp cho attendance
     * Ưu tiên: ShiftSpecialRate (nếu match chính xác shift) > ShiftBaseRate
     */
    private ShiftRate findApplicableShiftRate(Employee employee, Shift shift, DayTypeEnum dayType, Instant now) {
        // 1. Tìm ShiftSpecialRate cho chính xác shift này
        ShiftRate specialRate = employee.getShiftRates().stream()
                .filter(rate -> rate instanceof ShiftSpecialRate)
                .map(rate -> (ShiftSpecialRate) rate)
                .filter(rate -> rate.getShift() != null && rate.getShift().getId().equals(shift.getId()))
                .filter(rate -> rate.getDayType() == dayType)
                .filter(rate -> rate.getIsActive())
                .filter(rate -> rate.getEffectiveFrom().isBefore(now) || rate.getEffectiveFrom().equals(now))
                .filter(rate -> rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(now))
                .findFirst()
                .orElse(null);

        if (specialRate != null) {
            return specialRate;
        }

        // 2. Nếu không có ShiftSpecialRate, dùng ShiftBaseRate
        return employee.getShiftRates().stream()
                .filter(rate -> !(rate instanceof ShiftSpecialRate)) // Chỉ lấy BaseRate
                .filter(rate -> rate.getDayType() == dayType)
                .filter(rate -> rate.getIsActive())
                .filter(rate -> rate.getEffectiveFrom().isBefore(now) || rate.getEffectiveFrom().equals(now))
                .filter(rate -> rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(now))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tìm ShiftRate đang active cho nhân viên theo loại ngày
     * 
     * @deprecated Use findApplicableShiftRate instead
     */
    @Deprecated
    private ShiftRate findActiveShiftRate(Employee employee, DayTypeEnum dayType, Instant now) {
        return employee.getShiftRates().stream()
                .filter(rate -> rate.getDayType() == dayType)
                .filter(rate -> rate.getIsActive())
                .filter(rate -> rate.getEffectiveFrom().isBefore(now) || rate.getEffectiveFrom().equals(now))
                .filter(rate -> rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(now))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tìm ShiftOtRate đang active cho nhân viên
     * OtType luôn là ALL_OT và dayType = null (áp dụng cho tất cả ngày)
     */
    private ShiftOtRate findActiveShiftOtRate(Employee employee, Instant now) {
        return employee.getShiftOtRates().stream()
                .filter(rate -> rate.getIsActive())
                .filter(rate -> rate.getEffectiveFrom().isBefore(now) || rate.getEffectiveFrom().equals(now))
                .filter(rate -> rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(now))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cập nhật bảng lương
     */
    @Transactional
    public Payroll updatePayroll(Long id, Payroll payroll) {
        Payroll existingPayroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng lương với ID: " + id));

        // Cập nhật các trường có thể sửa
        if (payroll.getTotalHour() != null) {
            existingPayroll.setTotalHour(payroll.getTotalHour());
        }
        if (payroll.getTotalOtHour() != null) {
            existingPayroll.setTotalOtHour(payroll.getTotalOtHour());
        }
        if (payroll.getBaseSalary() != null) {
            existingPayroll.setBaseSalary(payroll.getBaseSalary());
        }
        if (payroll.getShiftSalary() != null) {
            existingPayroll.setShiftSalary(payroll.getShiftSalary());
        }
        if (payroll.getOtSalary() != null) {
            existingPayroll.setOtSalary(payroll.getOtSalary());
        }
        if (payroll.getPenaltyTotal() != null) {
            existingPayroll.setPenaltyTotal(payroll.getPenaltyTotal());
        }
        if (payroll.getFinalSalary() != null) {
            existingPayroll.setFinalSalary(payroll.getFinalSalary());
        }
        if (payroll.getStatus() != null) {
            existingPayroll.setStatus(payroll.getStatus());
        }

        return payrollRepository.save(existingPayroll);
    }

    /**
     * Xóa bảng lương
     */
    @Transactional
    public void deleteById(Long id) {
        payrollRepository.deleteById(id);
    }
}
