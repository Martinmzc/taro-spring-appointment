# taro-spring-appointment (WeChat Mini Program)

A minimal full-stack demo for:
- Login with phone number + fixed verification code
- Authenticated API access (Bearer token)
- Create appointment (idempotent: no duplicate records for same user + same appointment)
- List "My appointments" and see newly created appointment immediately

## Tech Stack
### Frontend
- Taro (TypeScript) 
- WeChat Mini Program

### Backend
- Java 21
- Spring Boot 3.x
- Gradle
- REST + JSON
- H2 (in-memory)

---

## How to Start Backend
```bash
cd backend
./gradlew bootRun
```

Backend default address:
- http://127.0.0.1:8080

Quick check:
```bash
curl http://127.0.0.1:8080/api/ping
```

---

## How to Run the Mini Program (Frontend)
```bash
cd frontend
npm install
npm run build:weapp
```

Then open `frontend/dist` using **WeChat DevTools**.

### Backend Base URL used by Frontend
Frontend base URL is currently configured in:
- `frontend/src/services/request.ts`

Default value:
- `http://127.0.0.1:8080`

> Note: Real device testing requires an accessible IP/domain (LAN IP or tunneling). This is not required for this challenge.

---

## Verify the Appointment Flow (Manual Steps)
1. Start the backend.
2. Build the frontend and open it in WeChat DevTools.
3. Go to **Login** page:
   - Phone: any numeric string (e.g. `13800000000`)
   - Verification code: fixed value `123456`
   <img width="388" height="779" alt="image" src="https://github.com/user-attachments/assets/07387b69-4a1a-4040-bf2e-fe4aca872142" />

4. After login, navigate to **Create appointment** page and submit:
   - Service name (string)
   - Date (YYYY-MM-DD)
   - Time slot (string)
   <img width="373" height="784" alt="image" src="https://github.com/user-attachments/assets/be74034c-e519-42f5-8b84-8d1b39abc928" />

5. Navigate to **My appointments** page:
   - The newly created appointment should appear immediately.
   <img width="376" height="790" alt="image" src="https://github.com/user-attachments/assets/1b6dc50e-5a76-4fab-9b5e-e3821aee06a8" />

6. Submit the same appointment again with the same values:
   - No duplicate record should be created (idempotency check).

---

## Notes on Idempotency
Backend enforces idempotency using a database unique constraint:
- `(user_id, service_name, date, time_slot)`

If the same authenticated user submits the same appointment multiple times, backend returns the existing record instead of creating duplicates.

---

## Error Handling (Simple)
- If accessing protected APIs without a valid token, backend returns `401`.
- Frontend clears token and redirects to Login page on `401`.
