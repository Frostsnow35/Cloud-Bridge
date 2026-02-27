import requests
import json

BASE_URL = "http://localhost:8080/api"

def test_achievements():
    print("Testing /api/achievements...")
    try:
        response = requests.get(f"{BASE_URL}/achievements")
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"Success! Found {data['totalElements']} achievements.")
            return True
        else:
            print(f"Failed: {response.text}")
            return False
    except Exception as e:
        print(f"Error: {e}")
        return False

def test_matching(keyword="人工智能"):
    print(f"\nTesting /api/matching/match with keyword '{keyword}'...")
    try:
        url = "http://localhost:8080/api/matching/match"
        headers = {"Content-Type": "application/json"}
        payload = {"description": keyword}
        response = requests.post(url, json=payload, headers=headers)
        
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            matches = data.get('matches', [])
            recommendations = data.get('recommendations', [])
            
            print(f"Success!")
            print(f" - Exact Matches: {len(matches)}")
            print(f" - Recommendations: {len(recommendations)}")
            
            if len(matches) > 0:
                print(f"   First Match: {matches[0].get('title', 'No Title')}")
            elif len(recommendations) > 0:
                print(f"   First Rec: {recommendations[0].get('title', 'No Title')}")
                
            return True
        else:
            print(f"Failed: {response.text}")
            return False
    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print("Testing API...")
    
    # 1. Test Exact Match
    print("\n--- Test 1: Exact Match ---")
    test_matching("高性能分布式数据库系统")
    
    # 2. Test Recommendation (Use a domain keyword that likely has no exact title match but fits a field)
    # Assuming "Medical" field exists, try "CT Imaging" which might not be in title but is Medical
    print("\n--- Test 2: Recommendation ---")
    test_matching("CT影像分析") 

