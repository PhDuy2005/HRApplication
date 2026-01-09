package com.se347.nhom4.HRApplication.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.EmployeeSalaryType;
import com.se347.nhom4.HRApplication.domain.table.MonthlySalary;
import com.se347.nhom4.HRApplication.domain.table.ShiftBaseRate;
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
        EmployeeSalaryType empSalaryType = new EmployeeSalaryType(dto.getEmpSalaryType());
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
                shiftRate.setRateMultiplier(rateDTO.getRateMultiplier());
                shiftRate.setEffectiveFrom(
                        rateDTO.getEffectiveFrom() != null ? rateDTO.getEffectiveFrom() : Instant.now());
                shiftRate.setEffectiveTo(rateDTO.getEffectiveTo());
                shiftRate.setIsActive(rateDTO.getIsActive() != null ? rateDTO.getIsActive() : true);

                empShiftRates.add(shiftRate);
            }
        }
        newEmp.setShiftRates(empShiftRates);

        // Monthly salary không được xử lý - set null
        newEmp.setMonthlySalaries(null);

        // TODO: Review - Xử lý OT Rates khi tạo nhân viên mới
        // Tạo OT Rates cho nhân viên
        // List<ShiftOtRate> empOtRates = new ArrayList<>();
        // if (dto.getEmpOtRates() != null && !dto.getEmpOtRates().isEmpty()) {
        // for (ReqCreateEmpDTO.CreateEmpOtRate otRateDTO : dto.getEmpOtRates()) {
        // ShiftOtRate otRate = ShiftOtRate.builder()
        // .employee(newEmp)
        // .otType(otRateDTO.getOtType())
        // .dayType(otRateDTO.getDayType())
        // .rateMultiplier(otRateDTO.getRateMultiplier())
        // .isActive(otRateDTO.getIsActive() != null ? otRateDTO.getIsActive() : true)
        // .effectiveFrom(otRateDTO.getEffectiveFrom() != null ?
        // otRateDTO.getEffectiveFrom() : Instant.now())
        // .effectiveTo(otRateDTO.getEffectiveTo())
        // .build();
        // empOtRates.add(otRate);
        // }
        // }
        // newEmp.setShiftOtRates(empOtRates);

        // Khởi tạo các list còn lại để tránh null pointer
        newEmp.setShiftOtRates(new ArrayList<>());
        newEmp.setEmployeePenalties(new ArrayList<>());
        newEmp.setAttendancePenalties(new ArrayList<>());
        return newEmp;
    }
}
