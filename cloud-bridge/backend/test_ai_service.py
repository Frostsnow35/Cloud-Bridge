import json
import urllib.request
import urllib.error

API_BASE = "http://36.140.97.212:9004/v1"
API_KEY = "sk-my-secret-key-888"

def list_models():
    url = f"{API_BASE}/models"
    headers = {"Authorization": f"Bearer {API_KEY}"}
    req = urllib.request.Request(url, headers=headers)
    
    print(f"Listing models from {url}...")
    try:
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode('utf-8'))
            print("Available models:")
            for model in data['data']:
                print(f"- {model['id']}")
            return [m['id'] for m in data['data']]
    except Exception as e:
        print(f"❌ Failed to list models: {e}")
        return []

def test_ai_extraction(model_name):
    url = f"{API_BASE}/chat/completions"
    demand = "我们正在寻找一种能够用于制造下一代电动汽车电池的高容量锂离子电池材料，要求能量密度超过300Wh/kg。"
    prompt = f"""你是一个科技成果转化专家。请从以下用户需求描述中，提取出最核心的1个技术关键词（Keyword）和1个所属领域（Field）。
用户描述："{demand}"。
请严格以JSON格式返回，不要包含任何Markdown标记。格式示例：{{"keyword": "碳纤维", "field": "新材料"}}"""

    data = {
        "model": model_name,
        "temperature": 0.3,
        "messages": [
            {"role": "system", "content": "You are a helpful assistant that outputs JSON."},
            {"role": "user", "content": prompt}
        ]
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }

    req = urllib.request.Request(
        url, 
        data=json.dumps(data).encode('utf-8'), 
        headers=headers
    )

    print(f"\nTesting extraction with model '{model_name}'...")
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode('utf-8'))
            if 'choices' in result and len(result['choices']) > 0:
                content = result['choices'][0]['message']['content']
                print("\nAI Response Content:")
                print(content)
                
                try:
                    parsed = json.loads(content.replace("```json", "").replace("```", "").strip())
                    print("\nParsed JSON:")
                    print(json.dumps(parsed, indent=2, ensure_ascii=False))
                    print("\n✅ TEST PASSED: AI extraction successful.")
                except json.JSONDecodeError:
                    print("\n❌ TEST FAILED: Response is not valid JSON.")
            else:
                print("\n❌ TEST FAILED: No choices in response.")

    except urllib.error.HTTPError as e:
        print(f"\n❌ HTTP Error: {e.code} {e.reason}")
        print(e.read().decode('utf-8'))
    except Exception as e:
        print(f"\n❌ Unexpected Error: {e}")

if __name__ == "__main__":
    models = list_models()
    if models:
        # Use the first model or look for a specific one
        target_model = models[0]
        # Prefer qwen models if available
        for m in models:
            if 'qwen' in m.lower():
                target_model = m
                break
        
        test_ai_extraction(target_model)
    else:
        print("No models available to test.")
