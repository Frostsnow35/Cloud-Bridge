import requests
import json
import time
import random
import string

BASE_URL = "http://localhost:8080/api"

def log(msg):
    print(f"[TEST] {msg}")

def generate_random_string(length):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

def test_create_achievement():
    url = f"{BASE_URL}/achievements"
    payload = {
        "title": f"Test Achievement {generate_random_string(5)}",
        "description": "This is a test achievement for functional testing.",
        "field": "Software Engineering",
        "maturity": "Prototype",
        "price": 1000.0,
        "ownerId": 1,
        "status": "PUBLISHED"
    }
    start_time = time.time()
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
        elapsed = time.time() - start_time
        if response.status_code == 200:
            return {"status": "PASS", "time": elapsed, "data": response.json()}
        else:
            return {"status": "FAIL", "time": elapsed, "error": response.text}
    except Exception as e:
        return {"status": "ERROR", "time": 0, "error": str(e)}

def test_get_achievements():
    url = f"{BASE_URL}/achievements"
    start_time = time.time()
    try:
        response = requests.get(url)
        elapsed = time.time() - start_time
        if response.status_code == 200:
            data = response.json()
            return {"status": "PASS", "time": elapsed, "count": len(data)}
        else:
            return {"status": "FAIL", "time": elapsed, "error": response.text}
    except Exception as e:
        return {"status": "ERROR", "time": 0, "error": str(e)}

def test_create_demand():
    url = f"{BASE_URL}/demands"
    payload = {
        "title": f"Test Demand {generate_random_string(5)}",
        "description": "Looking for a software solution for automated testing.",
        "field": "Software Engineering",
        "budget": 5000.0,
        "ownerId": 2,
        "status": "PUBLISHED"
    }
    start_time = time.time()
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
        elapsed = time.time() - start_time
        if response.status_code == 200:
            return {"status": "PASS", "time": elapsed, "data": response.json()}
        else:
            return {"status": "FAIL", "time": elapsed, "error": response.text}
    except Exception as e:
        return {"status": "ERROR", "time": 0, "error": str(e)}

def test_match_quality(description, expected_keywords):
    url = f"{BASE_URL}/matching/match"
    payload = {"description": description}
    start_time = time.time()
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
        elapsed = time.time() - start_time
        if response.status_code == 200:
            results = response.json()
            # Simple keyword check in title or description of results
            matched = False
            match_details = []
            for item in results:
                content = (item.get('title', '') + item.get('description', '')).lower()
                if any(k.lower() in content for k in expected_keywords):
                    matched = True
                match_details.append({"id": item.get('id'), "title": item.get('title')})
            
            # If no results, it might be due to lack of data, but let's see if it returns anything
            if not results:
                 return {"status": "WARN", "time": elapsed, "message": "No matches found", "count": 0}

            return {
                "status": "PASS" if matched else "WARN", 
                "time": elapsed, 
                "count": len(results),
                "details": match_details,
                "message": "Keywords found" if matched else "Keywords not found in top results"
            }
        else:
            return {"status": "FAIL", "time": elapsed, "error": response.text}
    except Exception as e:
        return {"status": "ERROR", "time": 0, "error": str(e)}

def test_edge_case_empty():
    return test_match_quality("", [])

def test_edge_case_long():
    long_desc = "Test " * 2000 # 10000 chars
    return test_match_quality(long_desc, ["test"])

def test_edge_case_special_chars():
    special_desc = "!@#$%^&*()_+{}|:<>?~`"
    return test_match_quality(special_desc, [])

def run_tests():
    results = {}
    
    log("Running Functional Tests...")
    results["create_achievement"] = test_create_achievement()
    results["get_achievements"] = test_get_achievements()
    results["create_demand"] = test_create_demand()
    
    log("Running Match Quality Tests...")
    # Assumes some data exists. We just created one "Software Engineering" achievement.
    results["match_software"] = test_match_quality("Need software testing solution", ["software", "test"])
    
    log("Running Edge Case Tests...")
    results["edge_empty"] = test_edge_case_empty()
    results["edge_long"] = test_edge_case_long()
    results["edge_special"] = test_edge_case_special_chars()
    
    return results

if __name__ == "__main__":
    final_results = run_tests()
    print(json.dumps(final_results, indent=2))
