# Public Release Checklist

## 공개 전 필수 조치

- [ ] 기존 Git 이력을 포함하지 않는 새 저장소를 생성한다.
- [ ] 과거에 커밋된 Google OAuth client secret을 폐기하고 새로 발급한다.
- [ ] 과거에 커밋된 JWT signing key를 폐기하고 긴 난수 키로 교체한다.
- [ ] AWS, Google OAuth, RDS 자격증명을 GitHub Actions Secrets 또는 배포 환경에만 등록한다.
- [ ] `EB_APPLICATION_NAME`, `EB_ENVIRONMENT_NAME`을 GitHub Repository Variables에 등록한다.
- [ ] GitHub Secret scanning과 Push protection을 활성화한다.
- [ ] GitHub Private vulnerability reporting을 활성화한다.
- [ ] `main` 브랜치에 필수 CI 체크와 pull request 보호 규칙을 설정한다.
- [ ] 공개 전 `./gradlew clean build`와 secret scanner를 다시 실행한다.
- [ ] 원하는 오픈소스 라이선스를 선택해 `LICENSE`를 추가한다.

## Git 이력 주의

이 저장소의 초기 커밋에는 현재 파일에서 제거된 OAuth client secret과 JWT signing key가 포함되어 있습니다. 값을 지운 새 커밋을 추가해도 과거 이력에서 조회할 수 있으므로, 새 public 저장소에는 정리된 현재 파일만 첫 커밋으로 추가하고 기존 `.git` 이력을 옮기지 마세요.

불가피하게 기존 이력을 공개해야 한다면 `git-filter-repo` 등으로 모든 브랜치와 태그를 정리한 뒤 밀어내기 전에 별도 백업과 동료 협의가 필요합니다. 이력 정리 여부와 무관하게 노출된 자격증명은 반드시 교체해야 합니다.
