[README.md](https://github.com/user-attachments/files/27791165/README.md)
# TicketOps — Enterprise Ticket Management System

A full-stack ticket management application with a Spring Boot REST API backend and Angular 17 frontend featuring a premium dark enterprise UI.

---

## Tech Stack

| Layer     | Technology                              |
|-----------|----------------------------------------|
| Frontend  | Angular 17 (Standalone), Angular Material |
| Backend   | Spring Boot 3.2, Spring Data JPA       |
| Database  | MySQL 8.x                              |
| Language  | Java 17, TypeScript                    |
| ORM       | Hibernate via Spring Data JPA          |

---

## Project Structure

```
ticket-system/
├── backend/                          # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/ticketsystem/
│       ├── TicketManagementApplication.java
│       ├── config/CorsConfig.java
│       ├── entity/                   # JPA Entities
│       │   ├── Project.java
│       │   ├── Employee.java
│       │   └── Ticket.java
│       ├── dto/                      # Data Transfer Objects
│       │   ├── ProjectDTO.java
│       │   ├── EmployeeDTO.java
│       │   ├── TicketDTO.java
│       │   ├── TicketFilterDTO.java
│       │   └── ApiResponse.java
│       ├── repository/               # Spring Data Repositories
│       │   ├── ProjectRepository.java
│       │   ├── EmployeeRepository.java
│       │   ├── TicketRepository.java
│       │   └── TicketSpecification.java
│       ├── service/                  # Business Logic
│       │   ├── ProjectService.java
│       │   ├── EmployeeService.java
│       │   └── TicketService.java
│       ├── controller/               # REST Controllers
│       │   ├── ProjectController.java
│       │   ├── EmployeeController.java
│       │   └── TicketController.java
│       └── exception/                # Error Handling
│           ├── ResourceNotFoundException.java
│           └── GlobalExceptionHandler.java
│
└── frontend/                         # Angular 17 application
    ├── angular.json
    ├── package.json
    └── src/
        ├── index.html
        ├── main.ts
        ├── styles.css                # Global dark theme
        └── app/
            ├── app.config.ts
            ├── app.routes.ts
            ├── app.component.ts
            ├── models/models.ts      # TypeScript interfaces
            ├── services/
            │   ├── api.service.ts    # HTTP API calls
            │   └── toast.service.ts  # Notifications
            └── components/
                ├── ticket-form/      # Create / Edit ticket
                ├── ticket-list/      # Searchable ticket grid
                └── ticket-detail/    # Ticket view page
```

---

## Prerequisites

- **Java 17+** — https://adoptium.net/
- **Maven 3.8+** — https://maven.apache.org/
- **MySQL 8.x** — https://dev.mysql.com/downloads/
- **Node.js 18+** — https://nodejs.org/
- **Angular CLI 17** — `npm install -g @angular/cli@17`

---

## Setup Instructions

### Step 1 — MySQL Database

```sql
-- Run the schema file:
mysql -u root -p < backend/src/main/resources/schema-and-data.sql

-- Or manually:
CREATE DATABASE ticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticket_db;
-- (Tables auto-created by Hibernate on first run)
```

### Step 2 — Configure Database Credentials

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticket_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root          # ← Your MySQL username
spring.datasource.password=root          # ← Your MySQL password
```

### Step 3 — Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs at: **http://localhost:8080**

Verify it's working:
```bash
curl http://localhost:8080/api/projects
```

### Step 4 — Load Sample Data

```bash
mysql -u root -p ticket_db < backend/src/main/resources/schema-and-data.sql
```

This seeds:
- 5 Projects (HR Portal, ERP System, Telemedicine, Payroll System, Inventory Management)
- 6 Employees (John.D, Smith.K, David.R, Maria.T, Alex.P, Chen.W)
- 8 sample tickets with various statuses and priorities

### Step 5 — Start Frontend

```bash
cd frontend
npm install
ng serve
```

Frontend runs at: **http://localhost:4200**

---

## REST API Reference

### Projects

| Method | Endpoint             | Description      |
|--------|---------------------|------------------|
| GET    | /api/projects        | Get all projects |
| GET    | /api/projects/{id}   | Get by ID        |
| POST   | /api/projects        | Create project   |
| PUT    | /api/projects/{id}   | Update project   |
| DELETE | /api/projects/{id}   | Delete project   |

### Employees

| Method | Endpoint              | Description       |
|--------|----------------------|-------------------|
| GET    | /api/employees        | Get all employees |
| GET    | /api/employees/{id}   | Get by ID         |
| POST   | /api/employees        | Create employee   |
| PUT    | /api/employees/{id}   | Update employee   |
| DELETE | /api/employees/{id}   | Delete employee   |

### Tickets

| Method | Endpoint              | Description              |
|--------|----------------------|--------------------------|
| GET    | /api/tickets          | Get all (paginated + filtered) |
| GET    | /api/tickets/{id}     | Get by ID                |
| POST   | /api/tickets          | Create ticket            |
| PUT    | /api/tickets/{id}     | Update ticket            |
| DELETE | /api/tickets/{id}     | Delete ticket            |

**GET /api/tickets Query Parameters:**

| Param          | Type    | Example         |
|----------------|---------|-----------------|
| page           | int     | 0               |
| size           | int     | 10              |
| sortBy         | string  | createdAt       |
| sortDir        | string  | desc / asc      |
| ticketNumber   | string  | INC-1001        |
| projectId      | long    | 1               |
| employeeId     | long    | 2               |
| priority       | string  | P1 - Critical   |
| currentStatus  | string  | Open            |
| supportLevel   | string  | L2              |

**Example Request — Create Ticket:**
```json
POST /api/tickets
{
  "projectId": 1,
  "issueDescription": "Users unable to access the HR dashboard after recent update.",
  "assignedEmployeeId": 2,
  "supportLevel": "L2",
  "priority": "P2 - High",
  "currentStatus": "Open",
  "remarks": "Reported by 5 users"
}
```

**Example Response:**
```json
{
  "success": true,
  "message": "Ticket created successfully",
  "data": {
    "id": 9,
    "ticketNumber": "INC-1009",
    "projectName": "HR Portal",
    "issueDescription": "Users unable to access...",
    "currentStatus": "Open",
    "priority": "P2 - High",
    "generationDatetime": "2024-01-15T10:30:00",
    ...
  }
}
```

---

## Application Features

### Ticket Entry Form
- Auto-generated Ticket ID (INC-1001, INC-1002...)
- Auto-populated generation timestamp (read-only)
- Response datetime picker with auto-calculated resolution time
- Conditional validation: Resolution Details required for Resolved/Closed
- Form reset functionality

### Ticket List Page
- Paginated table with sorting on all columns
- 6-field filter bar (Ticket ID, Project, Employee, Priority, Status, Level)
- Color-coded status badges and priority badges
- Inline action buttons (View, Edit, Delete)

### Ticket Detail Page
- Full ticket information display
- Timeline section with resolution time calculation
- Edit and Delete actions

### UI Theme
- Dark navy (#0a0f1e) primary background
- IBM Plex Sans + IBM Plex Mono typography
- Blue accent system (#2563eb)
- Color-coded badges for all statuses and priorities
- Responsive grid layout

---

## Troubleshooting

**CORS error in browser:**
Make sure backend is running on port 8080. The CORS config allows `http://localhost:4200`.

**Database connection failed:**
Check MySQL is running: `mysql -u root -p -e "SELECT 1"`
Verify credentials in `application.properties`.

**Angular fails to start:**
Ensure Node 18+ is installed: `node --version`
Delete `node_modules` and re-run `npm install`.

**Tickets not loading:**
Open browser DevTools → Network tab — check if API calls to `localhost:8080` succeed.
