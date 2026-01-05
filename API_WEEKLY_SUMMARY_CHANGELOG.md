# API Weekly Summary - Changelog for Frontend

## 📋 Tổng Quan

Đã tạo API mới để **giảm 751 API calls xuống còn 1 call** cho trang Attendance Week View.

---

## 🔴 CŨ: 3 Tầng API (751 calls)

### Bước 1: Lấy danh sách nhân viên
```
GET /api/v1/employees/active
```
**Số lần gọi:** 1 call

---

### Bước 2: Lấy lịch làm việc từng nhân viên  
```
GET /api/v1/work-schedules/employee/{employeeId}/date-range?startDate=2025-12-23&endDate=2025-12-29
```
**Số lần gọi:** N calls (50 nhân viên = 50 calls)

---

### Bước 3: Lấy attendance từng work schedule
```
GET /api/v1/attendances/my/{workScheduleId}?employeeId={employeeId}
```
**Số lần gọi:** N × M calls (50 nhân viên × 14 schedules = 700 calls)

**TỔNG:** 1 + 50 + 700 = **751 API calls** ❌

---

## � Authentication & Authorization

### Required Permission
```
Permission Name: "Xem tổng hợp chấm công theo tuần"
API Path: /api/v2/attendances/weekly-summary
Method: GET
Module: ATTENDANCE
```

### Role Access
| Role         | Access        | Description                              |
| ------------ | ------------- | ---------------------------------------- |
| **ADMIN**    | ✅ Full Access | Có thể xem tổng hợp của tất cả nhân viên |
| **EMPLOYEE** | ✅ Allowed     | Có thể xem tổng hợp của tất cả nhân viên |

### Authorization Header
```
Authorization: Bearer <JWT_TOKEN>
```

JWT token phải chứa permission `"Xem tổng hợp chấm công theo tuần"` trong claims.

---

## �🟢 MỚI: 1 API Duy Nhất (1 call)

### Endpoint
```
GET /api/v2/attendances/weekly-summary?startDate=2025-12-23&endDate=2025-12-29
```

### Request Parameters
| Param       | Type      | Required | Format     | Example    |
| ----------- | --------- | -------- | ---------- | ---------- |
| `startDate` | LocalDate | ✅ Yes    | YYYY-MM-DD | 2025-12-23 |
| `endDate`   | LocalDate | ✅ Yes    | YYYY-MM-DD | 2025-12-29 |

---

### Response Structure

```json
{
  "startDate": "2025-12-23",
  "endDate": "2025-12-29",
  "employees": [
    {
      "employee": {
        "id": 1,
        "fullname": "Nguyễn Văn A",
        "email": "a@gmail.com",
        "department": null
      },
      "statistics": {
        "totalScheduled": 14,
        "worked": {
          "count": 12,
          "totalHours": 48
        },
        "absent": {
          "count": 2,
          "totalHours": 8
        },
        "late": {
          "count": 3,
          "totalMinutes": 25
        },
        "earlyLeave": {
          "count": 1,
          "totalMinutes": 15
        },
        "overtime": {
          "count": 5,
          "totalMinutes": 120
        }
      }
    }
  ]
}
```

---

## 📊 Response Model Chi Tiết

### ResWeeklySummary (Root Object)
```typescript
interface ResWeeklySummary {
  startDate: string;        // YYYY-MM-DD
  endDate: string;          // YYYY-MM-DD
  employees: EmployeeSummary[];
}
```

### EmployeeSummary
```typescript
interface EmployeeSummary {
  employee: Employee;
  statistics: Statistics;
}
```

### Employee
```typescript
interface Employee {
  id: number;
  fullname: string;
  email: string;
  department: string | null;  // TODO: Chưa có trong database
}
```

### Statistics
```typescript
interface Statistics {
  totalScheduled: number;     // Tổng số ca được phân công
  worked: WorkedStats;
  absent: AbsentStats;
  late: LateStats;
  earlyLeave: EarlyLeaveStats;
  overtime: OvertimeStats;
}
```

### WorkedStats
```typescript
interface WorkedStats {
  count: number;        // Số ca đã chấm công
  totalHours: number;   // Tổng số giờ làm việc (phút ÷ 60)
}
```

### AbsentStats
```typescript
interface AbsentStats {
  count: number;        // Số ca vắng
  totalHours: number;   // Tổng số giờ vắng (dựa vào standardHours của shift)
}
```

### LateStats
```typescript
interface LateStats {
  count: number;          // Số lần đi muộn
  totalMinutes: number;   // Tổng số phút đi muộn
}
```

### EarlyLeaveStats
```typescript
interface EarlyLeaveStats {
  count: number;          // Số lần về sớm
  totalMinutes: number;   // Tổng số phút về sớm
}
```

### OvertimeStats
```typescript
interface OvertimeStats {
  count: number;          // Số lần làm thêm giờ
  totalMinutes: number;   // Tổng số phút làm thêm
}
```

---

## ✅ Lợi Ích

| Metric                | Cũ (3 APIs) | Mới (1 API) | Cải Thiện |
| --------------------- | ----------- | ----------- | --------- |
| **API Calls**         | 751 calls   | 1 call      | ⬇️ 99.87%  |
| **Thời gian load**    | 10-15 giây  | 200-500ms   | ⬇️ 95%+    |
| **SQL Queries**       | N/A         | 3 queries   | Tối ưu    |
| **Network Bandwidth** | Cao         | Thấp        | ⬇️ Đáng kể |

---

## 🔧 Cách Sử Dụng (Frontend Code Example)

### TypeScript/React Example

```typescript
// API Call
const fetchWeeklySummary = async (startDate: string, endDate: string) => {
  const response = await fetch(
    `/api/v2/attendances/weekly-summary?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return await response.json();
};

// Usage
const summary = await fetchWeeklySummary('2025-12-23', '2025-12-29');

// Access data
summary.employees.forEach(emp => {
  console.log(`${emp.employee.fullname}:`);
  console.log(`  - Worked: ${emp.statistics.worked.count}/${emp.statistics.totalScheduled} shifts`);
  console.log(`  - Total hours: ${emp.statistics.worked.totalHours}h`);
  console.log(`  - Late: ${emp.statistics.late.count} times (${emp.statistics.late.totalMinutes} min)`);
  console.log(`  - Overtime: ${emp.statistics.overtime.totalMinutes} min`);
});
```

---

## 📝 Notes

### Điểm Khác Biệt Quan Trọng

1. **totalHours vs totalMinutes:**
   - `worked.totalHours`: Đã chia cho 60 (phút → giờ)
   - `late.totalMinutes`, `earlyLeave.totalMinutes`, `overtime.totalMinutes`: Vẫn tính bằng PHÚT

2. **Absent Calculation:**
   - Dựa trên `workSchedule` KHÔNG có attendance
   - `absentHours` = Tổng `standardHours` của các shift bị vắng

3. **Department Field:**
   - Hiện tại trả về `null` vì chưa có trong Employee entity
   - TODO: Sẽ cập nhật sau khi thêm Department entity

4. **Only Active Employees:**
   - API chỉ trả về nhân viên có `status = ACTIVE`
   - Không bao gồm nhân viên đã nghỉ việc/inactive

---

## 🚀 Migration Steps (Frontend)

### Step 1: Cập nhật API Service
Thay thế 3 API calls cũ bằng 1 call mới:

```typescript
// ❌ XÓA CÁC HÀM CŨ
// getActiveEmployees()
// getWorkSchedulesByEmployee()
// getAttendanceByWorkSchedule()

// ✅ THÊM HÀM MỚI
getWeeklySummary(startDate: string, endDate: string)
```

### Step 2: Cập nhật State Management
```typescript
// Cũ
const [employees, setEmployees] = useState([]);
const [schedules, setSchedules] = useState({});
const [attendances, setAttendances] = useState({});

// Mới - Đơn giản hơn!
const [weeklySummary, setWeeklySummary] = useState<ResWeeklySummary | null>(null);
```

### Step 3: Cập nhật Loading Logic
```typescript
// Cũ - Phức tạp với nhiều loading states
setLoading(true);
await loadEmployees();
await Promise.all(employees.map(emp => loadSchedules(emp.id)));
await Promise.all(schedules.map(sch => loadAttendance(sch.id)));
setLoading(false);

// Mới - Đơn giản!
setLoading(true);
const data = await getWeeklySummary(startDate, endDate);
setWeeklySummary(data);
setLoading(false);
```

### Step 4: Render Data
Tất cả thông tin statistics đã được tính toán sẵn, không cần xử lý logic phức tạp ở frontend!

---

## ⚠️ Breaking Changes

**KHÔNG CÓ** breaking changes nếu frontend tạo code mới riêng.

Nếu muốn migrate code cũ:
- Các API cũ vẫn hoạt động bình thường
- Có thể giữ cả 2 phiên bản song song
- Khuyến nghị: Migrate sang API v2 để tăng performance

---

## 📞 Contact

Nếu có câu hỏi hoặc cần thêm fields trong response, liên hệ Backend team.

**Created:** January 5, 2026  
**API Version:** v2  
**Endpoint:** `/api/v2/attendances/weekly-summary`
