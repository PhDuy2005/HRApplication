# Development Guidelines & Instructions

## 📋 Table of Contents
- [Language & Documentation](#language--documentation)
- [Code Conventions](#code-conventions)
- [REST API Format](#rest-api-format)
- [Global Error Handling](#global-error-handling)
- [Database Guidelines](#database-guidelines)
- [Git Workflow](#git-workflow)
- [Testing Standards](#testing-standards)

---

## 🌐 Language & Documentation

### Documentation Language
- **Code**: Tiếng Anh (English) - Class names, methods, variables, comments
- **Documentation**: Tiếng Việt - README, INSTRUCTIONS, Javadoc descriptions
- **Commit Messages**: Tiếng Anh (English)
- **PR/Issue Descriptions**: Tiếng Việt
- **Code Reviews**: Tiếng Việt
- **API Documentation**: Tiếng Việt (Swagger/OpenAPI descriptions)

### Yêu cầu bắt buộc
✅ **Code và technical terms PHẢI viết bằng tiếng Anh**
```java
// ✅ Đúng
public class EmployeeService {
    public Employee findById(Long id) { }
}

// ❌ Sai
public class DichVuNhanVien {
    public NhanVien timTheoId(Long id) { }
}
```

✅ **Comments và documentation PHẢI viết bằng tiếng Việt**
```java
// ✅ Đúng
/**
 * Tìm nhân viên theo ID.
 * 
 * @param id ID của nhân viên cần tìm.
 * @return Optional chứa nhân viên nếu tìm thấy.
 */
public Optional<Employee> findById(Long id) {
    return employeeRepository.findById(id);
}

// ❌ Sai - dùng tiếng Anh cho documentation
/**
 * Find employee by ID.
 * 
 * @param id the employee ID to search for.
 * @return Optional containing the employee if found.
 */
```

✅ **Tất cả response messages, error messages trả về API PHẢI bằng tiếng Việt**
```java
// ✅ Đúng
throw new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id);

// ❌ Sai
throw new ResourceNotFoundException("Employee not found with id: " + id);
```

---

## 🎨 Code Conventions

### Java Naming Conventions

#### Classes
- **PascalCase** for class names
- Descriptive and singular nouns
```java
// ✅ Good
public class Employee { }
public class UserDetailsCustom { }

// ❌ Bad
public class employee { }
public class Users { }
```

#### Methods
- **camelCase** for method names
- Start with verb
- Clear and descriptive
```java
// ✅ Good
public Employee findById(Long id) { }
public void updateUserRefreshToken(String token, String email) { }

// ❌ Bad
public Employee FindById(Long id) { }
public void update_user_refresh_token(String token, String email) { }
```

#### Variables
- **camelCase** for variable names
- Meaningful names
```java
// ✅ Good
private String refreshToken;
private final EmployeeRepository employeeRepository;

// ❌ Bad
private String rt;
private final EmployeeRepository emp_repo;
```

#### Constants
- **UPPER_SNAKE_CASE** for constants
```java
// ✅ Good
public static final int MAX_PAGE_SIZE = 2000;
public static final String JWT_ALGORITHM = "HS512";

// ❌ Bad
public static final int maxPageSize = 2000;
```

#### Packages
- **lowercase** only
- Use reverse domain naming
```
com.se347.nhom4.HRApplication.controller
com.se347.nhom4.HRApplication.service
com.se347.nhom4.HRApplication.repository
```

### Code Structure

#### Controller Layer
```java
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }
}
```

**Rules:**
- Always use `@RestController` for REST APIs
- Use `@RequestMapping` for base path
- Use `@RequiredArgsConstructor` from Lombok for dependency injection
- Return `ResponseEntity<T>` for all endpoints
- Keep controllers thin - delegate logic to services

#### Service Layer
```java
@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Find employee by ID.
     * 
     * @param id the employee ID to search for.
     * @return Optional containing the employee if found.
     */
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }
}
```

**Rules:**
- Always use `@Service` annotation
- Add Javadoc for public methods
- Handle business logic here
- Use repositories for data access only

#### Repository Layer
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Employee findByEmailAndRefreshToken(String email, String refreshToken);
}
```

**Rules:**
- Always use `@Repository` annotation
- Extend `JpaRepository<Entity, ID>`
- Use Spring Data JPA query methods
- No implementation needed for standard CRUD

#### Entity/Domain Layer
```java
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
```

**Rules:**
- Always use `@Entity` and `@Table` annotations
- Use Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Specify column constraints with `@Column`
- Use proper relationship annotations: `@ManyToOne`, `@OneToMany`, etc.
- Table names should be plural (employees, roles, permissions)

### Javadoc Comments

Required for:
- All public methods in Services
- All Controllers endpoints
- Complex business logic

```java
/**
 * Update user's refresh token.
 * 
 * @param token the new refresh token.
 * @param email the user's email.
 * @throws NoSuchElementException if user not found.
 */
public void updateUserRefreshToken(String token, String email) {
    // Implementation
}
```

### Code Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Braces**: Always use, even for single-line blocks
- **Blank lines**: One blank line between methods

---

## 🌐 REST API Format

### URL Structure

```
https://domain.com/api/{version}/{resource}
```

**Example:**
```
https://localhost:8080/api/v1/employees
https://localhost:8080/api/v1/auth/login
```

### HTTP Methods

| Method | Purpose                | Idempotent |
| ------ | ---------------------- | ---------- |
| GET    | Retrieve resource(s)   | ✅ Yes      |
| POST   | Create new resource    | ❌ No       |
| PUT    | Update entire resource | ✅ Yes      |
| PATCH  | Partial update         | ❌ No       |
| DELETE | Remove resource        | ✅ Yes      |

### Request Format

#### Headers
```
Content-Type: application/json
Authorization: Bearer {access_token}
Accept: application/json
```

#### Request Body (POST/PUT)
```json
{
  "fullname": "Nguyen Van A",
  "email": "nguyenvana@example.com",
  "phone": "0123456789"
}
```

### Response Format

#### Success Response

**Single Resource:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "fullname": "Nguyen Van A",
    "email": "nguyenvana@example.com",
    "phone": "0123456789"
  }
}
```

**List Resources (with pagination):**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "fullname": "Nguyen Van A"
      },
      {
        "id": 2,
        "fullname": "Tran Thi B"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

#### Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ],
  "timestamp": "2025-12-19T10:30:00Z",
  "path": "/api/v1/employees"
}
```

### Status Codes

| Code | Meaning               | Usage                          |
| ---- | --------------------- | ------------------------------ |
| 200  | OK                    | Successful GET, PUT            |
| 201  | Created               | Successful POST                |
| 204  | No Content            | Successful DELETE              |
| 400  | Bad Request           | Invalid request data           |
| 401  | Unauthorized          | Missing/invalid authentication |
| 403  | Forbidden             | Insufficient permissions       |
| 404  | Not Found             | Resource not found             |
| 409  | Conflict              | Resource already exists        |
| 500  | Internal Server Error | Server error                   |

### Endpoint Examples

#### GET - Retrieve all (with pagination)
```
GET /api/v1/employees?page=0&size=20&sort=fullname,asc
```

#### GET - Retrieve by ID
```
GET /api/v1/employees/1
```

#### POST - Create new
```
POST /api/v1/employees
Content-Type: application/json

{
  "fullname": "Nguyen Van A",
  "email": "nguyenvana@example.com",
  "phone": "0123456789"
}
```

#### PUT - Update entire resource
```
PUT /api/v1/employees/1
Content-Type: application/json

{
  "fullname": "Nguyen Van A Updated",
  "email": "updated@example.com",
  "phone": "0987654321"
}
```

#### DELETE - Remove resource
```
DELETE /api/v1/employees/1
```

### Authentication Endpoints

#### Login
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Refresh Token
```
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Logout
```
POST /api/v1/auth/logout
Authorization: Bearer {access_token}
```

---

## 🚨 Global Error Handling

### Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException (404)
├── BadRequestException (400)
├── UnauthorizedException (401)
├── ForbiddenException (403)
└── ConflictException (409)
```

### Custom Exception Classes

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

Create `GlobalExceptionHandler.java`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
                
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                    error.getField(),
                    error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
                
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
                
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### Error Response DTO

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private List<FieldError> errors;
    private LocalDateTime timestamp;
    private String path;
}

@Data
@AllArgsConstructor
public class FieldError {
    private String field;
    private String message;
}
```

### Usage in Service Layer

```java
@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + id
                ));
    }
    
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new ConflictException(
                "Employee with email " + employee.getEmail() + " already exists"
            );
        }
        return employeeRepository.save(employee);
    }
}
```

### Exception to Status Code Mapping

| Exception                         | HTTP Status           | Code |
| --------------------------------- | --------------------- | ---- |
| `ResourceNotFoundException`       | Not Found             | 404  |
| `BadRequestException`             | Bad Request           | 400  |
| `IllegalArgumentException`        | Bad Request           | 400  |
| `UnauthorizedException`           | Unauthorized          | 401  |
| `UsernameNotFoundException`       | Unauthorized          | 401  |
| `ForbiddenException`              | Forbidden             | 403  |
| `AccessDeniedException`           | Forbidden             | 403  |
| `ConflictException`               | Conflict              | 409  |
| `MethodArgumentNotValidException` | Bad Request           | 400  |
| `Exception`                       | Internal Server Error | 500  |

---

## 💾 Database Guidelines

### Naming Conventions

- **Tables**: plural, snake_case (e.g., `employees`, `user_roles`)
- **Columns**: singular, snake_case (e.g., `full_name`, `created_at`)
- **Foreign Keys**: `{referenced_table}_id` (e.g., `role_id`, `employee_id`)
- **Junction Tables**: `{table1}_{table2}` (e.g., `role_permissions`)

### Entity Relationships

- Always specify `@JoinColumn` name explicitly
- Use `FetchType.LAZY` for better performance
- Avoid bidirectional relationships unless necessary
- Use `@JsonIgnore` or `@JsonIgnoreProperties` to prevent circular references

### Timestamps

Include audit fields in all tables:
```java
@Column(name = "created_at")
private Instant createdAt;

@Column(name = "updated_at")
private Instant updatedAt;

@Column(name = "created_by")
private String createdBy;

@Column(name = "updated_by")
private String updatedBy;

@PrePersist
protected void onCreate() {
    createdAt = Instant.now();
    createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
}

@PreUpdate
protected void onUpdate() {
    updatedAt = Instant.now();
    updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
}
```

---

## 🔀 Git Workflow

### Branch Naming

- `main` - Production-ready code
- `develop` - Integration branch
- `feature/{feature-name}` - New features
- `bugfix/{bug-name}` - Bug fixes
- `hotfix/{issue-name}` - Critical production fixes

**Examples:**
```
feature/employee-crud
bugfix/fix-login-error
hotfix/security-patch
```

### Commit Messages

Format: `<type>: <description>`

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**
```
feat: add employee CRUD endpoints
fix: resolve JWT token validation error
docs: update API documentation
refactor: improve service layer structure
```

### Pull Request Guidelines

1. Create feature branch from `develop`
2. Make changes and commit
3. Write clear PR description
4. Request review from team members
5. Address review comments
6. Merge after approval

---

## 🧪 Testing Standards

### Test Structure

```java
@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceTest {
    
    @Autowired
    private EmployeeService employeeService;
    
    @MockBean
    private EmployeeRepository employeeRepository;
    
    @Test
    @DisplayName("Should find employee by ID successfully")
    void testFindById_Success() {
        // Given
        Long employeeId = 1L;
        Employee expected = Employee.builder()
                .id(employeeId)
                .fullname("Test User")
                .email("test@example.com")
                .build();
        
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(expected));
        
        // When
        Optional<Employee> result = employeeService.findById(employeeId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expected.getId(), result.get().getId());
        verify(employeeRepository).findById(employeeId);
    }
}
```

### Test Coverage Goals

- **Unit Tests**: 80% coverage minimum
- **Integration Tests**: Key business flows
- **Controller Tests**: All endpoints

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [REST API Best Practices](https://restfulapi.net/)
- [Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)

---

**Last Updated**: December 2025  
**Maintained by**: SE347 - Nhóm 4
