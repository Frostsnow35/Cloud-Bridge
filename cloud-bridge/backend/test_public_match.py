import urllib.request
import json

url = 'http://localhost:8080/api/matching/match'
payload = {'description': '需要通过人工智能技术进行早期疾病筛查的诊断工具'}
data = json.dumps(payload).encode('utf-8')

print(f"Testing public matching API: {url}")
try:
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as response:
        result = json.loads(response.read().decode('utf-8'))
        print(f"Success! Found {len(result.get('matches', []))} matches.")
except Exception as e:
    print(f"Failed: {e}")
