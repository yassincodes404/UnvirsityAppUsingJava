# JavaFX + Spring Boot + MySQL (Docker Compose)

A full-stack application with clean separation of concerns.

## Architecture

```
┌─────────────┐       ┌──────────────┐       ┌─────────┐
│  JavaFX UI  │──────▶│ Spring Boot  │──────▶│  MySQL  │
│  (Desktop)  │ REST  │   Backend    │  JPA  │   8.0   │
└─────────────┘       └──────────────┘       └─────────┘
      Native          Docker Container     Docker Container
```

## Prerequisites

- **Docker Desktop** (with Docker Compose v2)
- **Java 21** (for running the frontend locally)
- **Maven 3.9+** (for building the frontend locally)

## Quick Start

### 1. Start Backend + Database (Docker)

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

# List items
curl http://localhost:8081/api/items

# Create item
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"Hello Docker"}'
```

### 3. Run Frontend (Native)

```bash
cd frontend
mvn javafx:run
```

## API Endpoints

| Method  | Endpoint          | Description       |
|---------|-------------------|--------------------|
| GET     | /api/health       | Health check       |
| GET     | /api/items        | List all items     |
| GET     | /api/items/{id}   | Get item by ID     |
| POST    | /api/items        | Create new item    |
| PUT     | /api/items/{id}   | Update item        |
| DELETE  | /api/items/{id}   | Delete item        |

## Project Structure

```
.
├── docker-compose.yml
├── .env
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/app/
│       ├── BackendApplication.java
│       ├── model/Item.java
│       ├── repository/ItemRepository.java
│       └── controller/
│           ├── ItemController.java
│           └── HealthController.java
├── frontend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/app/frontend/
│       ├── App.java
│       └── ApiClient.java
└── README.md
```

## Stop Services

```bash
docker compose down          # Stop containers
docker compose down -v       # Stop + delete volumes (data)
```

## Environment Variables

See `.env` file for database credentials and port configuration.
