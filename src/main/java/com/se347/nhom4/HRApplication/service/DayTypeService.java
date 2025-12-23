package com.se347.nhom4.HRApplication.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DayTypeService {

    // TODO: Cần tích hợp với database để lưu danh sách ngày lễ
    private static final Set<LocalDate> HOLIDAYS = new HashSet<>();

    static {
        // Thêm các ngày lễ cố định cho năm 2025
        // Tết Dương lịch
        HOLIDAYS.add(LocalDate.of(2025, 1, 1));

        // Tết Nguyên Đán (Âm lịch - cần tính toán hoặc cập nhật hàng năm)
        HOLIDAYS.add(LocalDate.of(2025, 1, 28)); // Mùng 1 Tết
        HOLIDAYS.add(LocalDate.of(2025, 1, 29)); // Mùng 2 Tết
        HOLIDAYS.add(LocalDate.of(2025, 1, 30)); // Mùng 3 Tết
        HOLIDAYS.add(LocalDate.of(2025, 1, 31)); // Mùng 4 Tết
        HOLIDAYS.add(LocalDate.of(2025, 2, 1)); // Mùng 5 Tết

        // Giỗ Tổ Hùng Vương
        HOLIDAYS.add(LocalDate.of(2025, 4, 7));

        // 30/4 - 1/5
        HOLIDAYS.add(LocalDate.of(2025, 4, 30));
        HOLIDAYS.add(LocalDate.of(2025, 5, 1));

        // Quốc khánh 2/9
        HOLIDAYS.add(LocalDate.of(2025, 9, 2));
    }

    /**
     * Xác định loại ngày từ LocalDate.
     * 
     * @param date ngày cần xác định
     * @return DayTypeEnum (HOLIDAY, SUNDAY, SATURDAY, WEEKDAY)
     */
    public DayTypeEnum getDayType(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        // Kiểm tra ngày lễ
        if (isHoliday(date)) {
            return DayTypeEnum.HOLIDAY;
        }

        // Kiểm tra thứ trong tuần
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return switch (dayOfWeek) {
            case SUNDAY -> DayTypeEnum.SUNDAY;
            case SATURDAY -> DayTypeEnum.SATURDAY;
            default -> DayTypeEnum.WEEKDAY;
        };
    }

    /**
     * Xác định loại ngày từ Instant.
     * Sử dụng timezone mặc định của hệ thống.
     * 
     * @param instant thời điểm cần xác định
     * @return DayTypeEnum (HOLIDAY, SUNDAY, SATURDAY, WEEKDAY)
     */
    public DayTypeEnum getDayType(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("Instant cannot be null");
        }

        // Chuyển Instant sang LocalDate với timezone mặc định
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return getDayType(date);
    }

    /**
     * Xác định loại ngày từ Instant với timezone cụ thể.
     * 
     * @param instant thời điểm cần xác định
     * @param zoneId  timezone để chuyển đổi
     * @return DayTypeEnum (HOLIDAY, SUNDAY, SATURDAY, WEEKDAY)
     */
    public DayTypeEnum getDayType(Instant instant, ZoneId zoneId) {
        if (instant == null) {
            throw new IllegalArgumentException("Instant cannot be null");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId cannot be null");
        }

        LocalDate date = instant.atZone(zoneId).toLocalDate();
        return getDayType(date);
    }

    /**
     * Kiểm tra xem ngày có phải là ngày lễ không.
     * 
     * @param date ngày cần kiểm tra
     * @return true nếu là ngày lễ, false nếu không
     */
    public boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }

    /**
     * Thêm ngày lễ vào danh sách.
     * TODO: Nên persist vào database thay vì lưu trong memory.
     * 
     * @param date ngày lễ cần thêm
     */
    public void addHoliday(LocalDate date) {
        if (date != null) {
            HOLIDAYS.add(date);
        }
    }

    /**
     * Xóa ngày lễ khỏi danh sách.
     * 
     * @param date ngày lễ cần xóa
     */
    public void removeHoliday(LocalDate date) {
        if (date != null) {
            HOLIDAYS.remove(date);
        }
    }

    /**
     * Lấy danh sách tất cả ngày lễ.
     * 
     * @return Set chứa các ngày lễ
     */
    public Set<LocalDate> getAllHolidays() {
        return new HashSet<>(HOLIDAYS);
    }

    /**
     * Kiểm tra xem ngày có phải là cuối tuần (Saturday hoặc Sunday).
     * 
     * @param date ngày cần kiểm tra
     * @return true nếu là cuối tuần, false nếu không
     */
    public boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Kiểm tra xem ngày có phải là ngày làm việc (không phải lễ, không phải cuối
     * tuần).
     * 
     * @param date ngày cần kiểm tra
     * @return true nếu là ngày làm việc, false nếu không
     */
    public boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !isHoliday(date) && !isWeekend(date);
    }
}
