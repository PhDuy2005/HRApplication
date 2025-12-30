# 🔐 Hướng Dẫn Hệ Thống Phân Quyền - HR Application

## 📋 Mục Lục
- [Tổng Quan](#tổng-quan)
- [Cấu Trúc Hệ Thống](#cấu-trúc-hệ-thống)
- [Danh Sách Roles](#danh-sách-roles)
- [Danh Sách Permissions](#danh-sách-permissions)
- [Cách Hoạt Động](#cách-hoạt-động)
- [Hướng Dẫn Frontend Integration](#hướng-dẫn-frontend-integration)
- [API Endpoints](#api-endpoints)

---

## Tổng Quan

Hệ thống phân quyền của HR Application sử dụng mô hình **Role-Based Access Control (RBAC)**, cho phép:
- Quản lý quyền truy cập chi tiết đến từng API endpoint
- Gán nhiều quyền (permissions) cho một vai trò (role)
- Mỗi nhân viên (employee) có một role duy nhất
- Kiểm soát truy cập dựa trên HTTP method và API path

---

## Cấu Trúc Hệ Thống

```
Employee (Nhân viên)
    └── Role (Vai trò)
            └── Permissions[] (Danh sách quyền)
                    ├── name: Tên quyền
                    ├── apiPath: Đường dẫn API
                    ├── method: HTTP method (GET/POST/PUT/DELETE)
                    └── module: Module thuộc về
```

### Entity: Permission
```java
{
  "id": Long,
  "name": String,           // VD: "Xem danh sách nhân viên"
  "apiPath": String,        // VD: "/api/v1/employees"
  "method": String,         // VD: "GET"
  "module": String,         // VD: "EMPLOYEE"
  "createdAt": Instant,
  "updatedAt": Instant,
  "createdBy": String,
  "updatedBy": String
}
```

### Entity: Role
```java
{
  "id": Long,
  "name": String,           // VD: "ADMIN"
  "description": String,
  "active": Boolean,
  "permissions": Permission[],
  "createdAt": Instant,
  "updatedAt": Instant,
  "createdBy": String,
  "updatedBy": String
}
```

---

## Danh Sách Roles

Hệ thống có 2 roles mặc định được tạo tự động khi khởi động:

### 1. 👑 ADMIN (Quản Trị Viên)
- **Mô tả**: Quản trị viên - Có toàn quyền truy cập hệ thống
- **Permissions**: **TẤT CẢ** (61 permissions)
- **Quyền hạn**:
  - ✅ Toàn quyền quản lý nhân viên (CRUD)
  - ✅ Toàn quyền quản lý vai trò và quyền (CRUD)
  - ✅ Quản lý lịch làm việc, ca làm việc
  - ✅ Quản lý chấm công
  - ✅ Tính toán và quản lý lương
  - ✅ Xem và xuất báo cáo
  - ✅ Quản lý địa điểm làm việc

### 2. 👤 EMPLOYEE (Nhân Viên)
- **Mô tả**: Nhân viên - Quyền truy cập cơ bản
- **Permissions**: 31 permissions (giới hạn)
- **Quyền hạn**:
  - ✅ Đăng nhập/đăng xuất
  - ✅ Xem thông tin tài khoản cá nhân
  - ✅ Check-in/Check-out (chấm công)
  - ✅ Xem chấm công của bản thân
  - ✅ Xem lịch làm việc của mình
  - ✅ Xem bảng lương của mình
  - ✅ Xem danh sách nhân viên active
  - ✅ Xem ca làm việc (bao gồm tìm kiếm)
  - ✅ Xem địa điểm làm việc
  - ❌ **KHÔNG** được tạo/sửa/xóa nhân viên
  - ❌ **KHÔNG** được quản lý roles/permissions
  - ❌ **KHÔNG** được sửa/xóa chấm công
  - ❌ **KHÔNG** được tính lương

---

## Danh Sách Permissions

### 🔐 AUTH Module (4 permissions)
| Permission Name         | API Path               | Method | Module |
| ----------------------- | ---------------------- | ------ | ------ |
| Đăng nhập               | `/api/v1/auth/login`   | POST   | AUTH   |
| Lấy thông tin tài khoản | `/api/v1/auth/account` | GET    | AUTH   |
| Refresh token           | `/api/v1/auth/refresh` | GET    | AUTH   |
| Đăng xuất               | `/api/v1/auth/logout`  | POST   | AUTH   |

### 👥 EMPLOYEE Module (6 permissions)
| Permission Name                | API Path                            | Method | Module   | ADMIN | EMPLOYEE |
| ------------------------------ | ----------------------------------- | ------ | -------- | ----- | -------- |
| Xem danh sách nhân viên        | `/api/v1/employees`                 | GET    | EMPLOYEE | ✅     | ❌        |
| Xem danh sách nhân viên active | `/api/v1/employees/active`          | GET    | EMPLOYEE | ✅     | ✅        |
| Xem chi tiết nhân viên         | `/api/v1/employees/{id}`            | GET    | EMPLOYEE | ✅     | ✅        |
| Tạo nhân viên mới              | `/api/v1/employees`                 | POST   | EMPLOYEE | ✅     | ❌        |
| Cập nhật thông tin nhân viên   | `/api/v1/employees/{id}/basic-info` | PUT    | EMPLOYEE | ✅     | ❌        |
| Xóa nhân viên                  | `/api/v1/employees/{id}`            | DELETE | EMPLOYEE | ✅     | ❌        |

### ⏰ ATTENDANCE Module (9 permissions)
| Permission Name                  | API Path                                  | Method | Module     | ADMIN | EMPLOYEE |
| -------------------------------- | ----------------------------------------- | ------ | ---------- | ----- | -------- |
| Xem danh sách chấm công          | `/api/v1/attendances`                     | GET    | ATTENDANCE | ✅     | ✅        |
| Xem chi tiết chấm công           | `/api/v1/attendances/{id}`                | GET    | ATTENDANCE | ✅     | ✅        |
| Check-in                         | `/api/v1/attendances/check-in`            | POST   | ATTENDANCE | ✅     | ✅        |
| Check-out                        | `/api/v1/attendances/check-out`           | POST   | ATTENDANCE | ✅     | ✅        |
| Xem chấm công của bản thân       | `/api/v1/attendances/my`                  | GET    | ATTENDANCE | ✅     | ✅        |
| Xem chấm công theo lịch làm việc | `/api/v1/attendances/my/{workScheduleId}` | GET    | ATTENDANCE | ✅     | ✅        |
| Tạo chấm công thủ công           | `/api/v1/attendances`                     | POST   | ATTENDANCE | ✅     | ❌        |
| Cập nhật chấm công               | `/api/v1/attendances/{id}`                | PUT    | ATTENDANCE | ✅     | ❌        |
| Xóa chấm công                    | `/api/v1/attendances/{id}`                | DELETE | ATTENDANCE | ✅     | ❌        |

### 📅 WORK_SCHEDULE Module (12 permissions)
| Permission Name                                   | API Path                                                       | Method | Module        | ADMIN | EMPLOYEE |
| ------------------------------------------------- | -------------------------------------------------------------- | ------ | ------------- | ----- | -------- |
| Xem lịch làm việc                                 | `/api/v1/work-schedules`                                       | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem chi tiết lịch làm việc                        | `/api/v1/work-schedules/{id}`                                  | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc theo nhân viên                  | `/api/v1/work-schedules/employee/{employeeId}`                 | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc theo ca                         | `/api/v1/work-schedules/shift/{shiftId}`                       | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc theo ca và khoảng thời gian     | `/api/v1/work-schedules/shift/{shiftId}/date-range`            | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc theo ngày                       | `/api/v1/work-schedules/date/{workDate}`                       | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc nhân viên theo ngày             | `/api/v1/work-schedules/employee/{employeeId}/date/{workDate}` | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Xem lịch làm việc nhân viên theo khoảng thời gian | `/api/v1/work-schedules/employee/{employeeId}/date-range`      | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Kiểm tra lịch làm việc tồn tại                    | `/api/v1/work-schedules/exists`                                | GET    | WORK_SCHEDULE | ✅     | ✅        |
| Tạo lịch làm việc                                 | `/api/v1/work-schedules`                                       | POST   | WORK_SCHEDULE | ✅     | ❌        |
| Cập nhật lịch làm việc                            | `/api/v1/work-schedules/{id}`                                  | PUT    | WORK_SCHEDULE | ✅     | ❌        |
| Xóa lịch làm việc                                 | `/api/v1/work-schedules/{id}`                                  | DELETE | WORK_SCHEDULE | ✅     | ❌        |

### 🕐 SHIFT Module (7 permissions)
| Permission Name                          | API Path                | Method | Module | ADMIN | EMPLOYEE |
| ---------------------------------------- | ----------------------- | ------ | ------ | ----- | -------- |
| Xem danh sách ca làm việc                | `/api/v1/shifts`        | GET    | SHIFT  | ✅     | ✅        |
| Xem danh sách ca làm việc đang hoạt động | `/api/v1/shifts/active` | GET    | SHIFT  | ✅     | ✅        |
| Xem chi tiết ca làm việc                 | `/api/v1/shifts/{id}`   | GET    | SHIFT  | ✅     | ✅        |
| Tìm kiếm ca làm việc theo tên            | `/api/v1/shifts/search` | GET    | SHIFT  | ✅     | ✅        |
| Tạo ca làm việc mới                      | `/api/v1/shifts`        | POST   | SHIFT  | ✅     | ❌        |
| Cập nhật ca làm việc                     | `/api/v1/shifts/{id}`   | PUT    | SHIFT  | ✅     | ❌        |
| Xóa ca làm việc                          | `/api/v1/shifts/{id}`   | DELETE | SHIFT  | ✅     | ❌        |

### 📍 WORK_SITE Module (5 permissions)
| Permission Name                 | API Path                  | Method | Module    | ADMIN | EMPLOYEE |
| ------------------------------- | ------------------------- | ------ | --------- | ----- | -------- |
| Xem danh sách địa điểm làm việc | `/api/v1/work-sites`      | GET    | WORK_SITE | ✅     | ✅        |
| Xem chi tiết địa điểm làm việc  | `/api/v1/work-sites/{id}` | GET    | WORK_SITE | ✅     | ✅        |
| Tạo địa điểm làm việc           | `/api/v1/work-sites`      | POST   | WORK_SITE | ✅     | ❌        |
| Cập nhật địa điểm làm việc      | `/api/v1/work-sites/{id}` | PUT    | WORK_SITE | ✅     | ❌        |
| Xóa địa điểm làm việc           | `/api/v1/work-sites/{id}` | DELETE | WORK_SITE | ✅     | ❌        |

### 🎭 ROLE Module (5 permissions)
| Permission Name       | API Path             | Method | Module | ADMIN | EMPLOYEE |
| --------------------- | -------------------- | ------ | ------ | ----- | -------- |
| Xem danh sách vai trò | `/api/v1/roles`      | GET    | ROLE   | ✅     | ❌        |
| Xem chi tiết vai trò  | `/api/v1/roles/{id}` | GET    | ROLE   | ✅     | ❌        |
| Tạo vai trò mới       | `/api/v1/roles`      | POST   | ROLE   | ✅     | ❌        |
| Cập nhật vai trò      | `/api/v1/roles/{id}` | PUT    | ROLE   | ✅     | ❌        |
| Xóa vai trò           | `/api/v1/roles/{id}` | DELETE | ROLE   | ✅     | ❌        |

### 🔑 PERMISSION Module (5 permissions)
| Permission Name     | API Path                   | Method | Module     | ADMIN | EMPLOYEE |
| ------------------- | -------------------------- | ------ | ---------- | ----- | -------- |
| Xem danh sách quyền | `/api/v1/permissions`      | GET    | PERMISSION | ✅     | ❌        |
| Xem chi tiết quyền  | `/api/v1/permissions/{id}` | GET    | PERMISSION | ✅     | ❌        |
| Tạo quyền mới       | `/api/v1/permissions`      | POST   | PERMISSION | ✅     | ❌        |
| Cập nhật quyền      | `/api/v1/permissions/{id}` | PUT    | PERMISSION | ✅     | ❌        |
| Xóa quyền           | `/api/v1/permissions/{id}` | DELETE | PERMISSION | ✅     | ❌        |

### 💰 SALARY Module (4 permissions)
| Permission Name    | API Path                     | Method | Module | ADMIN | EMPLOYEE |
| ------------------ | ---------------------------- | ------ | ------ | ----- | -------- |
| Xem bảng lương     | `/api/v1/salaries`           | GET    | SALARY | ✅     | ✅        |
| Xem chi tiết lương | `/api/v1/salaries/{id}`      | GET    | SALARY | ✅     | ✅        |
| Tính lương         | `/api/v1/salaries/calculate` | POST   | SALARY | ✅     | ❌        |
| Cập nhật lương     | `/api/v1/salaries/{id}`      | PUT    | SALARY | ✅     | ❌        |

### 📊 REPORT Module (4 permissions)
| Permission Name       | API Path                     | Method | Module | ADMIN | EMPLOYEE |
| --------------------- | ---------------------------- | ------ | ------ | ----- | -------- |
| Xem báo cáo tổng quan | `/api/v1/reports/overview`   | GET    | REPORT | ✅     | ❌        |
| Xem báo cáo chấm công | `/api/v1/reports/attendance` | GET    | REPORT | ✅     | ❌        |
| Xem báo cáo lương     | `/api/v1/reports/salary`     | GET    | REPORT | ✅     | ❌        |
| Xuất báo cáo Excel    | `/api/v1/reports/export`     | GET    | REPORT | ✅     | ❌        |

---

## Cách Hoạt Động

### 1. Authentication Flow
```
1. User đăng nhập với username/password
2. Backend xác thực và trả về:
   - Access Token (JWT)
   - Refresh Token (trong cookie)
   - User Info (bao gồm role và permissions)
```

### 2. JWT Token Structure
```json
{
  "sub": "user@example.com",
  "permission": [
    "Đăng nhập",
    "Xem danh sách nhân viên",
    "Check-in",
    ...
  ],
  "iat": 1735550000,
  "exp": 1735553600
}
```

### 3. Authorization Check
Backend sẽ kiểm tra:
- Token có hợp lệ không?
- User có permission phù hợp với API endpoint đang gọi không?
- Method có khớp không? (GET/POST/PUT/DELETE)

---

## Hướng Dẫn Frontend Integration

### 1. Login và Lưu Trữ Token

```typescript
// Login API Response
interface LoginResponse {
  accessToken: string;
  user: {
    id: number;
    email: string;
    name: string;
    role: {
      id: number;
      name: string;  // "ADMIN" hoặc "EMPLOYEE"
      description: string;
      permissions: Permission[];
    }
  }
}

// Lưu token vào localStorage/sessionStorage
localStorage.setItem('accessToken', response.accessToken);
localStorage.setItem('userRole', response.user.role.name);
localStorage.setItem('permissions', JSON.stringify(response.user.role.permissions));
```

### 2. Gửi Request với Token

```typescript
// Axios interceptor
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 3. Kiểm Tra Permission Trên Frontend

```typescript
// Permission helper
class PermissionHelper {
  private permissions: Permission[] = [];

  constructor() {
    const stored = localStorage.getItem('permissions');
    this.permissions = stored ? JSON.parse(stored) : [];
  }

  // Kiểm tra user có permission không
  hasPermission(apiPath: string, method: string): boolean {
    return this.permissions.some(
      p => p.apiPath === apiPath && p.method === method
    );
  }

  // Kiểm tra theo tên permission
  hasPermissionByName(permissionName: string): boolean {
    return this.permissions.some(p => p.name === permissionName);
  }

  // Kiểm tra user có phải là admin không
  isAdmin(): boolean {
    return localStorage.getItem('userRole') === 'ADMIN';
  }

  // Kiểm tra user có phải là employee không
  isEmployee(): boolean {
    return localStorage.getItem('userRole') === 'EMPLOYEE';
  }
}

const permissionHelper = new PermissionHelper();
```

### 4. Ẩn/Hiện UI Elements Dựa Trên Quyền

```typescript
// React example
import { permissionHelper } from './helpers/permissionHelper';

function EmployeeList() {
  const canCreate = permissionHelper.hasPermission('/api/v1/employees', 'POST');
  const canDelete = permissionHelper.hasPermission('/api/v1/employees/{id}', 'DELETE');
  const isAdmin = permissionHelper.isAdmin();

  return (
    <div>
      {/* Chỉ hiện nút tạo nếu có quyền */}
      {canCreate && (
        <button onClick={handleCreate}>Tạo Nhân Viên Mới</button>
      )}

      <table>
        {employees.map(emp => (
          <tr key={emp.id}>
            <td>{emp.name}</td>
            <td>
              {/* Chỉ hiện nút xóa nếu có quyền */}
              {canDelete && (
                <button onClick={() => handleDelete(emp.id)}>Xóa</button>
              )}
            </td>
          </tr>
        ))}
      </table>
    </div>
  );
}
```

### 5. Route Protection

```typescript
// React Router example
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ 
  children, 
  requiredPermission 
}: { 
  children: React.ReactNode; 
  requiredPermission?: string;
}) {
  const token = localStorage.getItem('accessToken');
  
  // Chưa đăng nhập
  if (!token) {
    return <Navigate to="/login" />;
  }

  // Cần permission cụ thể
  if (requiredPermission) {
    const hasPermission = permissionHelper.hasPermissionByName(requiredPermission);
    if (!hasPermission) {
      return <Navigate to="/403" />; // Forbidden
    }
  }

  return <>{children}</>;
}

// Sử dụng
<Route path="/employees" element={
  <ProtectedRoute requiredPermission="Xem danh sách nhân viên">
    <EmployeePage />
  </ProtectedRoute>
} />
```

### 6. Menu/Navigation Dựa Trên Role

```typescript
const navigationItems = [
  {
    title: 'Trang Chủ',
    path: '/',
    icon: 'home',
    allowedRoles: ['ADMIN', 'EMPLOYEE']
  },
  {
    title: 'Nhân Viên',
    path: '/employees',
    icon: 'users',
    allowedRoles: ['ADMIN', 'EMPLOYEE'],
    requiredPermission: 'Xem danh sách nhân viên active'
  },
  {
    title: 'Chấm Công',
    path: '/attendance',
    icon: 'clock',
    allowedRoles: ['ADMIN', 'EMPLOYEE']
  },
  {
    title: 'Quản Lý Vai Trò',
    path: '/roles',
    icon: 'shield',
    allowedRoles: ['ADMIN']
  },
  {
    title: 'Báo Cáo',
    path: '/reports',
    icon: 'chart',
    allowedRoles: ['ADMIN']
  }
];

// Filter menu theo role
function Navigation() {
  const userRole = localStorage.getItem('userRole');
  
  const visibleItems = navigationItems.filter(item => {
    // Kiểm tra role
    if (!item.allowedRoles.includes(userRole)) {
      return false;
    }
    
    // Kiểm tra permission nếu có
    if (item.requiredPermission) {
      return permissionHelper.hasPermissionByName(item.requiredPermission);
    }
    
    return true;
  });

  return (
    <nav>
      {visibleItems.map(item => (
        <NavLink key={item.path} to={item.path}>
          {item.title}
        </NavLink>
      ))}
    </nav>
  );
}
```

---

## API Endpoints

### Authentication
```
POST   /api/v1/auth/login         - Đăng nhập
GET    /api/v1/auth/account       - Lấy thông tin tài khoản
GET    /api/v1/auth/refresh       - Refresh token
POST   /api/v1/auth/logout        - Đăng xuất
```

### Roles Management (Admin only)
```
GET    /api/v1/roles              - Lấy danh sách roles
GET    /api/v1/roles/{id}         - Lấy chi tiết role
POST   /api/v1/roles              - Tạo role mới
PUT    /api/v1/roles/{id}         - Cập nhật role
DELETE /api/v1/roles/{id}         - Xóa role
```

### Permissions Management (Admin only)
```
GET    /api/v1/permissions        - Lấy danh sách permissions
GET    /api/v1/permissions/{id}   - Lấy chi tiết permission
POST   /api/v1/permissions        - Tạo permission mới
PUT    /api/v1/permissions/{id}   - Cập nhật permission
DELETE /api/v1/permissions/{id}   - Xóa permission
```

---

## Best Practices

### 🔒 Security
1. **Luôn kiểm tra token** ở mỗi request
2. **Không tin tưởng frontend**: Backend phải tự validate permissions
3. **Refresh token** khi access token hết hạn
4. **Xóa token** khi logout
5. **HTTPS only** trong production

### ⚡ Performance
1. **Cache permissions** trong localStorage
2. **Lazy load** các module theo quyền
3. **Prefetch** data cho các route user có quyền truy cập

### 🎨 UX
1. **Ẩn ngay** các chức năng user không có quyền (đừng hiện rồi disable)
2. **Hiển thị message rõ ràng** khi user không có quyền
3. **Redirect** về trang phù hợp khi user cố truy cập route bị cấm

### 🐛 Error Handling
```typescript
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Unauthorized - chưa đăng nhập
      localStorage.clear();
      window.location.href = '/login';
    }
    
    if (error.response?.status === 403) {
      // Forbidden - không có quyền
      window.location.href = '/403';
    }
    
    return Promise.reject(error);
  }
);
```

---

## Kiểm Tra Hệ Thống

### Test Cases cho Frontend

1. **Login với ADMIN**
   - ✅ Có thể truy cập tất cả routes
   - ✅ Thấy tất cả menu items
   - ✅ Có thể CRUD employees, roles, permissions

2. **Login với EMPLOYEE**
   - ✅ Chỉ thấy menu giới hạn
   - ✅ Không thấy menu "Quản lý vai trò", "Báo cáo"
   - ✅ Có thể check-in/check-out
   - ❌ Không thể tạo/sửa/xóa nhân viên
   - ❌ Không thể truy cập `/roles`, `/permissions`

3. **No Token (Chưa đăng nhập)**
   - ❌ Tất cả routes redirect về `/login`
   - ✅ Chỉ có thể truy cập `/login`

---

## Liên Hệ & Hỗ Trợ

Nếu có thắc mắc về hệ thống phân quyền, vui lòng liên hệ Backend team.

**Created**: December 30, 2025  
**Last Updated**: December 30, 2025  
**Version**: 1.0
