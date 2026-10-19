# Mobile app against QA

The Android app does not run inside Docker. It uses the API and IA services started by the shared QA Compose stack. The existing `debug` build already accepts `apiBaseUrl` and `iaBaseUrl` as Gradle or `local.properties` values; this QA check builds with the local emulator addresses.

For an Android emulator running on the same computer as the QA Compose stack, put this in the ignored `local.properties` file:

```properties
apiBaseUrl=http://10.0.2.2:18080/
iaBaseUrl=http://10.0.2.2:18000/
```

Run `./gradlew assembleDebug` or use Android Studio. Check that the API responds at `/actuator/health` and IA at `/readyz` before testing the app. A physical phone cannot use `10.0.2.2`; once the team has a shared HTTPS host, configure both URLs with that host and a trailing slash. Never point the QA app at real operational data.

The current Firebase configuration is still shared with the existing debug app. The QA build should not be distributed to the whole team until a dedicated Firebase QA project and app registration are configured; this prevents test accounts and notifications from touching the production Firebase project.
