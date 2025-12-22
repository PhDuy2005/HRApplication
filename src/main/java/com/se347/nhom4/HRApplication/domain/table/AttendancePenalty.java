package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;

import com.se347.nhom4.HRApplication.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho penalty được áp dụng cho attendance cụ thể.
 * 
 * Được sử dụng để ghi nhận các khoản phạt áp dụng cho nhân viên
 * dựa trên attendance record (đi muộn, về sớm, vắng mặt, v.v.)
 * 
 * Quan hệ:
 * - ManyToOne với Employee: Nhiều attendance penalty thuộc về 1 employee
 * - ManyToOne với PenaltyType: Nhiều attendance penalty có cùng 1 penalty type
 */
@Entity
@Table(name = "attendance_penalties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nhân viên bị áp dụng penalty.
     * FK: attendance_penalties.employee_id -> employees.id
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Loại penalty được áp dụng.
     * FK: attendance_penalties.penalty_type_id -> penalty_types.id
     */
    @ManyToOne
    @JoinColumn(name = "penalty_type_id", nullable = false)
    private PenaltyType penaltyType;

    /**
     * Số tiền penalty cụ thể được áp dụng.
     * Có thể khác với rate trong PenaltyType tùy theo tình huống.
     */
    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * Ghi chú về penalty (lý do, tình huống cụ thể).
     * Ví dụ: "Đi muộn 30 phút ngày 15/12", "Vắng mặt không phép"
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * Ngày áp dụng penalty.
     * Thường là ngày có attendance vi phạm.
     */
    @Column(name = "penalty_date", nullable = false)
    private Instant penaltyDate;

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
        if (penaltyDate == null) {
            penaltyDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}