# AWS 상태

## 현재 상태

[CONFIRMED] 사용자는 현재 이 프로젝트에 AWS infrastructure가 없다고 밝혔습니다.

[CONFIRMED] 이 repository에서 AWS SDK dependency, AWS resource configuration, S3 adapter, Terraform, CloudFormation, CDK, ECS, EKS, Lambda, EC2, RDS, ElastiCache, CloudFront, Route 53, ACM configuration은 발견되지 않았습니다.

## 기존 deployment evidence

[CONFIRMED] `.github/workflows/deploy.yml`은 self-hosted GitHub Actions runner를 사용하여 `main`에 push될 때 deploy합니다.

[CONFIRMED] 해당 workflow는 runner host의 고정 path에서 `.env`, `docker-compose.yml`, 선택적으로 `monitoring`을 복사한 뒤 `docker compose down`과 `docker compose up -d --build`를 실행합니다.

[CONFIRMED] 해당 workflow에는 별도의 automated test, application health verification, production approval, rollback 단계가 없습니다.

[UNKNOWN] Self-hosted runner workspace에 복사된 `.env`의 cleanup 및 접근 통제 상태는 repository에서 확인할 수 없습니다.

[CONFIRMED] Production Compose는 PostgreSQL, Redis, MinIO가 Compose service 외부에 이미 존재한다고 가정합니다. `host.docker.internal` 또는 environment variable을 사용하여 host-local service에 접근합니다.

[CONFIRMED] Production Compose에는 application, Prometheus, Grafana service가 포함됩니다. mount하는 monitoring file은 이 repository에 없습니다.

## AWS 이전 전 migration 및 운영 결정

[INFERRED] AWS 이전 전에는 기존 PostgreSQL data의 export/import와 verification, Redis refresh-token cutover, MinIO object inventory와 migration, traffic cutover 및 rollback plan이 필요합니다.

[INFERRED] OAuth provider redirect URL, CORS, Domain/DNS, TLS certificate 변경 영향을 함께 검토해야 합니다.

[UNKNOWN] Monitoring, logging, alerting, backup, recovery objective와 cost-control requirement는 아직 결정되지 않았습니다.

## AWS implementation 상태

[UNKNOWN] 의도한 AWS region, account structure, networking, compute platform, IAM policy, DNS, certificate, database service, object-storage migration approach, backup policy, cost budget은 이 repository에 명시되어 있지 않습니다.

[UNKNOWN] AWS target architecture, AWS Service 선택 및 IaC 도구는 아직 결정되지 않았습니다.

[INFERRED] 기존 host-oriented deployment를 AWS로 이전하려면 deployment configuration 변경만이 아니라 새로운 infrastructure design과 implementation이 필요합니다.
