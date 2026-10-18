# sublab

> Markdown 기반 개인 블로그 플랫폼
> 실제 서비스를 목적으로한 Spring Boot 기반 개인 블로그입니다.

AWS 기반 운영 환경을 직접 구축, Github Action을 활용하여 테스트/빌드/배포 자동화, Prometheus/Grafana 기반 모니터링 등 실제 운영을 위한 아키텍쳐를 설계하고 구현했습니다.
현재는 AI를 활용하여 기능을 개선 및 확장해가고 있습니다.

---

## 로컬 실행

Java 17 이상이 필요합니다. 저장소 루트에서 실행합니다.

```bash
cp .env.example .env
./gradlew bootRun --args='--spring.profiles.active=local'
```

기본값으로 H2를 사용합니다. Google 로그인과 S3 업로드를 사용하려면 `.env`에 본인의 자격증명을 설정하세요. 실제 `.env`는 커밋하지 않습니다.

```bash
./gradlew build
```

이 저장소는 운영 프로젝트의 공개용 코드 사본이며 운영 저장소의 Git 이력은 포함하지 않습니다.

## 주요 기능 / Features

- 소셜 로그인 : Google OAuth2, JWT
- 블로그 : 마크다운(CommonMark / Flexmark) 기반 블로그 에디터, 관리자 기능, AWS S3 Presigned URL 이미지 업로드, 임시 저장, 비밀글
- GitHub Actions 기반 배포 자동화
- Spring Boot Actuator + Prometheus + Grafana 모니터링
- AWS EC2, Nginx, Docker, MySQL 기반 서버 환경 구성
- Swagger UI API 문서

---

## 기술 스택 / Tech Stack

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security 6, Google OAuth2, JWT (jjwt 0.9.1) |
| ORM | JPA / Hibernate |
| DB (local) | H2 in-memory |
| DB (prod) | MySQL 8 / AWS RDS |
| Infrastructure | AWS EC2, RDS, S3, Route53 |
| Deploy | Docker, GitHub Actions |
| Monitoring| Spring Boot Actuator, Prometheus, Grafana |
| Build | Gradle 8.2 |
| Docs | SpringDoc OpenAPI (Swagger) |

---

## 아키텍처 / Architecture

<img width="793" height="687" alt="image" src="https://github.com/user-attachments/assets/649b5402-d987-445e-9920-5008c1b6fb4c" />

### 패키지 구조

도메인별로 코드를 구성하며, Article과 Draft는 포트와 어댑터를 분리합니다.

```text
src/main/java/me/kimyeonsup/home/
├── domain/
│   ├── article/     # 글 조회·관리: adaptor / application / domain
│   ├── draft/       # 임시 저장: adaptor / application / domain
│   ├── menu/        # 카테고리·메뉴 관리
│   ├── login/       # 로그인·토큰 갱신
│   └── upload/      # 이미지 업로드
├── config/         # Security, OAuth2, JWT, S3 설정
├── global/         # 공통 API 응답, 예외, 필터
└── util/           # 공통 유틸리티
```

### 응답 포맷 / Response Format

모든 API 응답은 `ApiResponse<T>` 래퍼를 사용합니다.

```json
{
  "success": true,
  "data": { ... }
}
```
