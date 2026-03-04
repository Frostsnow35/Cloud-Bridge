# Tasks

- [ ] Task 1: Create Domain Hierarchy Map
  - [ ] Define `DOMAIN_HIERARCHY` constant in `MatchingService`.
  - [ ] Include mappings for Biomedicine (细胞, 基因...), New Materials (石墨烯, 纳米...), AI (深度学习, 机器学习...), IT (大数据, 云计算...), etc.

- [ ] Task 2: Implement Keyword Expansion in Search
  - [ ] Modify `match()` method to expand `keyword` and `effectiveField` using `DOMAIN_HIERARCHY`.
  - [ ] Search `achievementRepository` for achievements matching *expanded keywords*.
  - [ ] Score these matches (maybe slightly lower, e.g., `SCORE_FIELD_MATCH` or `SCORE_KEYWORD_MATCH` * 0.8).

- [ ] Task 3: Enhance Graph Generation with Hierarchy
  - [ ] Modify `augmentGraphWithAchievements()`:
    - [ ] When iterating graph nodes, check if a node is a parent term in `DOMAIN_HIERARCHY`.
    - [ ] If yes, check if any *child terms* have matching achievements.
    - [ ] If yes, dynamically create the child node in the graph (if not present).
    - [ ] Connect Parent Node -> Child Node.
    - [ ] Connect Child Node -> Achievement.
    - [ ] Ensure "Leaf Node" logic still applies (achievements only connect to the new child node).
