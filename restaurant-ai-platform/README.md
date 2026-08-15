# Restaurant AI WhatsApp Automation Platform

Single-restaurant MVP: Spring Boot 3 / Java 21 backend, Gemini for the AI/tool-calling
loop, PostgreSQL, mock WhatsApp (real Meta adapter exists but is dormant), and a
React dashboard.

## Backend

```bash
cd restaurant-ai-platform
cp .env.example .env      # fill in GEMINI_API_KEY, generate a real JWT_SECRET
docker compose up
```

On first boot, `DevUserSeeder` creates a `RESTAURANT_OWNER` account and logs a
one-time password - watch the app container's logs for it:

```
======================================================================
 No dashboard users existed - created a default owner account:
   email:    owner@spiceroute.test
   password: <random>
======================================================================
```

Backend runs at `http://localhost:8080`.

### Testing the AI loop without WhatsApp

```bash
curl -X POST localhost:8080/api/ai/chat -H "Content-Type: application/json" \
  -d '{"message":"do you have anything vegetarian under 200 rupees?"}'
```

Add `"customerWhatsappNumber": "+919000000001"` to the body to get a real
persisted `Customer`/`Conversation`, which lets you also exercise the order
tools (`create_order`, `cancel_order`, etc.) through this endpoint.

### Simulating an inbound WhatsApp message

POST a Meta-shaped payload at `/api/webhooks/whatsapp` (see
`WhatsAppWebhookPayload` for the exact shape) to exercise the real pipeline -
webhook → conversation → Gemini → mock WhatsApp send (logged, not sent).

## Frontend

```bash
cd frontend
npm install
cp .env.example .env.local   # defaults to http://localhost:8080, fine for local dev
npm run dev
```

Dashboard runs at `http://localhost:5173`. Log in with the seeded owner
credentials above.

## What's not built yet

- Order line-item options/add-ons in the ordering flow (simplified out of MVP)
- Analytics endpoints/charts (Phase 18)
- Menu item create/edit forms in the dashboard (API + role checks exist;
  the UI only supports availability toggling and browsing today)
- Refresh token revocation / server-side sessions
- WhatsApp webhook payload signature verification (works today via the
  verify-token handshake + manual/mock testing, not hardened for public
  internet traffic)

## A note on verification

This was built in a sandboxed environment without Maven, npm, or internet
access, so none of it has been compiled or run end-to-end. Everything was
written carefully and reviewed by re-reading, but treat the first real
`mvn clean compile` / `npm run build` as the actual test - small issues
(an import, a type mismatch) are likely and should be quick to fix with
the compiler's help.
