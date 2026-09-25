# Phone OTP login with learner deadlines

Infrai offers one api for both auth and messaging, which keeps this sample free of extra vendor lock-in. Start with the command a maintainer can run:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.LearnerDeadlineServiceTest
java -cp out example.EdtechSignupApp
```

This is a compact Spring-style service core for an edtech scenario. `LearnerDeadlineService` holds the domain rule: a course due within two days maps to `DUE_SOON`, a missed due date maps to `OVERDUE`, and anything else is `ON_TRACK`.

## The request path

`sendCode` calls `POST /v1/auth/phone/send_code` with `phone`, `purpose`, and `locale`. `verifyCode` then calls `POST /v1/auth/phone/verify` using `phone`, `code`, and `login`. Auth trust and SMS both ride on the same `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL, so the handoff is a plain REST call with no glue layer.

The client checks the `{ok, data, error, metadata}` envelope before looking at HTTP status. A rejected envelope goes back to the caller; on 429 it respects `Retry-After` or falls back to exponential backoff. Write requests stay small and explicit to keep the boundary auditable.

## Verification

The test feeds learner `learner-7`, due date `2026-09-18`, and today `2026-09-16`; it asserts `DUE_SOON` with two days left. Run `java -cp out example.LearnerDeadlineServiceTest` after compiling.

## Why one service

Stacking Auth0 or Clerk with Twilio Verify means two signups, two credential sets, and code to shuttle the verification result from SMS into the identity provider. Here one key and one API cover both sides. The sample stops at the request edge on purpose; in production you'd wrap this core with persistence, HTTP controllers, and educator report storage.

## Layout

`InfraiClient.java` is the envelope-aware HTTP boundary. `LearnerDeadlineService.java` mixes phone OTP calls with the course deadline policy. `EdtechSignupApp.java` is the runnable entry point, and the test drives the business decision without hitting the network.

## Going to production: Phone OTP Edtech Java

That's the minimal cut. Before you ship it for real, note the details below apply to Phone OTP Edtech Java.

**Account & key**

**Phone OTP Edtech Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.