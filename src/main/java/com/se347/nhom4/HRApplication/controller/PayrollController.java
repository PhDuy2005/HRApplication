package com.se347.nhom4.HRApplication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.table.Payroll;
import com.se347.nhom4.HRApplication.service.PayrollService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/salaries")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    /**
     * Lấy danh sách tất cả bảng lương
     * GET /api/v1/salaries
     */
    @GetMapping
    @ApiMessage("Lấy danh sách bảng lương")
    public ResponseEntity<List<Payroll>> getAllPayrolls() {
        return ResponseEntity.ok(payrollService.findAll());
    }

    /**
     * Lấy chi tiết bảng lương theo ID
     * GET /api/v1/salaries/{id}
     */
    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết bảng lương")
    public ResponseEntity<Payroll> getPayrollById(@PathVariable Long id) {
        return payrollService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tính lương cho nhân viên theo tháng/năm
     * POST /api/v1/salaries/calculate?employeeId=1&month=12&year=2025
     */
    @PostMapping("/calculate")
    @ApiMessage("Tính lương cho nhân viên")
    public ResponseEntity<Payroll> calculateSalary(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("month") Integer month,
            @RequestParam("year") Integer year) {
        Payroll payroll = payrollService.calculateSalary(employeeId, month, year);
        return ResponseEntity.ok(payroll);
    }

    /**
     * Cập nhật bảng lương
     * PUT /api/v1/salaries/{id}
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật bảng lương")
    public ResponseEntity<Payroll> updatePayroll(
            @PathVariable Long id,
            @RequestBody Payroll payroll) {
        Payroll updated = payrollService.updatePayroll(id, payroll);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa bảng lương
     * DELETE /api/v1/salaries/{id}
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa bảng lương")
    public ResponseEntity<Void> deletePayroll(@PathVariable Long id) {
        payrollService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
