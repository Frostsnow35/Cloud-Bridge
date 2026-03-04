# Hierarchical Domain Matching Spec

## Why
Currently, searching for broad domain terms (e.g., "生物医药") fails to retrieve achievements tagged with specific sub-domain keywords (e.g., "细胞"), resulting in poor search recall. The user expects a hierarchical understanding where searching a parent term automatically includes relevant child concepts.

## What Changes

### 1. Backend (`MatchingService.java`)
- **ADD** `DomainHierarchy` Constant/Map:
    - Define explicit parent-child relationships for key domains (Biomedicine, New Materials, AI, etc.).
    - Example: `生物医药` -> `[细胞, 基因, 药物, 疫苗, 抗体, 蛋白, 免疫, 试剂, 诊断, 治疗]`
- **MODIFY** `match()` method:
    - **Keyword Expansion**: When a search keyword matches a parent term in the hierarchy, automatically add all its children terms to the search scope.
    - **Score Boosting**: Achievements matching child terms should be scored relevantly (perhaps slightly lower than exact match, but high enough to appear).
- **MODIFY** `augmentGraphWithAchievements()`:
    - **Dynamic Node Injection**: If the graph contains a parent node (e.g., "生物医药") and we find achievements matching its *children* (e.g., "细胞"), but the graph *doesn't* have the child node:
        - Dynamically create the child node ("细胞").
        - Connect Parent -> Child ("生物医药" -> "细胞").
        - Connect Child -> Achievement ("细胞" -> Achievement).
    - This ensures the "farthest specific node" logic requested previously is maintained even when the initial AI graph is too high-level.

## Impact
- **Search**: "生物医药" will now return achievements tagged "细胞", "基因" etc.
- **Graph**: The graph will become richer, automatically expanding broad nodes into specific sub-nodes when relevant achievements exist.

## ADDED Requirements
### Requirement: Domain Expansion
The system SHALL expand broad search queries into specific sub-domain keywords based on a predefined hierarchy.

### Requirement: Graph Granularity
The system SHALL dynamically generate specific sub-domain nodes in the knowledge graph if achievements match those sub-domains, ensuring achievements connect to the most specific concept available.

## MODIFIED Requirements
### Requirement: Search Relevance
Achievements matching sub-domain keywords SHALL be considered relevant matches for the parent domain query.
