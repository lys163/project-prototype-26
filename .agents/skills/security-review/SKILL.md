# Security Review Skill

## 목적

제안되었거나 완료된 변경의 authentication, authorization, secret handling, storage, input validation, operational exposure를 검토합니다.

## 알려진 repository finding

- OAuth2 success handling은 현재 access token과 refresh token을 logging합니다.
- Development Compose에는 commit된 plaintext secret material이 포함되어 있습니다.
- 구성된 MinIO bucket은 initialization 중에 public read access를 부여받습니다.
- 일치하는 규칙이 없는 security route는 기본적으로 permit-all입니다.

## 절차

1. 영향을 받는 모든 endpoint의 route matching과 authorization을 검토합니다.
2. authentication이 변경될 때 OAuth, JWT, refresh token, CORS, cookie, Redis에 미치는 영향을 검토합니다.
3. request validation, object ownership, object-key handling, storage ACL/policy, log, error message를 검토합니다.
4. finding, test, commit, documentation에 sensitive value를 절대 포함하지 않습니다.
5. evidence와 severity를 기준으로 finding을 보고하며, 사용할 수 없는 operational control에는 `[UNKNOWN]`을 사용합니다.
6. 명시적인 authorization 없이 security posture 또는 production resource를 수정하지 않습니다.
