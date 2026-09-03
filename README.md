# sup-lab (포트폴리오 공개용)
해당 Repository는 포트폴리오 공개용 GitHub Repository입니다. 실제 운영 중인 Repository는 보안 및 프로젝트 신규 개발 진행상의 이유로 비공개로 관리하고 있으며, 포트폴리오에는 주요 설계 및 구현 내용을 중심으로 선별하여 공개하였습니다.

> Markdown 기반 개인 블로그 플랫폼
> 실제 서비스를 목적으로한 Spring Boot 기반 개인 블로그입니다.

AWS 기반 운영 환경을 직접 구축, Github Action을 활용하여 테스트/빌드/배포 자동화, Prometheus/Grafana 기반 모니터링 등 실제 운영을 위한 아키텍쳐를 설계하고 구현했습니다.
현재는 AI를 활용하여 기능을 개선 및 확장해가고 있습니다.

---

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
| Template | Thymeleaf |
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
DDD 기반 계층형 아키텍처를 따릅니다.  
Controller → Service → Repository → Entity 계층 간 엄격한 단방향 의존성을 유지합니다.

```
src/main/java/me/kimyeonsup/home/
├── domain/
│   ├── blog/
│   │   ├── article/        # 공개 글 조회 / Public article view
│   │   ├── admin/
│   │   │   ├── article/    # 관리자 글 CRUD / Admin article CRUD
│   │   │   └── menu/       # 카테고리·메뉴 관리 / Category & menu
│   │   ├── draft/          # 임시 저장 / Draft management
│   │   └── markdown/       # Markdown 파싱 / Markdown parsing
│   ├── main/               # 홈 화면 / Homepage aggregation
│   └── upload/             # 파일 업로드 / File upload
├── global/
│   ├── api/                # 공통 응답 ApiResponse<T>
│   ├── config/             # Security, S3, JPA 설정
│   ├── exception/          # 비즈니스 예외 처리
│   └── auth/               # OAuth2, JWT
├── login/                  # 로그인·토큰 갱신 / Auth & token refresh
└── infra/                  # S3, Markdown 인프라 / Infrastructure
```

### 응답 포맷 / Response Format

모든 API 응답은 `ApiResponse<T>` 래퍼를 사용합니다.

```json
{
  "success": true,
  "data": { ... }
}
```

