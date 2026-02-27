const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

const user = {
    username: `verifier_${Date.now()}`,
    password: 'password123',
    email: `verifier_${Date.now()}@example.com`,
    role: 'ENTERPRISE'
};

async function run() {
    try {
        console.log("Waiting for backend...");
        // Simple health check or wait
        await new Promise(r => setTimeout(r, 2000));

        console.log(`Registering user ${user.username}...`);
        try {
            await axios.post(`${BASE_URL}/auth/register`, user);
        } catch (e) {
            // Ignore if already exists (unlikely with timestamp)
            console.log("Registration skipped or failed:", e.response?.data || e.message);
        }

        console.log(`Logging in as ${user.username}...`);
        const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
            username: user.username,
            password: user.password
        });
        const token = loginRes.data.token;
        console.log("Login successful, token received.");

        console.log("Testing matching algorithm...");
        const matchPayload = {
            description: "需要通过人工智能技术进行早期疾病筛查的诊断工具"
        };
        
        const matchRes = await axios.post(`${BASE_URL}/matching/match`, matchPayload, {
            headers: { Authorization: `Bearer ${token}` }
        });

        console.log("\n✅ 匹配成功！返回结果摘要：");
        const matches = matchRes.data.matches || [];
        console.log(`- 匹配到的成果数量: ${matches.length}`);
        
        if (matchRes.data.aiGraph) {
            const nodeCount = matchRes.data.aiGraph.nodes ? matchRes.data.aiGraph.nodes.length : 0;
            console.log(`- 知识图谱数据: 已提取 (Nodes: ${nodeCount})`);
        } else {
            console.log(`- 知识图谱数据: ⚠️ 未提取`);
        }

        if (matches.length > 0) {
            console.log("\n首个匹配详情:");
            console.log(JSON.stringify(matches[0], null, 2));
            
            // Check specific scoring fields to verify optimization
            const firstMatch = matches[0];
            if (firstMatch.score > 0) {
                 console.log(`\n验证分数: ${firstMatch.score} (预期 > 0)`);
            } else {
                 console.warn(`\n警告: 分数为 0, 匹配算法可能未生效`);
            }
        } else {
            console.warn("\n警告: 未找到匹配项");
        }

    } catch (e) {
        console.error("\n❌ 测试失败:", e.response?.data || e.message);
        if (e.response?.status === 401) {
             console.error("可能是认证失败。");
        } else if (e.response?.status === 405) {
             console.error("方法不允许 (405)。请检查 URL 或请求方法。");
        } else if (e.response?.status === 500) {
             console.error("服务器内部错误 (500)。请检查后端日志。");
        }
        process.exit(1);
    }
}

run();
