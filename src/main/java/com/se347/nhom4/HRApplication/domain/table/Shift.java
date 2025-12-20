package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.time.LocalTime;

import com.se347.nhom4.HRApplication.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho ca làm việc (Shift).
 * 
 * Định nghĩa các ca làm việc trong hệ thống như:
 * - Ca sáng: 7h - 12h
 * - Ca chiều: 13h - 18h
 * - Ca tối: 18h - 22h
 * - Ca đêm: 22h - 6h sáng hôm sau
 * 
 * Được sử dụng bởi:
 * - WorkSchedule: Phân ca cho nhân viên
 * - ShiftSpecialRate: Định nghĩa lương riêng cho ca
 * - Attendance: Ghi nhận giờ vào/ra theo ca
 */
@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên ca làm việc.
     * Ví dụ: "Ca sáng", "Ca đêm", "Ca tết"
     */
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Mô tả chi tiết về ca làm việc.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Giờ bắt đầu ca.
     * Ví dụ: 07:00:00 (7h sáng)
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Giờ kết thúc ca.
     * Ví dụ: 15:00:00 (3h chiều)
     * 
     * Note: Nếu endTime < startTime, nghĩa là ca qua đêm
     * Ví dụ: 22:00 - 06:00 (10h tối đến 6h sáng hôm sau)
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Số giờ làm việc chuẩn của ca này.
     * Dùng để tính overtime.
     * Ví dụ: Ca 8 tiếng, làm 10 tiếng → 2 tiếng OT
     */
    @Column(name = "standard_hours", nullable = false)
    private Double standardHours;
    // = endTime.isAfter(startTime)
    // ? (double) java.time.Duration.between(startTime, endTime).toHours()
    // : (double) java.time.Duration.between(startTime,
    // endTime.plusHours(24)).toHours();

    /**
     * Ca này có active không.
     * Cho phép disable ca mà không xóa.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Mã màu để hiển thị trên UI (hex color).
     * Ví dụ: "#FF5733" (màu cam)
     * Kduii: wtf có cần ko nhỉ
     */
    @Column(name = "color_code", length = 7)
    private String colorCode;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        if (isActive == null) {
            isActive = true;
        }
        if (startTime != null && endTime != null) {

            standardHours = endTime.isAfter(startTime)
                    ? (double) java.time.Duration.between(startTime, endTime).toHours()
                    : (double) java.time.Duration.between(startTime, endTime.plusHours(24)).toHours();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    /**
     * Kiểm tra ca này có qua đêm không.
     * 
     * @return true nếu endTime < startTime
     */
    public boolean isOvernightShift() {
        return endTime.isBefore(startTime);
    }
}
