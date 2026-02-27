# Achievement Detail Page Optimization Specification

## 1. Overview
This specification details the optimization of the Achievement Detail page to improve user experience and conversion rates. The goal is to remove the complex knowledge graph visualization and replace it with more practical information such as patent certificates, detailed team profiles, and relevant service recommendations.

## 2. Requirements

### 2.1 Remove Knowledge Graph
- **Action**: Completely remove the "Knowledge Graph Analysis" section from the Achievement Detail page.
- **Reason**: The graph is deemed unnecessary for the current user flow.

### 2.2 Enhance Content Display
- **Patent Certificates**:
  - Add a new section to display patent certificates.
  - Use placeholder images for now as backend storage is not yet implemented.
  - Display multiple images in a carousel or grid layout.

- **Team/Expert Information**:
  - Enhance the sidebar "Owner Info" section.
  - Make the team avatar and name clickable.
  - **Action**: Redirect to a new "Team Detail Page" upon click.
  - **Team Detail Page**:
    - Create a new page `TeamDetail.vue`.
    - Display team profile, research focus, and a list of published achievements.
    - Use mock data for a specific team (e.g., "Professor Zhang's Team").

- **Jump Logic**:
  - **Related Technology**: If listed, clicking on a related technology name should redirect to the **Demand Hall** (`/needs`) filtered by that technology keyword.
  - **Tags/Labels**: Clicking on a tag (e.g., "New Material") should redirect to the **Achievement Hall** (`/achievements`) filtered by that tag.

### 2.3 AI-Recommended Services
- **New Section**: Add a "Recommended Services" section at the bottom of the page.
- **Content**: Display a list of service products relevant to the current achievement (e.g., "IP Assessment", "Legal Consultation").
- **Source**: These should be "existing service products" in the system. Since no `Service` entity exists, we will mock these products in the backend or frontend service layer.
- **Interaction**: Clicking a service card should ideally lead to a service detail or purchase page (mocked for now).

## 3. Technical Implementation

### 3.1 Frontend (Vue.js)
- **Files to Modify**:
  - `src/views/AchievementDetail.vue`: Remove graph, add patent section, add service section, update links.
  - `src/router/index.ts`: Add route for `TeamDetail`.
- **New Files**:
  - `src/views/TeamDetail.vue`: New page for team details.

### 3.2 Backend (Spring Boot)
- **Files to Modify**:
  - `MatchingController.java`: Update the `/match` or `/detail` endpoint to return recommended services.
  - `MatchingService.java`: Implement logic to return mock service data based on achievement context.

## 4. Data Structures (Mock)

### 4.1 Service Product
```json
{
  "id": "s1",
  "name": "Technology Valuation Service",
  "price": 5000,
  "description": "Professional assessment of technology market value.",
  "provider": "CloudBridge Legal"
}
```

### 4.2 Team Profile (Mock)
```json
{
  "id": "t1",
  "name": "Professor Zhang's Team",
  "institution": "Top Material Science Institute",
  "description": "Focusing on high-performance composite materials...",
  "achievements": [...]
}
```
