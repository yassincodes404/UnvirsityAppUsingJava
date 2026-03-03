# JavaFX + Spring Boot + MySQL (Docker Compose)

A full-stack application with clean separation of concerns.

## Architecture

```
┌─────────────┐       ┌──────────────┐       ┌─────────┐
│  JavaFX UI  │──────▶│ Spring Boot  │──────▶│  MySQL  │
│  (Desktop)  │ REST  │   Backend    │  JPA  │   8.0   │
└─────────────┘       └──────────────┘       └─────────┘
    Native/WSL        Docker Container     Docker Container
```

---

## 1. Start Backend + Database (Docker)

> These steps are the same regardless of how you run the frontend.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose v2)

### Start Services

```bash
docker compose up --build -d
```

This starts:
- **MySQL 8** on port `3307` (configurable in `.env`)
- **Spring Boot API** on port `8081` (configurable in `.env`)

### Verify Backend

```bash
# Health check
curl http://localhost:8081/api/health

# List items
curl http://localhost:8081/api/items

# Create item (Linux/Mac/WSL)
curl -X POST http://localhost:8081/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"Hello Docker"}'

# Create item (PowerShell)
Invoke-RestMethod -Uri http://localhost:8081/api/items -Method POST `
  -ContentType "application/json" `
  -Body (@{name="Test";description="Hello Docker"} | ConvertTo-Json)
```

---

## 2. Run the JavaFX Frontend

Choose **one** of the two methods below.

---

### Option A: Run on Windows (Native)

#### Install Dependencies (Fresh Windows)

1. **Install Java 21 (JDK)**

   Download and install from [Adoptium](https://adoptium.net/temurin/releases/?version=21) (Eclipse Temurin JDK 21).

   Or install via [winget](https://learn.microsoft.com/en-us/windows/package-manager/winget/):
   ```powershell
   winget install EclipseAdoptium.Temurin.21.JDK
   ```

   Verify:
   ```powershell
   java --version
   # Should show: openjdk 21.x.x
   ```

2. **Install Maven**

   Download from [maven.apache.org](https://maven.apache.org/download.cgi) and add `bin/` to your PATH.

   Or install via winget:
   ```powershell
   winget install Apache.Maven
   ```

   Verify:
   ```powershell
   mvn --version
   # Should show: Apache Maven 3.9.x
   ```

   > **Note:** You may need to restart your terminal after installing Java/Maven for PATH changes to take effect.

#### Run the Frontend

```powershell
cd frontend
mvn javafx:run
```

The JavaFX window will open and connect to the backend at `http://localhost:8081`.

---

### Option B: Run on WSL (Linux via WSLg)

WSLg (Windows Subsystem for Linux GUI) lets you run Linux GUI apps directly from WSL with automatic display forwarding.

#### Prerequisites

- **Windows 11** (or Windows 10 Build 19044+ with WSLg support)
- **WSL 2** with a Linux distro (Ubuntu recommended)

#### Install Dependencies (Fresh WSL Ubuntu)

Open your WSL terminal and run:

```bash
# 1. Update packages
sudo apt-get update && sudo apt-get upgrade -y

# 2. Install Java 21 (JDK)
sudo apt-get install -y openjdk-21-jdk

# Verify
java --version
# Should show: openjdk 21.x.x

# 3. Install Maven
sudo apt-get install -y maven

# Verify
mvn --version
# Should show: Apache Maven 3.8.x+

# 4. Install JavaFX system libraries (required for GUI rendering)
sudo apt-get install -y openjfx libopenjfx-java

# 5. Install GTK and display libraries (needed by JavaFX)
sudo apt-get install -y libgtk-3-0 libgl1 libx11-6 libxxf86vm1
```

#### Verify WSLg Display

```bash
# Check that DISPLAY is set (WSLg sets this automatically)
echo $DISPLAY
# Should output: :0  (or similar)

# Quick test — open a GUI app
sudo apt-get install -y x11-apps
xeyes
# If a pair of eyes appears on screen, WSLg is working!
```

> **Troubleshooting:** If `DISPLAY` is empty or xeyes fails:
> ```bash
> # Make sure WSL is updated
> wsl --update         # Run this in PowerShell (not WSL)
>
> # Set DISPLAY manually (fallback)
> export DISPLAY=:0
> ```

#### Run the Frontend

```bash
# Navigate to the project (Windows drives are mounted under /mnt/)
cd /mnt/c/Users/yasee/Projects/JavaFXDev/frontend

# Run the JavaFX app
mvn javafx:run
```

The JavaFX window will appear on your Windows desktop via WSLg.

---

## API Endpoints

| Method | Endpoint        | Description    |
|--------|-----------------|----------------|
| GET    | /api/health     | Health check   |
| GET    | /api/items      | List all items |
| GET    | /api/items/{id} | Get item by ID |
| POST   | /api/items      | Create item    |
| PUT    | /api/items/{id} | Update item    |
| DELETE | /api/items/{id} | Delete item    |

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
