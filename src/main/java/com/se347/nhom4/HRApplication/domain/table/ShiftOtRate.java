package com.se347.nhom4.HRApplication.domain.table;

import java.math.BigDecimal;
import java.time.Instant;

import com.se347.nhom4.HRApplication.util.SecurityUtil;
import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.OtTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "shift_ot_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftOtRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ot_type", nullable = false)
    private OtTypeEnum otType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    private DayTypeEnum dayType;

    @NotNull
    @Column(name = "rate_multiplier", precision = 5, scale = 2, nullable = false)
    private BigDecimal rateMultiplier;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    public boolean isCurrentlyActive() {
        Instant now = Instant.now();
        return isActive != null && isActive &&
                !effectiveFrom.isAfter(now) &&
                (effectiveTo == null || !effectiveTo.isBefore(now));
    }
}
