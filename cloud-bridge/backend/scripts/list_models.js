const API_URL = "http://api.siliconflow.cn/v1/models";
const API_KEY = process.env.AI_API_KEY || "sk-zzfnrqhpsxrvkutyjqbkfukflugzkurpwbhmopzpsxygukqm";

async function main() {
    console.log(`Checking models at ${API_URL}...`);
    
    try {
        const response = await fetch(API_URL, {
            headers: {
                "Authorization": `Bearer ${API_KEY}`
            }
        });
        
        if (!response.ok) {
            console.error(`Error: ${response.status} ${response.statusText}`);
            console.error(await response.text());
            return;
        }
        
        const data = await response.json();
        if (data.data) {
            console.log(`Found ${data.data.length} models.`);
            data.data.forEach(m => console.log(`- ${m.id}`));
        } else {
            console.log("No models found in response.");
            console.log(JSON.stringify(data, null, 2));
        }
        
    } catch (error) {
        console.error("Failed to list models:", error.message);
    }
}

main();
