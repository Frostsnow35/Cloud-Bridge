import requests
import json

url = "http://localhost:8080/api/matching/match"
payload = {"description": "本企业急需生物医药领域的基因编辑治疗技术_309，希望解决当前技术瓶颈，预算充足。"}
headers = {'Content-Type': 'application/json'}

try:
    response = requests.post(url, json=payload, headers=headers)
    print(response.status_code)
    print(json.dumps(response.json(), ensure_ascii=False, indent=2))
except Exception as e:
    print(e)
