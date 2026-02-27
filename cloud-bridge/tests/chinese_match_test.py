import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def test_chinese_search():
    # 1. Create a specific achievement
    url_create = f"{BASE_URL}/achievements"
    title = "高性能分布式数据库系统"
    desc = "支持海量并发的分布式数据库解决方案"
    payload = {
        "title": title,
        "description": desc,
        "field": "IT",
        "maturity": "Product",
        "price": 10000.0,
        "ownerId": 1,
        "status": "PUBLISHED"
    }
    
    try:
        # Create
        resp = requests.post(url_create, json=payload, headers={"Content-Type": "application/json"})
        if resp.status_code != 200:
            log(f"Create failed: {resp.text}", "FAIL")
            return False
        
        log(f"Created achievement: {title}", "INFO")
        time.sleep(1) # Give it a moment (though DB is immediate)

        # 2. Search using a keyword that is a substring of title
        search_keyword = "分布式数据库"
        url_match = f"{BASE_URL}/matching/match"
        # We simulate the case where AI extraction might return this keyword, 
        # or fallback uses it if input is just this keyword.
        match_payload = {"description": search_keyword}
        
        resp_match = requests.post(url_match, json=match_payload, headers={"Content-Type": "application/json"})
        if resp_match.status_code == 200:
            results = resp_match.json()
            # Check if our created item is in results
            found = False
            for item in results:
                if item["title"] == title:
                    found = True
                    break
            
            if found:
                log(f"Search for '{search_keyword}' found '{title}'.", "PASS")
                return True
            else:
                log(f"Search for '{search_keyword}' did NOT find '{title}'. Results count: {len(results)}", "FAIL")
                return False
        else:
            log(f"Match request failed: {resp_match.status_code}", "FAIL")
            return False

    except Exception as e:
        log(f"Exception: {e}", "ERROR")
        return False

if __name__ == "__main__":
    test_chinese_search()
