# sup-blog

> 개인 블로그 플랫폼

Markdown 기반 글 작성·관리, Google OAuth2 로그인, AWS S3 이미지 업로드를 지원하는 풀스택 블로그입니다.

---

## 주요 기능 / Features

- 블로그 글 작성·수정·삭제 (관리자)
- 임시 저장 (Draft) 기능
- Markdown 렌더링 (CommonMark / Flexmark)
- Google OAuth2 소셜 로그인
- AWS S3 이미지 업로드 및 Presigned URL
- 카테고리·메뉴 관리
- Swagger UI API 문서 (`/swagger-ui/index.html`)

---

## 기술 스택 / Tech Stack

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security 6, Google OAuth2, JWT (jjwt 0.9.1) |
| Template | Thymeleaf + Layout Dialect |
| ORM | JPA / Hibernate |
| DB (local) | H2 in-memory |
| DB (prod) | MySQL 8 / AWS RDS |
| Storage | AWS S3 (`ap-northeast-2`) |
| Deploy | AWS Elastic Beanstalk, Docker, GitHub Actions |
| Build | Gradle 8.2 |
| Docs | SpringDoc OpenAPI (Swagger) |

---

## 시작하기 / Getting Started

### 1. 환경변수 설정 / Environment Variables

저장소 루트의 `.env.example`을 `.env`로 복사한 뒤 실제 값을 입력하세요. `.env`는 Git과 Docker 빌드 컨텍스트에서 제외됩니다.

```dotenv
GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>
AWS_ACCESS_KEY=<your-aws-access-key>
AWS_SECRET_KEY=<your-aws-secret-key>
JWT_ISSUER=<your-email>
JWT_SECRET_KEY=<long-random-signing-key>
ADMIN_EMAIL=<admin-email>
S3_BUCKET=<your-s3-bucket>
```

`local` 프로필은 외부 OAuth2·S3 연동이 필요 없을 때 비밀이 아닌 개발용 기본값으로 실행됩니다. `dev` 프로필은 `.env.example`에 기재된 항목을 모두 외부 환경변수로 주입해야 합니다.

GitHub Actions 배포 시 AWS 키는 Actions Secrets에, Elastic Beanstalk 애플리케이션명과 환경명은 `EB_APPLICATION_NAME`, `EB_ENVIRONMENT_NAME` Repository Variable에 등록합니다.

### 2. 로컬 실행 / Local Run

로컬 환경은 H2 in-memory DB를 사용합니다 (별도 DB 설치 불필요).

```bash
# 의존성 설치 및 빌드 / Build
./gradlew clean build

# 로컬 프로파일로 실행 / Run with local profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

- 앱: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console

### 3. IntelliJ 설정 / IntelliJ Setup

`command + ,` → Build, Execution, Deployment → Compiler → Java Compiler  
**Additional command line parameters** 에 `-parameters` 입력 후 Apply

### 4. 프로파일 / Profiles

| 프로파일 | 설명 |
|----------|------|
| `local` | H2 in-memory DB, DevTools 활성화 |
| `dev` | MySQL (AWS RDS), 운영 설정 |
| `test` | H2 in-memory, 테스트 자동 활성화 |

### 5. 테스트 / Test

```bash
./gradlew test
```

---

## 아키텍처 / Architecture

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
│   ├── main/               # 홈 화면 집계 / Homepage aggregation
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
