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
    public ResponseEntity<List<ResEmployeeInfo>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.findAll().stream()
                        .map(ResEmployeeInfo::new)
                        .toList());
    }

    /**
     * Lấy danh sách tất cả nhân viên đang hoạt động (status = ACTIVE).
     *
     * @return ResponseEntity chứa danh sách nhân viên active
     *
     *         <h3>Ví dụ sử dụng API:</h3>
     * 
     *         <pre>
     *         GET / api / v1 / employees / active
     *         </pre>
     */
    @GetMapping("/active")
    @ApiMessage("Lấy danh sách nhân viên đang hoạt động")
    public ResponseEntity<List<ResEmployeeInfo>> getActiveEmployees() {
        System.out.println(">>>EMPLOYEE MODULE: Fetching all active employees");
        List<ResEmployeeInfo> activeEmployees = employeeService.findAllActive().stream()
                .map(ResEmployeeInfo::new)
                .toList();
        System.out.println(">>>EMPLOYEE MODULE: Successfully fetched " + activeEmployees.size() + " active employees");
        return ResponseEntity.ok(activeEmployees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResEmployeeInfo> getEmployeeById(@PathVariable("id") Long id) {
        return employeeService.findById(id)
                .map(e -> ResponseEntity.ok(new ResEmployeeInfo(e)))
                .orElse(ResponseEntity.notFound().build());
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

    @PutMapping("/{id}/basic-info")
    public ResponseEntity<ResEmployeeInfo> updateEmployee(@PathVariable("id") Long id, @RequestBody Employee employee) {
        System.out.println(">>>EMPLOYEE MODULE: Updating employee ID: " + id);
        ResEmployeeInfo updatedEmployee = new ResEmployeeInfo(employeeService.updateEmployeeBasicInfo(id, employee));
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
