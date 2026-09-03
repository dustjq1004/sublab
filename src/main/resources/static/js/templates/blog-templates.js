const articleVisibilityLabel = (visibility) => visibility === 'PUBLIC' ? '공개' : '<i class="bi bi-lock-fill"></i>';
const articleVisibilityClass = (visibility) => visibility === 'PUBLIC'
    ? 'article-status-badge article-status-badge-public'
    : 'article-status-badge article-status-badge-private';
const articleStatusBadgeTemplate = (visibility) => {
    if (visibility === 'PRIVATE') {
        const label = articleVisibilityLabel(visibility);
        const badgeClass = articleVisibilityClass(visibility);
        return `<span class="${badgeClass} article-list-status-badge">${label}</span>`;
    }

    return "";
}

const articlesTemplate = (articles, totalPages) => {
    if (totalPages === 0) {
        return `
                <div class="empty-article-message">
                  <div class="empty-article-icon">
                    <!-- Bootstrap 아이콘 사용: 느낌표 원형 -->
                    <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                      <circle cx="32" cy="32" r="30" stroke="#3B82F6" stroke-width="3"/>
                      <text x="32" y="44" text-anchor="middle" font-size="40" fill="#3B82F6" font-family="Arial, sans-serif">!</text>
                    </svg>
                  </div>
                  <div class="empty-article-text">
                    조회된 블로그 글이 없습니다.
                  </div>
                </div>
            `
    }
    const template = articles.map(article =>
            `
                <div class="col">
                    <a href="/blog/${article.menuName}/${article.id}" class="link-offset-2 link-underline link-underline-opacity-0">
                        <div class="card border border-0 p-2 rounded-4 article-list-card">
                            ${articleStatusBadgeTemplate(article.visibility)}
                            <svg class="bd-placeholder-img card-img-top rounded-4" width="100%" height="180"
                                 xmlns="http://www.w3.org/2000/svg" role="img"
                                 preserveAspectRatio="xMidYMid slice" focusable="false"><title></title>
                                <image height="100%" width="100%"
                                       preserveAspectRatio="xMidYMid slice"
                                       href="${article.thumbnailUrl}"></image>

                            </svg>
                            <div class="article-card card-body">
                                <div class="article-card-meta mb-2">
                                    <span class="text-secondary fs-7"
                                          text="${moment(article.updatedAt).format("YYYY.MM.DD")}"></span>
                                </div>
                                <h5 class="mb-1">${article.title}</h5>
                                <p class="card-text text-body-tertiary text-truncate">${article.subTitle}</p>
                            </div>
                        </div>
                    </a>
                </div>
            `
    ).join("");
    return template;
}
