# ecommerce-user-service
I will develop an e-commerce website backend (project is suggested by ChatGPT) to develop my azure, microservices and kubernetes experience
docker build -t user-service:0.0.1-SNAPSHOT -f docker/Dockerfile .
docker tag user-service:0.0.1-SNAPSHOT bmcnpnr/ecommerce-user-service:latest
docker push bmcnpnr/ecommerce-user-service:latest

## Contract tests (Pact)

- `src/test/java/.../contract/UserServiceProviderPactTest` — **provider** for api-gateway: (1) `GET /api/v1/users/me` resolved from the injected `X-Username` header, replayed over HTTP against the real application on H2; (2) the JWT issued at login — claim names/types (`sub`, numeric `userId`, `role`) and HS256/384/512 — decoded from a real `UserService.login` and matched as a message pact.

Run them alone with `mvn test -Dtest='*PactTest'`; they are ordinary Surefire tests, so `mvn verify` and CI run them too. Regenerate and redistribute pacts across repositories with `ecommerce-platform/sync-pacts.sh` (see its README, "Contract tests").
