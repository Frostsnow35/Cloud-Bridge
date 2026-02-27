import urllib.request
import urllib.error
import json
import time
import sys

BASE_URL = "http://localhost:8080/api"

def wait_for_backend(timeout=300):
    print("正在等待后端服务就绪...", end="", flush=True)
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/matching/match", method='GET')
            try:
                with urllib.request.urlopen(req, timeout=2) as response:
                    pass
            except urllib.error.HTTPError as e:
                if e.code in [405, 200, 401]: 
                    print("\n后端服务已就绪！")
                    return True
        except Exception:
            pass
        time.sleep(2)
        print(".", end="", flush=True)
    print("\n等待超时，请检查 Docker 日志。")
    return False

def get_token():
    url = f"{BASE_URL}/auth/login"
    payload = {
        "username": "admin",
        "password": "admin123"
    }
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    
    print(f"\n正在尝试登录获取 Token: {url}")
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode('utf-8'))
            token = result.get("token")
            if token:
                print("✅ 登录成功，已获取 Token")
                return token
    except Exception as e:
        print(f"❌ 登录失败: {e}")
    return None

def test_matching(token):
    url = f"{BASE_URL}/matching/match"
    payload = {
        "description": "需要通过人工智能技术进行早期疾病筛查的诊断工具",
        "domains": ["医疗", "AI"] 
    }
    
    data = json.dumps(payload).encode('utf-8')
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    req = urllib.request.Request(url, data=data, headers=headers)
    
    print(f"\n正在发送测试请求: {url}")
    print(f"请求参数: {json.dumps(payload, ensure_ascii=False)}")
    
    try:
        with urllib.request.urlopen(req) as response:
            response_body = response.read().decode('utf-8')
            # print(f"原始响应: {response_body}")
            result = json.loads(response_body)
            print("\n✅ 匹配服务调用成功！")
            
            matches = result.get("matches", [])
            print(f"- 匹配到的成果数量: {len(matches)}")
            
            if "aiGraph" in result:
                 print(f"- 知识图谱数据: 已提取")
            else:
                 print(f"- 知识图谱数据: ⚠️ 未提取")
            
            if "relatedKeywords" in result:
                print(f"- 提取的关键词: {result.get('relatedKeywords')}")
                 
            if matches:
                print(f"\nTop 1 匹配结果: {matches[0].get('title')} (得分: {matches[0].get('score')})")
                
            return True
    except urllib.error.HTTPError as e:
        print(f"\n❌ 请求失败 (HTTP {e.code}): {e.read().decode('utf-8')}")
    except Exception as e:
        print(f"\n❌ 请求出错: {e}")
    return False

if __name__ == "__main__":
    if wait_for_backend():
        token = get_token()
        if token:
            test_matching(token)
        else:
            print("无法继续测试：未获得有效 Token")
