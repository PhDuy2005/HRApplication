# HRApplication - Human Resource Management System

## 📋 Giới thiệu

HRApplication là hệ thống quản lý nhân sự (HR Management System) được xây dựng bằng Spring Boot 3.5.8 và Java 17. Hệ thống cung cấp các chức năng quản lý nhân viên, roles, permissions và xác thực JWT.

## 🚀 Công nghệ sử dụng

### Backend Framework & Core
- **Java 17** - Language version
- **Spring Boot 3.5.8** - Application framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data access layer
- **OAuth2 Resource Server** - JWT token handling

### Database
- **MySQL** - Primary database (AWS Aurora MySQL compatible)
- **Hibernate** - ORM framework

### Security & Authentication
- **JWT (JSON Web Token)** - Access & Refresh tokens
- **BCrypt** - Password hashing
- **OAuth2 Resource Server** - Token validation

### API Documentation
- **SpringDoc OpenAPI 3** - API documentation
- **Swagger UI** - Interactive API explorer

### Utilities & Tools
- **Lombok** - Reduce boilerplate code
- **Spring Boot DevTools** - Hot reload during development
- **Spring Boot Actuator** - Monitoring & health checks
- **Thymeleaf** - Template engine
- **Jakarta Validation** - Request validation

## 📁 Cấu trúc dự án

```
src/main/java/com/se347/nhom4/HRApplication/
├── config/                     # Configuration classes
│   ├── SecurityConfiguration.java
│   ├── UserDetailsCustom.java
│   └── CustomAuthenticationEntryPoint.java
├── controller/                 # REST API Controllers
│   ├── AuthController.java
│   └── EmployeeController.java
├── domain/                     # Domain models
│   ├── table/                  # Entity classes
│   │   ├── Employee.java
│   │   ├── Role.java
│   │   └── Permission.java
│   ├── requestDTO/             # Request DTOs
│   └── responseDTO/            # Response DTOs
├── repository/                 # Data access layer
│   └── EmployeeRepository.java
├── service/                    # Business logic layer
│   └── EmployeeService.java
├── util/                       # Utility classes
│   ├── SecurityUtil.java
│   └── enums/
└── HrApplication.java          # Main application class

src/main/resources/
├── application.properties      # Application configuration
├── static/                     # Static resources
└── templates/                  # Thymeleaf templates
```

## ⚙️ Cài đặt và chạy dự án

### Yêu cầu hệ thống
- Java 17 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- Gradle 7.x (hoặc sử dụng Gradle Wrapper đi kèm)

### 1. Clone repository
```bash
git clone https://github.com/PhDuy2005/HRApplication.git
cd HRApplication
```

### 2. Cấu hình môi trường

Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```

Chỉnh sửa file `.env` với thông tin database của bạn:
```properties
DB_URL=jdbc:mysql://localhost:3306/do_an_se347?useSSL=true&requireSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_base64
```

### 3. Tạo database
```sql
CREATE DATABASE do_an_se347;
```

### 4. Chạy ứng dụng

**Sử dụng Gradle Wrapper (Windows):**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/do_an_se347"; $env:DB_USERNAME="root"; $env:DB_PASSWORD="your_password"; $env:JWT_SECRET="your_secret"; .\gradlew bootRun
```

**Sử dụng Gradle Wrapper (Linux/Mac):**
```bash
export DB_URL="jdbc:mysql://localhost:3306/do_an_se347"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export JWT_SECRET="your_secret"
./gradlew bootRun
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### 5. Truy cập API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🔐 Xác thực và phân quyền

### JWT Authentication Flow

1. **Login**: POST `/api/v1/auth/login`
   - Trả về: `access_token` và `refresh_token`
   - Access token có thời gian sống: 10 ngày (có thể cấu hình)
   - Refresh token có thời gian sống: 10 ngày

2. **API Requests**: Gửi access token trong header
   ```
   Authorization: Bearer <access_token>
   ```

3. **Refresh Token**: POST `/api/v1/auth/refresh`
   - Khi access token hết hạn, sử dụng refresh token để lấy token mới

4. **Logout**: POST `/api/v1/auth/logout`
   - Xóa refresh token khỏi database

## 📡 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Đăng ký tài khoản mới
- `POST /api/v1/auth/login` - Đăng nhập
- `POST /api/v1/auth/refresh` - Làm mới access token
- `POST /api/v1/auth/logout` - Đăng xuất

### Employee Management
- `GET /api/v1/employees` - Lấy danh sách nhân viên
- `GET /api/v1/employees/{id}` - Lấy thông tin nhân viên theo ID
- `POST /api/v1/employees` - Tạo nhân viên mới
- `PUT /api/v1/employees/{id}` - Cập nhật thông tin nhân viên
- `DELETE /api/v1/employees/{id}` - Xóa nhân viên

### Actuator (Monitoring)
- `GET /actuator/health` - Kiểm tra health của ứng dụng
- `GET /actuator/info` - Thông tin ứng dụng
- `GET /actuator/metrics` - Metrics
- `GET /actuator/env` - Environment variables (khi authorized)

## 🗄️ Database Schema

### Employees Table
- `id` (BIGINT, PK, Auto Increment)
- `fullname` (VARCHAR)
- `email` (VARCHAR, UNIQUE)
- `password` (VARCHAR, Hashed)
- `phone` (VARCHAR)
- `hired_date` (TIMESTAMP)
- `status` (ENUM)
- `role_id` (BIGINT, FK to Roles)
- `refresh_token` (TEXT)

### Roles Table
- `id` (BIGINT, PK, Auto Increment)
- `name` (VARCHAR, UNIQUE)
- `description` (TEXT)
- `active` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `created_by` (VARCHAR)
- `updated_by` (VARCHAR)

### Permissions Table
- `id` (BIGINT, PK, Auto Increment)
- `name` (VARCHAR, UNIQUE)
- `api_path` (VARCHAR)
- `method` (VARCHAR)
- `module` (VARCHAR)
- Many-to-Many relationship with Roles

## 🔧 Configuration

Các cấu hình chính trong `application.properties`:

```properties
# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
se347.jwt.base64-secret=${JWT_SECRET}
se347.jwt.access-token-validity-in-seconds=864000
se347.jwt.refresh-token-validity-in-seconds=864000

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Pagination
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=2000
```

## 🛠️ Development

### Build project
```bash
./gradlew build
```

### Run tests
```bash
./gradlew test
```

### Generate JAR file
```bash
./gradlew bootJar
```

JAR file sẽ được tạo tại: `build/libs/HRApplication-0.0.1-SNAPSHOT.jar`

## 📝 Environment Variables

| Variable      | Description             | Default                                   |
| ------------- | ----------------------- | ----------------------------------------- |
| `DB_URL`      | Database connection URL | `jdbc:mysql://localhost:3306/do_an_se347` |
| `DB_USERNAME` | Database username       | `root`                                    |
| `DB_PASSWORD` | Database password       | `password`                                |
| `JWT_SECRET`  | JWT secret key (Base64) | `changeme`                                |

## 🔒 Bảo mật

### Best Practices
- ✅ Passwords được hash bằng BCrypt
- ✅ JWT tokens cho stateless authentication
- ✅ Environment variables cho sensitive data
- ✅ CORS configuration
- ✅ CSRF protection (disabled cho REST API)
- ✅ SQL injection prevention qua JPA

### Production Checklist
- [ ] Thay đổi `JWT_SECRET` sang giá trị mạnh và bảo mật
- [ ] Cấu hình CORS cho phép chỉ trusted domains
- [ ] Enable HTTPS
- [ ] Giảm thời gian sống của access token (khuyến nghị: 15 phút)
- [ ] Cấu hình rate limiting
- [ ] Enable security headers
- [ ] Sử dụng secrets management service (AWS Secrets Manager, Azure Key Vault)

## 👥 Team

- **Nhóm 4 - SE347**
- **Owner**: [PhDuy2005](https://github.com/PhDuy2005)

## 📄 License

This project is for educational purposes (SE347 - Web Application Development).

## 📞 Support

Nếu có vấn đề hoặc câu hỏi, vui lòng tạo issue trên GitHub repository.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: December 2025
