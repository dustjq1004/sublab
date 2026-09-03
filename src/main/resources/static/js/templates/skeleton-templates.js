// 스켈레톤 카드 템플릿
const skeletonCardTemplate = () => {
    return `
        <div class="col skeleton-card">
            <div class="skeleton skeleton-image"></div>
            <div class="skeleton skeleton-title"></div>
            <div class="skeleton skeleton-subtitle"></div>
        </div>
    `;
};

// 스켈레톤 로딩 템플릿 (여러 개)
const skeletonLoadingTemplate = (count = 6) => {
    let skeletonHTML = '';
    for (let i = 0; i < count; i++) {
        skeletonHTML += skeletonCardTemplate();
    }
    return skeletonHTML;
};

// 스켈레톤 로딩 표시
const showSkeletonLoading = (htmlId) => {
    const skeletonHTML = skeletonLoadingTemplate(12); // 6개의 스켈레톤 카드
    $(htmlId).append(skeletonHTML);
};

// 스켈레톤 로딩 숨기기
const hideSkeletonLoading = () => {
    // 스켈레톤은 success/fail 콜백에서 실제 데이터로 교체되므로 별도 처리 불필요
};