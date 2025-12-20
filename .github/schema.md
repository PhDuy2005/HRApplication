EMPLOYEE {
		int id PK ""  
		string fullName  ""  
		string email  ""  
		string passwordHash  ""  
		string phoneNumber  ""  
		date hireDate  ""  
		string position  ""  
		enum status  "ACTIVE | INACTIVE"
		int role_id FK  
	}

	SHIFT {
		int id PK ""  
		string name  ""  
		string description  ""  
		time start  ""  
		time end  ""  
		bool isActive  ""  
	}

	WORK_SCHEDULE {
		int id PK ""  
		int employeeId FK ""  
		int shiftId FK ""  
		date workDate  ""  
	}

	ATTENDANCE {
		int id PK ""  
		int employeeId FK ""  
		int workScheduleId FK "NULLABLE"
        datetime workDate ""  
		datetime checkIn  ""  
		datetime checkOut  ""  
		int totalWorkTime  ""  
		int overtime  ""  
		int lateTime  ""
	}

	EMPLOYEE_SALARY_TYPE {
		int id PK ""  
		enum salaryType  "SHIFT|DAY|MONTHLY"  
		date effectiveFrom  ""  
		date effectiveTo  ""  
	}

	SHIFT_BASE_RATE {
		int id PK ""  
		int employeeId FK ""  
		enum dayType  "WEEKDAY|SAT|SUN|HOLIDAY"  
		long defaultBaseRate  ""  
	}

	SHIFT_SPECIAL_RATE {
		int id PK ""  
		int employeeId FK ""  
		enum dayType  ""  
		long defaultBaseRate  ""  
		int shiftId FK ""  
	}

	SHIFT_OT_RATE {
		int employeeId FK ""  
		enum OtType  ""  
		long percentage  ""  
	}

	MONTHLY_SALARY {
		int employeeId FK ""  
		long baseSalary  ""  
		long allowance  ""  
	}

	PENALTY_TYPE {
		int penaltyId PK,FK ""  
		string name  ""  
		enum frequencyType  ""  
		long rate  ""  
	}

	EMPLOYEE_PENALTY {
		int employeeId FK ""  
		int penaltyId FK ""  
		boolean active  ""  
	}

	PAYROLL {
		int payrollId PK ""  
		int employeeId FK ""  
		int month  ""  
		int year  ""  
		long totalHour  ""  
		long totalOtHour  ""  
		long baseSalary  ""  
		long shiftSalary  ""  
		long otSalary  ""  
		long penaltyTotal  ""  
		long finalSalary  ""  
		enum Status  ""  
	}

	ATTENDANCE_PENALTY {
		int id PK ""  
		int employeeId FK ""  
		int penaltyId FK ""  
		long amount  ""  
		String Note  ""  
	}

	ROLE{
		int id PK
		string name ""
	}

	ROLE_PERMISSION{
		int roleId FK ""
		int permissionId FK ""
	}

	PERMISSION{
		int id PK ""
		string name ""
		string description ""
		string apiPath ""
		string method ""
	}

	EMPLOYEE||--o{WORK_SCHEDULE:"has"
	SHIFT||--o{WORK_SCHEDULE:"assigned to"
	WORK_SCHEDULE||--o|ATTENDANCE:"records"
	EMPLOYEE||--o{ATTENDANCE:"has"
	EMPLOYEE||--o|EMPLOYEE_SALARY_TYPE:"has"
	EMPLOYEE||--o{SHIFT_BASE_RATE:"has"
	EMPLOYEE||--o{SHIFT_SPECIAL_RATE:"has"
	SHIFT||--o{SHIFT_SPECIAL_RATE:"applies to"
	EMPLOYEE||--o{SHIFT_OT_RATE:"has"

	EMPLOYEE||--o|MONTHLY_SALARY:"has"
	PENALTY_TYPE||--o{EMPLOYEE_PENALTY:"assigned to"
	EMPLOYEE||--o{EMPLOYEE_PENALTY:"has"
	PENALTY_TYPE||--o{ATTENDANCE_PENALTY:"applied in"
	EMPLOYEE||--o{ATTENDANCE_PENALTY:"receives"
	EMPLOYEE||--o{PAYROLL:"receives"

	ROLE||--o{EMPLOYEE:"assigned to"
	ROLE||--o{ROLE_PERMISSION:"has"
	PERMISSION||--o{ROLE_PERMISSION:"granted to"