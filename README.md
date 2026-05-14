# 🛒 Store 

A Spring Boot REST API for managing **customers**, **products**, and **orders** in a local store.
Built with Java 21, PostgreSQL, Redis (caching), Liquibase (migrations), and fully containerised with Docker.

---

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Data Model](#data-model)
- [Running the Application](#running-the-application)
    - [Option 1: With Docker (Recommended)](#option-1-with-docker-recommended)
    - [Option 2: Without Docker](#option-2-without-docker)
- [API Endpoints & curl Examples](#api-endpoints--curl-examples)
    - [Customer](#customer)
    - [Product](#product)
    - [Order](#order)
- [Interactive API Docs](#interactive-api-docs)
- [API Testing with Newman](#api-testing-with-newman)
- [CI Pipeline](#ci-pipeline)
- [Scope for Improvement](#scope-for-improvement)

---

## Tech Stack

| Layer         | Technology                      |
|---------------|---------------------------------|
| Language      | Java 21                         |
| Framework     | Spring Boot 3.4                 |
| Database      | PostgreSQL 16                   |
| Caching       | Redis 7                         |
| Migrations    | Liquibase                       |
| Build Tool    | Gradle (Wrapper)                |
| Containerise  | Docker + Docker Compose         |
| API Spec      | OpenAPI 3 / Swagger UI          |
| Code Style    | Spotless (Palantir Java Format) |
| Test Coverage | JaCoCo - **99% line coverage**  |
| Logging       | Slf4j                           |

---

## Data Model

```
Customer  1 ──── * Order * ──── * Product
```

- A **Customer** has an ID and a name. They can have zero or more orders.
- An **Order** has an ID, a description, belongs to one customer, and contains one or more products.
- A **Product** has an ID and a description. It can appear in multiple orders.

---

## Running the Application

### Option 1: With Docker (Recommended)

#### Prerequisites

| Requirement | Version  |
|-------------|----------|
| Docker      | 20.10+   |
| Docker Compose | v2+   |

> No Java, PostgreSQL, or Redis installation needed — everything runs inside containers.

#### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Subham-CB/store.git
cd store
```

```bash
# 2. (Optional) Customise credentials — defaults are admin/admin
#    Create a .env file if you want to override:
#    POSTGRES_USER=admin
#    POSTGRES_PASSWORD=admin
#    POSTGRES_DB=store

# 3. Build and start all services (app + postgres + redis)
docker compose up --build
# 4. The API is now available at:
#    http://localhost:8080
```

To run in detached mode (background):

```bash
docker compose up --build -d
```

To stop all services:

```bash
docker compose down
```

To stop and remove persistent volumes (wipes database data):

```bash
docker compose down -v
```

---

### Option 2: Without Docker

#### Prerequisites

| Requirement     | Version / Notes                                      |
|-----------------|------------------------------------------------------|
| Java (JDK)      | 21 (Temurin recommended)                             |
| PostgreSQL      | 16.x — must be running on `localhost:5433`           |
| Redis           | 7.x — must be running on `localhost:6379`            |

#### PostgreSQL Setup

Create a database and user matching the application defaults:

```bash
# Start a PostgreSQL instance via Docker (easiest approach for local dev)
docker run -d \
  --name store-postgres \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -p 5433:5432 \
  postgres:16-alpine
```

Or if PostgreSQL is already installed locally, create the database manually:

```sql
CREATE USER admin WITH PASSWORD 'admin';
CREATE DATABASE store OWNER admin;
```

#### Redis Setup

```bash
# Start a Redis instance via Docker
docker run -d \
  --name store-redis \
  -p 6379:6379 \
  redis:7-alpine
```

Or install Redis locally and ensure it is running on port `6379`.

#### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Subham-CB/store.git
cd store

# 2. Grant execute permission to Gradle wrapper (Linux/macOS only)
chmod +x gradlew

# 3. Run the application
./gradlew bootRun

# Windows:
# gradlew.bat bootRun

# 4. The API is now available at:
#    http://localhost:8080
```

Liquibase will automatically apply all database migrations and seed sample data on startup.

---

## API Endpoints & curl Examples

> Base URL: `http://localhost:8080`
>
> All request/response bodies are `application/json`.
>
> **Pagination parameters** (optional, supported on all list endpoints):
> - `page` — 0-based page number (default: `0`)
> - `limit` — items per page (default: `30`)
> - `sortBy` — field name to sort by
> - `sortDir` — `ASC` or `DESC`

---

### Customer

#### Create a Customer

```bash
curl -X POST http://localhost:8080/customer \
  -H "Content-Type: application/json" \
  -d '{"name": "John Clair"}'
```

#### Get All Customers

```bash
curl http://localhost:8080/customer
```

#### Get All Customers — with Pagination & Sorting

```bash
curl "http://localhost:8080/customer?page=0&limit=10&sortBy=name&sortDir=ASC"
```

#### Get All Customers — Filter by Name (substring match)

```bash
curl "http://localhost:8080/customer?name=john"
```

#### Get a Customer by ID

```bash
curl http://localhost:8080/customer/1
```

---

### Product

#### Create a Product

```bash
curl -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -d '{"description": "Wireless Keyboard"}'
```

#### Get All Products

```bash
curl http://localhost:8080/product
```

#### Get All Products — with Pagination & Sorting

```bash
curl "http://localhost:8080/product?page=0&limit=10&sortBy=description&sortDir=ASC"
```

#### Get a Product by ID

```bash
curl http://localhost:8080/product/1
```

---

### Order

#### Create an Order

```bash
curl -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Office supplies order",
    "customerId": 1,
    "productIds": [1, 2]
  }'
```

> `productIds` must contain **at least 1** existing product ID. `customerId` must reference an existing customer.

#### Get All Orders

```bash
curl http://localhost:8080/order
```

#### Get All Orders — with Pagination & Sorting

```bash
curl "http://localhost:8080/order?page=0&limit=10&sortBy=id&sortDir=DESC"
```

#### Get an Order by ID

```bash
curl http://localhost:8080/order/1
```

---

## Interactive API Docs

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI spec is served at:

```
http://localhost:8080/v3/api-docs
```

---

## API Testing with Newman

The `postman/` directory contains a Postman collection and environment file that can be used to run the full API test suite via [Newman](https://github.com/postmanlabs/newman) — no Postman desktop app required.

```
postman/
├── postman_collection.json   # All requests + test assertions
└── environment.json          # Environment variables (baseUrl, IDs)
```

### Requests Covered

| Folder    | Request                     | Method | Assertions                          |
|-----------|-----------------------------|--------|-------------------------------------|
| Customers | Get All Customers           | GET    | Status 200, response is array       |
| Customers | Get Customer by ID          | GET    | Status 200 or 404                   |
| Customers | Search Customers by Name    | GET    | Status 200, response is array       |
| Customers | Create Customer             | POST   | Status 201, saves `{{customerId}}`  |
| Products  | Get All Products            | GET    | Status 200, response is array       |
| Products  | Get Product by ID           | GET    | Status 200 or 404                   |
| Products  | Create Product              | POST   | Status 201, saves `{{productId}}`   |
| Orders    | Get All Orders              | GET    | Status 200, response is array       |
| Orders    | Get Order by ID             | GET    | Status 200 or 404                   |
| Orders    | Create Order                | POST   | Status 201, saves `{{orderId}}`     |

### Prerequisites

- Docker installed and running
- The Store API must be running on port `8080`  
  (either via `docker compose up` or `./gradlew bootRun`)

### Running the Tests

> Newman runs inside Docker

**macOS / Linux:**

```bash
docker run --rm \
  -v $(pwd):/etc/newman \
  postman/newman run postman/postman_collection.json \
  --environment postman/environment.json \
  --env-var "baseUrl=http://host.docker.internal:8080" \
  -r cli
```

**Windows (PowerShell):**

```powershell
docker run --rm `
  -v ${PWD}:/etc/newman `
  postman/newman run postman/postman_collection.json `
  --environment postman/environment.json `
  --env-var "baseUrl=http://host.docker.internal:8080" `
  -r cli
```

**Windows (CMD):**

```cmd
docker run --rm -v %cd%:/etc/newman postman/newman run postman/postman_collection.json --environment postman/environment.json --env-var "baseUrl=http://host.docker.internal:8080" -r cli
```

## CI Pipeline

The project uses a GitHub Actions pipeline (`.github/workflows/`) that triggers on every push and pull request to `main`.

### Pipeline Stages

| Stage             | What it does                                                                 |
|-------------------|------------------------------------------------------------------------------|
| **Build & Test**  | Checks code formatting (Spotless), runs all tests, generates JaCoCo coverage report |
| **Docker**        | Builds and pushes the Docker image to Docker Hub (only on merge to `main`)   |

### Docker Images (on merge to `main`)

```
<DOCKERHUB_USERNAME>/store:latest
<DOCKERHUB_USERNAME>/store:sha-<git-sha>
```

### Required GitHub Secrets

| Secret               | Description                        |
|----------------------|------------------------------------|
| `DOCKERHUB_USERNAME` | Your Docker Hub username           |
| `DOCKERHUB_TOKEN`    | Your Docker Hub access token       |

---

## Scope for Improvement

### 🔐 Authentication
The API is currently fully open with no authentication. JWT-based authentication
should be integrated via Spring Security so that all endpoints require a valid bearer token before responding.

### 🔐 Authorisation
Role-based Access Control (RBAC) should be introduced to define roles (e.g. `ADMIN`, `USER`),
restricting write operations (`POST`, `PUT`, `DELETE`) to privileged users while allowing reads for
standard authenticated users.

### 🔧 Missing Endpoints
The API currently only supports **create** and **read** operations. The following are missing:
- `PUT` / `PATCH` — update an existing Customer, Product, or Order
- `DELETE` — remove a resource


### 📊 Spring Actuator
Integrate Spring Boot Actuator to expose production-ready operational endpoints such as `/actuator/health`,
`/actuator/metrics`, and `/actuator/info`. 

---