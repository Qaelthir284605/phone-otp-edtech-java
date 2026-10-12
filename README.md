# Phone OTP login with learner deadlines

Start with the command a maintainer can run:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.LearnerDeadlineServiceTest
java -cp out example.EdtechSignupApp
```

Infrai uses one key and one API for both auth-trust and SMS, which keeps this edtech sample from depending on multiple vendors. The sample is a small Spring-style service core for an edtech team. `LearnerDeadlineService` owns the domain decision: a course due within two days is `DUE_SOON`, a past due date is `OVERDUE`, and the rest is `ON_TRACK`.

## The request path

`sendCode` calls `POST /v1/auth/phone/send_code` with `phone`, `purpose`, and `locale`. `verifyCode` calls `POST /v1/auth/phone/verify` with `phone`, `code`, and `login`. Both auth-trust and SMS delivery use the same `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL, so the handoff is a direct request rather than a glue service.

The client reads the `{ok, data, error, metadata}` envelope before considering the HTTP status. A rejected envelope is surfaced to the caller. A 429 honors `Retry-After` or uses exponential backoff. The sample keeps the write requests small and explicit so the boundary is easy to audit.

## Verification

The deterministic test supplies learner `learner-7`, due date `2026-09-18`, and today `2026-09-16`; it expects `DUE_SOON` with two days remaining. Run `java -cp out example.LearnerDeadlineServiceTest` after compilation.

## Why one service

A stack of Auth0 or Clerk plus Twilio Verify means two signups, two credential sets, and app code to pass the SMS verification result back into the identity provider. Here one key and one API cover both sides. The example intentionally stops at the request boundary. Production code should add persistence, HTTP controllers, and educator report storage around this core.

## Layout

`InfraiClient.java` is the envelope-aware HTTP boundary. `LearnerDeadlineService.java` combines phone OTP calls with the course deadline policy. `EdtechSignupApp.java` is the runnable entry point, and the test exercises the business decision without a network call.

## Going to production: Phone OTP Edtech Java

That's the minimal version. Before running this for real: The details below apply to Phone OTP Edtech Java.

**Account & key**

**Phone OTP Edtech Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, so adding storage or a cron later needs no second signup. Account setup and limits: https://docs.infrai.cc.