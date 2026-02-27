const fs = require('fs');
const path = require('path');

// Configuration
const API_URL = "http://api.siliconflow.cn/v1/chat/completions";
const API_KEY = process.env.AI_API_KEY || "sk-zzfnrqhpsxrvkutyjqbkfukflugzkurpwbhmopzpsxygukqm";
const MODEL = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B";
const OUTPUT_DIR = path.join(__dirname, "../data");

const CATEGORIES = {
    policies: {
        prompt: `Generate 5 realistic Chinese government policy documents related to {domain}. 
        Output a valid JSON array of objects with these fields:
        - id: unique string (e.g., P-{domain_code}-001)
        - title: rigorous policy name
        - department: issuing department (e.g., Ministry of Industry and Information Technology, local Science Bureau)
        - content: detailed summary including support measures and funding limits (100-200 words)
        - publishDate: YYYY-MM-DD (within last 2 years)
        - policyType: one of ["Subsidy", "TaxBreak", "Grant", "Equity"]
        - industry: array of strings
        Ensure strict JSON format without markdown code blocks.`,
        domain_code: "POL"
    },
    funds: {
        prompt: `Generate 5 realistic financial products or funds for {domain} startups.
        Output a valid JSON array of objects with these fields:
        - id: unique string (e.g., F-{domain_code}-001)
        - name: product name (e.g., "Tech Innovation Loan", "Angel Fund A")
        - provider: bank or VC firm name
        - amountRange: string (e.g., "100-500万", "1000-5000万")
        - industryFocus: array of strings
        - interestRate: string (e.g., "3.5%", "Equity 10%")
        - description: brief description of eligibility
        Ensure strict JSON format without markdown code blocks.`,
        domain_code: "FND"
    },
    equipments: {
        prompt: `Generate 5 high-end research equipment items available for sharing in {domain}.
        Output a valid JSON array of objects with these fields:
        - id: unique string (e.g., E-{domain_code}-001)
        - name: equipment name (e.g., "Scanning Electron Microscope")
        - facilityName: lab or center name
        - owner: university or institute name
        - category: one of ["Analysis", "Processing", "Testing", "Computing"]
        - specs: technical specifications string
        - availability: "Available" or "Booked"
        Ensure strict JSON format without markdown code blocks.`,
        domain_code: "EQP"
    },
    experts: {
        prompt: `Generate 5 leading experts/professors in {domain}.
        Output a valid JSON array of objects with these fields:
        - id: unique string (e.g., EXP-{domain_code}-001)
        - name: full name
        - title: job title (e.g., Professor, Senior Engineer)
        - affiliation: university or institute
        - field: array of research fields
        - achievements: brief summary of major contributions
        Ensure strict JSON format without markdown code blocks.`,
        domain_code: "EXP"
    }
};

const DOMAINS = ["New Materials", "Bio-medicine", "Integrated Circuits", "Artificial Intelligence", "Green Energy"];

// Helper to delay
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function callLLM(prompt, retries = 3) {
    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`
    };
    
    const payload = {
        model: MODEL,
        messages: [{ role: "user", content: prompt }],
        temperature: 0.7,
        max_tokens: 2000
    };
    
    for (let attempt = 0; attempt < retries; attempt++) {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 120000); // 120s timeout
            
            const response = await fetch(API_URL, {
                method: "POST",
                headers: headers,
                body: JSON.stringify(payload),
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            
            if (!response.ok) {
                const text = await response.text();
                console.error(`API Error: ${response.status} - ${text}`);
                if ([429, 500, 502, 503, 504].includes(response.status)) {
                    await sleep(2000 * (attempt + 1));
                    continue;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error(`Error calling LLM (attempt ${attempt + 1}): ${error.message}`);
            if (attempt === retries - 1) return null;
            await sleep(2000 * (attempt + 1));
        }
    }
    return null;
}

async function generateBatch(category, domain, batchIndex) {
    const config = CATEGORIES[category];
    const uniqueBatchId = `${Math.floor(Date.now() / 1000)}-${batchIndex}`;
    const prompt = config.prompt
        .replace("{domain}", domain)
        .replace("{domain_code}", `${config.domain_code}-${uniqueBatchId}`);
        
    console.log(`Generating batch ${batchIndex} (ID: ${uniqueBatchId}) for ${category} in ${domain}...`);
    
    const result = await callLLM(prompt);
    
    if (result && result.choices && result.choices[0]) {
        let content = result.choices[0].message.content;
        // Clean up markdown
        content = content.replace(/```json/g, "").replace(/```/g, "").trim();
        
        try {
            const data = JSON.parse(content);
            // Ensure IDs exist
            data.forEach((item, i) => {
                if (!item.id) {
                    item.id = `${config.domain_code}-${domain.substring(0,3)}-${uniqueBatchId}-${i}`;
                }
            });
            return data;
        } catch (e) {
            console.error(`Failed to parse JSON for ${category} batch ${batchIndex}`);
            console.error(`Raw content: ${content.substring(0, 200)}...`);
        }
    }
    return [];
}

async function main() {
    // Parse args
    const args = process.argv.slice(2);
    let count = 50;
    let concurrency = 3;
    
    for (let i = 0; i < args.length; i++) {
        if (args[i] === "--count" && args[i+1]) count = parseInt(args[i+1]);
        if (args[i] === "--workers" && args[i+1]) concurrency = parseInt(args[i+1]);
    }
    
    if (!fs.existsSync(OUTPUT_DIR)) {
        fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    }
    
    for (const category of Object.keys(CATEGORIES)) {
        const outputFile = path.join(OUTPUT_DIR, `${category}.jsonl`);
        const existingIds = new Set();
        let existingCount = 0;
        
        if (fs.existsSync(outputFile)) {
            const content = fs.readFileSync(outputFile, 'utf-8');
            const lines = content.split('\n').filter(line => line.trim());
            existingCount = lines.length;
            lines.forEach(line => {
                try {
                    const doc = JSON.parse(line);
                    if (doc.id) existingIds.add(doc.id);
                } catch (e) {}
            });
        }
        
        const needed = count - existingCount;
        if (needed <= 0) {
            console.log(`Skipping ${category}, already have ${existingCount} items.`);
            continue;
        }
        
        console.log(`Generating ${needed} items for ${category}...`);
        
        const itemsPerBatch = 5;
        const batchesNeeded = Math.ceil(needed / itemsPerBatch);
        const tasks = [];
        
        for (let i = 0; i < batchesNeeded; i++) {
            const domain = DOMAINS[i % DOMAINS.length];
            const batchIndex = Math.floor(existingCount / itemsPerBatch) + i;
            
            // Concurrency control
            const task = async () => {
                const items = await generateBatch(category, domain, batchIndex);
                if (items && items.length > 0) {
                    let savedCount = 0;
                    items.forEach(item => {
                        if (item.id && existingIds.has(item.id)) {
                            console.log(`Skipping duplicate ID: ${item.id}`);
                            return;
                        }
                        fs.appendFileSync(outputFile, JSON.stringify(item) + "\n");
                        if (item.id) existingIds.add(item.id);
                        savedCount++;
                    });
                    if (savedCount > 0) console.log(`Saved ${savedCount} items to ${outputFile}`);
                }
                await sleep(1000); // Rate limit
            };
            tasks.push(task);
        }
        
        // Run tasks with limited concurrency
        for (let i = 0; i < tasks.length; i += concurrency) {
            const batch = tasks.slice(i, i + concurrency);
            await Promise.all(batch.map(t => t()));
        }
    }
}

main().catch(console.error);
