# API Contract — Resource Management and Reservation Platform

**Base URL:** `http://localhost:8080`

All request and response bodies are JSON (`application/json`).  
Dates use ISO-8601 format (`yyyy-MM-dd'T'HH:mm:ss`).

---

## Authentication

Endpoints under `/api/auth` are **public** (no token required).  
All other endpoints require a **Bearer token** in the `Authorization` header:

```
Authorization: Bearer <accessToken>
```

Endpoints marked 🔒 **ADMIN** additionally require the `ROLE_ADMIN` role.

---

## Error responses

All errors follow the same shape:

```json
{
  "timestamp": "2026-02-11T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message"
}
```

---

## 1. Auth (`/api/auth`)

### 1.1 Register

|          |                      |
| -------- | -------------------- |
| **POST** | `/api/auth/register` |
| **Auth** | None                 |

**Request body**

| Field       | Type   | Required | Description |
| ----------- | ------ | -------- | ----------- |
| `email`     | string | ✅       | User email  |
| `password`  | string | ✅       | Password    |
| `firstName` | string | ✅       | First name  |
| `lastName`  | string | ✅       | Last name   |

```json
{
  "email": "user@example.com",
  "password": "Secret123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Responses**

| Code | Description              | Body           |
| ---- | ------------------------ | -------------- |
| 201  | User registered          | `AuthResponse` |
| 400  | Invalid request          | Error          |
| 409  | Email already registered | Error          |

---

### 1.2 Login

|          |                   |
| -------- | ----------------- |
| **POST** | `/api/auth/login` |
| **Auth** | None              |

**Request body**

| Field      | Type   | Required |
| ---------- | ------ | -------- |
| `email`    | string | ✅       |
| `password` | string | ✅       |

```json
{
  "email": "user@example.com",
  "password": "Secret123!"
}
```

**Responses**

| Code | Description         | Body           |
| ---- | ------------------- | -------------- |
| 200  | Authenticated       | `AuthResponse` |
| 401  | Invalid credentials | Error          |

---

### 1.3 Logout

|          |                    |
| -------- | ------------------ |
| **POST** | `/api/auth/logout` |
| **Auth** | Bearer token       |

**Request body:** _none_

**Responses**

| Code | Description  | Body                                       |
| ---- | ------------ | ------------------------------------------ |
| 200  | Logged out   | `{ "message": "Logged out successfully" }` |
| 401  | Unauthorized | Error                                      |

---

### 1.4 Refresh token

|          |                     |
| -------- | ------------------- |
| **POST** | `/api/auth/refresh` |
| **Auth** | None                |

**Request body**

| Field          | Type   | Required |
| -------------- | ------ | -------- |
| `refreshToken` | string | ✅       |

```json
{
  "refreshToken": "abc123-refresh-token"
}
```

**Responses**

| Code | Description                     | Body           |
| ---- | ------------------------------- | -------------- |
| 200  | Token refreshed                 | `AuthResponse` |
| 401  | Invalid / expired refresh token | Error          |

---

### AuthResponse shape

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "d4f8a2b1-...",
  "email": "user@example.com"
}
```

---

## 2. Resource Types (`/api/resource-types`)

### 2.1 List all

|          |                       |
| -------- | --------------------- |
| **GET**  | `/api/resource-types` |
| **Auth** | Bearer token          |

**Responses**

| Code | Description              | Body                     |
| ---- | ------------------------ | ------------------------ |
| 200  | Resource types retrieved | `ResourceTypeResponse[]` |

---

### 2.2 Get by ID

|          |                            |
| -------- | -------------------------- |
| **GET**  | `/api/resource-types/{id}` |
| **Auth** | Bearer token               |

**Path parameters**

| Param | Type    | Description      |
| ----- | ------- | ---------------- |
| `id`  | integer | Resource type ID |

**Responses**

| Code | Description | Body                   |
| ---- | ----------- | ---------------------- |
| 200  | Found       | `ResourceTypeResponse` |
| 404  | Not found   | Error                  |

---

### 2.3 Create 🔒 ADMIN

|          |                       |
| -------- | --------------------- |
| **POST** | `/api/resource-types` |
| **Auth** | Bearer token (ADMIN)  |

**Request body**

| Field         | Type   | Required |
| ------------- | ------ | -------- |
| `name`        | string | ✅       |
| `description` | string | ❌       |

```json
{
  "name": "Meeting Room",
  "description": "Conference and meeting rooms"
}
```

**Responses**

| Code | Description     | Body                   |
| ---- | --------------- | ---------------------- |
| 201  | Created         | `ResourceTypeResponse` |
| 400  | Invalid request | Error                  |
| 403  | Forbidden       | Error                  |
| 409  | Duplicate name  | Error                  |

---

### 2.4 Update 🔒 ADMIN

|          |                            |
| -------- | -------------------------- |
| **PUT**  | `/api/resource-types/{id}` |
| **Auth** | Bearer token (ADMIN)       |

**Path parameters**

| Param | Type    | Description      |
| ----- | ------- | ---------------- |
| `id`  | integer | Resource type ID |

**Request body** — same as Create.

**Responses**

| Code | Description | Body                   |
| ---- | ----------- | ---------------------- |
| 200  | Updated     | `ResourceTypeResponse` |
| 403  | Forbidden   | Error                  |
| 404  | Not found   | Error                  |

---

### 2.5 Delete 🔒 ADMIN

|            |                            |
| ---------- | -------------------------- |
| **DELETE** | `/api/resource-types/{id}` |
| **Auth**   | Bearer token (ADMIN)       |

**Responses**

| Code | Description | Body    |
| ---- | ----------- | ------- |
| 204  | Deleted     | _empty_ |
| 403  | Forbidden   | Error   |
| 404  | Not found   | Error   |

---

### ResourceTypeResponse shape

```json
{
  "id": 1,
  "name": "Meeting Room",
  "description": "Conference and meeting rooms"
}
```

---

## 3. Resources (`/api/resources`)

### 3.1 List resources

|          |                  |
| -------- | ---------------- |
| **GET**  | `/api/resources` |
| **Auth** | Bearer token     |

**Query parameters**

| Param    | Type    | Required | Description                              |
| -------- | ------- | -------- | ---------------------------------------- |
| `active` | boolean | ❌       | Filter by active status (`true`/`false`) |
| `typeId` | integer | ❌       | Filter by resource type ID               |

**Responses**

| Code | Description         | Body                 |
| ---- | ------------------- | -------------------- |
| 200  | Resources retrieved | `ResourceResponse[]` |

---

### 3.2 Get by ID

|          |                       |
| -------- | --------------------- |
| **GET**  | `/api/resources/{id}` |
| **Auth** | Bearer token          |

**Path parameters**

| Param | Type | Description |
| ----- | ---- | ----------- |
| `id`  | UUID | Resource ID |

**Responses**

| Code | Description | Body               |
| ---- | ----------- | ------------------ |
| 200  | Found       | `ResourceResponse` |
| 404  | Not found   | Error              |

---

### 3.3 Create 🔒 ADMIN

|          |                      |
| -------- | -------------------- |
| **POST** | `/api/resources`     |
| **Auth** | Bearer token (ADMIN) |

**Request body**

| Field            | Type    | Required | Description         |
| ---------------- | ------- | -------- | ------------------- |
| `name`           | string  | ✅       | Resource name       |
| `description`    | string  | ❌       | Description         |
| `resourceTypeId` | integer | ✅       | FK to resource type |
| `location`       | string  | ❌       | Physical location   |

```json
{
  "name": "Room A-101",
  "description": "10-person meeting room",
  "resourceTypeId": 1,
  "location": "Building A, Floor 1"
}
```

**Responses**

| Code | Description     | Body               |
| ---- | --------------- | ------------------ |
| 201  | Created         | `ResourceResponse` |
| 400  | Invalid request | Error              |
| 403  | Forbidden       | Error              |
| 409  | Duplicate name  | Error              |

---

### 3.4 Update 🔒 ADMIN

|          |                       |
| -------- | --------------------- |
| **PUT**  | `/api/resources/{id}` |
| **Auth** | Bearer token (ADMIN)  |

**Path parameters**

| Param | Type | Description |
| ----- | ---- | ----------- |
| `id`  | UUID | Resource ID |

**Request body** — same as Create.

**Responses**

| Code | Description | Body               |
| ---- | ----------- | ------------------ |
| 200  | Updated     | `ResourceResponse` |
| 403  | Forbidden   | Error              |
| 404  | Not found   | Error              |

---

### 3.5 Delete (soft) 🔒 ADMIN

|            |                       |
| ---------- | --------------------- |
| **DELETE** | `/api/resources/{id}` |
| **Auth**   | Bearer token (ADMIN)  |

**Responses**

| Code | Description           | Body    |
| ---- | --------------------- | ------- |
| 204  | Deleted (deactivated) | _empty_ |
| 403  | Forbidden             | Error   |
| 404  | Not found             | Error   |

---

### ResourceResponse shape

```json
{
  "id": "a1b2c3d4-...",
  "name": "Room A-101",
  "description": "10-person meeting room",
  "resourceType": {
    "id": 1,
    "name": "Meeting Room",
    "description": "Conference and meeting rooms"
  },
  "location": "Building A, Floor 1",
  "active": true,
  "createdAt": "2026-02-11T10:00:00",
  "updatedAt": "2026-02-11T10:00:00"
}
```

---

## 4. Reservations (`/api/reservations`)

### 4.1 Create reservation

|          |                     |
| -------- | ------------------- |
| **POST** | `/api/reservations` |
| **Auth** | Bearer token        |

**Request body**

| Field        | Type          | Required | Description          |
| ------------ | ------------- | -------- | -------------------- |
| `resourceId` | UUID          | ✅       | Resource to reserve  |
| `startTime`  | ISO date-time | ✅       | Start of reservation |
| `endTime`    | ISO date-time | ✅       | End of reservation   |
| `notes`      | string        | ❌       | Optional notes       |

```json
{
  "resourceId": "a1b2c3d4-...",
  "startTime": "2026-02-12T09:00:00",
  "endTime": "2026-02-12T10:00:00",
  "notes": "Team standup"
}
```

**Responses**

| Code | Description        | Body                  |
| ---- | ------------------ | --------------------- |
| 201  | Created            | `ReservationResponse` |
| 400  | Invalid request    | Error                 |
| 401  | Unauthorized       | Error                 |
| 409  | Time slot overlaps | Error                 |

---

### 4.2 Get by ID

|          |                          |
| -------- | ------------------------ |
| **GET**  | `/api/reservations/{id}` |
| **Auth** | Bearer token             |

**Path parameters**

| Param | Type | Description    |
| ----- | ---- | -------------- |
| `id`  | UUID | Reservation ID |

**Responses**

| Code | Description | Body                  |
| ---- | ----------- | --------------------- |
| 200  | Found       | `ReservationResponse` |
| 404  | Not found   | Error                 |

---

### 4.3 List all active reservations

|          |                            |
| -------- | -------------------------- |
| **GET**  | `/api/reservations/active` |
| **Auth** | Bearer token               |

**Responses**

| Code | Description         | Body                    |
| ---- | ------------------- | ----------------------- |
| 200  | Active reservations | `ReservationResponse[]` |

---

### 4.4 My active reservations

|          |                        |
| -------- | ---------------------- |
| **GET**  | `/api/reservations/my` |
| **Auth** | Bearer token           |

**Responses**

| Code | Description                | Body                    |
| ---- | -------------------------- | ----------------------- |
| 200  | User's active reservations | `ReservationResponse[]` |

---

### 4.5 My reservation history

|          |                                |
| -------- | ------------------------------ |
| **GET**  | `/api/reservations/my/history` |
| **Auth** | Bearer token                   |

**Responses**

| Code | Description                | Body                    |
| ---- | -------------------------- | ----------------------- |
| 200  | User's reservation history | `ReservationResponse[]` |

---

### 4.6 Resource reservation history

|          |                                                   |
| -------- | ------------------------------------------------- |
| **GET**  | `/api/reservations/resource/{resourceId}/history` |
| **Auth** | Bearer token                                      |

**Path parameters**

| Param        | Type | Description |
| ------------ | ---- | ----------- |
| `resourceId` | UUID | Resource ID |

**Responses**

| Code | Description                  | Body                    |
| ---- | ---------------------------- | ----------------------- |
| 200  | Resource reservation history | `ReservationResponse[]` |
| 404  | Resource not found           | Error                   |

---

### 4.7 Resource availability

|          |                                                        |
| -------- | ------------------------------------------------------ |
| **GET**  | `/api/reservations/resource/{resourceId}/availability` |
| **Auth** | Bearer token                                           |

**Path parameters**

| Param        | Type | Description |
| ------------ | ---- | ----------- |
| `resourceId` | UUID | Resource ID |

**Query parameters**

| Param   | Type          | Required | Description  |
| ------- | ------------- | -------- | ------------ |
| `start` | ISO date-time | ✅       | Window start |
| `end`   | ISO date-time | ✅       | Window end   |

Example: `/api/reservations/resource/{id}/availability?start=2026-02-12T00:00:00&end=2026-02-13T00:00:00`

**Responses**

| Code | Description        | Body                 |
| ---- | ------------------ | -------------------- |
| 200  | Availability slots | `AvailabilitySlot[]` |
| 404  | Resource not found | Error                |

---

### 4.8 Cancel reservation

|           |                                 |
| --------- | ------------------------------- |
| **PATCH** | `/api/reservations/{id}/cancel` |
| **Auth**  | Bearer token (creator or ADMIN) |

**Path parameters**

| Param | Type | Description    |
| ----- | ---- | -------------- |
| `id`  | UUID | Reservation ID |

**Request body:** _none_

**Responses**

| Code | Description              | Body                  |
| ---- | ------------------------ | --------------------- |
| 200  | Cancelled                | `ReservationResponse` |
| 401  | Unauthorized             | Error                 |
| 403  | Not the creator or Admin | Error                 |
| 404  | Not found                | Error                 |

---

### ReservationResponse shape

```json
{
  "id": "e5f6a7b8-...",
  "resourceId": "a1b2c3d4-...",
  "resourceName": "Room A-101",
  "userId": "c9d0e1f2-...",
  "userEmail": "user@example.com",
  "startTime": "2026-02-12T09:00:00",
  "endTime": "2026-02-12T10:00:00",
  "status": "ACTIVE",
  "notes": "Team standup",
  "cancelledById": null,
  "cancelledAt": null,
  "createdAt": "2026-02-11T10:00:00",
  "updatedAt": "2026-02-11T10:00:00"
}
```

### AvailabilitySlot shape

```json
{
  "start": "2026-02-12T09:00:00",
  "end": "2026-02-12T10:00:00",
  "available": false
}
```
