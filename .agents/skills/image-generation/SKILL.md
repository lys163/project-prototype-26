# Image Generation Skill

## 목적

picture, page-image, cover-image generation을 계획하거나 구현합니다.

## 확인된 기준선

- `Page`에는 image URL, image source, image prompt, layout override field가 있습니다.
- `ImageGenerationLog` 및 `CoverGenerationLog` entity가 존재합니다.
- storage module은 MinIO presigned URL을 발급할 수 있습니다.
- 이 repository에는 image-generation execution, provider client, generation endpoint가 없습니다.

## 절차

1. provider, model, prompt policy, cost, safety handling, result retention에 대한 product approval을 확인합니다.
2. book/page/character/style/layout input을 versioned generation request에 명시적으로 전달합니다.
3. generated asset에는 승인된 object-storage abstraction을 사용하며, binary 또는 credential을 entity field에 저장하지 않습니다.
4. generation을 asynchronous, idempotent, authorized, retry-aware하게 만듭니다.
5. schema가 승인된 workflow를 지원하는 경우에만 기존 model을 사용하여 선택된 result와 alternative를 기록합니다.
6. authorization, result persistence, object-storage failure, provider failure, retry, selection behavior를 test합니다.
