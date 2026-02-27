const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080/api';

const user = {
    username: `uploader_${Date.now()}`,
    password: 'password123',
    email: `uploader_${Date.now()}@example.com`,
    role: 'ENTERPRISE'
};

async function run() {
    try {
        console.log("Waiting for backend...");
        await new Promise(r => setTimeout(r, 1000));

        console.log(`Registering user ${user.username}...`);
        try {
            await axios.post(`${BASE_URL}/auth/register`, user);
        } catch (e) {
            console.log("Registration skipped or failed:", e.response?.data || e.message);
        }

        console.log(`Logging in as ${user.username}...`);
        const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
            username: user.username,
            password: user.password
        });
        const token = loginRes.data.token;
        console.log("Login successful, token received.");

        console.log("Testing file upload...");
        
        // Create a dummy file
        const filePath = path.join(__dirname, 'test_upload.txt');
        fs.writeFileSync(filePath, 'This is a test file content for upload verification.');
        
        const form = new FormData();
        form.append('file', fs.createReadStream(filePath));

        const uploadRes = await axios.post(`${BASE_URL}/upload`, form, {
            headers: {
                ...form.getHeaders(),
                Authorization: `Bearer ${token}`
            }
        });

        console.log("\n✅ 上传成功！返回结果：");
        console.log(JSON.stringify(uploadRes.data, null, 2));

        const fileUrl = uploadRes.data.fileDownloadUri;
        if (fileUrl) {
            console.log(`\n验证文件访问: ${fileUrl}`);
            // Fix: Replace backslashes with forward slashes for URL if needed, though backend should return valid URL
            // Also handle localhost port if needed. Assuming backend returns full URL or relative path.
            
            let downloadUrl = fileUrl;
            if (!fileUrl.startsWith('http')) {
                downloadUrl = `http://localhost:8080${fileUrl}`;
            }

            try {
                const downloadRes = await axios.get(downloadUrl);
                if (downloadRes.status === 200) {
                     console.log("✅ 文件访问成功！内容长度:", downloadRes.data.length);
                } else {
                     console.error("❌ 文件访问失败:", downloadRes.status);
                }
            } catch (e) {
                 console.error("❌ 文件访问异常:", e.message);
            }
        }

        // Cleanup
        fs.unlinkSync(filePath);

    } catch (e) {
        console.error("\n❌ 上传测试失败:", e.response?.data || e.message);
        process.exit(1);
    }
}

run();
