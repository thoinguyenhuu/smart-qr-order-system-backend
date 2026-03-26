# API Handoff: Smart QR Menu & Order System Backend

## Common API Conventions

- Base URL: `/api`
- All REST endpoints return JSON in this wrapper format, except the QR image endpoint:

```json
{
  "success": 1,
  "data": {},
  "message": "optional"
}
```

- Error format:

```json
{
  "success": 0,
  "message": "Human-readable error message"
}
```

- Authentication header for protected endpoints:

```http
Authorization: Bearer <access_token>
```

- Role model used by backend:
  - `OWNER`: restaurant owner, full management access
  - `CASHIER`: restaurant staff, can view restaurant data and manage order status
  - `CUSTOMER`: no backend role object; customer actions are public endpoints without JWT
- Tenant model:
  - Most endpoints use `restaurantId` in the URL
  - `restaurantId` must be a valid UUID
  - Frontend should always use the exact `restaurantId` returned by backend
- Sorting:
  - Categories: `displayOrder ASC`
  - Dishes: `name ASC`
  - Tables: `tableNumber ASC`
  - Orders: `createdAt DESC`
- Time format: ISO-8601 UTC instant, for example `2026-03-24T10:15:30.000Z`
- Money format: JSON number, mapped from Java `BigDecimal`
- QR endpoint returns raw `image/png`, not `ApiResult`

---

## Feature: Authentication & Account Activation

### 1. Business Context

- Handles owner registration, email verification, login, token refresh, and logout.
- Used by: `OWNER` during sign-up and login; `CASHIER` during login; frontend auth layer for token refresh.
- Key business rules:
  - Registration always creates an `OWNER` account.
  - New owner accounts are created with `active = false`.
  - Login is blocked until email verification is completed.
  - Refresh token returns a new access token and a new refresh token.
  - Logout is stateless: backend does not revoke tokens.

---

### 2. Authentication

- Auth required: No
- Header format: Not required
- Role required:
  - Register/Login/Refresh/Verify/Logout: public
- Special rules:
  - Access token TTL: 1 day
  - Refresh token TTL: 2 weeks
  - Registration verify token TTL: 1 day
  - Logout only means frontend should clear tokens locally

---

### 3. Endpoints

#### Register Owner

**Method:** `POST`  
**URL:** `/auth/register`

**Description:**  
Creates a new owner account, stores it as inactive, and sends a verification email.

**Request:**

```json
{
  "email": "owner@example.com",
  "password": "secret123",
  "phone": "0901234567",
  "fullName": "Nguyen Van A"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Please check your email to verify!!!",
  "data": {
    "id": "c41d0d88-caf4-4b29-9e5c-29fe488c81f1",
    "fullName": "Nguyen Van A",
    "phone": "0901234567",
    "email": "owner@example.com",
    "role": "OWNER",
    "active": false
  }
}
```

**Error Cases:**

- `400`: invalid email, password shorter than 6, full name missing
- `400`: email already exists

---

#### Verify Account

**Method:** `GET`  
**URL:** `/auth/verify?token=<register_token>`

**Description:**  
Activates a newly registered owner account so it can log in.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "c41d0d88-caf4-4b29-9e5c-29fe488c81f1",
    "fullName": "Nguyen Van A",
    "phone": "0901234567",
    "email": "owner@example.com",
    "role": "OWNER",
    "active": true
  }
}
```

**Error Cases:**

- `401`: token invalid or expired
- `404`: user not found

---

#### Login

**Method:** `POST`  
**URL:** `/auth/login`

**Description:**  
Authenticates owner or cashier and returns a JWT pair for frontend session management.

**Request:**

```json
{
  "email": "owner@example.com",
  "password": "secret123"
}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.access",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.refresh"
  }
}
```

**Error Cases:**

- `400`: invalid email format or password missing
- `401`: invalid email or password
- `403`: user is not active

---

#### Refresh Token

**Method:** `POST`  
**URL:** `/auth/refresh`

**Description:**  
Exchanges a valid refresh token for a new access token and a new refresh token.

**Request:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.refresh"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.new_access",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.new_refresh"
  }
}
```

**Error Cases:**

- `400`: refresh token missing
- `401`: token invalid or expired
- `403`: user is not active
- `404`: user not found

---

#### Logout

**Method:** `POST`  
**URL:** `/auth/logout`

**Description:**  
Returns success so frontend can clear local tokens. Backend does not blacklist or revoke JWTs.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "message": "Logout successfully"
}
```

**Error Cases:**

- No business validation is performed by backend

---

### 4. Data Models (DTO)

#### CreateUserRequest

```json
{
  "email": "owner@example.com",
  "password": "secret123",
  "phone": "0901234567",
  "fullName": "Nguyen Van A"
}
```

Explain each field:

- `email`: login email, must be unique
- `password`: raw password, backend hashes before saving
- `phone`: optional phone number
- `fullName`: display name of owner or cashier

#### LoginRequest

```json
{
  "email": "owner@example.com",
  "password": "secret123"
}
```

Explain each field:

- `email`: account email
- `password`: raw password

#### RefreshTokenRequest

```json
{
  "refreshToken": "jwt_refresh_token"
}
```

Explain each field:

- `refreshToken`: JWT refresh token returned from login/refresh

#### LoginResponse

```json
{
  "accessToken": "jwt_access_token",
  "refreshToken": "jwt_refresh_token"
}
```

Explain each field:

- `accessToken`: send in `Authorization` header for protected APIs
- `refreshToken`: store securely and use to get a new token pair

#### UserResponse

```json
{
  "id": "c41d0d88-caf4-4b29-9e5c-29fe488c81f1",
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "email": "owner@example.com",
  "role": "OWNER",
  "active": true
}
```

Explain each field:

- `id`: user UUID
- `fullName`: display name
- `phone`: optional phone
- `email`: unique login email
- `role`: `OWNER` or `CASHIER`
- `active`: account activation flag

---

### 5. Business Logic Flow

1. Frontend registers owner with email/password/full name.
2. Backend saves owner as inactive and sends verification email.
3. User opens verification link and frontend calls `/auth/verify`.
4. User logs in and receives access/refresh tokens.
5. Frontend stores tokens and attaches the access token to protected requests.
6. When access token expires, frontend calls `/auth/refresh`.
7. On logout, frontend must clear tokens locally.

---

### 6. Validation Rules

- `email` is required and must be valid
- `email` must be unique
- `password` is required and length must be `>= 6`
- `fullName` is required for registration
- `refreshToken` is required
- inactive users cannot log in or refresh

---

### 7. Edge Cases

- duplicate email on register
- login before email verification
- expired verify token
- expired refresh token
- logout does not invalidate existing access token on server side

---

### 8. Frontend Notes (VERY IMPORTANT)

- Always store both `accessToken` and `refreshToken`.
- Implement automatic refresh on `401` only for protected endpoints.
- After register, show a “Check your email” screen. Do not auto-login.
- After verify success, redirect user to login.
- Logout must remove tokens from frontend state/storage immediately.
- Do not assume backend keeps a session.

---

### 9. Example Flow (End-to-End)

1. User opens sign-up page.
2. Frontend calls `POST /api/auth/register`.
3. User clicks email verification link.
4. Frontend calls `GET /api/auth/verify?token=...`.
5. Frontend sends `POST /api/auth/login`.
6. Frontend stores returned tokens.
7. Frontend uses access token to create restaurant and manage data.

---

### 10. Important Notes / Gotchas

- Backend login works for both owner and cashier.
- Backend does not expose a “current user profile” endpoint.
- Backend does not return token expiry timestamps; frontend should handle refresh reactively.
- Verify token is a special token type; do not use access/refresh token for `/auth/verify`.

---

## Feature: Restaurant Management & Public Menu

### 1. Business Context

- Handles restaurant creation, restaurant profile retrieval/update, and public menu retrieval.
- Used by:
  - `OWNER`: create and update restaurant
  - `OWNER`, `CASHIER`: view assigned restaurants
  - `CUSTOMER`: public menu view
- Key business rules:
  - Only `OWNER` can create restaurant.
  - Restaurant creator is automatically linked to that restaurant.
  - Public menu only returns dishes where `available = true`.
  - Menu categories are returned even if a category has zero available dishes.

---

### 2. Authentication

- Auth required:
  - `POST /restaurants`: yes
  - `GET /restaurants`: yes
  - `GET /restaurants/{restaurantId}`: yes
  - `PUT /restaurants/{restaurantId}`: yes
  - `GET /restaurants/{restaurantId}/menu`: no
- Header format: `Authorization: Bearer <access_token>` for protected routes
- Role required:
  - create/update: `OWNER`
  - get list/get one: any assigned restaurant member (`OWNER` or `CASHIER`)
  - public menu: customer/public
- Special rules:
  - `restaurantId` must be a valid UUID
  - access to a specific restaurant is based on restaurant membership

---

### 3. Endpoints

#### Create Restaurant

**Method:** `POST`  
**URL:** `/restaurants`

**Description:**  
Creates a new restaurant owned by the current owner and links the owner to that restaurant.

**Request:**

```json
{
  "name": "Smart BBQ",
  "address": "123 Nguyen Trai, District 1"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Restaurant created successfully",
  "data": {
    "id": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Smart BBQ",
    "address": "123 Nguyen Trai, District 1"
  }
}
```

**Error Cases:**

- `400`: name or address missing
- `401`: unauthenticated
- `403`: current user is not owner
- `404`: user not found

---

#### Get My Restaurants

**Method:** `GET`  
**URL:** `/restaurants`

**Description:**  
Returns all restaurants assigned to the current user.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "2b7f540f-7904-45cf-b91a-319196d5cc11",
      "name": "Smart BBQ",
      "address": "123 Nguyen Trai, District 1"
    }
  ]
}
```

**Error Cases:**

- `401`: unauthenticated

---

#### Get Restaurant Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}`

**Description:**  
Returns one restaurant profile for the current member.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Smart BBQ",
    "address": "123 Nguyen Trai, District 1"
  }
}
```

**Error Cases:**

- `400`: invalid restaurant UUID
- `401`: unauthenticated
- `403`: user is not assigned to this restaurant
- `404`: restaurant not found

---

#### Update Restaurant

**Method:** `PUT`  
**URL:** `/restaurants/{restaurantId}`

**Description:**  
Updates restaurant name and address.

**Request:**

```json
{
  "name": "Smart BBQ Premium",
  "address": "456 Tran Hung Dao, District 1"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Restaurant updated successfully",
  "data": {
    "id": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Smart BBQ Premium",
    "address": "456 Tran Hung Dao, District 1"
  }
}
```

**Error Cases:**

- `400`: invalid UUID, name missing, address missing
- `401`: unauthenticated
- `403`: only owner can update
- `404`: restaurant not found

---

#### Get Public Menu

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/menu`

**Description:**  
Returns customer-facing menu data grouped by category.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "restaurantName": "Smart BBQ",
    "categories": [
      {
        "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
        "name": "Main Dish",
        "displayOrder": 1,
        "dishes": [
          {
            "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
            "name": "Beef Combo",
            "description": "Premium beef set",
            "price": 199000,
            "imageUrl": "https://cdn.example.com/beef.jpg",
            "available": true
          }
        ]
      }
    ]
  }
}
```

**Error Cases:**

- `400`: invalid restaurant UUID
- `404`: restaurant not found

---

### 4. Data Models (DTO)

#### RestaurantResponse

```json
{
  "id": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "name": "Smart BBQ",
  "address": "123 Nguyen Trai, District 1"
}
```

Explain each field:

- `id`: restaurant UUID
- `name`: restaurant display name
- `address`: restaurant address

#### MenuResponse

```json
{
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "restaurantName": "Smart BBQ",
  "categories": []
}
```

Explain each field:

- `restaurantId`: restaurant UUID
- `restaurantName`: restaurant display name for customer view
- `categories`: ordered menu categories

#### MenuCategoryResponse

```json
{
  "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
  "name": "Main Dish",
  "displayOrder": 1,
  "dishes": []
}
```

Explain each field:

- `id`: category UUID
- `name`: category name
- `displayOrder`: display ordering number
- `dishes`: only available dishes in this category

#### MenuDishResponse

```json
{
  "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
  "name": "Beef Combo",
  "description": "Premium beef set",
  "price": 199000,
  "imageUrl": "https://cdn.example.com/beef.jpg",
  "available": true
}
```

Explain each field:

- `id`: dish UUID
- `name`: dish name
- `description`: optional customer-facing text
- `price`: selling price
- `imageUrl`: optional image URL
- `available`: true if dish can currently be ordered

---

### 5. Business Logic Flow

1. Owner logs in.
2. Owner creates restaurant.
3. Backend saves restaurant and auto-links owner to restaurant membership.
4. Frontend fetches restaurant list after login.
5. Customer-facing app uses restaurant ID to fetch public menu.

---

### 6. Validation Rules

- restaurant `name` is required
- restaurant `address` is required
- only owner can create and update restaurants
- public menu only includes available dishes

---

### 7. Edge Cases

- cashier token used for create/update restaurant
- restaurant exists but current user is not a member
- public menu for restaurant with no categories
- public menu for category with no available dishes

---

### 8. Frontend Notes (VERY IMPORTANT)

- Fetch `/restaurants` immediately after login to determine available workspaces.
- Cache restaurant list per logged-in user.
- Refetch restaurant detail after update.
- Public customer UI should call `/restaurants/{restaurantId}/menu` separately from table lookup.
- There is no delete restaurant endpoint in current backend.

---

### 9. Example Flow (End-to-End)

1. Owner logs in.
2. Frontend calls `POST /api/restaurants`.
3. Frontend redirects to restaurant dashboard.
4. Dashboard calls `GET /api/restaurants/{restaurantId}`.
5. Customer side calls `GET /api/restaurants/{restaurantId}/menu`.

---

### 10. Important Notes / Gotchas

- `GET /restaurants` returns assigned restaurants, not only owned restaurants.
- Public menu route does not require JWT.
- Menu response does not include hidden/unavailable dishes.

---

## Feature: Category Management

### 1. Business Context

- Categories group dishes inside one restaurant menu.
- Used by:
  - `OWNER`: create, update, delete categories
  - `OWNER`, `CASHIER`: view categories
- Key business rules:
  - Category belongs to one restaurant only.
  - Ordering is controlled by `displayOrder`.
  - Read access is member-level; write access is owner-level.

---

### 2. Authentication

- Auth required: Yes
- Header format: `Authorization: Bearer <access_token>`
- Role required:
  - create/update/delete: `OWNER`
  - get list/get detail: assigned member (`OWNER` or `CASHIER`)
- Special rules:
  - `restaurantId` and `categoryId` are UUID strings

---

### 3. Endpoints

#### Create Category

**Method:** `POST`  
**URL:** `/restaurants/{restaurantId}/categories`

**Description:**  
Creates a new menu category in the target restaurant.

**Request:**

```json
{
  "name": "Main Dish",
  "displayOrder": 1
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Category created successfully",
  "data": {
    "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Main Dish",
    "displayOrder": 1
  }
}
```

**Error Cases:**

- `400`: invalid UUID, name missing, display order < 0
- `401`: unauthenticated
- `403`: only owner can create
- `404`: restaurant not found

---

#### Get Categories

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/categories`

**Description:**  
Returns all categories of the restaurant ordered by `displayOrder ASC`.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
      "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
      "name": "Main Dish",
      "displayOrder": 1
    }
  ]
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member

---

#### Get Category Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/categories/{categoryId}`

**Description:**  
Returns one category for editing or menu setup screens.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Main Dish",
    "displayOrder": 1
  }
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: category not found

---

#### Update Category

**Method:** `PUT`  
**URL:** `/restaurants/{restaurantId}/categories/{categoryId}`

**Description:**  
Updates category name and display order.

**Request:**

```json
{
  "name": "Signature Main Dish",
  "displayOrder": 2
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Category updated successfully",
  "data": {
    "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "name": "Signature Main Dish",
    "displayOrder": 2
  }
}
```

**Error Cases:**

- `400`: invalid UUID, invalid display order, name missing
- `401`: unauthenticated
- `403`: only owner can update
- `404`: category not found

---

#### Delete Category

**Method:** `DELETE`  
**URL:** `/restaurants/{restaurantId}/categories/{categoryId}`

**Description:**  
Deletes a category from the restaurant.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "message": "Category deleted successfully"
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: only owner can delete
- `404`: category not found

---

### 4. Data Models (DTO)

#### CategoryResponse

```json
{
  "id": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "name": "Main Dish",
  "displayOrder": 1
}
```

Explain each field:

- `id`: category UUID
- `restaurantId`: parent restaurant UUID
- `name`: category name
- `displayOrder`: frontend display order

---

### 5. Business Logic Flow

1. Frontend loads categories for a restaurant.
2. Owner creates or updates categories.
3. Backend stores categories scoped to that restaurant.
4. Public menu uses category ordering to render sections.

---

### 6. Validation Rules

- `name` is required
- `displayOrder >= 0`
- category must belong to the same restaurant as the URL

---

### 7. Edge Cases

- category ID belongs to another restaurant
- cashier tries to create/update/delete
- empty category list

---

### 8. Frontend Notes (VERY IMPORTANT)

- Refetch category list after create/update/delete.
- Use `displayOrder` for sorting UI to match backend ordering.
- When deleting a category, confirm with user because dishes under it may become unreachable in UI planning.
- There is no pagination.

---

### 9. Example Flow (End-to-End)

1. Owner opens menu settings.
2. Frontend calls `GET /api/restaurants/{restaurantId}/categories`.
3. Owner creates a new category.
4. Frontend calls `POST /api/restaurants/{restaurantId}/categories`.
5. Frontend refetches the list.

---

### 10. Important Notes / Gotchas

- Read access is broader than write access.
- Public menu endpoint does not reuse `CategoryResponse`; it uses `MenuCategoryResponse`.

---

## Feature: Dish Management

### 1. Business Context

- Dishes are sellable menu items under a category.
- Used by:
  - `OWNER`: create, update, delete
  - `OWNER`, `CASHIER`: view
  - `CUSTOMER`: indirectly through public menu only if `available = true`
- Key business rules:
  - Dish must belong to a category inside the same restaurant.
  - Public menu only shows available dishes.
  - Protected dish list shows all dishes, including unavailable ones.

---

### 2. Authentication

- Auth required: Yes for all management endpoints
- Header format: `Authorization: Bearer <access_token>`
- Role required:
  - create/update/delete: `OWNER`
  - get list/get detail: assigned member (`OWNER` or `CASHIER`)
- Special rules:
  - category must exist in the same restaurant

---

### 3. Endpoints

#### Create Dish

**Method:** `POST`  
**URL:** `/restaurants/{restaurantId}/dishes`

**Description:**  
Creates a new dish inside a category of the restaurant.

**Request:**

```json
{
  "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
  "name": "Beef Combo",
  "description": "Premium beef set",
  "price": 199000,
  "imageUrl": "https://cdn.example.com/beef.jpg",
  "available": true
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Dish created successfully",
  "data": {
    "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "name": "Beef Combo",
    "description": "Premium beef set",
    "price": 199000,
    "imageUrl": "https://cdn.example.com/beef.jpg",
    "available": true
  }
}
```

**Error Cases:**

- `400`: invalid UUID, name missing, price <= 0, availability missing
- `401`: unauthenticated
- `403`: only owner can create
- `404`: category not found

---

#### Get Dishes

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/dishes`

**Description:**  
Returns all dishes in the restaurant ordered by name.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
      "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
      "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
      "name": "Beef Combo",
      "description": "Premium beef set",
      "price": 199000,
      "imageUrl": "https://cdn.example.com/beef.jpg",
      "available": true
    }
  ]
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member

---

#### Get Dish Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/dishes/{dishId}`

**Description:**  
Returns one dish for edit/detail screens.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "name": "Beef Combo",
    "description": "Premium beef set",
    "price": 199000,
    "imageUrl": "https://cdn.example.com/beef.jpg",
    "available": true
  }
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: dish not found

---

#### Update Dish

**Method:** `PUT`  
**URL:** `/restaurants/{restaurantId}/dishes/{dishId}`

**Description:**  
Updates dish metadata and availability.

**Request:**

```json
{
  "name": "Beef Combo XL",
  "description": "Premium beef set for 2 people",
  "price": 259000,
  "imageUrl": "https://cdn.example.com/beef-xl.jpg",
  "available": false
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Dish updated successfully",
  "data": {
    "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
    "name": "Beef Combo XL",
    "description": "Premium beef set for 2 people",
    "price": 259000,
    "imageUrl": "https://cdn.example.com/beef-xl.jpg",
    "available": false
  }
}
```

**Error Cases:**

- `400`: invalid UUID, invalid price, missing availability
- `401`: unauthenticated
- `403`: only owner can update
- `404`: dish not found

---

#### Delete Dish

**Method:** `DELETE`  
**URL:** `/restaurants/{restaurantId}/dishes/{dishId}`

**Description:**  
Deletes a dish from the restaurant.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "message": "Dish deleted successfully"
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: only owner can delete
- `404`: dish not found

---

### 4. Data Models (DTO)

#### DishResponse

```json
{
  "id": "8b28df12-8089-4324-a727-7f55c5be4d5e",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "categoryId": "a3a53b80-7374-46fd-b26a-bbc1dfbe57d3",
  "name": "Beef Combo",
  "description": "Premium beef set",
  "price": 199000,
  "imageUrl": "https://cdn.example.com/beef.jpg",
  "available": true
}
```

Explain each field:

- `id`: dish UUID
- `restaurantId`: parent restaurant UUID
- `categoryId`: parent category UUID
- `name`: dish name
- `description`: optional text
- `price`: price snapshot for new orders
- `imageUrl`: optional image URL
- `available`: true if can appear on public menu and be ordered

---

### 5. Business Logic Flow

1. Frontend loads categories first.
2. Owner creates or edits dish inside a category.
3. Backend validates category ownership and saves the dish.
4. Public menu includes the dish only when `available = true`.

---

### 6. Validation Rules

- `categoryId` is required on create
- `name` is required
- `price` is required and must be `> 0`
- `available` is required

---

### 7. Edge Cases

- dish created under category from another restaurant
- unavailable dish should still appear in protected management list
- deleting a dish that is currently referenced in UI state

---

### 8. Frontend Notes (VERY IMPORTANT)

- Use protected dish list for admin UI, not public menu API.
- Toggle availability instead of deleting whenever possible for safer operations.
- Refetch dish list after create/update/delete.
- Public customer UI must not use management dish APIs.

---

### 9. Example Flow (End-to-End)

1. Owner opens dish management.
2. Frontend loads categories and dishes.
3. Owner submits create/edit form.
4. Frontend calls create or update API.
5. Frontend refetches dishes and optionally public menu preview.

---

### 10. Important Notes / Gotchas

- Dish update does not change `categoryId`; current backend only updates dish fields, not its category.
- Public menu and management dish list are intentionally different datasets.

---

## Feature: Table & QR Management

### 1. Business Context

- Tables identify seating positions and connect QR codes to a specific restaurant table.
- Used by:
  - `OWNER`: create, update, delete tables
  - `OWNER`, `CASHIER`: view table list/detail and fetch QR image
  - `CUSTOMER`: access public table info via QR target URL
- Key business rules:
  - Table number must be unique inside one restaurant.
  - Each table has one absolute QR target URL in `qrCode`.
  - QR image endpoint returns a PNG that encodes the public table URL.

---

### 2. Authentication

- Auth required:
  - create/get list/get detail/update/delete/get QR: yes
  - public table detail: no
- Header format: `Authorization: Bearer <access_token>` for protected routes
- Role required:
  - create/update/delete: `OWNER`
  - get list/get detail/get QR: assigned member (`OWNER` or `CASHIER`)
  - public table detail: customer/public
- Special rules:
  - `qrCode` stored by backend is built from `app.qr.base_url`

---

### 3. Endpoints

#### Create Table

**Method:** `POST`  
**URL:** `/restaurants/{restaurantId}/tables`

**Description:**  
Creates a new table and generates its QR target URL.

**Request:**

```json
{
  "tableNumber": 12,
  "available": true
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Table created successfully",
  "data": {
    "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "tableNumber": 12,
    "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
    "available": true
  }
}
```

**Error Cases:**

- `400`: invalid UUID, table number < 1
- `400`: table number already exists
- `401`: unauthenticated
- `403`: only owner can create
- `404`: restaurant not found

---

#### Get Tables

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/tables`

**Description:**  
Returns all tables in ascending table number order.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
      "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
      "tableNumber": 12,
      "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
      "available": true
    }
  ]
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member

---

#### Get Table Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/tables/{tableId}`

**Description:**  
Returns one table for admin screens.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "tableNumber": 12,
    "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
    "available": true
  }
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: table not found

---

#### Get Public Table Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/tables/{tableId}/public`

**Description:**  
Public endpoint used after scanning the QR code. It tells frontend which table and restaurant are active.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "tableNumber": 12,
    "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
    "available": true
  }
}
```

**Error Cases:**

- `400`: invalid UUID
- `404`: table not found

---

#### Get Table QR Image

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/tables/{tableId}/qr`

**Description:**  
Returns a PNG QR image for printing or downloading. The QR content points to the public table URL.

**Request:**

```json
{}
```

**Response:**

```text
Binary PNG image
Content-Type: image/png
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: table not found
- `500`: QR image generation failed

---

#### Update Table

**Method:** `PUT`  
**URL:** `/restaurants/{restaurantId}/tables/{tableId}`

**Description:**  
Updates table number and availability. QR URL is recalculated automatically.

**Request:**

```json
{
  "tableNumber": 15,
  "available": false
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Table updated successfully",
  "data": {
    "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "tableNumber": 15,
    "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
    "available": false
  }
}
```

**Error Cases:**

- `400`: invalid UUID, table number < 1
- `400`: duplicate table number
- `401`: unauthenticated
- `403`: only owner can update
- `404`: table not found

---

#### Delete Table

**Method:** `DELETE`  
**URL:** `/restaurants/{restaurantId}/tables/{tableId}`

**Description:**  
Deletes a table.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "message": "Table deleted successfully"
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: only owner can delete
- `404`: table not found

---

### 4. Data Models (DTO)

#### TableResponse

```json
{
  "id": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "tableNumber": 12,
  "qrCode": "http://localhost:8080/api/restaurants/2b7f540f-7904-45cf-b91a-319196d5cc11/tables/6ff1b833-fcdd-44ba-9122-6833ae2b0785/public",
  "available": true
}
```

Explain each field:

- `id`: table UUID
- `restaurantId`: parent restaurant UUID
- `tableNumber`: human-facing table number
- `qrCode`: absolute URL encoded into the QR image and usable by customer frontend
- `available`: whether table can accept orders

---

### 5. Business Logic Flow

1. Owner creates table.
2. Backend checks duplicate table number inside the restaurant.
3. Backend stores table and builds its QR target URL.
4. Staff loads table list and downloads QR PNG if needed.
5. Customer scans QR and frontend calls the public table endpoint.

---

### 6. Validation Rules

- `tableNumber >= 1`
- `available` is required
- `tableNumber` must be unique per restaurant

---

### 7. Edge Cases

- duplicate table number
- table not found
- customer scans QR of a table marked unavailable
- cashier can fetch QR image but cannot modify table

---

### 8. Frontend Notes (VERY IMPORTANT)

- Use `qrCode` as the canonical customer entry URL for each table.
- For print/download features, use `/qr` endpoint to fetch PNG.
- After QR scan, call public table API first, then public menu API.
- Refetch table list after create/update/delete.
- `available = false` should disable customer ordering UI for that table.

---

### 9. Example Flow (End-to-End)

1. Owner creates table 12.
2. Frontend gets `TableResponse` with `qrCode`.
3. Frontend downloads `/api/restaurants/{restaurantId}/tables/{tableId}/qr`.
4. Staff prints QR and places it on table 12.
5. Customer scans the QR.
6. Frontend calls public table detail and then public menu.

---

### 10. Important Notes / Gotchas

- QR endpoint is protected, but public table detail endpoint is not.
- Table availability is checked during order creation.
- `qrCode` is URL text, not base64 image data.

---

## Feature: Order Management

### 1. Business Context

- Handles customer order creation and staff order monitoring/status updates.
- Used by:
  - `CUSTOMER`: create order
  - `OWNER`, `CASHIER`: view orders, view details, update status
- Key business rules:
  - Customer creates orders without authentication.
  - Order status starts at `NEW`.
  - Allowed transitions:
    - `NEW -> PROCESSING`
    - `NEW -> CANCELLED`
    - `PROCESSING -> COMPLETED`
    - `PROCESSING -> CANCELLED`
  - Completed or cancelled orders cannot transition again.
  - Order uses dish price snapshot at order time.
  - WebSocket notification is sent on create and status update.

---

### 2. Authentication

- Auth required:
  - create order: no
  - get list/get detail/update status: yes
- Header format: `Authorization: Bearer <access_token>` for protected routes
- Role required:
  - create: customer/public
  - get list/get detail/update status: assigned member (`OWNER` or `CASHIER`)
- Special rules:
  - table must belong to restaurant and be available
  - dish must belong to restaurant and be available

---

### 3. Endpoints

#### Create Order

**Method:** `POST`  
**URL:** `/restaurants/{restaurantId}/orders`

**Description:**  
Creates a customer order for one table using dish IDs and quantities.

**Request:**

```json
{
  "tableId": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
  "note": "No spicy",
  "items": [
    {
      "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
      "quantity": 2
    },
    {
      "dishId": "2cbf744d-df54-4325-94d0-9c7b4e7f22e2",
      "quantity": 1
    }
  ]
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Order created successfully",
  "data": {
    "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "status": "NEW",
    "note": "No spicy",
    "createdAt": "2026-03-24T10:15:30.000Z",
    "tableNumber": 12,
    "totalPrice": 497000,
    "items": [
      {
        "id": "5bc43949-6162-4f61-919e-62f62a7dc1b5",
        "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
        "dishName": "Beef Combo",
        "quantity": 2,
        "priceAtOrder": 199000,
        "lineTotal": 398000
      },
      {
        "id": "19b6c3aa-0b3e-4fd8-a4cd-84dc4ef8d9e1",
        "dishId": "2cbf744d-df54-4325-94d0-9c7b4e7f22e2",
        "dishName": "Kimchi",
        "quantity": 1,
        "priceAtOrder": 99000,
        "lineTotal": 99000
      }
    ]
  }
}
```

**Error Cases:**

- `400`: invalid UUID, empty items, quantity < 1
- `400`: table is not available
- `400`: dish is out of stock
- `404`: restaurant not found
- `404`: table not found
- `404`: dish not found

---

#### Get Orders

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/orders`

**Description:**  
Returns restaurant orders sorted newest first.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
      "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
      "status": "NEW",
      "note": "No spicy",
      "createdAt": "2026-03-24T10:15:30.000Z",
      "tableNumber": 12,
      "totalPrice": 497000,
      "items": []
    }
  ]
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member

---

#### Get Order Detail

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/orders/{orderId}`

**Description:**  
Returns one order including its line items.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": {
    "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "status": "PROCESSING",
    "note": "No spicy",
    "createdAt": "2026-03-24T10:15:30.000Z",
    "tableNumber": 12,
    "totalPrice": 497000,
    "items": [
      {
        "id": "5bc43949-6162-4f61-919e-62f62a7dc1b5",
        "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
        "dishName": "Beef Combo",
        "quantity": 2,
        "priceAtOrder": 199000,
        "lineTotal": 398000
      }
    ]
  }
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: order not found

---

#### Update Order Status

**Method:** `PATCH`  
**URL:** `/restaurants/{restaurantId}/orders/{orderId}/status`

**Description:**  
Changes order workflow status for restaurant staff.

**Request:**

```json
{
  "status": "PROCESSING"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "Order status updated successfully",
  "data": {
    "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
    "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
    "status": "PROCESSING",
    "note": "No spicy",
    "createdAt": "2026-03-24T10:15:30.000Z",
    "tableNumber": 12,
    "totalPrice": 497000,
    "items": [
      {
        "id": "5bc43949-6162-4f61-919e-62f62a7dc1b5",
        "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
        "dishName": "Beef Combo",
        "quantity": 2,
        "priceAtOrder": 199000,
        "lineTotal": 398000
      }
    ]
  }
}
```

**Error Cases:**

- `400`: missing status
- `400`: invalid order status transition
- `400`: invalid UUID
- `401`: unauthenticated
- `403`: not a restaurant member
- `404`: order not found

---

### 4. Data Models (DTO)

#### CreateOrderRequest

```json
{
  "tableId": "6ff1b833-fcdd-44ba-9122-6833ae2b0785",
  "note": "No spicy",
  "items": [
    {
      "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
      "quantity": 2
    }
  ]
}
```

Explain each field:

- `tableId`: table UUID from public table detail
- `note`: optional customer note
- `items`: list of ordered dishes

#### OrderResponse

```json
{
  "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "status": "NEW",
  "note": "No spicy",
  "createdAt": "2026-03-24T10:15:30.000Z",
  "tableNumber": 12,
  "totalPrice": 497000,
  "items": []
}
```

Explain each field:

- `id`: order UUID
- `restaurantId`: restaurant UUID
- `status`: `NEW`, `PROCESSING`, `COMPLETED`, `CANCELLED`
- `note`: optional customer note
- `createdAt`: creation timestamp
- `tableNumber`: table number snapshot for display
- `totalPrice`: total calculated from line items
- `items`: line items with dish snapshots

#### OrderItemResponse

```json
{
  "id": "5bc43949-6162-4f61-919e-62f62a7dc1b5",
  "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
  "dishName": "Beef Combo",
  "quantity": 2,
  "priceAtOrder": 199000,
  "lineTotal": 398000
}
```

Explain each field:

- `id`: order item UUID
- `dishId`: dish UUID
- `dishName`: dish name snapshot
- `quantity`: ordered quantity
- `priceAtOrder`: dish price at order time
- `lineTotal`: `priceAtOrder * quantity`

---

### 5. Business Logic Flow

1. Customer scans table QR.
2. Frontend resolves table and menu.
3. Customer submits order with `tableId` and dish IDs.
4. Backend validates table and dishes, calculates totals, saves order and items.
5. Backend pushes WebSocket event to restaurant topic.
6. Staff dashboard refetches or patches local state.
7. Staff updates status, backend pushes another WebSocket event.

---

### 6. Validation Rules

- `tableId` is required
- `items` must not be empty
- each `dishId` is required
- each `quantity >= 1`
- status update requires non-null status
- table must be available
- dish must be available

---

### 7. Edge Cases

- customer orders unavailable dish
- customer orders on unavailable table
- invalid status transition
- order exists but user is from another restaurant

---

### 8. Frontend Notes (VERY IMPORTANT)

- Customer side needs only the public create-order endpoint.
- Staff side should subscribe to WebSocket and refetch order list on new events.
- Do not try to derive `tableId` from `tableNumber`; backend create order requires `tableId`.
- Render status transitions based on current status:
  - `NEW`: can move to `PROCESSING` or `CANCELLED`
  - `PROCESSING`: can move to `COMPLETED` or `CANCELLED`
  - `COMPLETED`, `CANCELLED`: no further actions

---

### 9. Example Flow (End-to-End)

1. Customer scans QR.
2. Frontend calls public table detail.
3. Frontend calls public menu.
4. Customer submits `POST /api/restaurants/{restaurantId}/orders`.
5. Staff dashboard receives WebSocket event.
6. Staff opens order and calls status update API.

---

### 10. Important Notes / Gotchas

- Order response returns `tableNumber`, not `tableId`.
- Backend does not merge duplicate dish IDs in request; frontend should merge duplicates before submit.
- Backend does not expose order delete/cancel by customer endpoint.

---

## Feature: Restaurant User Management

### 1. Business Context

- Lets owners manage cashier accounts for a specific restaurant.
- Used by:
  - `OWNER`: create cashier, list members, delete cashier
- Key business rules:
  - New members created here are always `CASHIER`.
  - Created cashier accounts are active immediately.
  - Owner account cannot be removed from restaurant through this API.
  - If a deleted cashier is not assigned to any restaurant anymore, backend deletes the user record too.

---

### 2. Authentication

- Auth required: Yes
- Header format: `Authorization: Bearer <access_token>`
- Role required: `OWNER`
- Special rules:
  - Current owner must be a member of the target restaurant

---

### 3. Endpoints

#### Create Cashier

**Method:** `POST`  
**URL:** `/restaurants/{restaurantId}/users`

**Description:**  
Creates a cashier account and links it to the restaurant.

**Request:**

```json
{
  "email": "cashier@example.com",
  "password": "secret123",
  "phone": "0908888888",
  "fullName": "Le Thi B"
}
```

**Response:**

```json
{
  "success": 1,
  "message": "User created successfully",
  "data": {
    "id": "50dd2d80-c4aa-45d3-a10f-aab3fdef4b01",
    "fullName": "Le Thi B",
    "phone": "0908888888",
    "email": "cashier@example.com",
    "role": "CASHIER",
    "active": true
  }
}
```

**Error Cases:**

- `400`: invalid email, password shorter than 6, full name missing
- `400`: email already exists
- `401`: unauthenticated
- `403`: only owner can create cashier
- `404`: restaurant not found

---

#### Get Restaurant Users

**Method:** `GET`  
**URL:** `/restaurants/{restaurantId}/users`

**Description:**  
Returns all users assigned to the restaurant.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "data": [
    {
      "id": "c41d0d88-caf4-4b29-9e5c-29fe488c81f1",
      "fullName": "Nguyen Van A",
      "phone": "0901234567",
      "email": "owner@example.com",
      "role": "OWNER",
      "active": true
    },
    {
      "id": "50dd2d80-c4aa-45d3-a10f-aab3fdef4b01",
      "fullName": "Le Thi B",
      "phone": "0908888888",
      "email": "cashier@example.com",
      "role": "CASHIER",
      "active": true
    }
  ]
}
```

**Error Cases:**

- `400`: invalid UUID
- `401`: unauthenticated
- `403`: only owner can view members

---

#### Delete Cashier

**Method:** `DELETE`  
**URL:** `/restaurants/{restaurantId}/users/{userId}`

**Description:**  
Removes a cashier from the restaurant. If that user is no longer linked to any restaurant, the backend deletes the user account record.

**Request:**

```json
{}
```

**Response:**

```json
{
  "success": 1,
  "message": "User deleted successfully"
}
```

**Error Cases:**

- `400`: invalid UUID
- `400`: owner account cannot be removed from restaurant
- `401`: unauthenticated
- `403`: only owner can delete cashier
- `404`: user not found in this restaurant

---

### 4. Data Models (DTO)

#### UserResponse

```json
{
  "id": "50dd2d80-c4aa-45d3-a10f-aab3fdef4b01",
  "fullName": "Le Thi B",
  "phone": "0908888888",
  "email": "cashier@example.com",
  "role": "CASHIER",
  "active": true
}
```

Explain each field:

- `id`: user UUID
- `fullName`: display name
- `phone`: optional phone
- `email`: login email
- `role`: `OWNER` or `CASHIER`
- `active`: account active flag

---

### 5. Business Logic Flow

1. Owner opens staff management.
2. Frontend loads current restaurant users.
3. Owner creates cashier or deletes cashier.
4. Backend updates restaurant membership records.
5. Frontend refetches member list.

---

### 6. Validation Rules

- same validation as register for `email`, `password`, `fullName`
- email must be unique across all users
- created users through this flow always become `CASHIER`

---

### 7. Edge Cases

- deleting owner account
- creating cashier with duplicate email
- deleting cashier who belongs to multiple restaurants

---

### 8. Frontend Notes (VERY IMPORTANT)

- Do not show create/delete staff UI to cashier users.
- Refetch user list after create/delete.
- If delete succeeds, remove the user from UI immediately.
- There is no update staff endpoint in current backend.

---

### 9. Example Flow (End-to-End)

1. Owner opens staff page.
2. Frontend calls `GET /api/restaurants/{restaurantId}/users`.
3. Owner creates cashier.
4. Frontend calls `POST /api/restaurants/{restaurantId}/users`.
5. Frontend refetches list.

---

### 10. Important Notes / Gotchas

- Cashier accounts are active immediately and do not require email verification.
- Delete removes membership first, then may remove user record entirely if unused elsewhere.

---

## Feature: Realtime Order Notifications (WebSocket)

### 1. Business Context

- Provides real-time order updates for staff dashboards.
- Used by:
  - `OWNER`
  - `CASHIER`
- Key business rules:
  - Backend pushes an order payload whenever a new order is created.
  - Backend also pushes after order status changes.
  - Message payload is the same structure as `OrderResponse`.

---

### 2. Authentication

- Auth required to open WebSocket handshake: No explicit backend auth is enforced in current code
- STOMP endpoint: `/ws`
- Role required: frontend should still treat this as staff-only UI behavior
- Special rules:
  - Subscribe by `restaurantId`

---

### 3. Endpoints

#### WebSocket Handshake

**Method:** `GET`  
**URL:** `/ws`

**Description:**  
Opens STOMP over WebSocket connection.

**Request:**

```json
{}
```

**Response:**

```json
{}
```

**Error Cases:**

- Connection-level failures depend on transport/client setup

---

#### Topic Subscription

**Method:** `SUBSCRIBE`  
**URL:** `/topic/orders/{restaurantId}`

**Description:**  
Receives `OrderResponse` payload each time a restaurant order is created or updated.

**Request:**

```json
{}
```

**Response:**

```json
{
  "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "status": "NEW",
  "note": "No spicy",
  "createdAt": "2026-03-24T10:15:30.000Z",
  "tableNumber": 12,
  "totalPrice": 497000,
  "items": [
    {
      "id": "5bc43949-6162-4f61-919e-62f62a7dc1b5",
      "dishId": "8b28df12-8089-4324-a727-7f55c5be4d5e",
      "dishName": "Beef Combo",
      "quantity": 2,
      "priceAtOrder": 199000,
      "lineTotal": 398000
    }
  ]
}
```

**Error Cases:**

- Wrong `restaurantId` means client simply receives no relevant messages

---

### 4. Data Models (DTO)

#### OrderResponse

```json
{
  "id": "d7bb47ec-cd70-4d17-95db-1831c79255a0",
  "restaurantId": "2b7f540f-7904-45cf-b91a-319196d5cc11",
  "status": "NEW",
  "note": "No spicy",
  "createdAt": "2026-03-24T10:15:30.000Z",
  "tableNumber": 12,
  "totalPrice": 497000,
  "items": []
}
```

Explain each field:

- same semantics as REST `OrderResponse`

---

### 5. Business Logic Flow

1. Staff dashboard loads restaurant and initial order list.
2. Frontend opens WebSocket to `/ws`.
3. Frontend subscribes to `/topic/orders/{restaurantId}`.
4. Customer creates order or staff updates status.
5. Backend pushes latest order payload.
6. Frontend updates order list/detail UI.

---

### 6. Validation Rules

- none at REST payload level
- client must subscribe to the correct `restaurantId`

---

### 7. Edge Cases

- socket connected before user selected restaurant
- duplicate updates if frontend both refetches and patches local state
- reconnect after network drop

---

### 8. Frontend Notes (VERY IMPORTANT)

- Always load initial orders from REST first, then use WebSocket for live updates.
- On each WebSocket message, replace or upsert the order by `id`.
- Reconnect automatically if the socket drops.
- If your app supports multi-restaurant switching, unsubscribe from old topic and subscribe to the new one.

---

### 9. Example Flow (End-to-End)

1. Cashier opens order dashboard.
2. Frontend calls `GET /api/restaurants/{restaurantId}/orders`.
3. Frontend opens `/ws`.
4. Frontend subscribes to `/topic/orders/{restaurantId}`.
5. Customer places order.
6. Dashboard receives real-time order payload and updates list.

---

### 10. Important Notes / Gotchas

- Backend exposes only outbound notifications; there is no inbound STOMP command handling in current code.
- WebSocket payload is raw `OrderResponse`, not wrapped in `ApiResult`.

---

## Final Frontend Checklist

- Always prefix REST requests with `/api`.
- Use `Bearer` token for all protected endpoints.
- Treat customer order flow as public and token-free.
- After QR scan:
  1. call public table detail
  2. call public menu
  3. create order with `tableId`
- For staff dashboard:
  1. login
  2. load restaurants
  3. choose restaurant
  4. load orders
  5. subscribe to `/topic/orders/{restaurantId}`
- Handle all errors using `message` from the backend response.
