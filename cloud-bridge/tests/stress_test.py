import requests
import concurrent.futures
import time
import json
import random
import statistics

BASE_URL = "http://localhost:8080/api"

def get_achievements():
    try:
        response = requests.get(f"{BASE_URL}/achievements")
        return response.elapsed.total_seconds(), response.status_code
    except Exception:
        return 0, 500

def post_demand():
    payload = {
        "title": f"Stress Test Demand {random.randint(1, 10000)}",
        "description": "Stress testing demand",
        "field": "Stress Testing",
        "budget": 1000.0,
        "ownerId": 999,
        "status": "PUBLISHED"
    }
    try:
        response = requests.post(f"{BASE_URL}/demands", json=payload, headers={"Content-Type": "application/json"})
        return response.elapsed.total_seconds(), response.status_code
    except Exception:
        return 0, 500

def match_request():
    payload = {"description": "software testing solutions"}
    try:
        response = requests.post(f"{BASE_URL}/matching/match", json=payload, headers={"Content-Type": "application/json"})
        return response.elapsed.total_seconds(), response.status_code
    except Exception:
        return 0, 500

def run_stress_test(func, num_requests, concurrency):
    times = []
    status_codes = []
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(func) for _ in range(num_requests)]
        for future in concurrent.futures.as_completed(futures):
            t, code = future.result()
            times.append(t)
            status_codes.append(code)
            
    avg_time = statistics.mean(times) if times else 0
    p95_time = statistics.quantiles(times, n=20)[18] if len(times) >= 20 else max(times) if times else 0
    success_rate = (status_codes.count(200) / len(status_codes)) * 100 if status_codes else 0
    
    return {
        "requests": num_requests,
        "concurrency": concurrency,
        "avg_time_ms": round(avg_time * 1000, 2),
        "p95_time_ms": round(p95_time * 1000, 2),
        "success_rate": success_rate,
        "errors": len([c for c in status_codes if c != 200])
    }

if __name__ == "__main__":
    print("Running Stress Tests...")
    
    # 1. Read Load: 100 requests, 20 concurrent
    read_results = run_stress_test(get_achievements, 100, 20)
    print(f"Read Load (GET /achievements): {json.dumps(read_results)}")
    
    # 2. Write Load: 50 requests, 10 concurrent
    write_results = run_stress_test(post_demand, 50, 10)
    print(f"Write Load (POST /demands): {json.dumps(write_results)}")
    
    # 3. Compute Load: 20 requests, 5 concurrent (AI Matching)
    compute_results = run_stress_test(match_request, 20, 5)
    print(f"Compute Load (POST /matching/match): {json.dumps(compute_results)}")
