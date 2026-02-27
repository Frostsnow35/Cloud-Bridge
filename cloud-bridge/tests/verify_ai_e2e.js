
async function verify() {
    console.log("Starting verification...");

    // 1. Verify Frontend
    try {
        console.log("Checking Frontend (http://localhost:5173/)...");
        const feRes = await fetch("http://localhost:5173/");
        if (feRes.ok) {
            console.log("✅ Frontend is reachable (Status: " + feRes.status + ")");
        } else {
            console.error("❌ Frontend returned status: " + feRes.status);
        }
    } catch (e) {
        console.error("❌ Frontend check failed: " + e.message);
    }

    // 2. Verify Backend Achievements API
    try {
        console.log("\nChecking Backend API (http://localhost:8080/api/achievements)...");
        const beRes = await fetch("http://localhost:8080/api/achievements");
        if (beRes.ok) {
            const data = await beRes.json();
            console.log("✅ Backend API is reachable (Status: " + beRes.status + ")");
            console.log(`   Found ${data.totalElements} achievements.`);
        } else {
            console.error("❌ Backend API returned status: " + beRes.status);
        }
    } catch (e) {
        console.error("❌ Backend API check failed: " + e.message);
    }

    // 3. Verify AI Analysis (RAG)
    try {
        console.log("\nChecking AI Service (http://localhost:8080/api/ai/analyze)...");
        const payload = { demand: "我们需要研发用于航空发动机叶片的耐高温陶瓷基复合材料。" };
        
        // Use AbortController for timeout
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 60000); // 60s timeout for AI

        const aiRes = await fetch("http://localhost:8080/api/ai/analyze", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
            signal: controller.signal
        });
        clearTimeout(timeout);

        if (aiRes.ok) {
            const text = await aiRes.text();
            console.log("✅ AI Service is working (Status: " + aiRes.status + ")");
            console.log("   Response length: " + text.length);
            console.log("   Preview: " + text.substring(0, 100) + "...");
        } else {
            console.error("❌ AI Service returned status: " + aiRes.status);
            console.error("   Response: " + await aiRes.text());
        }
    } catch (e) {
        console.error("❌ AI Service check failed: " + e.message);
    }
}

verify();
