# Nexus Ticketing — Enhanced

A full-stack **Ticket Management System** extended with **Project CRUD**, **Employee CRUD**, and **Dark / Light theme** support. Built with Angular 17 on the frontend and Spring Boot 3 on the backend, backed by MySQL.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Theme System](#theme-system)
- [What Was Added (Enhancement Summary)](#what-was-added-enhancement-summary)

---

## Features

### Existing (Unchanged)
- Ticket CRUD — create, view, edit, delete tickets
- Ticket search & filter by ID, project, status, priority
- Paginated ticket list with sortable columns
- Dashboard statistics
- Priority and status indicators

### New in This Release
- **Project Management** — full CRUD for projects with code, name, description, status, start/end dates
- **Employee Management** — full CRUD for employees with name, email, designation, department, assigned project, status
- **Search & Filter** — both Project and Employee lists support live search + status filtering with debounce
- **Dark / Light Theme** — toggle button in the navbar; preference persisted to `localStorage`; dark mode is pixel-identical to the original

---

## Tech Stack

| Layer     | Technology                                      |
|-----------|-------------------------------------------------|
| Frontend  | Angular 17, TypeScript, RxJS, Angular Router    |
| Backend   | Spring Boot 3.2, Spring Data JPA, Jakarta EE    |
| Database  | MySQL 8                                         |
| Build     | Maven (backend), Angular CLI (frontend)         |
| Java      | 17                                              |
| Node      | 18+                                             |

---

## Project Structure

```
TicketAppEnhanced/
├── backend/
│   └── src/main/java/com/ticketsystem/
│       ├── controller/
│       │   ├── TicketController.java
│       │   ├── ProjectController.java       ← NEW
│       │   └── EmployeeController.java      ← NEW
│       ├── entity/
│       │   ├── Ticket.java
│       │   ├── Project.java                 ← NEW
│       │   └── Employee.java                ← NEW
│       ├── dto/
│       │   ├── ApiResponse.java
│       │   ├── TicketRequestDTO.java
│       │   ├── TicketResponseDTO.java
│       │   ├── ProjectRequestDTO.java        ← NEW
│       │   ├── ProjectResponseDTO.java       ← NEW
│       │   ├── EmployeeRequestDTO.java       ← NEW
│       │   └── EmployeeResponseDTO.java      ← NEW
│       ├── service/
│       │   ├── TicketService.java
│       │   ├── ProjectService.java           ← NEW
│       │   └── EmployeeService.java          ← NEW
│       ├── serviceImpl/
│       │   ├── TicketServiceImpl.java
│       │   ├── ProjectServiceImpl.java       ← NEW
│       │   └── EmployeeServiceImpl.java      ← NEW
│       ├── repository/
│       │   ├── TicketRepository.java
│       │   ├── ProjectRepository.java        ← NEW
│       │   └── EmployeeRepository.java       ← NEW
│       └── exception/
│           ├── GlobalExceptionHandler.java   ← MODIFIED
│           ├── ResourceNotFoundException.java
│           └── ErrorResponse.java
│   └── src/main/resources/
│       ├── application.properties
│       └── schema.sql                        ← MODIFIED
│
└── frontend/
    └── src/app/
        ├── app.component.ts                  ← MODIFIED (nav + theme toggle)
        ├── app.routes.ts                     ← MODIFIED (new routes)
        ├── models/
        │   ├── ticket.model.ts
        │   ├── project.model.ts              ← NEW
        │   └── employee.model.ts             ← NEW
        ├── services/
        │   ├── ticket.service.ts
        │   ├── project.service.ts            ← NEW
        │   ├── employee.service.ts           ← NEW
        │   └── theme.service.ts              ← NEW
        └── components/
            ├── ticket-list/                  ← CSS extended for theme
            ├── ticket-form/                  ← CSS extended for theme
            ├── ticket-detail/                ← CSS extended for theme
            ├── project-list/                 ← NEW
            ├── project-form/                 ← NEW
            ├── employee-list/                ← NEW
            └── employee-form/                ← NEW
```

---

## Database Schema

### `tickets` (existing — unchanged)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| ticket_id | VARCHAR(20) | Unique, auto-generated |
| project_assignment | VARCHAR(100) | Required |
| issue_description | TEXT | Required |
| assigned_employee | VARCHAR(100) | Optional |
| support_level | ENUM(L1, L2, L3) | Required |
| priority | ENUM(P1_CRITICAL … P4_LOW) | Required |
| current_status | ENUM(OPEN, IN_PROGRESS, RESOLVED, CLOSED) | Default OPEN |
| resolution_details | TEXT | Optional |
| remarks | TEXT | Optional |
| generation_date_time | DATETIME | Optional |
| response_date_time | DATETIME | Optional |
| resolution_time | DATETIME | Optional |
| created_at / updated_at | DATETIME | Auto-managed |

### `projects` (new)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| project_code | VARCHAR(30) | Unique, required |
| project_name | VARCHAR(100) | Required |
| description | TEXT | Optional |
| status | ENUM(ACTIVE, ON_HOLD, COMPLETED, CANCELLED) | Default ACTIVE |
| start_date | DATE | Optional |
| end_date | DATE | Optional |
| created_at / updated_at | DATETIME | Auto-managed |

### `employees` (new)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| employee_name | VARCHAR(100) | Required |
| email | VARCHAR(150) | Unique, required |
| designation | VARCHAR(100) | Optional |
| department | VARCHAR(100) | Optional |
| assigned_project | VARCHAR(100) | Optional |
| status | ENUM(ACTIVE, INACTIVE, ON_LEAVE) | Default ACTIVE |
| created_at / updated_at | DATETIME | Auto-managed |

---

## Backend Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running locally or remotely

### Steps

**1. Clone and navigate to backend**
```bash
cd TicketAppEnhanced/backend
```

**2. Configure environment variables** (see [Environment Variables](#environment-variables))

**3. Build**
```bash
mvn clean install
```

**4. Run**
```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080` by default. The schema is applied automatically via `schema.sql` on startup (tables use `CREATE TABLE IF NOT EXISTS`, so existing data is safe).

---

## Frontend Setup

### Prerequisites
- Node.js 18+
- Angular CLI 17

```bash
npm install -g @angular/cli
```

### Steps

**1. Navigate to frontend**
```bash
cd TicketAppEnhanced/frontend
```

**2. Install dependencies**
```bash
npm install
```

**3. Set API URL**

Edit `src/environments/environment.ts`:
```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'   // point to your backend
};
```

**4. Start dev server**
```bash
ng serve
```

App available at `http://localhost:4200`.

**5. Production build**
```bash
ng build --configuration production
```

---

## Environment Variables

The backend reads these from environment variables (or `.env` / system properties):

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/ticketdb` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `secret` |
| `PORT` | Server port (optional) | `8080` |
| `FRONTEND_URL` | Allowed CORS origin | `http://localhost:4200` |

For local development you can export them in your shell:
```bash
export DB_URL=jdbc:mysql://localhost:3306/ticketdb
export DB_USERNAME=root
export DB_PASSWORD=secret
export FRONTEND_URL=http://localhost:4200
```

Or create an `application-local.properties` file and run with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## API Reference

All responses follow the unified wrapper:
```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2025-05-18T10:30:00"
}
```

### Tickets — `/api/tickets`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tickets` | List all tickets (paginated) |
| GET | `/api/tickets/search` | Search with filters |
| GET | `/api/tickets/{id}` | Get ticket by ID |
| POST | `/api/tickets` | Create ticket |
| PUT | `/api/tickets/{id}` | Update ticket |
| DELETE | `/api/tickets/{id}` | Delete ticket |
| GET | `/api/tickets/dashboard` | Dashboard statistics |

**Query params (GET /api/tickets):** `page`, `size`, `sortBy`, `sortDir`

**Search params:** `ticketId`, `projectAssignment`, `status`, `priority`, `page`, `size`

---

### Projects — `/api/projects`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects` | List all projects (paginated) |
| GET | `/api/projects/search` | Search with filters |
| GET | `/api/projects/{id}` | Get project by ID |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |

**Query params (GET /api/projects):** `page`, `size`, `sortBy`, `sortDir`

**Search params:** `search` (matches name or code), `status`, `page`, `size`

**POST / PUT body:**
```json
{
  "projectCode": "HR-PORTAL",
  "projectName": "HR Management Portal",
  "description": "Internal HR tooling project",
  "status": "ACTIVE",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}
```

---

### Employees — `/api/employees`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/employees` | List all employees (paginated) |
| GET | `/api/employees/search` | Search with filters |
| GET | `/api/employees/{id}` | Get employee by ID |
| POST | `/api/employees` | Create employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |

**Search params:** `search` (matches name, email, or department), `status`, `page`, `size`

**POST / PUT body:**
```json
{
  "employeeName": "Jane Doe",
  "email": "jane.doe@company.com",
  "designation": "Senior Developer",
  "department": "Engineering",
  "assignedProject": "HR-PORTAL",
  "status": "ACTIVE"
}
```

---

## Theme System

The theme is controlled by a `data-theme` attribute on the `<html>` element, set by `ThemeService`.

| Theme | Attribute value | Stored in localStorage as |
|---|---|---|
| Dark (default) | `data-theme="dark"` | `nexus-theme=dark` |
| Light | `data-theme="light"` | `nexus-theme=light` |

All colors across every component are expressed as CSS custom properties defined in `styles.css`. Switching themes changes only the variable values — no layout, spacing, typography, or component structure changes.

The toggle button (☀ / ☾) is in the top-right of the navbar. The selected theme persists across page refreshes via `localStorage`.

---

## What Was Added (Enhancement Summary)

### Backend
- `Project` and `Employee` JPA entities with full field set and `@CreationTimestamp` / `@UpdateTimestamp`
- `ProjectRepository` and `EmployeeRepository` with JPQL search queries supporting optional `search` text and `status` filter
- `ProjectService` / `EmployeeService` interfaces and `Impl` classes with full CRUD, duplicate-check validation, and `@Transactional` boundaries
- `ProjectController` and `EmployeeController` with standard REST endpoints mirroring `TicketController` patterns
- Request and Response DTOs for both entities using the same Lombok Builder pattern
- `IllegalArgumentException` handler added to `GlobalExceptionHandler` returning HTTP 409 Conflict
- `schema.sql` extended with `projects` and `employees` tables (safe `IF NOT EXISTS`)

### Frontend
- `project.model.ts` and `employee.model.ts` with TypeScript interfaces, request types, and status label maps
- `ProjectService` and `EmployeeService` — HTTP clients wrapping all CRUD and search endpoints
- `ThemeService` — manages `data-theme` attribute and persists preference to `localStorage` using Angular signals
- `ProjectListComponent` and `EmployeeListComponent` — paginated, searchable, filterable tables with live-search debounce (400ms), delete confirmation, and inline success/error alerts
- `ProjectFormComponent` and `EmployeeFormComponent` — reactive forms with validation, edit-mode pre-fill, reset, and success redirect
- `AppComponent` updated with Projects and Employees nav links and a compact theme toggle button
- `app.routes.ts` updated with 6 new lazy-loaded routes
- `styles.css` converted to a full CSS custom property system with dark and light theme token sets
- Existing ticket component CSS files extended with theme variable overrides (appended, no existing rules removed)

---

## Authentication & Security (v2 Addition)

### Overview

JWT-based stateless authentication with Spring Security 6 and role-based access control across the full stack.

### Roles

| Role | Tickets | Projects | Employees |
|---|---|---|---|
| `ADMIN` | Full CRUD | Full CRUD | Full CRUD |
| `PROJECT_MANAGER` | View + Create + Edit | View + Create + Edit | View only |
| `EMPLOYEE` | View + Update status | No access | No access |

### Auth Endpoints

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login, returns JWT |
| GET | `/api/auth/me` | Yes | Get current user profile |

**Login request:**
```json
{ "usernameOrEmail": "admin", "password": "secret123" }
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "email": "admin@nexus.io",
    "role": "ADMIN"
  }
}
```

### New Environment Variables

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | HS256 signing secret (min 32 chars) | Built-in dev default |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |

### New Backend Files

| File | Purpose |
|---|---|
| `entity/User.java` | User JPA entity implementing `UserDetails` |
| `entity/Role.java` | Enum: `ADMIN`, `PROJECT_MANAGER`, `EMPLOYEE` |
| `repository/UserRepository.java` | User lookups by username/email |
| `dto/RegisterRequest.java` | Validated registration payload |
| `dto/LoginRequest.java` | Login credentials payload |
| `dto/AuthResponse.java` | JWT + user info returned after auth |
| `dto/UserProfileResponse.java` | `/me` endpoint response |
| `security/JwtService.java` | Token generation and validation (JJWT 0.11.5) |
| `security/JwtAuthenticationFilter.java` | `OncePerRequestFilter` — validates Bearer token |
| `config/SecurityConfig.java` | `SecurityFilterChain` (Spring Security 6, stateless) |
| `service/AuthService.java` | Auth service interface |
| `serviceImpl/AuthServiceImpl.java` | Register, login, profile logic |
| `controller/AuthController.java` | `/api/auth/**` endpoints |

### New Frontend Files

| File | Purpose |
|---|---|
| `models/auth.model.ts` | TypeScript interfaces for auth data |
| `services/auth.service.ts` | Login/logout, token storage, role helpers, Angular signals |
| `interceptors/jwt.interceptor.ts` | Attaches `Authorization: Bearer` header; handles 401 auto-logout |
| `guards/auth.guard.ts` | Redirects unauthenticated users to `/login` |
| `guards/role.guard.ts` | Redirects users without required role to `/tickets` |
| `components/login/login.component.*` | Login page matching existing app aesthetics |

### What Changed in Existing Files

| File | Change |
|---|---|
| `pom.xml` | Added `spring-boot-starter-security`, `jjwt-api/impl/jackson` |
| `schema.sql` | Added `users` table (`CREATE TABLE IF NOT EXISTS`) |
| `application.properties` | Added `jwt.secret` and `jwt.expiration` |
| `GlobalExceptionHandler.java` | Added handlers for `BadCredentialsException` and `AccessDeniedException` |
| `app.component.ts` | Added user pill (username + role badge), logout button, role-based nav visibility |
| `app.routes.ts` | All routes protected with `AuthGuard`; restricted routes use `RoleGuard` |
| `app.config.ts` | Registered `JwtInterceptor` as `HTTP_INTERCEPTORS` |
