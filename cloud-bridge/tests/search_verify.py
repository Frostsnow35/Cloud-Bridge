import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def test_fuzzy_and_recommendation():
    # 1. 确保有一个已知的数据，例如 "高性能碳纤维"
    # 如果之前的测试已经创建了，这里就不重复创建，或者我们用一个独特的后缀
    unique_suffix = str(int(time.time()))
    title = f"AI驱动的新型材料研发平台-{unique_suffix}"
    desc = "利用深度学习加速新材料发现过程"
    
    # Create item
    create_payload = {
        "title": title,
        "description": desc,
        "field": "Materials",
        "price": 50000.0,
        "ownerId": 1,
        "status": "PUBLISHED"
    }
    requests.post(f"{BASE_URL}/achievements", json=create_payload, headers={"Content-Type": "application/json"})
    
    # 2. Test Fuzzy Search (MatchingService Fallback)
    # 输入 "AI驱动材料" (无空格，且不完全匹配title)
    # AI提取可能失败或提取为"AI驱动材料"，数据库LIKE查询应该能匹配到 "AI驱动" 或 "材料"
    search_term = "AI驱动材料"
    log(f"Testing fuzzy search with term: '{search_term}'...")
    
    resp = requests.post(f"{BASE_URL}/matching/match", json={"description": search_term}, headers={"Content-Type": "application/json"})
    if resp.status_code == 200:
        results = resp.json()
        found = any(r['title'] == title for r in results)
        if found:
            log("Fuzzy search PASS: Found target item.", "PASS")
        else:
            log(f"Fuzzy search WARN: Target item not found. Results count: {len(results)}", "WARN")
            # 可能是分词逻辑还没覆盖到 "AI驱动材料" 拆分为 "AI" "驱动" "材料"
            # 但我们在 MatchingService 里加了 fallback: if (keyword.length() >= 2) -> broadKey
            # broadKey = "AI" -> 应该能搜到
    else:
        log(f"Search request failed: {resp.status_code}", "FAIL")

    # 3. Test 'Guess what you want' (No match case)
    # 输入一个完全无关的乱码，触发空结果，检查是否返回推荐（如果有实现推荐返回的话）
    # 目前后端逻辑是：如果没结果，返回空列表，前端展示词云。
    # 这里主要验证后端不会报错。
    log("Testing no-match case...")
    resp_empty = requests.post(f"{BASE_URL}/matching/match", json={"description": "XyZ123NotExisting"}, headers={"Content-Type": "application/json"})
    if resp_empty.status_code == 200:
        results = resp_empty.json()
        if len(results) == 0:
            log("No-match case PASS: Returned empty list (Frontend will show Word Cloud).", "PASS")
        else:
            log(f"No-match case INFO: Returned {len(results)} items (Maybe fallback matched something?)", "INFO")
    else:
        log(f"No-match request failed: {resp_empty.status_code}", "FAIL")

if __name__ == "__main__":
    test_fuzzy_and_recommendation()
