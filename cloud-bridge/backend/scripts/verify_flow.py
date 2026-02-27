import requests
import json
import sys

BASE_URL = "http://localhost:8080/api"

def print_step(step):
    print(f"\n=== {step} ===")

def register(username, password, role, email="test@test.com"):
    url = f"{BASE_URL}/auth/register"
    data = {
        "username": username,
        "password": password,
        "email": email,
        "role": role
    }
    try:
        response = requests.post(url, json=data)
        if response.status_code == 200:
            print(f"Registered {username}: Success")
            return True
        elif "Username is already taken" in response.text:
            print(f"Registered {username}: Already exists (OK)")
            return True
        else:
            print(f"Registered {username}: Failed ({response.status_code}) - {response.text}")
            return False
    except Exception as e:
        print(f"Registered {username}: Error - {str(e)}")
        return False

def login(username, password):
    url = f"{BASE_URL}/auth/login"
    data = {"username": username, "password": password}
    response = requests.post(url, json=data)
    if response.status_code == 200:
        token = response.json()["token"]
        print(f"Login {username}: Success")
        return token
    else:
        print(f"Login {username}: Failed ({response.status_code})")
        return None

def create_demand(token):
    url = f"{BASE_URL}/demands"
    headers = {"Authorization": f"Bearer {token}"}
    data = {
        "title": "Test Demand for Audit",
        "description": "This is a test demand.",
        "field": "新材料",
        "budget": 100000,
        "deadline": "2026-12-31",
        "contactName": "Test User",
        "phone": "1234567890",
        "institution": "Test Corp",
        "type": "NORMAL"
    }
    response = requests.post(url, json=data, headers=headers)
    if response.status_code == 200:
        demand = response.json()
        print(f"Create Demand: Success (ID: {demand['id']}, Status: {demand['status']})")
        return demand['id']
    else:
        print(f"Create Demand: Failed ({response.status_code}) - {response.text}")
        return None

def list_pending_demands(token):
    url = f"{BASE_URL}/demands/pending"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        demands = response.json()
        print(f"List Pending Demands: Found {len(demands)} items")
        return demands
    else:
        print(f"List Pending Demands: Failed ({response.status_code})")
        return []

def audit_demand(token, demand_id, status):
    url = f"{BASE_URL}/demands/{demand_id}/audit?status={status}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.put(url, headers=headers)
    if response.status_code == 200:
        demand = response.json()
        print(f"Audit Demand {demand_id} -> {status}: Success (New Status: {demand['status']})")
        return True
    else:
        print(f"Audit Demand {demand_id} -> {status}: Failed ({response.status_code})")
        return False

def main():
    # 1. Register Users
    print_step("1. Register Users")
    if not register("admin", "admin", "ADMIN"): return
    if not register("enterprise", "123", "ENTERPRISE"): return
    
    # 2. Login Enterprise
    print_step("2. Login Enterprise")
    ent_token = login("enterprise", "123")
    if not ent_token: return

    # 3. Create Demand
    print_step("3. Create Demand")
    demand_id = create_demand(ent_token)
    if not demand_id: return

    # 4. Login Admin
    print_step("4. Login Admin")
    admin_token = login("admin", "admin")
    if not admin_token: return

    # 5. List Pending
    print_step("5. List Pending")
    pending = list_pending_demands(admin_token)
    found = False
    for d in pending:
        if d['id'] == demand_id:
            found = True
            print(f"Found created demand {demand_id} in pending list.")
            break
    if not found:
        print(f"ERROR: Created demand {demand_id} not found in pending list!")
        return

    # 6. Audit (Approve)
    print_step("6. Audit Demand")
    if audit_demand(admin_token, demand_id, "PUBLISHED"):
        print("Verification Passed!")
    else:
        print("Verification Failed!")

if __name__ == "__main__":
    main()
