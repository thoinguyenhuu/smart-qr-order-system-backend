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
