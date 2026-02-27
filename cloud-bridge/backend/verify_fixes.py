import requests
import json
import sys

BASE_URL = "http://localhost:8080/api"

def test_matching():
    print("\n--- Testing Matching API ---")
    url = f"{BASE_URL}/matching/match"
    # Description relevant to "gene editing" to match the generated data
    payload = {"description": "本企业急需生物医药领域的基因编辑治疗技术，希望解决当前技术瓶颈，预算充足。"}
    headers = {'Content-Type': 'application/json'}
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        if response.status_code == 200:
            data = response.json()
            matches = data.get("matches", [])
            print(f"Found {len(matches)} matches.")
            for i, m in enumerate(matches[:5]):
                print(f"Match {i+1}: ID={m.get('id')}, Score={m.get('score')}, Title={m.get('title')}")
            
            # Check scores
            scores = [m.get('score', 0) for m in matches]
            if not scores:
                print("FAIL: No matches found.")
                return
            
            # Rough check for 95, 80, 70 range
            has_high = any(s >= 90 for s in scores)
            has_med = any(75 <= s < 90 for s in scores)
            has_low = any(60 <= s < 75 for s in scores)
            
            if has_high and has_med and has_low:
                print("SUCCESS: Scores cover required ranges (High/Med/Low).")
            else:
                print(f"WARNING: Score distribution might not be perfect. Scores: {scores}")
        else:
            print(f"FAIL: Status {response.status_code}, Body: {response.text}")
    except Exception as e:
        print(f"ERROR: {e}")

def test_demand_detail():
    print("\n--- Testing Demand Detail API ---")
    # First get a list
    try:
        list_resp = requests.get(f"{BASE_URL}/demands")
        if list_resp.status_code != 200:
            print("FAIL: Could not fetch demands list.")
            return
            
        demands_page = list_resp.json()
        demands = demands_page.get('content', [])
        if not demands:
            print("FAIL: No demands found in DB.")
            return
            
        target_id = demands[0]['id']
        print(f"Testing Detail for Demand ID: {target_id}")
        
        detail_resp = requests.get(f"{BASE_URL}/demands/{target_id}")
        if detail_resp.status_code == 200:
            d = detail_resp.json()
            print("SUCCESS: Fetched detail.")
            print(f"Title: {d.get('title')}")
            # Check for fields that might cause white screen if missing
            required = ['id', 'title', 'description', 'field']
            missing = [f for f in required if f not in d]
            if missing:
                print(f"WARNING: Missing fields: {missing}")
            else:
                print("Data integrity: OK")
        else:
            print(f"FAIL: Detail fetch failed. Status: {detail_resp.status_code}")
            
    except Exception as e:
        print(f"ERROR: {e}")

if __name__ == "__main__":
    test_demand_detail()
    test_matching()
