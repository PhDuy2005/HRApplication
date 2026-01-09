package com.se347.nhom4.HRApplication.util.enums;

public enum AttendanceStatusEnum {
    /**
     * Ca đang mở (đã check-in, chưa check-out)
     */
    ACTIVE,
    
    /**
     * Ca đã tự đóng sau 6 tiếng (không tính lương)
     */
    AUTO_CLOSED,
    
    /**
     * Ca đã hoàn thành (đã check-out)
     */
    COMPLETED
}

