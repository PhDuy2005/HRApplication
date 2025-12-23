package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="work_sites")
public class WorkSite {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  Long id;

  String name;  //tên địa điểm làm việc
  String address;

  Double latitude;
  Double longitude;
  Integer radiusMeters; //bán kính hợp lệ từ tâm (latitude, longitude)

  Integer allowedAccuracyMaxMeters; //ví dụ 50m, nếu GPS accuracy > 50m thì từ chối
  Boolean active;

  Instant createdAt;
  Instant updatedAt;
}
