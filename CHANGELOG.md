# Changelog

## Unreleased

- Keep normal Firebase e-mail/password login; remove diagnostic/test shortcuts and compiled test credentials.
- Load product photos only from Open Food Facts by barcode, with queued requests and a persistent cache; keep profile uploads in Cloudinary per Firebase UID.

- Resolve mobile identity, role, store and company from the operational API profile;
  remove the Firebase custom-claim requirement and legacy Firestore user lookup.

- Authenticate passwords only with Firebase, use verified Firebase ID tokens for
  the operational API and exchange integration credentials only when needed.

- Revalidate the active SQL assignment before restoring the normal mobile session;
  operational integration tokens are not a prerequisite for Firebase login.

- Apply the Operational launcher artwork exported from Figma and Icon Kitchen,
  including all five density buckets and adaptive round/square launcher support.
  The ZIP's opaque monochrome layer is not enabled for themed icons.

- Replace staff-directory Firestore listing with authenticated employees-store API;
  reject cross-store responses and offer retry instead of displaying an empty list.
- Deny unknown roles, protect direct suggestion/invitation/member-edit entry,
  and bind main-session navigation to the current Firebase UID.
- Preserve chat UI state during recreation and scope conversation IDs to account,
  store and IA server. Ignore callbacks from destroyed views or switched accounts.
- Guard token refresh against account switches; remove Firestore/FCM registration
  and use the authenticated Redis inbox for internal notifications.
- Confirm PDV decisions and make failed approval-list loading retryable.
- Remove invitation/member-edit shortcuts from the MVP; retain read-only staff directory.

- Complement product list/detail images using Open Food Facts barcode lookups,
  keeping official image URLs first, deduplicating requests and limiting public
  API reads. Prefer front images and keep placeholders for unavailable products.
- Preserve Open Food Facts image URLs when using auxiliary product-form lookup.
- Allow profile-photo selection from Profile and My Account, uploading to the
  existing Cloudinary service and storing the returned URL per Firebase UID.
- Profile URL references remain local; cross-device restoration is not implemented.

Validation: three image DTO tests passed through direct JUnit execution; XML
and whitespace checks passed. Full Android build/test was blocked before source
compilation by the Windows Gradle loopback-connection error. Cloudinary upload
and UI still need validation on device with the configured preset.

- Autenticação SQL diretamente com Firebase ID Token; permissões pelo banco, sem senha SQL ou descriptografia de nome/e-mail no login. IA/Redis mantêm ponte de integração independente.
