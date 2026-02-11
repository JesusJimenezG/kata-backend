# Functional requirements rules for AI agents

Source of truth: [REQUIREMENTS.md](../../REQUIREMENTS.md). Implement the **minimum functional requirements** first; treat “Extras valorados” as optional and only add them if explicitly requested.

## User authentication & authorization

- Provide user registration and login with at least email + password.
- Store passwords **hashed** (never plaintext).
- Use JWT (or equivalent) for sessions.
- Support roles: `USER` and `ADMIN` (names may vary, but semantics must match).

## Resource management

- Implement full CRUD for resources: create, edit, delete, list, and detail.
- Enforce **unique resource names** (no duplicates).

## Reservations & availability

- Create reservations with start and end date/time for a given resource.
- Prevent overlapping reservations for the same resource when time intervals intersect.
- Provide listings of **active reservations**:
  - By user
  - Global
- Allow reservation cancellation only by the reservation creator or an admin.

## Quick availability view

- Provide a simple table/calendar-style view showing reserved vs available slots per resource.

## Reservation history

- Allow querying past reservations by resource.

## API documentation

- Document API endpoints in one of: README, Swagger/OpenAPI, Postman (or similar).

## Deliverables requirements (don’t skip)

- Ensure the README includes: how to run locally and how to test.
- Add unit tests covering:
  - Authentication: login success and login failure
  - Resource creation
  - Reservation creation
  - Overlap prevention
- Add a README section explaining architecture/tech decisions.
- Add a README section describing how/where AI was used and the criteria to accept/modify suggestions.
