const fs = require('fs');
const path = require('path');

// Constants
const SCORE_FIELD_MATCH = 100;
const SCORE_KEYWORD_MATCH = 50;
const SCORE_GRAPH_MATCH = 20;
const SCORE_RELATED_MATCH = 10;
const THRESHOLD_STRONG_MATCH = 100;
const THRESHOLD_MIN_SCORE = 30;

// Load Data
const achievementsPath = path.join(__dirname, '../data/raw_achievements.json');
const cypherPath = path.join(__dirname, '../src/main/resources/cypher/import.cypher');

const achievements = JSON.parse(fs.readFileSync(achievementsPath, 'utf8'));
const cypherContent = fs.readFileSync(cypherPath, 'utf8');

// Build Simple Graph from Cypher
const graph = {
    nodes: {}, // id -> {type, name}
    edges: []  // {from, to, type}
};

// Helper to parse Cypher (very basic parser for the generated file)
const lines = cypherContent.split('\n');
const nodeMap = {}; // varName -> id (name)

lines.forEach(line => {
    line = line.trim();
    if (!line || line.startsWith('//')) return;

    // CREATE (var:Type {key: 'val', ...})
    const createNodeMatch = line.match(/CREATE \(([^:]+):([^ ]+) \{name: '([^']+)'/);
    if (createNodeMatch) {
        const [_, varName, type, name] = createNodeMatch;
        const categoryMatch = line.match(/category: '([^']+)'/);
        const category = categoryMatch ? categoryMatch[1] : null;
        
        nodeMap[varName] = name;
        graph.nodes[name] = { type, name, category };
    }

    // CREATE (var1)-[:TYPE]->(var2)
    const createEdgeMatch = line.match(/CREATE \(([^)]+)\)-\[:([^\]]+)\]->\(([^)]+)\)/);
    if (createEdgeMatch) {
        const [_, srcVar, relType, dstVar] = createEdgeMatch;
        const src = nodeMap[srcVar];
        const dst = nodeMap[dstVar];
        if (src && dst) {
            graph.edges.push({ from: src, to: dst, type: relType });
        }
    }
});

// Mock AI Service
function mockAIService(text) {
    // For test case: "AI-powered diagnostic tool for early disease detection"
    if (text.toLowerCase().includes("ai") && text.toLowerCase().includes("diagnostic")) {
        return {
            keyword: "AI",
            field: "人工智能",
            graph: {
                nodes: [
                    { id: "AI", label: "人工智能", type: "Field" },
                    { id: "Diagnostic", label: "医疗诊断", type: "Technology" }
                ],
                relationships: []
            }
        };
    }
    return { keyword: "", field: "" };
}

// Matching Logic
function match(demandDescription) {
    console.log(`Matching for: "${demandDescription}"`);
    const aiResult = mockAIService(demandDescription);
    const keyword = aiResult.keyword;
    const field = aiResult.field;
    
    console.log(`AI Extracted: Keyword="${keyword}", Field="${field}"`);

    const scoredMatches = new Map(); // id -> {achievement, score}

    function addScore(achievement, points, reason) {
        if (!scoredMatches.has(achievement.ownerId + "_" + achievement.title)) { // Use composite key simulation
             scoredMatches.set(achievement.ownerId + "_" + achievement.title, { achievement, score: 0, reasons: [] });
        }
        const entry = scoredMatches.get(achievement.ownerId + "_" + achievement.title);
        entry.score += points;
        entry.reasons.push(`${reason} (+${points})`);
    }

    // 1. Find related technologies from Graph
    // In Java: technologyRepository.findRelatedTechnologies(keyword)
    // Here: BFS/Traversal in our graph
    const relatedKeywords = new Set();
    // Simple 1-hop traversal from keyword
    if (keyword) {
        // Find node with name == keyword (or similar)
        // Our graph nodes are indexed by name
        // Check if keyword maps to a node
        let startNode = null;
        // Try exact match
        if (graph.nodes[keyword]) startNode = keyword;
        // Try finding if keyword is a substring of any node
        if (!startNode) {
            for (const name in graph.nodes) {
                if (name.includes(keyword) || keyword.includes(name)) {
                    startNode = name;
                    break;
                }
            }
        }
        
        if (startNode) {
            console.log(`Found graph node for keyword: ${startNode}`);
            // Find neighbors
            graph.edges.forEach(edge => {
                if (edge.from === startNode) relatedKeywords.add(edge.to);
                if (edge.to === startNode) relatedKeywords.add(edge.from);
            });
        }
    }
    
    // Also add related from Field
    if (field) {
         graph.edges.forEach(edge => {
             if (edge.to === field && edge.type === 'BELONGS_TO') {
                 relatedKeywords.add(edge.from);
             }
         });
    }

    console.log(`Related Keywords from Graph: ${Array.from(relatedKeywords).join(", ")}`);

    // 2. Scoring

    // 2.1 Field Match
    if (field) {
        achievements.forEach(a => {
            if (a.field === field && a.status === 'PUBLISHED') {
                addScore(a, SCORE_FIELD_MATCH, `Field Match (${field})`);
            }
        });
    }

    // 2.2 Keyword Match
    if (keyword) {
        achievements.forEach(a => {
            if ((a.title.includes(keyword) || a.description.includes(keyword)) && a.status === 'PUBLISHED') {
                addScore(a, SCORE_KEYWORD_MATCH, `Keyword Match (${keyword})`);
            }
        });
    }

    // 2.3 Related Keyword Match
    relatedKeywords.forEach(k => {
        if (k === keyword) return;
        achievements.forEach(a => {
            if ((a.title.includes(k) || a.description.includes(k)) && a.status === 'PUBLISHED') {
                addScore(a, SCORE_RELATED_MATCH, `Related Match (${k})`);
            }
        });
    });

    // 2.4 Graph Match (Simulated from AIService.extractGraphData)
    // The Java code adds score if it finds achievements related to graph nodes.
    // Here we can assume if an achievement matches a related keyword, it's also a graph match candidate?
    // In Java code: `augmentGraphWithAchievements` adds SCORE_GRAPH_MATCH.
    // It finds achievements matching the labels of nodes in the returned graph.
    // Our mock AI returns "Diagnostic" (医疗诊断).
    // Let's verify if we have "医疗诊断" in our achievements.
    // Or if we have nodes in our graph that match achievements.
    
    // Let's simplify: Any achievement that matched a related keyword is conceptually a "Graph Match" in this simulation
    // because we derived related keywords from the graph.
    // But strictly speaking, the Java code separates them.
    // Let's add base score if any related match occurred? No, let's follow the logic.
    // Java: `augmentGraphWithAchievements` checks if achievement matches node label.
    // Our graph nodes are `relatedKeywords`.
    relatedKeywords.forEach(k => {
         achievements.forEach(a => {
            if ((a.title.includes(k) || a.description.includes(k)) && a.status === 'PUBLISHED') {
                 // Check if we already added this as related match? Yes.
                 // But Java code treats them separately.
                 // Let's add +20 if it matches a graph node (which is what related keywords are).
                 addScore(a, SCORE_GRAPH_MATCH, `Graph Node Match (${k})`);
            }
        });
    });


    // 3. Sort and Filter
    const sorted = Array.from(scoredMatches.values()).sort((a, b) => b.score - a.score);

    const hasStrongMatch = sorted.some(s => s.score >= THRESHOLD_STRONG_MATCH);
    let finalMatches = sorted;

    if (hasStrongMatch) {
        console.log("Strong match detected. Filtering low scores...");
        finalMatches = sorted.filter(s => s.score >= THRESHOLD_MIN_SCORE);
    }

    return finalMatches.map(s => ({
        title: s.achievement.title,
        score: s.score,
        field: s.achievement.field,
        reasons: s.reasons
    }));
}

// Execute Test
const testDemand = "AI-powered diagnostic tool for early disease detection";
const results = match(testDemand);

console.log("\n=== Test Results ===");
results.forEach((r, i) => {
    console.log(`${i + 1}. [${r.score}] ${r.title} (${r.field})`);
    // console.log(`   Reasons: ${r.reasons.join(", ")}`);
});

// Verification Check
const hasMedical = results.some(r => r.field === "生物医药" || r.field === "人工智能");
const hasTransport = results.some(r => r.field === "交通" || r.field === "新能源"); // Assuming Transport ~ NewEnergy/Material in error cases

console.log("\n=== Verification ===");
if (hasMedical) console.log("PASS: Found Medical/AI achievements.");
else console.log("FAIL: No Medical/AI achievements found.");

if (!hasTransport) console.log("PASS: No unrelated (Transport/Energy) achievements found (or filtered out).");
else {
    // Check if they are low score?
    const transport = results.filter(r => r.field === "交通" || r.field === "新能源");
    console.log(`WARNING: Found ${transport.length} Transport/Energy achievements.`);
    transport.forEach(t => console.log(`   - ${t.title}: ${t.score}`));
}
