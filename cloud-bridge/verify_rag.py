import requests
import json
import time

BASE_URL = "http://localhost:8080/api/ai"

def test_analyze():
    print("Testing RAG Analysis...")
    payload = {"demand": "我们需要研发用于航空发动机叶片的耐高温陶瓷基复合材料，并寻找相关资金支持。"}
    try:
        response = requests.post(f"{BASE_URL}/analyze", json=payload)
        if response.status_code == 200:
            print("Analysis Result:")
            print(response.text[:500] + "...")
        else:
            print(f"Analysis Failed: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Connection Failed: {e}")

def test_synthesis():
    print("\nTesting Data Synthesis...")
    seeds = [
        "我们需要一种高强度碳纤维用于汽车轻量化。",
        "寻求生物医药领域的抗癌新药研发合作。"
    ]
    try:
        response = requests.post(f"{BASE_URL}/synthesize-data", json=seeds)
        if response.status_code == 200:
            print(f"Synthesis Result: {response.text}")
        else:
            print(f"Synthesis Failed: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Connection Failed: {e}")

if __name__ == "__main__":
    # Wait for service to be ready
    print("Ensure backend is running on port 8080...")
    test_analyze()
    test_synthesis()
