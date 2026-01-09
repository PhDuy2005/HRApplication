package com.se347.nhom4.HRApplication.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqChangePasswordDTO;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqResetPasswordDTO;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.EmployeeSalaryType;
import com.se347.nhom4.HRApplication.domain.table.MonthlySalary;
import com.se347.nhom4.HRApplication.domain.table.ShiftBaseRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftOtRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftSpecialRate;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.ShiftRepository;
import com.se347.nhom4.HRApplication.util.enums.SalaryTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShiftRepository shiftRepository;
    private final RoleService roleService;

    private void checkUniquePhoneAndEmailForUpdate(Long id, String phone, String email) {
        if (phone != null && this.employeeRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new IllegalArgumentException("Phone number " + phone + " already exists");
        }
        if (email != null && this.employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email " + email + " already exists");
        }
    }

    /**
     * Get all employees from database.
     * 
     * @return List of all employees.
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * Get all active employees from database.
     * 
     * @return List of all active employees.
     */
    public List<Employee> findAllActive() {
        return employeeRepository.findByStatus(StatusEnum.ACTIVE);
    }

    /**
     * Get all inactive employees from database.
     * 
     * @return List of all inactive employees.
     */
    public List<Employee> findAllInactive() {
        return employeeRepository.findByStatus(StatusEnum.INACTIVE);
    }

    /**
     * Find employee by ID.
     * 
     * @param id the employee ID to search for.
     * @return Optional containing the employee if found.
     */
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Create a new employee.
     * 
     * @param employee the employee DTO object to create.
     * @return The created employee.
     */
    public Employee createEmployee(ReqCreateEmpDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            // set default:
            dto.setPassword(dto.getPhone());
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.checkUniquePhoneAndEmail(dto.getPhone(), dto.getEmail());
        Employee employee = this.toEmployeeEntity(dto);
        return employeeRepository.save(employee);
    }

    /**
     * Update employee using same DTO as create.
     * 
     * @param id  the employee ID to update.
     * @param dto the employee DTO with updated information.
     * @return The updated employee.
     */
    public Employee updateEmployee(Long id, ReqCreateEmpDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên với ID: " + id));

        // Bỏ qua password (không update)
        dto.setPassword(null);

        // Kiểm tra unique phone và email (nếu có thay đổi)
        if (dto.getPhone() != null && !dto.getPhone().equals(employee.getPhone())) {
            checkUniquePhoneAndEmailForUpdate(id, dto.getPhone(), null);
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(employee.getEmail())) {
            checkUniquePhoneAndEmailForUpdate(id, null, dto.getEmail());
        }

        // Update basic info
        if (dto.getFullname() != null)
            employee.setFullname(dto.getFullname());
        if (dto.getEmail() != null)
            employee.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            employee.setPhone(dto.getPhone());
        if (dto.getHiredDate() != null)
            employee.setHiredDate(dto.getHiredDate());
        if (dto.getStatus() != null)
            employee.setStatus(dto.getStatus());
        if (dto.getAllowance() != null)
            employee.setAllowance(dto.getAllowance());

        Instant now = Instant.now();

        // Update Shift Rates
        if (dto.getEmpShiftRates() != null && !dto.getEmpShiftRates().isEmpty()) {
            for (ReqCreateEmpDTO.CreateEmpShiftRate rateDTO : dto.getEmpShiftRates()) {
                // Bỏ qua nếu baseRate = 0
                if (rateDTO.getBaseRate() == null || rateDTO.getBaseRate() == 0) {
                    continue;
                }

                // Tìm shift rate cũ cùng dayType và deactivate
                employee.getShiftRates().stream()
                        .filter(rate -> rate.getDayType() == rateDTO.getDayType())
                        .filter(rate -> rate.getIsActive())
                        .filter(rate -> {
                            // Kiểm tra shiftId nếu là special rate
                            if (rateDTO.getShiftId() != null && rate instanceof ShiftSpecialRate) {
                                return ((ShiftSpecialRate) rate).getShift() != null &&
                                        ((ShiftSpecialRate) rate).getShift().getId().equals(rateDTO.getShiftId());
                            }
                            // Base rate: chỉ check nếu không phải special rate
                            return rateDTO.getShiftId() == null && !(rate instanceof ShiftSpecialRate);
                        })
                        .forEach(rate -> {
                            rate.setIsActive(false);
                            rate.setEffectiveTo(now);
                        });

                // Tạo shift rate mới
                ShiftRate newRate;
                if (rateDTO.getShiftId() == null) {
                    newRate = new ShiftBaseRate();
                } else {
                    ShiftSpecialRate specialRate = new ShiftSpecialRate();
                    specialRate.setShift(shiftRepository.findById(rateDTO.getShiftId()).orElse(null));
                    specialRate.setPriority(rateDTO.getPriority());
                    specialRate.setNote(rateDTO.getNote());
                    newRate = specialRate;
                }
                newRate.setEmployee(employee);
                newRate.setDayType(rateDTO.getDayType());
                newRate.setBaseRate(rateDTO.getBaseRate());
                newRate.setRateMultiplier(
                        rateDTO.getRateMultiplier() != null ? rateDTO.getRateMultiplier() : BigDecimal.ONE);
                newRate.setEffectiveFrom(
                        rateDTO.getEffectiveFrom() != null
                                ? rateDTO.getEffectiveFrom().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                                : now);
                newRate.setEffectiveTo(
                        rateDTO.getEffectiveTo() != null
                                ? rateDTO.getEffectiveTo().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                                : null);
                newRate.setIsActive(true);

                employee.getShiftRates().add(newRate);
            }
        }

        // Update OT Rates
        if (dto.getEmpOtRates() != null && !dto.getEmpOtRates().isEmpty()) {
            for (ReqCreateEmpDTO.CreateEmpOtRate otRateDTO : dto.getEmpOtRates()) {
                // Nếu rateMultiplier = 0 -> set về 1.0
                BigDecimal multiplier = otRateDTO.getRateMultiplier();
                if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) == 0) {
                    multiplier = BigDecimal.ONE;
                }

                // Deactivate tất cả OT rates cũ
                employee.getShiftOtRates().stream()
                        .filter(rate -> rate.getIsActive())
                        .forEach(rate -> {
                            rate.setIsActive(false);
                            rate.setEffectiveTo(now);
                        });

                // Tạo OT rate mới
                ShiftOtRate newOtRate = ShiftOtRate.builder()
                        .employee(employee)
                        .otType(com.se347.nhom4.HRApplication.util.enums.OtTypeEnum.ALL_OT)
                        .dayType(null)
                        .rateMultiplier(multiplier)
                        .isActive(true)
                        .effectiveFrom(
                                otRateDTO.getEffectiveFrom() != null
                                        ? otRateDTO.getEffectiveFrom().atStartOfDay(java.time.ZoneId.systemDefault())
                                                .toInstant()
                                        : now)
                        .effectiveTo(
                                otRateDTO.getEffectiveTo() != null
                                        ? otRateDTO.getEffectiveTo().atStartOfDay(java.time.ZoneId.systemDefault())
                                                .toInstant()
                                        : null)
                        .build();

                employee.getShiftOtRates().add(newOtRate);
            }
        }

        return employeeRepository.save(employee);
    }

    /**
     * Update employee information.
     * 
     * @param id       the employee ID to update.
     * @param employee the employee object with updated information.
     * @return The updated employee.
     * @throws NoSuchElementException if employee not found.
     */
    public Employee updateEmployeeBasicInfo(Long id, Employee employee) {
        Employee curEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found with id " + id));

        checkUniquePhoneAndEmailForUpdate(id, employee.getPhone(), employee.getEmail());

        if (employee.getFullname() != null)
            curEmployee.setFullname(employee.getFullname());
        if (employee.getEmail() != null)
            curEmployee.setEmail(employee.getEmail());
        if (employee.getPhone() != null)
            curEmployee.setPhone(employee.getPhone());
        if (employee.getHiredDate() != null)
            curEmployee.setHiredDate(employee.getHiredDate());
        if (employee.getStatus() != null)
            curEmployee.setStatus(employee.getStatus());

        return employeeRepository.save(curEmployee);
    }

    /**
     * Delete employee by ID.
     * 
     * @param id the employee ID to delete.
     * @throws NoSuchElementException if employee not found.
     */
    public void deleteById(Long id) {
        Employee curEmployee = employeeRepository.findById(id).orElseThrow();
        if (curEmployee == null) {
            throw new IllegalArgumentException("Employee not found with id " + id);
        }
        employeeRepository.deleteById(id);
    }

    /**
     * Find employee by username (email).
     * 
     * @param username the email to search for.
     * @return Employee if found, null otherwise.
     */
    public Employee handleFindByUsername(String username) {
        return employeeRepository.findByEmail(username).orElse(null);
    }

    /**
     * Update user's refresh token.
     * 
     * @param token the new refresh token.
     * @param email the user's email.
     */
    public void updateUserRefreshToken(String token, String email) {
        Employee user = handleFindByUsername(email);
        if (user != null) {
            user.setRefreshToken(token);
            employeeRepository.save(user);
        }
    }

    /**
     * Find employee by email and refresh token.
     * 
     * @param email        the user's email.
     * @param refreshToken the refresh token to match.
     * @return Employee if found with matching token, null otherwise.
     */
    public Employee handleFindByEmailAndRefreshToken(String email, String refreshToken) {
        return this.employeeRepository.findByEmailAndRefreshToken(email, refreshToken);
    }

    /**
     * Handle logout user by clearing their refresh token.
     * 
     * @throws NoSuchElementException if the user with the given email does not
     *                                exist.
     * @param email the email of the user to log out.
     * @return If successful, returns nothing.
     */
    public void handleLogOutUser(@NotNull String email) {
        Employee user = handleFindByUsername(email);
        if (user == null) {
            throw new NoSuchElementException("User with email " + email + " does not exist");
        }
        user.setRefreshToken(null);
    }

    /**
     * Check if the phone and email are unique.
     * 
     * @throws IllegalArgumentException if the phone or email given is already
     *                                  existed.
     * @param email the email of the user to check.
     * @param phone the phone number of the user to check.
     * @return If both phone and email are unique, return nothing.
     */
    private void checkUniquePhoneAndEmail(String phone, String email) {

        if (this.employeeRepository.existsByPhone(phone)) {
            System.err.println(">>>EMPLOYEE MODULE: Phone number " + phone
                    + " already exists. An IllegalArgumentException will be thrown.");
            throw new IllegalArgumentException("Phone number " + phone + " already exists");
        }

        if (this.employeeRepository.existsByEmail(email)) {
            System.err.println(">>>EMPLOYEE MODULE: Email " + email
                    + " already exists. An IllegalArgumentException will be thrown.");
            throw new IllegalArgumentException("Email " + email + " already exists");
            // return false; // Email already exists
        }
        // return true; // Both phone and email are unique
    }

    /**
     * Handle logout user by clearing their refresh token.
     * 
     * @param dto the ReqCreateEmpDTO contains employee creation data.
     * @return Return Employee entity mapped from the DTO.
     */
    public Employee toEmployeeEntity(ReqCreateEmpDTO dto) {
        System.out.println(">>>CREATE EMPLOYEE MODULE: Trying to Map ReqCreateEmpDTO to Employee entity");
        Employee newEmp = new Employee();
        newEmp.setFullname(dto.getFullname());
        newEmp.setEmail(dto.getEmail());
        newEmp.setPassword(dto.getPassword());
        newEmp.setPhone(dto.getPhone());
        newEmp.setHiredDate(dto.getHiredDate() != null ? dto.getHiredDate() : LocalDate.now());
        newEmp.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusEnum.ACTIVE);
        newEmp.setAllowance(dto.getAllowance());

        // Tự động gán role dựa trên email
        if ("admin@gmail.com".equals(dto.getEmail())) {
            // Admin account - gán role ADMIN
            roleService.findByName("ADMIN").ifPresent(newEmp::setRole);
            System.out.println(">>>CREATE EMPLOYEE MODULE: Gán role ADMIN cho email: " + dto.getEmail());
        } else {
            // Các account khác - gán role EMPLOYEE
            roleService.findByName("EMPLOYEE").ifPresent(newEmp::setRole);
            System.out.println(">>>CREATE EMPLOYEE MODULE: Gán role EMPLOYEE cho email: " + dto.getEmail());
        }

        // Khởi tạo EmployeeSalaryTypes
        newEmp.setEmployeeSalaryTypes(new ArrayList<>());

        // Kiểm tra empSalaryType có null không, nếu null thì tạo default SHIFT
        ReqCreateEmpDTO.CreateEmpSalaryType salaryTypeDTO = dto.getEmpSalaryType();
        if (salaryTypeDTO == null) {
            salaryTypeDTO = ReqCreateEmpDTO.CreateEmpSalaryType.builder()
                    .salaryType(SalaryTypeEnum.SHIFT)
                    .effectiveFrom(LocalDate.now())
                    .note("Default salary type")
                    .build();
            System.out.println(">>>CREATE EMPLOYEE MODULE: empSalaryType is null, using default SHIFT type");
        }

        EmployeeSalaryType empSalaryType = new EmployeeSalaryType(salaryTypeDTO);
        System.out.println(">>>CREATE EMPLOYEE MODULE: Type of salary: " +
                empSalaryType.getSalaryType());
        empSalaryType.setEmployee(newEmp);
        newEmp.getEmployeeSalaryTypes().add(empSalaryType);

        // Xử lý lương theo ca (chỉ hỗ trợ SHIFT type)
        // Tạo ShiftRates cho lương theo ca
        List<ShiftRate> empShiftRates = new ArrayList<>();
        if (dto.getEmpShiftRates() != null && !dto.getEmpShiftRates().isEmpty()) {
            for (ReqCreateEmpDTO.CreateEmpShiftRate rateDTO : dto.getEmpShiftRates()) {
                ShiftRate shiftRate;
                if (rateDTO.getShiftId() == null) {
                    // Tạo ShiftBaseRate
                    shiftRate = new ShiftBaseRate();
                } else {
                    // Tạo ShiftSpecialRate
                    ShiftSpecialRate specialRate = new ShiftSpecialRate();
                    specialRate.setShift(this.shiftRepository.findById(rateDTO.getShiftId()).orElse(null));
                    specialRate.setPriority(rateDTO.getPriority());
                    specialRate.setNote(rateDTO.getNote());
                    shiftRate = specialRate;
                }
                shiftRate.setEmployee(newEmp);
                shiftRate.setDayType(rateDTO.getDayType());
                shiftRate.setBaseRate(rateDTO.getBaseRate());
                shiftRate.setRateMultiplier(
                        rateDTO.getRateMultiplier() != null
                                ? rateDTO.getRateMultiplier()
                                : BigDecimal.ONE); // Default = 1.0 nếu không có
                shiftRate.setEffectiveFrom(
                        rateDTO.getEffectiveFrom() != null
                                ? rateDTO.getEffectiveFrom().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                                : Instant.now());
                shiftRate.setEffectiveTo(
                        rateDTO.getEffectiveTo() != null
                                ? rateDTO.getEffectiveTo().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                                : null);
                shiftRate.setIsActive(rateDTO.getIsActive() != null ? rateDTO.getIsActive() : true);

                empShiftRates.add(shiftRate);
            }
        }
        newEmp.setShiftRates(empShiftRates);

        // Monthly salary không được xử lý - set null
        newEmp.setMonthlySalaries(null);

        // Xử lý OT Rates khi tạo nhân viên mới
        List<ShiftOtRate> empOtRates = new ArrayList<>();
        if (dto.getEmpOtRates() != null && !dto.getEmpOtRates().isEmpty()) {
            for (ReqCreateEmpDTO.CreateEmpOtRate otRateDTO : dto.getEmpOtRates()) {
                ShiftOtRate otRate = ShiftOtRate.builder()
                        .employee(newEmp)
                        .otType(com.se347.nhom4.HRApplication.util.enums.OtTypeEnum.ALL_OT)
                        .dayType(null)
                        .rateMultiplier(otRateDTO.getRateMultiplier())
                        .isActive(otRateDTO.getIsActive() != null ? otRateDTO.getIsActive() : true)
                        .effectiveFrom(
                                otRateDTO.getEffectiveFrom() != null
                                        ? otRateDTO.getEffectiveFrom().atStartOfDay(java.time.ZoneId.systemDefault())
                                                .toInstant()
                                        : Instant.now())
                        .effectiveTo(
                                otRateDTO.getEffectiveTo() != null
                                        ? otRateDTO.getEffectiveTo().atStartOfDay(java.time.ZoneId.systemDefault())
                                                .toInstant()
                                        : null)
                        .build();
                empOtRates.add(otRate);
            }
        }
        newEmp.setShiftOtRates(empOtRates);

        // Khởi tạo các list còn lại để tránh null pointer
        newEmp.setEmployeePenalties(new ArrayList<>());
        newEmp.setAttendancePenalties(new ArrayList<>());
        return newEmp;
    }

    /**
     * Đổi mật khẩu cho nhân viên (tự đổi - yêu cầu mật khẩu cũ)
     * 
     * @param employeeId ID của nhân viên
     * @param dto        DTO chứa mật khẩu cũ và mật khẩu mới
     * @return Employee đã được cập nhật mật khẩu
     */
    public Employee changePassword(Long employeeId, ReqChangePasswordDTO dto) {
        // Kiểm tra mật khẩu mới và xác nhận có khớp không
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Tìm nhân viên
        Employee employee = this.employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(dto.getOldPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        // Mã hóa và cập nhật mật khẩu mới
        employee.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        return this.employeeRepository.save(employee);
    }

    /**
     * Reset mật khẩu cho nhân viên (admin reset - không yêu cầu mật khẩu cũ)
     * 
     * @param dto DTO chứa ID nhân viên và mật khẩu mới
     * @return Employee đã được reset mật khẩu
     */
    public Employee resetPassword(ReqResetPasswordDTO dto) {
        // Tìm nhân viên
        Employee employee = this.employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Không tìm thấy nhân viên với ID: " + dto.getEmployeeId()));

        // Mã hóa và cập nhật mật khẩu mới
        employee.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        return this.employeeRepository.save(employee);
    }
}
