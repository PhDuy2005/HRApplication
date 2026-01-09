-- Migration: Cho phép day_type = NULL và mở rộng ot_type trong shift_ot_rates
-- Lý do: OT type giờ là ALL_OT áp dụng cho tất cả ngày, không cần phân biệt dayType

ALTER TABLE shift_ot_rates MODIFY COLUMN day_type VARCHAR(50) NULL;

ALTER TABLE shift_ot_rates
MODIFY COLUMN ot_type VARCHAR(50) NOT NULL;

-- Verify: Kiểm tra constraint đã được update
DESCRIBE shift_ot_rates;