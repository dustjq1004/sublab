const MAX_LATEST_COUNT = 6;
const MAX_POPULAR_COUNT = 5;
const MAX_HERO_CATEGORY_COUNT = 6;

$(document).ready(() => {
    getLatestArticles();
    getCategoriesWithCount();
});

const getLatestArticles = async () => {
    const success = async (response) => {
        const latestArticles = (response?.data?.articles || []).slice(0, MAX_LATEST_COUNT);
        const featuredArticle = latestArticles[0];

        let latestArticlesHTML = '';
        for (const item of Object.entries(latestArticles)) {
            latestArticlesHTML += latestArticlesTemplate(item[1]);
        }
        $("#article-list").html(latestArticlesHTML);
        $("#hero-latest-post").html(heroLatestArticleTemplate(featuredArticle));
        $("#latest-count").text(latestArticles.length);
        renderPopularArticles(latestArticles);
    }

    const options = {
        method: "GET"
    }
    httpRequest('/api/main/articles/latest', options, success)
}

const getCategoriesWithCount = async () => {
    const success = async (response) => {
        const menus = response?.data?.menus || [];
        const categoryMap = new Map();

        // 메뉴별 articleCount를 카테고리 단위로 합산
        for (const menu of menus) {
            const categoryName = menu.name || "기타";
            const articleCount = menu.articleCount || 0;
            const count = categoryMap.get(categoryName) || 0;
            categoryMap.set(categoryName, count + articleCount);
        }

        let categoryHTML = "";
        for (const [menuName, articleCount] of categoryMap.entries()) {
            categoryHTML += categoryCardTemplate(menuName, articleCount);
        }

        if (!categoryHTML) {
            categoryHTML = `
                <div class="col">
                    <p class="text-secondary mb-0">표시할 카테고리가 없습니다.</p>
                </div>
            `;
        }

        $("#category-list").html(categoryHTML);
        renderHeroCategoryChips(categoryMap);
        $("#category-count").text(categoryMap.size);
    };

    const options = {
        method: "GET"
    };

    await httpRequest('/api/menus', options, success);
};

const renderPopularArticles = (articles) => {
    const popularArticles = (articles || []).slice(0, MAX_POPULAR_COUNT);
    let popularArticlesHTML = "";
    let rank = 1;

    for (const article of popularArticles) {
        popularArticlesHTML += popularArticlesTemplate({
            ...article,
            rank
        });
        rank += 1;
    }

    if (!popularArticlesHTML) {
        $("#popular-list").html('<p class="text-secondary mb-0">표시할 인기글이 없습니다.</p>');
        return;
    }

    $("#popular-list").html(popularArticlesHTML);
};

const renderHeroCategoryChips = (categoryMap) => {
    const categoryNames = Array.from(categoryMap.keys()).slice(0, MAX_HERO_CATEGORY_COUNT);

    if (categoryNames.length === 0) {
        $("#hero-category-chips").html('<span class="hero-topic-chip muted">카테고리를 준비 중입니다.</span>');
        return;
    }

    let heroCategoryHtml = "";
    for (const menuName of categoryNames) {
        heroCategoryHtml += heroCategoryChipTemplate(menuName);
    }

    $("#hero-category-chips").html(heroCategoryHtml);
};
