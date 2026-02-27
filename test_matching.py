import requests
import json

url = "http://localhost:8080/api/matching/match"
payload = {"description": "我们需要一种高能量密度的锂离子电池正极材料"}
headers = {"Content-Type": "application/json"}

try:
    print(f"Sending POST to {url} with payload: {payload}")
    response = requests.post(url, json=payload, headers=headers)
    print(f"Status Code: {response.status_code}")
    print(f"Response Body: {response.text}")
except Exception as e:
    print(f"Error: {e}")
