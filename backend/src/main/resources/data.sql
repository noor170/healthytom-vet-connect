-- Demo Data for Healthy Tom Vet Connect

-- Insert Users (Owners)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, enabled, email_verified, created_at, updated_at) 
VALUES ('owner1@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'John', 'Smith', '555-0001', 'OWNER', true, true, NOW(), NOW()),
       ('owner2@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'Sarah', 'Johnson', '555-0002', 'OWNER', true, true, NOW(), NOW()),
       ('owner3@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'Mike', 'Williams', '555-0003', 'OWNER', true, true, NOW(), NOW());

-- Insert Veterinarians
INSERT INTO users (email, password, first_name, last_name, phone_number, role, enabled, email_verified, specialization, license_number, created_at, updated_at) 
VALUES ('vet1@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'Dr. Emily', 'Brown', '555-1001', 'VETERINARIAN', true, true, 'Small Animals', 'VET-001-2024', NOW(), NOW()),
       ('vet2@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'Dr. James', 'Davis', '555-1002', 'VETERINARIAN', true, true, 'Exotic Animals', 'VET-002-2024', NOW(), NOW());

-- Insert Admin
INSERT INTO users (email, password, first_name, last_name, phone_number, role, enabled, email_verified, created_at, updated_at) 
VALUES ('admin@example.com', '$2a$10$slYQmyNdGzin7olVN3p5.OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2', 'Admin', 'User', '555-2001', 'ADMIN', true, true, NOW(), NOW());

-- Insert Pets for Owner 1 (John Smith)
INSERT INTO pets (name, species, breed, color, weight, date_of_birth, microchip_number, medical_record_number, medical_history, vaccinations, owner_id, created_at, updated_at) 
VALUES ('Buddy', 'Dog', 'Golden Retriever', 'Golden', 25.5, '2020-03-15', 'CHIP-001-DOG', 'MR-001-DOG', 'Healthy with no known allergies', 'Rabies (2024), DHPP (2024)', 1, NOW(), NOW()),
       ('Whiskers', 'Cat', 'Persian', 'White', 4.2, '2021-07-22', 'CHIP-002-CAT', 'MR-002-CAT', 'Prone to hairballs', 'FVRCP (2024), Rabies (2024)', 1, NOW(), NOW());

-- Insert Pets for Owner 2 (Sarah Johnson)
INSERT INTO pets (name, species, breed, color, weight, date_of_birth, microchip_number, medical_record_number, medical_history, vaccinations, owner_id, created_at, updated_at) 
VALUES ('Max', 'Dog', 'German Shepherd', 'Black/Tan', 32.0, '2019-05-10', 'CHIP-003-DOG', 'MR-003-DOG', 'Slightly overweight, needs exercise', 'Rabies (2024), DHPP (2024)', 2, NOW(), NOW()),
       ('Tweety', 'Bird', 'Parakeet', 'Blue', 0.04, '2022-12-01', 'CHIP-004-BIRD', 'MR-004-BIRD', 'Vocal and healthy', 'N/A', 2, NOW(), NOW());

-- Insert Pets for Owner 3 (Mike Williams)
INSERT INTO pets (name, species, breed, color, weight, date_of_birth, microchip_number, medical_record_number, medical_history, vaccinations, owner_id, created_at, updated_at) 
VALUES ('Rex', 'Dog', 'Labrador', 'Black', 28.0, '2018-08-20', 'CHIP-005-DOG', 'MR-005-DOG', 'Senior dog, annual checkups recommended', 'Rabies (2024), DHPP (2024)', 3, NOW(), NOW());

-- Insert Consultations
INSERT INTO consultations (pet_id, owner_id, veterinarian_id, title, description, symptoms, diagnosis, notes, status, consultation_date, completed_at, rating, feedback, created_at, updated_at) 
VALUES (1, 1, 4, 'Annual Checkup', 'Regular annual health checkup', 'None', 'All vitals normal, pet is healthy', 'Recommended continued balanced diet', 'COMPLETED', '2024-02-15 10:00:00', '2024-02-15 10:45:00', 5.0, 'Great service!', NOW(), NOW()),
       (2, 1, 4, 'Hairball Control', 'Pet has frequent hairballs', 'Vomiting once weekly', 'Excessive hair ingestion', 'Increase grooming frequency', 'COMPLETED', '2024-02-10 14:00:00', '2024-02-10 14:30:00', 4.5, 'Very helpful', NOW(), NOW()),
       (3, 2, 4, 'Weight Management', 'Dog is overweight', 'Lethargy, difficulty moving', 'Overweight condition, BMI elevated', 'Recommend diet change and more exercise', 'COMPLETED', '2024-02-20 11:00:00', '2024-02-20 11:45:00', 4.0, 'Good advice', NOW(), NOW()),
       (4, 2, 5, 'Behavioral Consultation', 'Bird is very loud', 'Excessive noise', 'Normal parakeet behavior, may need more socialization', 'Spend more time with pet', 'COMPLETED', '2024-02-18 15:00:00', '2024-02-18 15:30:00', 5.0, 'Excellent advice!', NOW(), NOW()),
       (5, 3, 4, 'Senior Health Check', 'Senior dog routine checkup', 'Slight stiffness in legs', 'Age-related, minor arthritis probable', 'Consider joint supplements, light exercise', 'SCHEDULED', '2024-03-10 09:00:00', NULL, NULL, NULL, NOW(), NOW());

-- Insert Prescriptions
INSERT INTO prescriptions (consultation_id, pet_id, veterinarian_id, medication_name, dosage, frequency, duration, instructions, side_effects, status, prescribed_date, start_date, end_date, notes, created_at, updated_at) 
VALUES (2, 2, 4, 'Laxatone', '1 tsp', 'Daily', 30, 'Mix with food', 'None known', 'ACTIVE', '2024-02-10', '2024-02-10', '2024-03-10', 'For hairball management', NOW(), NOW()),
       (3, 3, 4, 'Prescription Diet Food', '200g', 'Twice daily', 90, 'Feed in morning and evening', 'None', 'ACTIVE', '2024-02-20', '2024-02-20', '2024-05-20', 'Low calorie diet for weight loss', NOW(), NOW()),
       (5, 5, 4, 'Glucosamine Joint Supplement', '500mg', 'Daily', 180, 'Mix with food or give with treat', 'Rare digestive upset', 'ACTIVE', '2024-03-10', '2024-03-10', '2024-09-10', 'For joint health and mobility', NOW(), NOW());

-- Reset sequences to avoid conflicts
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users) + 1);
SELECT setval('pets_id_seq', (SELECT MAX(id) FROM pets) + 1);
SELECT setval('consultations_id_seq', (SELECT MAX(id) FROM consultations) + 1);
SELECT setval('prescriptions_id_seq', (SELECT MAX(id) FROM prescriptions) + 1);
