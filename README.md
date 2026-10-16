# Smart QR Backend

Backend cho hệ thống quản lý menu QR và đặt món theo nhà hàng. Ứng dụng được xây bằng Spring Boot, dùng PostgreSQL, JWT, WebSocket STOMP và hỗ trợ import menu từ ảnh bằng Gemini.

## Chức năng chính

- Xác thực chủ nhà hàng: đăng ký, xác minh email, đăng nhập, refresh token, logout.
- Quản lý nhà hàng, menu, danh mục món, món ăn, bàn và mã QR.
- Tạo và theo dõi đơn hàng theo từng bàn.
- Gửi cập nhật đơn hàng realtime qua WebSocket.
- Import menu từ ảnh.
- Tài liệu API chi tiết nằm trong `docs.md`.

## Stack

- Java 17
- Spring Boot 3.5
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL 15
- Spring Mail / SendGrid
- STOMP over WebSocket
- Maven

## Cấu trúc thư mục

```text
backend/
|-- src/main/java/...         # source code chinh
|-- src/main/resources/       # application.yaml va tai nguyen runtime
|-- src/test/java/...         # test
|-- Dockerfile
|-- docker-compose.yml
|-- docker-compose.example.yml
|-- .env.example
|-- docs.md
`-- pom.xml
```

## Yêu cầu môi trường

- JDK 17
- Maven 3.9+ hoặc dùng `./mvnw`
- PostgreSQL 15+
- Tài khoản SMTP Gmail hoặc SendGrid nếu dùng gửi mail
- Gemini API key nếu dùng import menu từ ảnh

## Biến môi trường

Sao chép `.env.example` thành `.env` rồi điền giá trị thật.

| Biến | Bắt buộc | Mô tả |
| --- | --- | --- |
| `DB_URL` | Có | JDBC URL tới PostgreSQL |
| `DB_USERNAME` | Có | Tài khoản database |
| `DB_PASSWORD` | Có | Mật khẩu database |
| `EMAIL_USERNAME` | Có nếu dùng Gmail SMTP | Email gửi xác minh |
| `EMAIL_PASSWORD` | Có nếu dùng Gmail SMTP | App password của Gmail |
| `APP_JWT_ACCESS_KEY` | Có | Secret ký access token |
| `APP_JWT_REFRESH_KEY` | Có | Secret ký refresh token |
| `APP_REGISTER_KEY` | Có | Secret ký token xác minh đăng ký |
| `APP_FRONTEND_URL` | Có | URL frontend, dùng để tạo link xác minh và QR base URL |
| `GEMINI_API_KEY` | Nên có | API key cho tính năng import menu từ ảnh |
| `GOOGLE_API_KEY` | Không | Tên biến cũ, vẫn được hỗ trợ làm fallback |
| `SENDGRID_API_KEY` | Không | API key SendGrid nếu dùng SendGrid |
| `SENDGRID_FROM_EMAIL` | Không | Email gửi từ SendGrid |
| `SENDGRID_FROM_NAME` | Không | Tên hiển thị người gửi |

## Chạy local

1. Tạo file môi trường:

```bash
cp .env.example .env
```

PowerShell:

```powershell
Copy-Item .env.example .env
```

2. Cập nhật giá trị trong `.env`.

3. Chạy PostgreSQL cục bộ hoặc bằng Docker.

4. Chạy ứng dụng:

```bash
./mvnw spring-boot:run
```

PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Ứng dụng mặc định chạy tại `http://localhost:8080/api`.

## Chạy bằng Docker Compose

Repo có 2 file compose:

- `docker-compose.yml`: file local với giá trị thực.
- `docker-compose.example.yml`: file mẫu để tham khảo hoặc chia sẻ.

```bash
docker compose up --build
```

Compose sẽ:

- chạy PostgreSQL 15
- build backend từ `Dockerfile`
- publish backend ra cổng `8080` hoặc giá trị `BACKEND_PORT`

Nếu muốn dùng file mẫu:

```bash
docker compose -f docker-compose.example.yml up --build
```

Lưu ý: khi chạy bằng Compose, backend nên kết nối tới service `db`, không phải `localhost`.

## API và realtime

- Base path REST: `/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`
- WebSocket endpoint: `/api/ws`
- Topic đơn hàng theo nhà hàng: `/topic/orders/{restaurantId}`

Các nhóm API chính:

- `auth`
- `restaurants`
- `categories`
- `dishes`
- `tables`
- `orders`
- `users`
- `menu/import-from-image`

## Lệnh thường dùng

```bash
./mvnw test
./mvnw clean package
```

PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## CI/CD

`.github/workflows/ci-cd.yml` chạy Maven test và package cho push/pull request. Push lên nhánh mặc định hoặc chạy thủ công trên nhánh đó sẽ build cùng image với hai tag `${{ github.sha }}` và `latest`, đẩy Docker Hub và triển khai VPS qua SSH bằng `IMAGE_TAG` là full commit SHA. Test lỗi thì không deploy.

GitHub **Settings → Secrets and variables → Actions** cần các repository secrets:

- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`: tài khoản có quyền push `thoinguyenhuu/smart-qr-backend`.
- `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`: thông tin SSH; `VPS_PORT` tùy chọn, mặc định `22`.
- `VPS_DEPLOY_PATH`: đường dẫn tuyệt đối tới thư mục chứa `docker-compose.yml` và `.env` trên VPS.

VPS cần Linux x86_64, Docker Compose hỗ trợ `--wait`, PostgreSQL đã chạy và file Compose có dòng image `thoinguyenhuu/smart-qr-backend:${IMAGE_TAG:-latest}` như repo. Nếu image private, đăng nhập Docker Hub trên VPS trước. Workflow chỉ cập nhật backend bằng `--no-deps --no-build`, không xóa volume database. Không đồng bộ `.env` từ GitHub; giữ cấu hình production trên VPS. `--wait` kiểm tra container chạy, chưa kiểm tra HTTP.

Đã bỏ test `ApplicationTests.contextLoads` rỗng; CI giữ test hành vi `UserServiceTest`, không cần database hoặc tài khoản mail thật.

Kiểm tra commit đang chạy: `docker inspect --format '{{.Config.Image}}' smartqr-backend`.

Rollback trong thư mục triển khai trên VPS:

```bash
export IMAGE_TAG=<full-commit-sha-cu>
docker compose pull backend
docker compose up -d --no-deps --no-build --wait --wait-timeout 180 backend
```

`IMAGE_TAG` không được lưu tự động vào `.env`; khi thao tác thủ công cần truyền tag hoặc lưu tag mong muốn vào `.env` để tránh dùng mặc định `latest`. Giữ image SHA cũ trên Docker Hub để rollback. Thời gian rollback phụ thuộc tải image và khởi động; rollback image không hoàn tác thay đổi schema database.
