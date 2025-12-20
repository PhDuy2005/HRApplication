package com.se347.nhom4.HRApplication.service;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqShiftDTO;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResShiftDTO;
import com.se347.nhom4.HRApplication.domain.table.Shift;
import com.se347.nhom4.HRApplication.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    // ================== HELPER ==================

    // Tính StandardHours
    private double calculateStandardHours(LocalTime start, LocalTime end) {
        if (start == null || end == null)
            throw new IllegalArgumentException("startTime/endTime must not be null");

        long minutes;
        if (end.isAfter(start)) {
            minutes = Duration.between(start, end).toMinutes();
        } else {
            // qua đêm
            minutes = Duration.between(start, end.plusHours(24)).toMinutes();
        }
        return minutes / 60.0;
    }

    // Entity -> Res DTO
    private ResShiftDTO toResDTO(Shift shift) {
        return ResShiftDTO.builder()
                .id(shift.getId())
                .name(shift.getName())
                .description(shift.getDescription())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .standardHours(shift.getStandardHours())
                .isActive(shift.getIsActive())
                .colorCode(shift.getColorCode())
                .build();
    }

    // Req DTO -> Entity (dùng cho create)
    private Shift toEntityFromReq(ReqShiftDTO dto) {
        Shift shift = Shift.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .colorCode(dto.getColorCode())
                .build();

        shift.setStandardHours(
                calculateStandardHours(shift.getStartTime(), shift.getEndTime()));

        return shift;
    }

    // ================== CRUD ==================

    // Thêm ca mới
    public ResShiftDTO createShift(ReqShiftDTO dto) {
        // check trùng tên
        shiftRepository.findByName(dto.getName())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("Shift name already exists: " + s.getName());
                });

        Shift shift = toEntityFromReq(dto);
        Shift saved = shiftRepository.save(shift);
        return toResDTO(saved);
    }

    // Sửa ca
    public ResShiftDTO updateShift(Long id, ReqShiftDTO dto) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with id: " + id));

        if (dto.getName() != null) {
            shift.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            shift.setDescription(dto.getDescription());
        }
        if (dto.getStartTime() != null) {
            shift.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            shift.setEndTime(dto.getEndTime());
        }
        if (dto.getIsActive() != null) {
            shift.setIsActive(dto.getIsActive());
        }
        if (dto.getColorCode() != null) {
            shift.setColorCode(dto.getColorCode());
        }

        if (shift.getStartTime() != null && shift.getEndTime() != null) {
            shift.setStandardHours(
                    calculateStandardHours(shift.getStartTime(), shift.getEndTime()));
        }

        Shift saved = shiftRepository.save(shift);
        return toResDTO(saved);
    }

    // Xóa ca (soft delete: disable)
    public void deactivateShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with id: " + id));
        shift.setIsActive(false);
        shiftRepository.save(shift);
    }

    // Lấy tất cả ca
    public List<ResShiftDTO> getAllShifts() {
        return shiftRepository.findAll()
                .stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    // Lấy ca active
    public List<ResShiftDTO> getActiveShifts() {
        return shiftRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    // Tìm theo id
    public ResShiftDTO getShiftById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with id: " + id));
        return toResDTO(shift);
    }

    // Tìm theo tên
    public List<ResShiftDTO> searchByName(String q) {
        return shiftRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q)
                .stream().map(this::toResDTO).toList();
    }

    // Lấy các ca đang diễn ra vào thời điểm time
    // public List<ResShiftDTO> getShiftsByTime(LocalTime time) {
    // return shiftRepository.findAll()
    // .stream()
    // .filter(shift -> {
    // if (!shift.getIsActive() || shift.getIsActive() == null)
    // return false;
    // if (shift.getStartTime() == null || shift.getEndTime() == null)
    // return false;

    // boolean overnight = shift.getEndTime().isBefore(shift.getStartTime());
    // if (overnight) {
    // // ca qua đêm: 22h-06h → time >= 22h || time <= 6h
    // return !time.isBefore(shift.getStartTime()) ||
    // !time.isAfter(shift.getEndTime());
    // } else {
    // // ca bình thường
    // return !time.isBefore(shift.getStartTime()) &&
    // !time.isAfter(shift.getEndTime());
    // }
    // })
    // .map(this::toResDTO)
    // .collect(Collectors.toList());
    // }
}
