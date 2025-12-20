package com.se347.nhom4.HRApplication.controller;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqShiftDTO;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResShiftDTO;
import com.se347.nhom4.HRApplication.service.ShiftService;
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
    public ResponseEntity<ResShiftDTO> createShift(@Valid @RequestBody ReqShiftDTO dto) {
        return ResponseEntity.ok(shiftService.createShift(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResShiftDTO> patchShift(
            @PathVariable Long id,
            @RequestBody ReqShiftDTO dto) {
        return ResponseEntity.ok(shiftService.updateShift(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateShift(@PathVariable Long id) {
        shiftService.deactivateShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ResShiftDTO>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ResShiftDTO>> getActiveShifts() {
        return ResponseEntity.ok(shiftService.getActiveShifts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResShiftDTO> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResShiftDTO>> searchByName(@RequestParam("name") String name) {
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
