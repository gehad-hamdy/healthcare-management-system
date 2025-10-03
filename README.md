# Healthcare Management System

A Spring Boot REST API for managing healthcare facilities and patient records with AI-powered query capabilities.

## Features

- **Facility Management**: CRUD operations for healthcare facilities
- **Patient Management**: Patient registration and record management
- **AI-Powered Queries**: Natural language query processing
- **Soft Delete**: Audit-friendly deletion with proper data retention
- **Pagination & Search**: Efficient data retrieval
- **Docker Support**: Containerized deployment

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- PostgreSQL
- JPA/Hibernate
- OpenAI GPT Integration (Optional)
- Docker & Docker Compose

## Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- PostgreSQL (if running locally)

### Using Docker (Recommended)
```bash
# Clone and build
git clone https://github.com/gehad-hamdy/healthcare-management-system
cd healthcare-management-system

# Build and run
docker-compose up --build


export OPENAI_API_KEY=your_openai_api_key_here
# OR for local AI:
# export SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434
````
## ? API Endpoints

### Facility Management

| Method | Endpoint | Description | Parameters |
|-------|----------|-------------|------------|
| GET   | /api/facilities | List all facilities | page, size, type, name |
| GET   | /api/facilities/{id} | Get facility details | id (path) |
| POST  | /api/facilities | Create new facility | Facility object (JSON) |
| PUT   | /api/facilities/{id} | Update facility | id (path), Facility object |
| DELETE| /api/facilities/{id} | Soft delete facility | id (path) |

### Patient Management

| Method | Endpoint | Description | Parameters |
|-------|----------|-------------|------------|
| GET   | /api/patients | List patients | page, size, facilityId, search |
| GET   | /api/patients/{id} | Get patient details | id (path) |
| POST  | /api/patients | Register new patient | Patient object (JSON) |
| PUT   | /api/patients/{id} | Update patient | id (path), Patient object |
| DELETE| /api/patients/{id} | Remove patient | id (path) |
| GET   | /api/facilities/{id}/patients | Get patients by facility | id (path) |)

### AI-Powered Query System

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST   | /api/chat | Natural language queries | `{"query": "your question"}` |
| POST   | /api/chat/sample-patients | Get sample patient profiles | None |
| POST   | /api/chat/facility-stats | Get facility statistics | None |


```bash
# Sample patient profiles
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"query": "Show me sample patient profiles"}'

# Facility analysis
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"query": "Analyze patient distribution across facilities"}'

# Patient statistics
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"query": "What are the common medical conditions?"}'

# Complex queries
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"query": "Which facility has the most elderly patients?"}'
```
```bash
# Build images
docker-compose build

# Start services
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## ? Testing
## Run Tests
```bash
./gradlew test

# With test coverage
./gradlew jacocoTestReport
````
### API Testing Examples
```bash
# Create a facility
curl -X POST http://localhost:8080/api/facilities \
-H "Content-Type: application/json" \
-d '{
"name": "General Hospital",
"type": "HOSPITAL",
"address": "123 Main St, City, State"
}'

# Create a patient
curl -X POST http://localhost:8080/api/patients \
-H "Content-Type: application/json" \
-d '{
"firstName": "John",
"lastName": "Doe",
"dateOfBirth": "1980-01-15",
"gender": "MALE",
"medicalCondition": "Hypertension",
"facilityId": 1
}'
````
### ? Monitoring & Debugging
## Health Check
```bash
curl http://localhost:8080/actuator/health
````
## AI Provider Check
```bash
curl http://localhost:8080/api/debug/ai-provider
```