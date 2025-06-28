import requests
import uuid

BASE_URL = "http://localhost:8080/api/v1.0/customers"

def print_response(resp):
    print(f"Status: {resp.status_code}")
    try:
        print("Response:", resp.json(pretty=True, indent=4))
    except:
        print("Response (non-JSON):", resp.text)
    print("-" * 60)

def main():
    # 1. Create customer
    customer_data = {
        "givenName": "Alice",
        "middleName": "M",
        "familyName": "Walker",
        "emailAddress": f"alice-{uuid.uuid4()}@example.com",
        "contactNumber": "+1234567890"
    }
    print("Creating customer...")
    response = requests.post(BASE_URL, json=customer_data)
    print_response(response)
    customer = response.json()
    customer_id = customer["id"]
    email = customer["emailAddress"]

    # 2. Get all customers
    print("Getting all customers...")
    print_response(requests.get(BASE_URL))

    # 3. Get customer by ID
    print("Getting customer by ID...")
    print_response(requests.get(f"{BASE_URL}/{customer_id}"))

    # 4. Search by email
    print("Searching customer by email...")
    print_response(requests.get(f"{BASE_URL}/search", params={"email": email}))

    # 5. Update customer
    print("Updating customer...")
    customer_data["givenName"] = "Alicia"
    print_response(requests.put(f"{BASE_URL}/{customer_id}", json=customer_data))

    # 6. Patch contact number
    print("Patching contact number...")
    print_response(requests.patch(f"{BASE_URL}/{customer_id}/contact", params={
        "contactNumber": "+19876543210"
    }))

    # 7. HEAD check
    print("HEAD request to check if customer exists...")
    resp = requests.head(f"{BASE_URL}/{customer_id}")
    print(f"Status: {resp.status_code}")
    print("-" * 60)

    # 8. OPTIONS
    print("OPTIONS request...")
    resp = requests.options(BASE_URL)
    print(f"Allowed Methods: {resp.headers.get('Allow')}")
    print("-" * 60)

    # 9. Delete customer
    print("Deleting customer...")
    print_response(requests.delete(f"{BASE_URL}/{customer_id}"))

    # 10. Confirm deletion
    print("Verifying deletion...")
    print_response(requests.get(f"{BASE_URL}/{customer_id}"))

if __name__ == "__main__":
    main()
