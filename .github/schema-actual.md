# Schema thực tế từ Entities (Generated: 2026-01-08)

```mermaid
erDiagram
	EMPLOYEE {
		bigint id PK "AUTO_INCREMENT"
		string fullname ""
		string email ""
		string password "hashed"
		string phone ""
		date hired_date "default: now()"
		enum status "ACTIVE|INACTIVE, default: ACTIVE"
		mediumtext refresh_token "nullable"
		bigint role_id FK "-> roles.id"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	ROLE {
		bigint id PK "AUTO_INCREMENT"
		string name "NOT NULL, unique"
		string description "nullable"
		boolean active ""
		timestamp created_at ""
		timestamp updated_at ""
		string created_by ""
		string updated_by ""
	}

	PERMISSION {
		bigint id PK "AUTO_INCREMENT"
		string name "NOT NULL"
		string api_path "NOT NULL"
		string method "NOT NULL"
		string module "NOT NULL"
		timestamp created_at ""
		timestamp updated_at ""
		string created_by ""
		string updated_by ""
	}

	PERMISSION_ROLE {
		bigint role_id FK "-> roles.id"
		bigint permission_id FK "-> permissions.id"
	}

	SHIFT {
		bigint id PK "AUTO_INCREMENT"
		string name "varchar(100), NOT NULL, unique"
		string description "varchar(500), nullable"
		time start_time "NOT NULL"
		time end_time "NOT NULL"
		double standard_hours "NOT NULL, auto-calculated"
		boolean is_active "NOT NULL, default: true"
		string color_code "varchar(7), nullable, hex color"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	WORK_SCHEDULE {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		bigint shift_id FK "-> shifts.id, NOT NULL"
		date work_date "NOT NULL"
		bigint work_site_id FK "-> work_sites.id, nullable"
		unique "employee_id, shift_id, work_date"
	}

	WORK_SITE {
		bigint id PK "AUTO_INCREMENT"
		string name ""
		string address ""
		double latitude ""
		double longitude ""
		int radius_meters "allowed radius from center"
		int allowed_accuracy_max_meters "max GPS accuracy allowed"
		boolean active ""
		timestamp created_at ""
		timestamp updated_at ""
	}

	ATTENDANCE {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		bigint work_schedule_id FK "-> work_schedules.id, nullable, UNIQUE"
		date work_date "NOT NULL"
		timestamp check_in "nullable"
		timestamp check_out "nullable"
		int total_work_time "minutes, nullable"
		int overtime "minutes, nullable"
		int late_time "minutes, nullable"
		int early_leave "minutes, nullable"
		double check_in_lat "GPS latitude"
		double check_in_lng "GPS longitude"
		int check_in_accuracy_meters "GPS accuracy"
		int check_in_distance_meters "distance to work site"
		double check_out_lat "GPS latitude"
		double check_out_lng "GPS longitude"
		int check_out_accuracy_meters "GPS accuracy"
		int check_out_distance_meters "distance to work site"
	}

	EMPLOYEE_SALARY_TYPE {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		enum salary_type "SHIFT|MONTHLY, NOT NULL"
		date effective_from "NOT NULL"
		date effective_to "nullable"
		string note "varchar(500), nullable"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	MONTHLY_SALARY {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		bigint base_salary "NOT NULL, VND"
		bigint allowance "NOT NULL, VND"
		date effective_from "NOT NULL"
		date effective_to "nullable"
		decimal performance_multiplier "decimal(5,2), default: 1.00"
		string note "varchar(500), nullable"
		boolean is_active "NOT NULL, default: true"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
		unique "employee_id, effective_from"
	}

	SHIFT_RATE {
		bigint id PK "AUTO_INCREMENT, abstract base class"
		bigint employee_id FK "-> employees.id, NOT NULL"
		enum day_type "WEEKDAY|SATURDAY|SUNDAY|HOLIDAY, NOT NULL"
		bigint base_rate "NOT NULL, VND/hour"
		decimal rate_multiplier "decimal(5,2), NOT NULL"
		timestamp effective_from "NOT NULL"
		timestamp effective_to "nullable"
		boolean is_active "NOT NULL, default: true"
		string rate_type "discriminator: BASE|SPECIAL"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	SHIFT_BASE_RATE {
		bigint id PK "FK -> shift_rates.id, JOINED inheritance"
	}

	SHIFT_SPECIAL_RATE {
		bigint id PK "FK -> shift_rates.id, JOINED inheritance"
		bigint shift_id FK "-> shifts.id, NOT NULL"
		string note "varchar(500), nullable"
		int priority "default: 0"
	}

	SHIFT_OT_RATE {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		enum ot_type "NORMAL|SPECIAL, NOT NULL"
		enum day_type "WEEKDAY|SATURDAY|SUNDAY|HOLIDAY, NOT NULL"
		decimal rate_multiplier "decimal(5,2), NOT NULL"
		boolean is_active "NOT NULL"
		timestamp effective_from "NOT NULL"
		timestamp effective_to "nullable"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	PENALTY_TYPE {
		bigint id PK "AUTO_INCREMENT"
		string name "NOT NULL, unique"
		enum frequency_type "DAILY|WEEKLY|MONTHLY, NOT NULL"
		decimal rate "decimal(10,2), NOT NULL"
		string description "nullable"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	EMPLOYEE_PENALTY {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		bigint penalty_type_id FK "-> penalty_types.id, NOT NULL"
		boolean is_active "NOT NULL"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
		unique "employee_id, penalty_type_id"
	}

	ATTENDANCE_PENALTY {
		bigint id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		bigint penalty_type_id FK "-> penalty_types.id, NOT NULL"
		bigint amount "NOT NULL, VND"
		string note "varchar(500), nullable"
		timestamp penalty_date "NOT NULL"
		timestamp created_at "NOT NULL"
		timestamp updated_at "nullable"
		string created_by "varchar(100)"
		string updated_by "varchar(100)"
	}

	PAYROLL {
		bigint payroll_id PK "AUTO_INCREMENT"
		bigint employee_id FK "-> employees.id, NOT NULL"
		int month "NOT NULL"
		int year "NOT NULL"
		bigint total_hour "nullable"
		bigint total_ot_hour "nullable"
		bigint base_salary "nullable"
		bigint shift_salary "nullable"
		bigint ot_salary "nullable"
		bigint penalty_total "nullable"
		bigint final_salary "nullable"
		enum status "DRAFT|APPROVED|PAID, NOT NULL"
		unique "employee_id, month, year"
	}

	%% Relationships
	EMPLOYEE ||--o{ WORK_SCHEDULE : "has"
	EMPLOYEE ||--o{ ATTENDANCE : "has"
	EMPLOYEE ||--o{ EMPLOYEE_SALARY_TYPE : "has"
	EMPLOYEE ||--o{ MONTHLY_SALARY : "has"
	EMPLOYEE ||--o{ SHIFT_RATE : "has (base + special)"
	EMPLOYEE ||--o{ SHIFT_BASE_RATE : "has"
	EMPLOYEE ||--o{ SHIFT_SPECIAL_RATE : "has"
	EMPLOYEE ||--o{ SHIFT_OT_RATE : "has"
	EMPLOYEE ||--o{ EMPLOYEE_PENALTY : "has"
	EMPLOYEE ||--o{ ATTENDANCE_PENALTY : "receives"
	EMPLOYEE ||--o{ PAYROLL : "receives"
	EMPLOYEE }o--|| ROLE : "belongs to"

	SHIFT ||--o{ WORK_SCHEDULE : "assigned to"
	SHIFT ||--o{ SHIFT_SPECIAL_RATE : "applies to"

	WORK_SCHEDULE ||--o| ATTENDANCE : "records (1-to-0..1)"
	WORK_SCHEDULE }o--|| WORK_SITE : "located at"

	PENALTY_TYPE ||--o{ EMPLOYEE_PENALTY : "assigned to"
	PENALTY_TYPE ||--o{ ATTENDANCE_PENALTY : "applied in"

	ROLE ||--o{ EMPLOYEE : "assigned to"
	ROLE }o--o{ PERMISSION : "has (many-to-many)"

	SHIFT_RATE ||--o| SHIFT_BASE_RATE : "inheritance (JOINED)"
	SHIFT_RATE ||--o| SHIFT_SPECIAL_RATE : "inheritance (JOINED)"
```

## Ghi chú quan trọng:

### 1. Inheritance Strategy (JOINED)
- `SHIFT_RATE` là abstract base class
- `SHIFT_BASE_RATE` và `SHIFT_SPECIAL_RATE` kế thừa từ `SHIFT_RATE`
- Sử dụng JOINED inheritance (3 tables riêng biệt)

### 2. Audit Fields
Tất cả entity đều có audit fields:
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP, nullable)
- `created_by` (VARCHAR(100))
- `updated_by` (VARCHAR(100))

### 3. GPS Tracking
`ATTENDANCE` có đầy đủ GPS tracking cho cả check-in và check-out:
- Latitude, Longitude
- Accuracy (meters)
- Distance to work site (meters)

### 4. Unique Constraints
- `employee_penalties`: (employee_id, penalty_type_id)
- `monthly_salaries`: (employee_id, effective_from)
- `work_schedules`: (employee_id, shift_id, work_date)
- `payrolls`: (employee_id, month, year)

### 5. Enums
- `StatusEnum`: ACTIVE, INACTIVE
- `SalaryTypeEnum`: SHIFT, MONTHLY (không có DAY như schema cũ)
- `DayTypeEnum`: WEEKDAY, SATURDAY, SUNDAY, HOLIDAY
- `FrequencyTypeEnum`: DAILY, WEEKLY, MONTHLY
- `OtTypeEnum`: NORMAL, SPECIAL
- `PayrollStatusEnum`: DRAFT, APPROVED, PAID

### 6. Relationship Notes
- `WORK_SCHEDULE` → `ATTENDANCE`: OneToOne optional (1 to 0..1)
- `EMPLOYEE` → `ROLE`: ManyToOne (nhiều employees có 1 role)
- `ROLE` ↔ `PERMISSION`: ManyToMany qua bảng `permission_role`
