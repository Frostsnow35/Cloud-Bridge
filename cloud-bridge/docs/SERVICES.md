# Service Management Guide

This guide explains how to manage the backend services (PostgreSQL, Neo4j, Elasticsearch) required for the Cloud Bridge platform.

## Prerequisites

- **Docker Desktop** installed and running.
- **Git** (optional, for version control operations).

## Quick Start

1. Navigate to the backend directory:
   ```bash
   cd cloud-bridge/backend
   ```

2. Start all services in the background:
   ```bash
   docker-compose up -d
   ```

3. Verify services are running:
   ```bash
   docker-compose ps
   ```

## Service Details

| Service       | Port | Credentials          | Volume Path (Local)       |
|---------------|------|----------------------|---------------------------|
| PostgreSQL    | 5432 | cloudbridge/password | `./data/postgres`         |
| Neo4j         | 7474 | neo4j/password       | `./data/neo4j`            |
| Elasticsearch | 9200 | (No Auth)            | `./data/elasticsearch`    |

## Troubleshooting

- **Connection Refused**: Ensure Docker Desktop is running and ports 5432, 7474, 7687, and 9200 are not occupied by other applications.
- **Data Persistence**: Data is persisted in the `backend/data` directory. To reset data, stop services and delete the corresponding folders in `backend/data`.

## Stopping Services

To stop and remove containers:
```bash
docker-compose down
```
