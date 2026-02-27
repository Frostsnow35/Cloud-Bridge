
const fs = require('fs');
const path = require('path');

// Configuration
const API_URL = process.env.AI_API_URL || "http://api.siliconflow.cn/v1/chat/completions";
const API_KEY = process.env.AI_API_KEY || "sk-zzfnrqhpsxrvkutyjqbkfukflugzkurpwbhmopzpsxygukqm";
const MODEL = process.env.AI_API_MODEL || "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B";
const OUTPUT_FILE = path.join(__dirname, '../data/raw_demands.json');
const ACHIEVEMENTS_FILE = path.join(__dirname, '../data/raw_achievements.json');
const BATCH_SIZE = 10; // Reduce batch size to avoid truncation
const TOTAL_BATCHES = 20; // 20 * 10 = 200 items

async function main() {
    console.log("Starting demand generation...");
    
    // 1. Read Achievements to get context
    let achievements = [];
    try {
        const data = fs.readFileSync(ACHIEVEMENTS_FILE, 'utf8');
        achievements = JSON.parse(data);
        console.log(`Loaded ${achievements.length} achievements for context.`);
    } catch (e) {
        console.error("Failed to read achievements file:", e.message);
        return;
    }

    const fields = [...new Set(achievements.map(a => a.field))];
    const titles = achievements.map(a => a.title);

    let allDemands = [];
    if (fs.existsSync(OUTPUT_FILE)) {
        try {
            const existingData = fs.readFileSync(OUTPUT_FILE, 'utf8');
            allDemands = JSON.parse(existingData);
            console.log(`Loaded ${allDemands.length} existing demands.`);
        } catch (e) {
            console.error("Failed to read existing demands, starting fresh.");
        }
    }

    let batchCount = 0;

    while (allDemands.length < 200 && batchCount < 30) { // Max 30 attempts
        batchCount++;
        console.log(`Generating batch ${batchCount} (Target: ${allDemands.length}/200)...`);
        
        // Randomly select 5 titles for context to vary the demands
        const randomTitles = titles.sort(() => 0.5 - Math.random()).slice(0, 5);
        
        try {
            const batch = await generateBatch(fields, randomTitles);
            if (batch && batch.length > 0) {
                allDemands = allDemands.concat(batch);
                console.log(`  Added ${batch.length} demands. Total: ${allDemands.length}`);
                
                // Save incrementally
                fs.writeFileSync(OUTPUT_FILE, JSON.stringify(allDemands, null, 2), 'utf8');
            }
        } catch (e) {
            console.error(`  Batch failed:`, e.message);
        }
        
        // Small delay
        await new Promise(resolve => setTimeout(resolve, 2000));
    }

    console.log(`Successfully saved ${allDemands.length} demands to ${OUTPUT_FILE}`);
}

async function generateBatch(fields, titles) {
    const prompt = `
    Generate ${BATCH_SIZE} realistic technology demands (JSON objects) for a technology transfer platform.
    
    Context:
    - The platform connects enterprises with technology providers.
    - Demands should cover these fields: ${fields.join(', ')}.
    - Some demands should be seeking solutions similar to: ${titles.join(', ')}.
    
    Schema Requirements:
    - title: Clear and specific demand title.
    - description: Brief requirements (30-80 chars).
    - field: Must be one of the provided fields.
    - budget: Numeric value (BigDecimal compatible), range 50,000 to 10,000,000.
    - deadline: Future date in ISO8601 format (e.g., "2026-12-31T23:59:59").
    - type: "NORMAL" or "REWARD".
    - ownerId: Random integer between 1 and 50.
    - status: "PUBLISHED".
    
    Output Format:
    - Return ONLY a valid JSON array of objects.
    - Do NOT include markdown formatting.
    - Do NOT include any explanation text.
    `;

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${API_KEY}`
            },
            body: JSON.stringify({
                model: MODEL,
                messages: [
                    { role: "system", content: "You are a data generator. Output valid JSON array only." },
                    { role: "user", content: prompt }
                ],
                temperature: 0.7,
                max_tokens: 4000
            })
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        let content = data.choices[0].message.content;
        
        // Clean up markdown
        content = content.replace(/```json/g, '').replace(/```/g, '').trim();
        
        // Try to fix truncated JSON if it ends abruptly
        if (!content.endsWith(']')) {
            const lastClose = content.lastIndexOf('}');
            if (lastClose > 0) {
                content = content.substring(0, lastClose + 1) + ']';
            }
        }

        const json = JSON.parse(content);
        return Array.isArray(json) ? json : (json.demands || []);
    } catch (e) {
        console.error("JSON Parse Error or API Error:", e.message);
        return [];
    }
}

main();
