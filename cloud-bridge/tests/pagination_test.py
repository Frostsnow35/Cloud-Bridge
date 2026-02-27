import requests
import json

BASE_URL = "http://localhost:8080/api"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def test_pagination():
    url = f"{BASE_URL}/achievements"
    # Default page size is 20 (defined in controller or property, usually)
    # Let's try explicit paging
    params = {"page": 0, "size": 5}
    
    try:
        response = requests.get(url, params=params)
        if response.status_code == 200:
            data = response.json()
            # Expecting a Page object structure, e.g. content, pageable, totalElements...
            if "content" in data and "totalPages" in data:
                content_size = len(data["content"])
                log(f"Pagination response valid. Page 0, Size 5. Returned {content_size} items.", "PASS")
                if content_size <= 5:
                    return True
                else:
                    log(f"Returned more items than page size: {content_size}", "FAIL")
                    return False
            else:
                log(f"Response does not look like a Page object: {data.keys()}", "FAIL")
                return False
        else:
            log(f"Request failed with status {response.status_code}", "FAIL")
            return False
    except Exception as e:
        log(f"Request exception: {e}", "ERROR")
        return False

if __name__ == "__main__":
    test_pagination()
