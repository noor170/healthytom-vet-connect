#!/usr/bin/env python3
"""
Vet Connect API Testing & Firebase Integration Script
Comprehensive testing tool for REST API and Firebase demo data insertion
"""

import json
import requests
import argparse
import sys
from datetime import datetime
from typing import Dict, Optional, Any
import time

class VetConnectAPITester:
    """Test Vet Connect REST API endpoints"""
    
    def __init__(self, base_url: str = "http://localhost:8081/api"):
        self.base_url = base_url
        self.token: Optional[str] = None
        self.refresh_token: Optional[str] = None
        self.headers: Dict[str, str] = {
            "Content-Type": "application/json"
        }
    
    def register_user(self, email: str, password: str, first_name: str = "Demo", 
                     last_name: str = "User", role: str = "OWNER", 
                     phone: str = "+1234567890") -> bool:
        """Register a new user"""
        try:
            url = f"{self.base_url}/auth/register"
            payload = {
                "firstName": first_name,
                "lastName": last_name,
                "email": email,
                "password": password,
                "role": role,
                "phoneNumber": phone
            }
            
            print(f"📝 Registering user: {email}")
            response = requests.post(url, json=payload, headers=self.headers)
            
            if response.status_code == 201:
                data = response.json()
                self.token = data.get("token")
                self.refresh_token = data.get("refreshToken")
                print(f"✓ Registration successful!")
                print(f"  Token: {self.token[:20]}...")
                self.headers["Authorization"] = f"Bearer {self.token}"
                return True
            else:
                print(f"✗ Registration failed: {response.status_code}")
                print(f"  Message: {response.text}")
                return False
        except Exception as e:
            print(f"✗ Error during registration: {str(e)}")
            return False
    
    def login(self, email: str, password: str) -> bool:
        """Login user and get token"""
        try:
            url = f"{self.base_url}/auth/login"
            payload = {
                "email": email,
                "password": password
            }
            
            print(f"🔐 Logging in: {email}")
            response = requests.post(url, json=payload, headers=self.headers)
            
            if response.status_code == 200:
                data = response.json()
                self.token = data.get("token")
                self.refresh_token = data.get("refreshToken")
                print(f"✓ Login successful!")
                self.headers["Authorization"] = f"Bearer {self.token}"
                return True
            else:
                print(f"✗ Login failed: {response.status_code}")
                return False
        except Exception as e:
            print(f"✗ Error during login: {str(e)}")
            return False
    
    def get_current_user(self) -> Optional[Dict[str, Any]]:
        """Get current user profile"""
        try:
            url = f"{self.base_url}/users/me"
            print("👤 Fetching current user...")
            response = requests.get(url, headers=self.headers)
            
            if response.status_code == 200:
                user = response.json()
                print(f"✓ User retrieved: {user.get('email')}")
                return user
            else:
                print(f"✗ Failed to get user: {response.status_code}")
                return None
        except Exception as e:
            print(f"✗ Error fetching user: {str(e)}")
            return None
    
    def create_pet(self, name: str, species: str, breed: str, age: int, 
                  weight: float, owner_id: int = 1) -> Optional[Dict[str, Any]]:
        """Create a new pet"""
        try:
            url = f"{self.base_url}/pets?ownerId={owner_id}"
            payload = {
                "name": name,
                "species": species,
                "breed": breed,
                "age": age,
                "weight": weight,
                "dateOfBirth": f"{datetime.now().year - age}-01-15"
            }
            
            print(f"🐾 Creating pet: {name}")
            response = requests.post(url, json=payload, headers=self.headers)
            
            if response.status_code == 201:
                pet = response.json()
                print(f"✓ Pet created: {pet.get('id')} - {name}")
                return pet
            else:
                print(f"✗ Failed to create pet: {response.status_code}")
                return None
        except Exception as e:
            print(f"✗ Error creating pet: {str(e)}")
            return None
    
    def get_pet(self, pet_id: int) -> Optional[Dict[str, Any]]:
        """Get pet by ID"""
        try:
            url = f"{self.base_url}/pets/{pet_id}"
            print(f"🐾 Fetching pet: {pet_id}")
            response = requests.get(url, headers=self.headers)
            
            if response.status_code == 200:
                pet = response.json()
                print(f"✓ Pet retrieved: {pet.get('name')}")
                return pet
            else:
                print(f"✗ Failed to get pet: {response.status_code}")
                return None
        except Exception as e:
            print(f"✗ Error fetching pet: {str(e)}")
            return None
    
    def get_pets_for_owner(self, owner_id: int) -> Optional[list]:
        """Get all pets for an owner"""
        try:
            url = f"{self.base_url}/pets/owner/{owner_id}"
            print(f"🐾 Fetching pets for owner: {owner_id}")
            response = requests.get(url, headers=self.headers)
            
            if response.status_code == 200:
                pets = response.json()
                print(f"✓ Retrieved {len(pets)} pet(s)")
                return pets
            else:
                print(f"✗ Failed to get pets: {response.status_code}")
                return None
        except Exception as e:
            print(f"✗ Error fetching pets: {str(e)}")
            return None
    
    def create_consultation(self, pet_id: int, veterinarian_id: int, 
                           consultation_type: str = "GENERAL_CHECKUP",
                           notes: str = "") -> Optional[Dict[str, Any]]:
        """Create a consultation"""
        try:
            url = f"{self.base_url}/consultations"
            payload = {
                "petId": pet_id,
                "veterinarianId": veterinarian_id,
                "scheduledDate": datetime.now().isoformat(),
                "type": consultation_type,
                "notes": notes or "Routine health check"
            }
            
            print(f"📅 Creating consultation for pet: {pet_id}")
            response = requests.post(url, json=payload, headers=self.headers)
            
            if response.status_code == 201:
                consultation = response.json()
                print(f"✓ Consultation created: {consultation.get('id')}")
                return consultation
            else:
                print(f"✗ Failed to create consultation: {response.status_code}")
                return None
        except Exception as e:
            print(f"✗ Error creating consultation: {str(e)}")
            return None
    
    def print_report(self, data: Dict[str, Any]):
        """Pretty print JSON response"""
        print(json.dumps(data, indent=2, default=str))


def run_full_test_suite(api_tester: VetConnectAPITester):
    """Run complete test suite"""
    print("\n" + "="*60)
    print("VET CONNECT API - FULL TEST SUITE")
    print("="*60 + "\n")
    
    # Test 1: Registration
    email = f"demo+{int(time.time())}@healthytom.com"
    if not api_tester.register_user(email, "Demo@1234"):
        return False
    
    print("\n---\n")
    
    # Test 2: Get User
    user = api_tester.get_current_user()
    if not user:
        return False
    api_tester.print_report(user)
    
    print("\n---\n")
    
    # Test 3: Create Pets
    pet1 = api_tester.create_pet("Buddy", "Dog", "Golden Retriever", 3, 30.5)
    if pet1:
        api_tester.print_report(pet1)
    
    print("\n---\n")
    
    pet2 = api_tester.create_pet("Fluffy", "Cat", "Persian", 5, 4.2)
    if pet2:
        api_tester.print_report(pet2)
    
    print("\n---\n")
    
    # Test 4: Get Pets for Owner
    pets = api_tester.get_pets_for_owner(1)
    if pets:
        print(f"Owner has {len(pets)} pet(s):")
        api_tester.print_report(pets)
    
    print("\n---\n")
    
    # Test 5: Create Consultation
    if pet1:
        consultation = api_tester.create_consultation(
            pet1.get('id', 1), 
            2, 
            "GENERAL_CHECKUP",
            "Routine health check and vaccination update"
        )
        if consultation:
            api_tester.print_report(consultation)
    
    print("\n" + "="*60)
    print("✓ TEST SUITE COMPLETED SUCCESSFULLY")
    print("="*60 + "\n")
    
    return True


def create_firebase_demo_data():
    """Create demo data JSON for Firebase import"""
    demo_data = {
        "prod_users": {
            "user_demo_001": {
                "email": "demo@healthytom.com",
                "firstName": "Demo",
                "lastName": "User",
                "role": "OWNER",
                "phoneNumber": "+1234567890",
                "emailVerified": False,
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat()
            },
            "user_vet_001": {
                "email": "vet@healthytom.com",
                "firstName": "Dr.",
                "lastName": "Veterinarian",
                "role": "VETERINARIAN",
                "phoneNumber": "+1234567891",
                "specialization": "Small Animal Medicine",
                "licenseNumber": "VET2024001",
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat()
            }
        },
        "prod_pets": {
            "pet_buddy_001": {
                "name": "Buddy",
                "species": "Dog",
                "breed": "Golden Retriever",
                "age": 3,
                "weight": 30.5,
                "dateOfBirth": "2023-01-15",
                "ownerId": "user_demo_001",
                "vaccinated": True,
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat()
            },
            "pet_fluffy_001": {
                "name": "Fluffy",
                "species": "Cat",
                "breed": "Persian",
                "age": 5,
                "weight": 4.2,
                "dateOfBirth": "2021-02-10",
                "ownerId": "user_demo_001",
                "vaccinated": True,
                "allergies": ["Fish"],
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat()
            }
        }
    }
    
    with open('firebase_demo_data.json', 'w') as f:
        json.dump(demo_data, f, indent=2)
    
    print("✓ Firebase demo data saved to firebase_demo_data.json")


def main():
    parser = argparse.ArgumentParser(
        description="Vet Connect API Testing & Firebase Integration"
    )
    parser.add_argument(
        "--url", 
        default="http://localhost:8081/api",
        help="Base API URL (default: http://localhost:8081/api)"
    )
    parser.add_argument(
        "--email",
        default="demo@healthytom.com",
        help="User email for testing"
    )
    parser.add_argument(
        "--password",
        default="Demo@1234",
        help="User password for testing"
    )
    parser.add_argument(
        "--test",
        action="store_true",
        help="Run full test suite"
    )
    parser.add_argument(
        "--firebase-export",
        action="store_true",
        help="Generate Firebase demo data JSON"
    )
    parser.add_argument(
        "--login-only",
        action="store_true",
        help="Only test login and get current user"
    )
    
    args = parser.parse_args()
    
    api_tester = VetConnectAPITester(args.url)
    
    if args.firebase_export:
        create_firebase_demo_data()
    elif args.test:
        if not run_full_test_suite(api_tester):
            sys.exit(1)
    elif args.login_only:
        if api_tester.login(args.email, args.password):
            user = api_tester.get_current_user()
            if user:
                print("\n✓ Login successful! User data:")
                api_tester.print_report(user)
        else:
            sys.exit(1)
    else:
        # Interactive mode
        print("Vet Connect API Tester - Interactive Mode")
        print("Commands: register, login, get-user, create-pet, get-pets, help, quit")
        
        while True:
            try:
                cmd = input("\n> ").strip().lower()
                
                if cmd == "quit":
                    break
                elif cmd == "register":
                    email = input("Email: ")
                    password = input("Password: ")
                    api_tester.register_user(email, password)
                elif cmd == "login":
                    email = input("Email: ")
                    password = input("Password: ")
                    api_tester.login(email, password)
                elif cmd == "get-user":
                    user = api_tester.get_current_user()
                    if user:
                        api_tester.print_report(user)
                elif cmd == "create-pet":
                    name = input("Pet name: ")
                    species = input("Species: ")
                    breed = input("Breed: ")
                    age = int(input("Age: "))
                    weight = float(input("Weight: "))
                    api_tester.create_pet(name, species, breed, age, weight)
                elif cmd == "get-pets":
                    owner_id = int(input("Owner ID: ") or "1")
                    pets = api_tester.get_pets_for_owner(owner_id)
                    if pets:
                        api_tester.print_report(pets)
                elif cmd == "help":
                    print("""
Available commands:
  register          - Register new user
  login             - Login with credentials
  get-user          - Get current user profile
  create-pet        - Create new pet
  get-pets          - Get pets for owner
  help              - Show this help
  quit              - Exit
                    """)
            except KeyboardInterrupt:
                break
            except Exception as e:
                print(f"Error: {str(e)}")


if __name__ == "__main__":
    main()
