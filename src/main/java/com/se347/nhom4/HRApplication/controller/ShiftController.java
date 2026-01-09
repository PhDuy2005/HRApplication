package com.se347.nhom4.HRApplication.controller;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqShiftDTO;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResShiftDTO;
import com.se347.nhom4.HRApplication.service.ShiftService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @ApiMessage("Tạo ca làm việc mới")
    public ResponseEntity<ResShiftDTO> createShift(@Valid @RequestBody ReqShiftDTO dto) {
        System.out.println(">>>SHIFT MODULE: Creating new shift with name: " + dto.getName());
        return ResponseEntity.ok(shiftService.createShift(dto));
    }

    /**
     * Kích hoạt lại ca làm việc đã bị vô hiệu hóa.
     * Đặt lại trạng thái isActive = true cho ca làm việc.
     * 
     * @param id ID của ca làm việc cần kích hoạt
     * @return ResponseEntity chứa thông tin ca làm việc đã được kích hoạt
     * @throws IllegalArgumentException nếu không tìm thấy ca làm việc với ID đã cho
     */
    @PostMapping("/activate/{id}")
    @ApiMessage("Kích hoạt ca làm việc")
    public ResponseEntity<ResShiftDTO> activateShift(@PathVariable("id") Long id) {
        System.out.println(">>>SHIFT MODULE: Activating shift with id: " + id);
        return ResponseEntity.ok(shiftService.activateShift(id));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật thông tin ca làm việc")
    public ResponseEntity<ResShiftDTO> updateShift(
            @PathVariable("id") Long id,
            @RequestBody ReqShiftDTO dto) {
        System.out.println(">>>SHIFT MODULE: Updating shift with id: " + id);
        return ResponseEntity.ok(shiftService.updateShift(id, dto));
    }

    /**
     * Vô hiệu hóa ca làm việc (soft delete).
     * Đặt trạng thái isActive = false cho ca làm việc.
     * Kiểm tra xem có còn WorkSchedule nào có ngày làm việc trong tương lai (> hôm
     * nay)
     * đang tham chiếu đến ca này hay không. Nếu có, không cho phép vô hiệu hóa.
     * 
     * @param id ID của ca làm việc cần vô hiệu hóa
     * @return ResponseEntity chứa thông tin ca làm việc đã được vô hiệu hóa
     * @throws IllegalArgumentException nếu không tìm thấy ca làm việc với ID đã cho
     * @throws IllegalStateException    nếu còn lịch làm việc tương lai đang sử dụng
     *                                  ca này
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Vô hiệu hóa ca làm việc")
    public ResponseEntity<ResShiftDTO> deactivateShift(@PathVariable("id") Long id) {
        System.out.println(">>>SHIFT MODULE: Deactivating shift with id: " + id);
        return ResponseEntity.ok(shiftService.deactivateShift(id));
    }

    @GetMapping
    @ApiMessage("Lấy danh sách tất cả ca làm việc")
    public ResponseEntity<List<ResShiftDTO>> getAllShifts() {
        System.out.println(">>>SHIFT MODULE: Fetching all shifts");
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/active")
    @ApiMessage("Lấy danh sách ca làm việc đang hoạt động")
    public ResponseEntity<List<ResShiftDTO>> getActiveShifts() {
        System.out.println(">>>SHIFT MODULE: Fetching active shifts");
        return ResponseEntity.ok(shiftService.getActiveShifts());
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết ca làm việc theo ID")
    public ResponseEntity<ResShiftDTO> getShiftById(@PathVariable Long id) {
        System.out.println(">>>SHIFT MODULE: Fetching shift details for id: " + id);
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @GetMapping("/search")
    @ApiMessage("Tìm kiếm ca làm việc theo tên")
    public ResponseEntity<List<ResShiftDTO>> searchByName(@RequestParam("name") String name) {
        System.out.println(">>>SHIFT MODULE: Searching shifts by name: " + name);
        return ResponseEntity.ok(shiftService.searchByName(name));
    }

    // @GetMapping("/by-time")
    // public ResponseEntity<List<ResShiftDTO>> getShiftsByTime(@RequestParam int
    // hour,
    // @RequestParam int minute) {
    // LocalTime time = LocalTime.of(hour, minute);
    // return ResponseEntity.ok(shiftService.getShiftsByTime(time));
    // }
}
