# So sánh Schema: .github/schema.md vs Entities thực tế

**Ngày tạo**: 2026-01-08  
**Mục đích**: Đối chiếu schema ban đầu với implementation thực tế

---

## 📊 TỔNG QUAN

| Tiêu chí              | Schema cũ             | Entities thực tế           | Khớp? |
| --------------------- | --------------------- | -------------------------- | ----- |
| Số lượng entity chính | 15                    | 17                         | ❌     |
| Số lượng table        | 16 (có join table)    | 18 (có join table)         | ❌     |
| Data types            | int, long, enum, bool | bigint, timestamp, decimal | ❌     |
| Audit fields          | ❌ Không có            | ✅ Có đầy đủ                | ❌     |
| GPS tracking          | ❌ Không có            | ✅ Có đầy đủ                | ❌     |

---

## ✅ CÁC ENTITY KHỚP (với minor differences)

### 1. EMPLOYEE ⚠️ Có khác biệt
| Field         | Schema cũ  | Thực tế    | Note      |
| ------------- | ---------- | ---------- | --------- |
| fullName      | ✅          | fullname   | Tên khác  |
| passwordHash  | ✅          | password   | Tên khác  |
| phoneNumber   | ✅          | phone      | Tên khác  |
| hireDate      | ✅          | hired_date | Tên khác  |
| position      | ✅          | ❌ Không có | **THIẾU** |
| refresh_token | ❌ Không có | ✅          | **THÊM**  |
| Audit fields  | ❌          | ✅ 4 fields | **THÊM**  |

### 2. SHIFT ⚠️ Có khác biệt
| Field        | Schema cũ | Thực tế             | Note     |
| ------------ | --------- | ------------------- | -------- |
| start/end    | ✅         | start_time/end_time | Tên khác |
| color_code   | ❌         | ✅                   | **THÊM** |
| Audit fields | ❌         | ✅ 4 fields          | **THÊM** |

### 3. WORK_SCHEDULE ⚠️ Có khác biệt
| Field             | Schema cũ | Thực tế    | Note     |
| ----------------- | --------- | ---------- | -------- |
| Cơ bản            | ✅         | ✅          | OK       |
| work_site_id      | ❌         | ✅ FK       | **THÊM** |
| Unique constraint | ❌         | ✅ 3 fields | **THÊM** |

### 4. ATTENDANCE ⚠️ Có khác biệt NHIỀU
| Field                       | Schema cũ  | Thực tế    | Note          |
| --------------------------- | ---------- | ---------- | ------------- |
| workDate                    | datetime   | date       | **Type khác** |
| early_leave                 | ❌          | ✅ int      | **THÊM**      |
| **GPS Check-in**            | ❌ KHÔNG CÓ | ✅ 4 fields | **THÊM**      |
| - check_in_lat              | ❌          | ✅ double   |               |
| - check_in_lng              | ❌          | ✅ double   |               |
| - check_in_accuracy_meters  | ❌          | ✅ int      |               |
| - check_in_distance_meters  | ❌          | ✅ int      |               |
| **GPS Check-out**           | ❌ KHÔNG CÓ | ✅ 4 fields | **THÊM**      |
| - check_out_lat             | ❌          | ✅ double   |               |
| - check_out_lng             | ❌          | ✅ double   |               |
| - check_out_accuracy_meters | ❌          | ✅ int      |               |
| - check_out_distance_meters | ❌          | ✅ int      |               |

### 5. EMPLOYEE_SALARY_TYPE ⚠️ Có khác biệt
| Field           | Schema cũ           | Thực tế        | Note       |
| --------------- | ------------------- | -------------- | ---------- |
| salaryType enum | SHIFT\|DAY\|MONTHLY | SHIFT\|MONTHLY | **Bỏ DAY** |
| employee_id     | ❌ Thiếu             | ✅ FK           | **THÊM**   |
| note            | ❌                   | ✅ varchar(500) | **THÊM**   |
| Audit fields    | ❌                   | ✅ 4 fields     | **THÊM**   |

---

## ❌ CÁC ENTITY CÓ SAI KHÁC LỚN

### 6. SHIFT_BASE_RATE ❌ SAI HOÀN TOÀN
**Schema cũ:**
```
int id PK
int employeeId FK
enum dayType
long defaultBaseRate
```

**Thực tế:**
```
JOINED inheritance từ SHIFT_RATE
├── shift_rates (base table)
│   ├── id PK
│   ├── employee_id FK
│   ├── day_type enum
│   ├── base_rate bigint
│   ├── rate_multiplier decimal(5,2)  ← THÊM
│   ├── effective_from timestamp      ← THÊM
│   ├── effective_to timestamp        ← THÊM
│   ├── is_active boolean             ← THÊM
│   ├── rate_type discriminator       ← THÊM
│   └── audit fields (4)              ← THÊM
└── shift_base_rates (child table)
    └── id PK/FK → shift_rates.id
        (không có field riêng)
```

**Khác biệt:**
- ❌ Schema cũ: Bảng độc lập
- ✅ Thực tế: JOINED inheritance với SHIFT_RATE
- ❌ Thiếu: rate_multiplier, effective_from/to, is_active
- ❌ Thiếu: Audit fields

### 7. SHIFT_SPECIAL_RATE ❌ SAI HOÀN TOÀN
**Schema cũ:**
```
int id PK
int employeeId FK
enum dayType
long defaultBaseRate
int shiftId FK
```

**Thực tế:**
```
JOINED inheritance từ SHIFT_RATE
├── shift_rates (inherited fields)
└── shift_special_rates (child table)
    ├── id PK/FK → shift_rates.id
    ├── shift_id FK → shifts.id
    ├── note varchar(500)     ← THÊM
    └── priority int          ← THÊM
```

**Khác biệt:**
- ❌ Schema cũ: Lặp lại fields từ base
- ✅ Thực tế: Kế thừa từ SHIFT_RATE
- ✅ THÊM: note, priority

### 8. SHIFT_OT_RATE ⚠️ Có khác biệt
**Schema cũ:**
```
int employeeId FK
enum DayTypeEnum
long percentage
```

**Thực tế:**
```
bigint id PK                    ← THÊM
bigint employee_id FK
enum ot_type (NORMAL|SPECIAL)   ← THÊM
enum day_type
decimal rate_multiplier         ← THAY percentage
boolean is_active               ← THÊM
timestamp effective_from        ← THÊM
timestamp effective_to          ← THÊM
audit fields (4)                ← THÊM
```

**Khác biệt:**
- ❌ Thiếu PK trong schema cũ
- ✅ THÊM: ot_type enum
- ❌ percentage → rate_multiplier (decimal)
- ✅ THÊM: effective dates, is_active
- ✅ THÊM: Audit fields

### 9. MONTHLY_SALARY ⚠️ Có khác biệt
**Schema cũ:**
```
int employeeId FK
long baseSalary
long allowance
```

**Thực tế:**
```
bigint id PK                        ← THÊM
bigint employee_id FK
bigint base_salary
bigint allowance
date effective_from                 ← THÊM
date effective_to                   ← THÊM
decimal performance_multiplier      ← THÊM
string note                         ← THÊM
boolean is_active                   ← THÊM
audit fields (4)                    ← THÊM
UNIQUE (employee_id, effective_from) ← THÊM
```

**Khác biệt:**
- ❌ Thiếu PK trong schema cũ
- ✅ THÊM: Effective dates (hỗ trợ lịch sử tăng lương)
- ✅ THÊM: performance_multiplier
- ✅ THÊM: is_active, note
- ✅ THÊM: Unique constraint
- ✅ THÊM: Audit fields

### 10. PENALTY_TYPE ⚠️ Có khác biệt
**Schema cứu:**
```
int penaltyId PK,FK  ← SAI (PK và FK?)
string name
enum frequencyType
long rate
```

**Thực tế:**
```
bigint id PK (chỉ PK, không FK)
string name (unique)
enum frequency_type
decimal rate (decimal(10,2))
string description              ← THÊM
audit fields (4)                ← THÊM
```

**Khác biệt:**
- ❌ Schema cũ: PK,FK là sai
- ✅ Thực tế: Chỉ PK
- ❌ rate: long → decimal(10,2)
- ✅ THÊM: description, audit fields

### 11. EMPLOYEE_PENALTY ⚠️ Có khác biệt
**Schema cũ:**
```
int employeeId FK
int penaltyId FK
boolean active
```

**Thực tế:**
```
bigint id PK                            ← THÊM
bigint employee_id FK
bigint penalty_type_id FK               ← Tên khác
boolean is_active
audit fields (4)                        ← THÊM
UNIQUE (employee_id, penalty_type_id)   ← THÊM
```

**Khác biệt:**
- ❌ Thiếu PK trong schema cũ
- ✅ penalty_id → penalty_type_id
- ✅ THÊM: Unique constraint
- ✅ THÊM: Audit fields

### 12. ATTENDANCE_PENALTY ⚠️ Có khác biệt
**Schema cũ:**
```
int id PK
int employeeId FK
int penaltyId FK
long amount
String Note
```

**Thực tế:**
```
bigint id PK
bigint employee_id FK
bigint penalty_type_id FK       ← Tên khác
bigint amount
string note (varchar(500))
timestamp penalty_date          ← THÊM
audit fields (4)                ← THÊM
```

**Khác biệt:**
- ✅ penalty_id → penalty_type_id
- ✅ THÊM: penalty_date (timestamp)
- ✅ THÊM: Audit fields

### 13. PAYROLL ✅ GẦN KHỚP
**Schema cũ vs Thực tế:**
- ✅ Các field chính khớp
- ⚠️ Type: int → bigint
- ✅ THÊM: Unique constraint (employee_id, month, year)

### 14. ROLE ⚠️ Có khác biệt
**Schema cũ:**
```
int id PK
string name
```

**Thực tế:**
```
bigint id PK
string name (NOT NULL)
string description      ← THÊM
boolean active          ← THÊM
audit fields (4)        ← THÊM
```

### 15. PERMISSION ⚠️ Có khác biệt
**Schema cũ:**
```
int id PK
string name
string description
string apiPath
string method
```

**Thực tế:**
```
bigint id PK
string name (NOT NULL)
string api_path (NOT NULL)
string method (NOT NULL)
string module           ← THÊM (thay description?)
audit fields (4)        ← THÊM
```

**Khác biệt:**
- ✅ description → module
- ✅ THÊM: NOT NULL constraints
- ✅ THÊM: Audit fields

### 16. PERMISSION_ROLE vs ROLE_PERMISSION
**Schema cũ:** ROLE_PERMISSION  
**Thực tế:** PERMISSION_ROLE

- Chỉ khác tên bảng
- Cấu trúc giống nhau (roleId, permissionId)

---

## ➕ CÁC ENTITY MỚI (không có trong schema cũ)

### 17. WORK_SITE ✅ ENTITY MỚI
```sql
bigint id PK
string name
string address
double latitude
double longitude
int radius_meters               -- bán kính hợp lệ
int allowed_accuracy_max_meters -- GPS accuracy tối đa
boolean active
timestamp created_at
timestamp updated_at
```

**Mục đích:**
- Quản lý địa điểm làm việc
- Hỗ trợ GPS tracking
- Kiểm tra nhân viên có check-in/out đúng địa điểm không

---

## 📋 BẢNG TÓM TẮT SAI KHÁC

| #   | Entity               | Status | Vấn đề chính                                                   |
| --- | -------------------- | ------ | -------------------------------------------------------------- |
| 1   | Employee             | ⚠️      | Thiếu position, thêm refresh_token, audit fields               |
| 2   | Role                 | ⚠️      | Thêm description, active, audit fields                         |
| 3   | Permission           | ⚠️      | description → module, thêm audit fields                        |
| 4   | Shift                | ⚠️      | Thêm color_code, audit fields                                  |
| 5   | WorkSchedule         | ⚠️      | Thêm work_site_id FK, unique constraint                        |
| 6   | **WorkSite**         | ✅      | **ENTITY MỚI**                                                 |
| 7   | **Attendance**       | ❌      | **SAI NHIỀU**: Thêm 8 GPS fields, early_leave                  |
| 8   | EmployeeSalaryType   | ⚠️      | Bỏ DAY enum, thêm note, audit fields                           |
| 9   | MonthlySalary        | ❌      | Thiếu PK, effective dates, performance_multiplier              |
| 10  | **ShiftRate**        | ✅      | **ENTITY MỚI** (abstract base class)                           |
| 11  | **ShiftBaseRate**    | ❌      | **SAI HOÀN TOÀN**: JOINED inheritance, không phải bảng độc lập |
| 12  | **ShiftSpecialRate** | ❌      | **SAI HOÀN TOÀN**: JOINED inheritance, thêm note, priority     |
| 13  | ShiftOtRate          | ❌      | Thiếu PK, thêm ot_type, effective dates                        |
| 14  | PenaltyType          | ⚠️      | PK,FK sai, rate type khác                                      |
| 15  | EmployeePenalty      | ⚠️      | Thiếu PK, thêm unique constraint, audit fields                 |
| 16  | AttendancePenalty    | ⚠️      | Thêm penalty_date, audit fields                                |
| 17  | Payroll              | ✅      | Gần khớp, chỉ khác type và thêm unique constraint              |

---

## 🎯 NHỮNG THAY ĐỔI QUAN TRỌNG NHẤT

### 1. 🔴 Inheritance Strategy cho Shift Rates
**Thay đổi lớn nhất:** ShiftBaseRate và ShiftSpecialRate không còn là bảng độc lập, mà kế thừa từ ShiftRate qua JOINED inheritance.

### 2. 🟡 GPS Tracking System
**Thêm mới:** 8 fields GPS trong ATTENDANCE để tracking vị trí check-in/check-out.

### 3. 🟡 Work Site Management
**Thêm mới:** Entity WORK_SITE để quản lý địa điểm làm việc và validate GPS.

### 4. 🟢 Audit Trail
**Thêm mới:** Tất cả entity đều có 4 audit fields (created_at, updated_at, created_by, updated_by).

### 5. 🟢 History Tracking
**Thêm mới:** Effective dates cho MONTHLY_SALARY, SHIFT_RATE, SHIFT_OT_RATE để theo dõi lịch sử thay đổi.

### 6. 🟢 Data Integrity
**Thêm mới:** Nhiều unique constraints và NOT NULL constraints.

---

## 📌 KHUYẾN NGHỊ

### Cần cập nhật schema.md với:
1. ✅ Thêm entity WORK_SITE
2. ✅ Thêm entity SHIFT_RATE (abstract)
3. ✅ Sửa ShiftBaseRate và ShiftSpecialRate theo JOINED inheritance
4. ✅ Thêm 8 GPS fields vào ATTENDANCE
5. ✅ Thêm audit fields cho tất cả entity
6. ✅ Thêm effective_from/to cho các entity có history tracking
7. ✅ Sửa data types: int → bigint, long → bigint/decimal
8. ✅ Thêm unique constraints
9. ✅ Bỏ DAY từ SalaryTypeEnum
10. ✅ Thêm các field mới: color_code, note, priority, performance_multiplier, etc.

---

## 📝 KẾT LUẬN

**Tỷ lệ khớp:** ~40% (nhiều thay đổi quan trọng)

**Lý do chính:**
- Schema cũ thiếu audit trail
- Schema cũ thiếu GPS tracking
- Schema cũ thiếu history tracking (effective dates)
- Schema cũ không thể hiện inheritance strategy
- Schema cũ thiếu entity WORK_SITE
- Schema cũ thiếu nhiều business logic fields

**➡️ Cần cập nhật schema.md theo implementation thực tế trong entities.**
