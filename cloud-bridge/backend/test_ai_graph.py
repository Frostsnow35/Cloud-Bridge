import requests
import json

url = "http://localhost:8080/api/matching/match"
payload = {"description": "需要一种用于航空航天的高性能碳纤维复合材料"}
headers = {"Content-Type": "application/json"}

try:
    response = requests.post(url, json=payload, headers=headers)
    if response.status_code == 200:
        data = response.json()
        print("Response received.")
        if "aiGraph" in data:
            print("SUCCESS: 'aiGraph' field found in response.")
            print("Graph Nodes:", len(data["aiGraph"].get("nodes", [])))
            print("Graph Edges:", len(data["aiGraph"].get("relationships", [])))
            print("Graph Content Sample:", json.dumps(data["aiGraph"], ensure_ascii=False)[:200])
        else:
            print("FAILURE: 'aiGraph' field NOT found.")
            print("Keys found:", data.keys())
    else:
        print(f"Error: {response.status_code} - {response.text}")
except Exception as e:
    print(f"Connection failed: {e}")
