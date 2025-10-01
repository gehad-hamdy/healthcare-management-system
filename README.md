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