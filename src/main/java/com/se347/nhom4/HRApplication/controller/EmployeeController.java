package com.se347.nhom4.HRApplication.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResEmployeeInfo;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.service.EmployeeService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ApiMessage("Tạo mới nhân viên")
    public ResponseEntity<ResEmployeeInfo> createEmployee(@RequestBody Employee employee) {
        System.out.println(">>>EMPLOYEE MODULE: Creating employee: " + employee.getEmail());
        Employee createdEmployee = employeeService.createEmployee(employee);
        System.out.println(">>>EMPLOYEE MODULE: Created employee with ID: " + createdEmployee.getId());
        System.out.println(">>>EMPLOYEE MODULE: Created employee with fullname: " + createdEmployee.getFullname());
        System.out.println(">>>EMPLOYEE MODULE: Created employee with email: " + createdEmployee.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResEmployeeInfo(createdEmployee));
    }

    @PostMapping
    @ApiMessage("Tạo mới nhân viên")
    public ResponseEntity<ResEmployeeInfo> createEmployeeWithDTO(@RequestBody ReqCreateEmpDTO employee) {
        System.out.println(">>>EMPLOYEE MODULE: Creating employee: " + employee.getEmail());
        Employee createdEmployee = employeeService.createEmployee(employee);
        System.out.println(">>>EMPLOYEE MODULE: Created employee with ID: " + createdEmployee.getId());
        System.out.println(">>>EMPLOYEE MODULE: Created employee with fullname: " + createdEmployee.getFullname());
        System.out.println(">>>EMPLOYEE MODULE: Created employee with email: " + createdEmployee.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResEmployeeInfo(createdEmployee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
