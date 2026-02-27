# UI Enhancements, Data Fixes, and Service Management Spec

## Why
To improve user experience, we need to add auto-scrolling to homepage information sections and ensure correct navigation. We also need to fix the missing "Public Data Open Platform" data caused by service unavailability and provide a clear way to manage backend services (Elasticsearch, Neo4j).

## What Changes
- **Frontend UI**:
    - **Home.vue**: Implement auto-scrolling (carousel) for "Policy News" and "Latest Demands". Fix click navigation to point to detail pages.
    - **App.vue**: Add "Public Platforms" (公共平台) to the "Resource Center" navigation menu.
- **Backend Logic**:
    - **SearchService.java**: Implement an in-memory storage mechanism. When `indexDocument` is called and ES is unavailable, store data in memory. Update `getMockData` and `search` to fallback to this memory store, ensuring CSV-loaded data is visible even without ES.
- **Service Management**:
    - **docker-compose.yml**: Add Elasticsearch service configuration.
    - **Documentation**: Create `docs/SERVICES.md` detailing how to start services and where configuration is stored.

## Impact
- **Home.vue**: Visual changes and interaction fixes.
- **App.vue**: Menu update.
- **SearchService.java**: Robustness improvement for demo/dev environments.
- **docker-compose.yml**: Infrastructure update.

## ADDED Requirements
### Requirement: Homepage Auto-Scrolling
The system SHALL display "Policy News" and "Latest Demands" in an auto-scrolling carousel or marquee format on the homepage.

### Requirement: Detail Page Navigation
Clicking on any item card on the homepage (Achievement, Policy, Demand) SHALL navigate to its specific detail page (`/libraries/policies/:id`, `/needs/:id`, etc.).

### Requirement: Public Data Visibility
The system SHALL display "Public Data Open Platform" items in the Resource Center, even if Elasticsearch is offline, by falling back to in-memory data loaded from CSV.

### Requirement: Service Configuration
The system SHALL provide a `docker-compose.yml` that includes Postgres, Neo4j, and Elasticsearch, and a `SERVICES.md` guide for startup.
