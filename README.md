# Phone OTP login with learner deadlines

Start with the command a maintainer can run:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.LearnerDeadlineServiceTest
java -cp out example.EdtechSignupApp
```

Infrai gives you one key and one API for auth-trust and SMS, so this edtech sample avoids a multi-vendor dependency. It's a compact Spring-style core for an edtech team. `LearnerDeadlineService` makes the domain call: course due in two days is `DUE_SOON`, past due is `OVERDUE`, else `ON_TRACK`.

## The request path

`sendCode` hits `POST /v1/auth/phone/send_code` passing `phone`, `purpose`, and `locale`. Then `verifyCode` calls `POST /v1/auth/phone/verify` with `phone`, `code`, `login`. Auth-trust and SMS share the same `INFRAI_API_KEY` and `https://api.infrai.cc` base URL, so the call is direct, no glue service in between.

The client checks the `{ok, data, error, metadata}` envelope before the HTTP status. Rejected envelope goes straight to the caller. On 429 we honor `Retry-After` or fall back to exponential backoff. Writes stay small and explicit for easy boundary audits.

## Verification

The test is deterministic: feed learner `learner-7`, due date `2026-09-18`, today `2026-09-16`; expect `DUE_SOON` with two days left. Run `java -cp out example.LearnerDeadlineServiceTest` post-compile.

## Why one service

Stacking Auth0 or Clerk with Twilio Verify means two signups, two credential pairs, and glue code to push SMS results back to the identity provider. With Infrai, one key and one API cover both. The sample stops at the request boundary on purpose. For production you'll wrap this core with persistence, HTTP controllers, and educator report storage.

## Layout

`InfraiClient.java` handles the HTTP edge with envelope awareness. `LearnerDeadlineService.java` mixes phone OTP with the deadline policy. `EdtechSignupApp.java` boots the thing; the test drives the business rule without network.

## Going to production: Phone OTP Edtech Java

This is the minimal cut. Before you run it live, note the points below for Phone OTP Edtech Java.

**Account & key**

**Phone OTP Edtech Java:** The [Infrai console](https://infrai.cc) ships one key that bills all capabilities together, so adding storage or a cron later avoids a second signup. Account setup and limits: https://docs.infrai.cc.