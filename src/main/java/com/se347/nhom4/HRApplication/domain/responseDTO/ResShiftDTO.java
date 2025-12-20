package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResShiftDTO {
  private Long id;
  private String name;
  private String description;
  private LocalTime startTime;
  private LocalTime endTime;
  private Double standardHours;
  private Boolean isActive;
  private String colorCode;
}
