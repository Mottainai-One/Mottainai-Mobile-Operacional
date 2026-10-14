# Mobile AI authentication

The IA chat sends the signed Firebase client ID token directly in the Bearer
header. It no longer calls POST /api/v1/ai/access-token on the SQL API. The IA
verifies the Firebase project/signature and resolves the existing operational
firebase_uid binding, numeric user/company IDs and database staff role.

## Local setup

Build with the IA service address reachable from the Android device. For the
standard emulator, host port 8001 is http://10.0.2.2:8001/:

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest -PiaBaseUrl=http://10.0.2.2:8001/ --no-daemon
```

Enable FIREBASE_AUTH_ENABLED and configure FIREBASE_PROJECT_ID in IA. The
operational account must already contain the verified Firebase UID and the
canonical protected email, an active employee, store, company and staff role.
Analytical consultations need a successful official publication. No identity
or company is supplied by the chat request body.

## Validation on 2026-10-14

Debug build and unit tests passed. The opt-in StaffAiChatSmokeTest ran on the
Android emulator with real Firebase logins for Estoquista, Gerente and Dono.
Each run entered the login screen, opened IA/Chat, sent a message to the real
IA service, asserted the returned business values and captured the screen.
The IA used an isolated installation of both official database schemas with
synthetic business fixtures; Firebase accounts were the existing supplied
accounts. No official table was added or removed.

Expected fixture answers: Estoquista/Gerente stock 48, minimum 7; Dono revenue
100 BRL, disposal cost 6 BRL and two active alerts. The IA API also rejected
cross-user history access and protected owner-only routes from the other roles.

The instrumentation test is opt-in. Supply testEmail, testPassword, testRole,
questionBase64 and expectedNumbers as runner arguments; do not commit passwords
or ID tokens. It is skipped when credentials are not supplied. Screenshots and
answer text are saved to the target app's external files directory.

Scope: Firebase login and IA chat. This does not validate every unrelated
mobile feature or migrate the existing Alerts/Suggestions repositories.
