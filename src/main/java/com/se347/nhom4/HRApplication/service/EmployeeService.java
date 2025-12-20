package com.se347.nhom4.HRApplication.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all employees from database.
     * 
     * @return List of all employees.
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
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
     * @param employee the employee object to create.
     * @return The created employee.
     */
    public Employee createEmployee(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    /**
     * Create a new employee.
     * 
     * @param employee the employee DTO object to create.
     * @return The created employee.
     */
    public Employee createEmployee(ReqCreateEmpDTO dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword())); // hash pwd trước khi mapping sang Entity
        Employee employee = new Employee(dto);

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
    public Employee updateEmployee(Long id, Employee employee) {
        Employee curEmployee = employeeRepository.findById(id).orElseThrow();
        if (curEmployee == null) {
            throw new IllegalArgumentException("Employee not found with id " + id);
        }
        if (employee.getFullname() != null)
            curEmployee.setFullname(employee.getFullname());
        if (employee.getEmail() != null)
            curEmployee.setEmail(employee.getEmail());
        if (employee.getPhone() != null)
            curEmployee.setPhone(employee.getPhone());
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
}
