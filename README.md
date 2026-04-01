# 🎓 University SIS — Student Information System

A full-stack university application built with **JavaFX** (Desktop UI), **Spring Boot** (REST API), and **MySQL** (Database), orchestrated with **Docker Compose**.

## Architecture

```
┌────────────────┐       ┌──────────────┐       ┌─────────┐
│   JavaFX UI    │──────▶│ Spring Boot  │──────▶│  MySQL  │
│  (Desktop App) │ REST  │   Backend    │  JPA  │   8.0   │
└────────────────┘       └──────────────┘       └─────────┘
   Runs natively          Docker Container     Docker Container
```

## Features

- **Students** — Add, edit, delete students with ID, name, email, major, year level
- **Doctors** — Manage professors/instructors with department, specialization, phone
- **Courses** — Create courses linked to doctors, with credits
- **Enrollments** — Enroll students in courses, assign grades, drop enrollments
- **Dashboard** — Overview stats for all entities
- **Full Sync** — Frontend and backend stay in sync after every action

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose v2)
- **Java 21 JDK** + **Maven** (for the frontend — see setup below)

---

## Quick Start

### 1. Start Backend + Database

```bash
docker compose up --build -d
```

This starts:
- **MySQL 8** on port `3307` (configurable in `.env`)
- **Spring Boot API** on port `8081` (configurable in `.env`)

### 2. Verify Backend

```bash
# Health check
curl http://localhost:8081/api/health

# PowerShell alternative
Invoke-RestMethod http://localhost:8081/api/health
```

### 3. Run the Frontend

```bash
cd frontend
mvn javafx:run
```

---

## Frontend Setup

### Option A: Windows (Native)

```powershell
# Install Java 21
winget install EclipseAdoptium.Temurin.21.JDK

# Install Maven
winget install Apache.Maven

# Restart terminal, then run:
java --version    # Should show 21.x
mvn --version     # Should show 3.9+

# Run the app
cd frontend
mvn javafx:run
```

### Option B: WSL (Linux via WSLg)

> Requires Windows 11 or Windows 10 Build 19044+ with WSLg support.

```bash
# Install dependencies
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y openjdk-21-jdk maven openjfx libopenjfx-java
sudo apt-get install -y libgtk-3-0 libgl1 libx11-6 libxxf86vm1

# Verify display
echo $DISPLAY     # Should show :0

# Run the app
cd /mnt/c/Users/yasee/Projects/JavaFXDev/frontend
mvn javafx:run
```

---

## API Endpoints

### Students — `/api/students`

| Method | Endpoint           | Description       |
|--------|--------------------|-------------------|
| GET    | /api/students      | List all          |
| GET    | /api/students/{id} | Get by ID         |
| POST   | /api/students      | Create            |
| PUT    | /api/students/{id} | Update            |
| DELETE | /api/students/{id} | Delete            |

### Doctors — `/api/doctors`

| Method | Endpoint          | Description       |
|--------|-------------------|-------------------|
| GET    | /api/doctors      | List all          |
| GET    | /api/doctors/{id} | Get by ID         |
| POST   | /api/doctors      | Create            |
| PUT    | /api/doctors/{id} | Update            |
| DELETE | /api/doctors/{id} | Delete            |

### Courses — `/api/courses`

| Method | Endpoint          | Description                    |
|--------|-------------------|--------------------------------|
| GET    | /api/courses      | List all (includes doctor)     |
| GET    | /api/courses/{id} | Get by ID                      |
| POST   | /api/courses      | Create (with `doctorId`)       |
| PUT    | /api/courses/{id} | Update                         |
| DELETE | /api/courses/{id} | Delete                         |

### Enrollments — `/api/enrollments`

| Method | Endpoint                      | Description            |
|--------|-------------------------------|------------------------|
| GET    | /api/enrollments              | List all               |
| GET    | /api/enrollments/student/{id} | By student             |
| GET    | /api/enrollments/course/{id}  | By course              |
| POST   | /api/enrollments              | Enroll (studentId + courseId) |
| PATCH  | /api/enrollments/{id}/grade   | Set grade              |
| DELETE | /api/enrollments/{id}         | Drop                   |

### Health — `/api/health`

| Method | Endpoint    | Description  |
|--------|-------------|--------------|
| GET    | /api/health | Health check |

---

## Project Structure

```
.
├── docker-compose.yml          # MySQL + Spring Boot services
├── .env                        # Credentials & ports (git-ignored)
├── .gitignore
├── README.md
│
├── backend/
│   ├── Dockerfile              # Multi-stage Maven → JRE build
│   ├── pom.xml                 # Spring Boot 3.2 + JPA + MySQL
│   └── src/main/java/com/app/
│       ├── BackendApplication.java
│       ├── model/
│       │   ├── Student.java
│       │   ├── Doctor.java
│       │   ├── Course.java        # FK → Doctor
│       │   └── Enrollment.java    # FK → Student + Course
│       ├── repository/
│       │   ├── StudentRepository.java
│       │   ├── DoctorRepository.java
│       │   ├── CourseRepository.java
│       │   └── EnrollmentRepository.java
│       └── controller/
│           ├── StudentController.java
│           ├── DoctorController.java
│           ├── CourseController.java
│           ├── EnrollmentController.java
│           └── HealthController.java
│
└── frontend/
    ├── Dockerfile
    ├── pom.xml                 # JavaFX 21 + Gson
    └── src/main/
        ├── java/com/app/frontend/
        │   ├── App.java           # Main UI (sidebar + 5 views)
        │   └── ApiClient.java     # HTTP client for REST API
        ├── java/module-info.java
        └── resources/styles.css   # Theme stylesheet
```

## Tech Stack

| Layer    | Technology             |
|----------|------------------------|
| Frontend | JavaFX 21, Gson        |
| Backend  | Spring Boot 3.2, JPA   |
| Database | MySQL 8                |
| DevOps   | Docker Compose         |
| Build    | Maven, Multi-stage Docker |

## Stop Services

```bash
docker compose down        # Stop containers
docker compose down -v     # Stop + delete data volumes
```

## Environment Variables

Configure ports and credentials in `.env`:

```env
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=appdb
MYSQL_USER=appuser
MYSQL_PASSWORD=apppass
DB_PORT=3307
BACKEND_PORT=8081
PHPMYADMIN_PORT=8080
```

---

## License

MIT
