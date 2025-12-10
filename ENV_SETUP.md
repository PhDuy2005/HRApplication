# HRApplication - Environment Setup

## Cấu hình Environment Variables

### Cách 1: Sử dụng file .env (Development)

1. Copy file `.env.example` thành `.env`:
   ```powershell
   Copy-Item .env.example .env
   ```

2. Chỉnh sửa file `.env` với thông tin thực tế của bạn

3. Chạy ứng dụng với Spring Boot Devtools (tự động load .env)

### Cách 2: Set Environment Variables trong IntelliJ IDEA

1. Run → Edit Configurations
2. Chọn application của bạn
3. Environment variables → thêm:
   - `DB_URL=jdbc:mysql://...`
   - `DB_USERNAME=admin`
   - `DB_PASSWORD=your_password`
   - `JWT_SECRET=your_secret`

### Cách 3: Set Environment Variables trong Terminal

**PowerShell:**
```powershell
$env:DB_URL="jdbc:mysql://test-graphql.c9im8egqedne.ap-southeast-2.rds.amazonaws.com:3306/do_an_se347?useSSL=true&requireSSL=false&serverTimezone=UTC"
$env:DB_USERNAME="admin"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your_secret"
./gradlew bootRun
```

### Cách 4: Deploy lên Production (AWS, Heroku, etc.)

Set environment variables trong platform của bạn:
- AWS Elastic Beanstalk: Configuration → Software → Environment properties
- Heroku: Settings → Config Vars
- Docker: `-e` flag hoặc docker-compose environment section

## Lưu ý bảo mật

- ⚠️ **KHÔNG BAO GIỜ** commit file `.env` lên Git
- ✅ File `.env.example` có thể commit (không chứa giá trị thật)
- ✅ File `application.properties` giờ an toàn để commit (chỉ chứa placeholders)
