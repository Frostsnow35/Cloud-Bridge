# Data Integration and UI Fixes Spec

## Why
The system currently relies on mock data and lacks integration with real-world datasets provided by the competition. Additionally, the user requested specific UI adjustments for "Policy News" images and the addition of a new resource category "Public Data Open Platform Library".

## What Changes
- **Data Integration**:
    - Clear existing mock data for Achievements.
    - Import real achievement data from `广州市白云区省、市级科技项目立项名单.csv`.
    - Map CSV columns to `Achievement` entity:
        - `承担单位` -> `institution` (New usage of existing field)
        - `项目名称` -> `title`
        - `支持方向` -> `description` (Partially)
        - `级别` -> `description` (Partially)
        - `项目批次` -> `description` (Partially)
    - Add random relevant background images to top achievements.
- **New Resource Category**:
    - Add "Public Data Open Platform Library" (`public_platforms`) to `SearchService`, `LibraryView`, and `ResourceDetail`.
    - Import data from `广州市白云区公共数据开放计划.csv`.
- **UI Fixes**:
    - **Home.vue**: Ensure "Policy News" cards display images/icons correctly (already implemented, just verifying).
    - **Home.vue**: Ensure "Featured Achievements" (Carousel) uses the new real data with random background images.

## Impact
- **Backend**:
    - `Achievement.java`: No schema change needed if we reuse `institution`.
    - `RAGDataSeeder.java`: Major update to parse CSVs and populate `achievements` (SQL/H2) and `public_platforms` (ES/Mock).
    - `SearchService.java`: Add support for `public_platforms` index/mock.
- **Frontend**:
    - `LibraryView.vue`: Add `public_platforms` category logic.
    - `ResourceDetail.vue`: Add display logic for `public_platforms`.
    - `Home.vue`: Ensure Featured Achievements pull from DB (which will now have real data).

## ADDED Requirements
### Requirement: Real Achievement Data
The system SHALL populate the `achievements` table with data from `广州市白云区省、市级科技项目立项名单.csv` at startup, replacing old mocks.
- Default `price` to 0.
- Default `maturity` to "待填入".
- Default `ownerId` to system user (e.g., 1).

### Requirement: Public Data Open Platform Library
The system SHALL provide a new resource library category "Public Data Open Platform Library".
- **Data Source**: `广州市白云区公共数据开放计划.csv`.
- **Fields**: Dataset Name, Domain, Format, Update Frequency, Provider (Department), Description.

### Requirement: Achievement Images
The system SHALL assign random, relevant-looking background images (from a pre-defined list of Unsplash URLs) to the top 5-10 achievements displayed on the homepage.
