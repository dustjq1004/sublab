const heroLatestArticleTemplate = (item) => {
    if (!item) {
        return '<p class="text-secondary mb-0">표시할 최신 글이 없습니다.</p>';
    }

    const updatedAt = dayjs(item.updatedAt).format('YYYY.MM.DD');
    const author = item.author ? item.author : 'SupLab';
    return `
        <a href="/blog/${item.menuName}/${item.id}" class="hero-latest-link">
            <span class="hero-latest-category">${item.menuName}</span>
            <h3 class="hero-latest-title">${item.title}</h3>
            <p class="hero-latest-subtitle">${item.subTitle || '새 글 내용을 확인해보세요.'}</p>
            <div class="hero-latest-footer">
                <span>${author}</span>
                <span>·</span>
                <span>${updatedAt}</span>
            </div>
        </a>
    `;
}

const heroCategoryChipTemplate = (menuName) => {
    return `<a href="/blog/${encodeURIComponent(menuName)}" class="hero-topic-chip">${menuName}</a>`;
}

const featuredArticleTemplate = (item) => {
    if (!item) {
        return `
            <div class="featured-post-card h-100 d-flex align-items-center justify-content-center p-4">
                <p class="text-secondary mb-0">표시할 글이 없습니다.</p>
            </div>
        `;
    }

    const thumbnailUrl = item.thumbnailUrl ? item.thumbnailUrl : '/img/noimage02.png';
    const updatedAt = dayjs(item.updatedAt).format('YYYY.MM.DD');
    const author = item.author ? item.author : 'SupLab';
    return `
        <a href="/blog/${item.menuName}/${item.id}" class="featured-post-link">
            <article class="featured-post-card h-100">
                <img class="featured-post-thumbnail" src="${thumbnailUrl}" alt="${item.title}">
                <div class="featured-post-body">
                    <div class="featured-post-meta">
                        <span class="badge text-bg-light border">${item.menuName}</span>
                        <span class="text-secondary fs-7">${updatedAt}</span>
                    </div>
                    <h3 class="featured-post-title">${item.title}</h3>
                    <p class="featured-post-subtitle">${item.subTitle || ''}</p>
                    <div class="featured-post-footer">
                        <span>${author}</span>
                        <span>최근 업데이트</span>
                    </div>
                </div>
            </article>
        </a>
    `;
}

const latestArticlesTemplate = (item) => {
    const thumbnailUrl = item.thumbnailUrl ? item.thumbnailUrl : '/img/noimage02.png';
    const updatedAt = dayjs(item.updatedAt).format('YYYY.MM.DD');
    return `
    <div class="col">
        <a href="/blog/${item.menuName}/${item.id}" class="post-card-link link-offset-2 link-underline link-underline-opacity-0">
            <div class="card border border-0 h-100">
                <img class="post-card-thumbnail" src="${thumbnailUrl}" alt="${item.title}">
                <div class="card-body">
                    <div class="post-card-meta">
                        <span class="badge text-bg-light border">${item.menuName}</span>
                        <span class="text-secondary fs-7 post-meta-date">${updatedAt}</span>
                    </div>
                    <h5 class="mb-2 post-card-title">${item.title}</h5>
                    <p class="card-text text-body-tertiary fs-6 post-subtitle">
                        ${item.subTitle}
                    </p>
                </div>
            </div>
        </a>
    </div>
    `
}

const popularArticlesTemplate = (item) => {
    const updatedAt = dayjs(item.updatedAt).format('YYYY.MM.DD');
    const author = item.author ? item.author : 'SupLab';
    return `
        <a href="/blog/${item.menuName}/${item.id}" class="popular-post-item text-decoration-none">
            <div class="d-flex align-items-center gap-3">
                <span class="popular-post-rank">${item.rank}</span>
                <div class="popular-post-text">
                    <span class="popular-post-title">${item.title}</span>
                    <div class="popular-post-meta">
                        <span>${item.menuName}</span>
                        <span class="mx-1">·</span>
                        <span>${author}</span>
                        <span class="mx-1">·</span>
                        <span>${updatedAt}</span>
                    </div>
                </div>
            </div>
        </a>
    `;
}

const categoryCardTemplate = (menuName, articleCount) => {
    return `
        <div class="col">
            <a href="/blog/${encodeURIComponent(menuName)}" class="category-card-link">
                <div class="card border-0 shadow-sm rounded-4 h-100 category-card">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <span class="section-eyebrow mb-2">Topic</span>
                            <h3 class="category-card-label mb-0">${menuName}</h3>
                        </div>
                        <span class="category-card-count">${articleCount}</span>
                    </div>
                </div>
            </a>
        </div>
    `;
}

const searchedArticlesTemplate = (item) => {
    return `<li>
                <a class="dropdown-item" href="/blog/${item.menuName}/${item.id}">
                    <div class="row">
                    <div class="col text-truncate" title="${item.title}">${item.title}</div>
                    <div class="col text-truncate" title="${item.subTitle}">${item.subTitle}</div>
                    <div class="arrow-right-icon col align-self-end text-end">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-box-arrow-right" viewBox="0 0 20 20">
                              <path fill-rule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0z"/>
                              <path fill-rule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708z"/>
                        </svg>
                    </div>
                    </div>
                </a>
            </li>`
}
