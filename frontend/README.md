# Splitease — Frontend

React + Tailwind frontend for your Spring Boot Splitease backend.

## Setup

```bash
npm install
npm run dev
```

Opens at `http://localhost:5173`.

## Connect to your backend

Make sure your Spring Boot backend is running (default `http://localhost:8080`).

If it runs on a different host/port, edit:

```
src/api/client.js  →  BASE_URL
```

## What's included

- **Auth**: register, login, JWT stored in `localStorage`, auto-attached to every request, auto-redirect to `/login` on 401.
- **Dashboard**: list your groups, create a new group.
- **Group page**:
  - **Expenses tab**: add / edit / delete expenses with EQUAL, PERCENTAGE, or EXACT splits (with live validation matching the backend rules).
  - **Balances tab**: net balance per member, plus the backend's simplified-debt suggestions with a one-click "Settle" action.
  - **Members**: search-and-add by name/email, or add directly by email; remove a member.

## Notes

- The backend's `GroupResponse` doesn't include a member list (only a count), so the member list used across the app (for split pickers, settle-up, etc.) is derived from the `/balances` endpoint, which does return every member's `userId` + `userName`.
- Amounts are shown with a ₹ symbol — swap the symbol in the relevant components if your app uses a different currency.
