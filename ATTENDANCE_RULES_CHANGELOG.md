# ATTENDANCE RULES - CHANGELOG

## Tổng quan

Đã cập nhật logic Attendance theo các rule mới:

1. Tính `totalWorkTime` theo công thức payable minutes (clamp trong khung ca)
2. Tự đóng ca sau 6 tiếng nếu chưa checkout (không tính lương, cho phép checkin ca tiếp theo)

---

## 📋 CÁC THAY ĐỔI CHI TIẾT

### 1. **ATTENDANCE ENTITY** (`domain/table/Attendance.java`)

#### Thêm mới:

- **Field `status`** (AttendanceStatusEnum):
  - `ACTIVE`: Ca đang mở (đã check-in, chưa check-out)
  - `AUTO_CLOSED`: Ca đã tự đóng sau 6 tiếng (không tính lương)
  - `COMPLETED`: Ca đã hoàn thành (đã check-out)

#### Database:

- Thêm column `status` VARCHAR(255) vào bảng `attendances`

---

### 2. **ATTENDANCE ENUM** (`util/enums/AttendanceStatusEnum.java`)

#### File mới:

- Enum định nghĩa các trạng thái của Attendance

---

### 3. **ATTENDANCE SERVICE** (`service/AttendanceService.java`)

#### Thêm constants:

- `CHECK_IN_WINDOW_MINUTES_BEFORE = 30`: Cửa sổ check-in (30 phút trước giờ vào ca)
- `CHECK_OUT_MAX_HOURS_AFTER = 6`: Thời gian tối đa cho phép checkout sau giờ tan ca

#### Thêm validation methods:

1. **`validateCheckInWindow(WorkSchedule schedule)`**:

   - Chỉ cho check-in từ trước giờ vào ca 30 phút đến trước giờ tan ca
   - Throw exception nếu ngoài cửa sổ

2. **`validateNoActiveShift(Long employeeId, LocalDate workDate, Long currentScheduleId)`**:

   - Kiểm tra không có 2 ca đang mở cùng lúc
   - **Cho phép checkin ca tiếp theo nếu ca trước đã AUTO_CLOSED**
   - Throw exception nếu có ca chưa checkout (trừ AUTO_CLOSED)

3. **`validateCheckOutMaxDelay(WorkSchedule schedule, Instant now)`**:
   - Kiểm tra checkout muộn tối đa 6 tiếng sau giờ tan ca
   - Throw exception nếu quá 6 tiếng

#### Thêm calculation method:

4. **`calculatePayableWorkTime(Attendance attendance, WorkSchedule schedule)`**:
   - Tính `totalWorkTime` theo công thức payable minutes (clamp):
     - `pay_start = max(checkin, shift_start)`
     - `pay_end = min(checkout, shift_end)`
     - `pay_minutes = max(0, pay_end - pay_start)`
     - Cap: `pay_minutes <= shift_minutes`
   - **Thay thế** logic cũ tính raw minutes

#### Sửa đổi methods:

1. **`checkIn()`**:

   - Thêm validation check-in window
   - Thêm validation không có 2 ca mở
   - Set `status = ACTIVE` khi check-in

2. **`checkOut()`**:

   - Thêm validation không cho checkout nếu đã AUTO_CLOSED
   - Thêm validation checkout muộn ≤ 6h
   - Set `status = COMPLETED` khi checkout
   - Thay đổi tính `totalWorkTime` từ raw minutes sang payable minutes

3. **`buildNewAttendance()`**:

   - Set `status = ACTIVE` mặc định

4. **`toResponse()`**:
   - Thêm field `status` vào response

#### Xóa/Thay đổi:

- **Xóa**: Logic tính `totalWorkTime` = raw minutes (checkout - checkin)
- **Thay bằng**: Logic tính `totalWorkTime` = payable minutes (clamp)

---

### 4. **ATTENDANCE REPOSITORY** (`repository/AttendanceRepository.java`)

#### Thêm query methods:

1. **`findByEmployee_IdAndWorkDateAndCheckInNotNullAndCheckOutNullAndStatusNotAutoClosed()`**:

   - Tìm các attendance đã check-in nhưng chưa check-out và chưa AUTO_CLOSED
   - Dùng để validate không có 2 ca mở

2. **`findAttendancesToAutoClose()`**:
   - Tìm các attendance cần tự đóng (đã check-in, chưa check-out, chưa AUTO_CLOSED)
   - Dùng cho scheduled task

---

### 5. **ATTENDANCE AUTO-CLOSE SCHEDULER** (`service/AttendanceAutoCloseScheduler.java`)

#### File mới:

- Scheduled task chạy mỗi 30 phút
- Tự động đóng các ca chấm công sau 6 tiếng:
  - Tìm các attendance đã check-in, chưa check-out, chưa AUTO_CLOSED
  - Kiểm tra nếu đã quá 6 tiếng sau giờ tan ca
  - Set `status = AUTO_CLOSED`, giữ `checkOut = null`
  - **Không tính lương** (PayrollService sẽ skip vì checkOut = null)

---

### 6. **SPRING BOOT APPLICATION** (`HrApplication.java`)

#### Thêm annotation:

- `@EnableScheduling`: Bật tính năng scheduled tasks

---

### 7. **RES ATTENDANCE DTO** (`domain/responseDTO/ResAttendance.java`)

#### Thêm field:

- `status` (String): Trạng thái của attendance

---

## 🔄 ẢNH HƯỞNG ĐẾN CÁC TABLE/COMPONENT KHÁC

### ✅ **PAYROLL SERVICE** - KHÔNG CẦN THAY ĐỔI

- Đã có logic skip nếu `checkOut == null` (dòng 156)
- Tự động không tính lương cho ca AUTO_CLOSED
- Dùng `totalWorkTime` (giờ đã là payable minutes) để tính lương → **ĐÚNG**

### ✅ **WEEKLY SUMMARY** - KHÔNG CẦN THAY ĐỔI

- Đã có logic skip nếu `checkOut == null` (dòng 410)
- Tự động không tính vào thống kê cho ca AUTO_CLOSED
- Dùng `totalWorkTime` (giờ đã là payable minutes) → **ĐÚNG**

### ✅ **FRONTEND** - CÓ THỂ CẦN CẬP NHẬT

- Có thể hiển thị `status` để phân biệt ca AUTO_CLOSED
- Có thể disable nút checkout nếu `status == AUTO_CLOSED`

---

## 📊 TÓM TẮT CÁC THAY ĐỔI

| Component                        | Thay đổi                              | Mức độ                              |
| -------------------------------- | ------------------------------------- | ----------------------------------- |
| **Attendance Entity**            | Thêm field `status`                   | ⚠️ BREAKING (cần migration)         |
| **AttendanceStatusEnum**         | File mới                              | ✅ Không ảnh hưởng                  |
| **AttendanceService**            | Thêm validation + sửa logic tính toán | ⚠️ BREAKING (thay đổi behavior)     |
| **AttendanceRepository**         | Thêm 2 query methods                  | ✅ Không ảnh hưởng                  |
| **AttendanceAutoCloseScheduler** | File mới                              | ✅ Không ảnh hưởng                  |
| **HrApplication**                | Thêm `@EnableScheduling`              | ✅ Không ảnh hưởng                  |
| **ResAttendance DTO**            | Thêm field `status`                   | ⚠️ BREAKING (API response thay đổi) |
| **PayrollService**               | Không thay đổi                        | ✅ Tương thích                      |
| **WeeklySummary**                | Không thay đổi                        | ✅ Tương thích                      |

---

## 🗄️ DATABASE MIGRATION CẦN THIẾT

```sql
-- Thêm column status vào bảng attendances
ALTER TABLE attendances
ADD COLUMN status VARCHAR(255) NULL;

-- Set giá trị mặc định cho các record cũ
UPDATE attendances
SET status = CASE
    WHEN check_out IS NOT NULL THEN 'COMPLETED'
    WHEN check_in IS NOT NULL THEN 'ACTIVE'
    ELSE NULL
END;

-- Có thể thêm index nếu cần query theo status
CREATE INDEX idx_attendances_status ON attendances(status);
CREATE INDEX idx_attendances_checkin_status ON attendances(check_in, status)
WHERE check_in IS NOT NULL AND check_out IS NULL;
```

---

## ✅ CÁC RULE ĐÃ IMPLEMENT

- [x] Check-in window: Chỉ cho check-in từ trước giờ vào ca 30 phút đến trước giờ tan ca
- [x] Không có 2 ca mở: Validate không cho checkin ca mới nếu ca trước chưa checkout (trừ AUTO_CLOSED)
- [x] Mỗi ca chỉ chấm công 1 lần: Đã có validation
- [x] Checkout muộn ≤ 6h: Validate checkout không quá 6 tiếng sau giờ tan ca
- [x] Tự đóng ca sau 6h: Scheduled task tự đóng ca, set status = AUTO_CLOSED
- [x] Tính lương theo payable minutes: `totalWorkTime` = clamp trong khung ca
- [x] Cho phép checkin ca tiếp theo nếu ca trước đã AUTO_CLOSED

---

## 🎯 KẾT QUẢ

- ✅ Logic tính lương đúng theo rule (chỉ tính trong khung ca)
- ✅ Ca tự đóng sau 6h không tính lương
- ✅ Cho phép checkin ca tiếp theo sau khi ca trước tự đóng
- ✅ Không cần thay đổi PayrollService và WeeklySummary
- ⚠️ Cần migration database để thêm column `status`
