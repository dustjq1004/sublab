let isRequesting = false;

document.addEventListener('DOMContentLoaded', function () {
    const menuName = getUrlMenuName();
    let pageNumber = 0;

    // 페이지 로드 시 첫 번째 페이지의 글 목록 가져오기
    isRequesting = true;
    getArticlesPagination(pageNumber++, menuName);

    // event handler
    $(window).scroll(function () {
        let scrollPosition = $(window).scrollTop()
        let documentHeight = $(document).height()
        let windowHeight = $(window).height()
        if (scrollPosition + windowHeight >= documentHeight && !isRequesting) {
            // 스크롤이 페이지의 끝에 도달했을 때 추가적인 동작을 수행할 수 있습니다.
            isRequesting = true
            // setTimeout(() => isRequesting = false, 1000);
            getArticlesPagination(pageNumber++, menuName)
        }
    });

    // 검색 버튼 클릭
    $('#blog-search-btn').on('click', function () {
        $('#article-list').html("");
        getArticlesPagination(0, menuName)
    });

    // input에서 Enter 키 입력 시
    $('#blog-search-input').on('keydown', function (e) {
        if (e.key === 'Enter') {
            $('#article-list').html("");
            getArticlesPagination(0, menuName)
        }
    });
});

const getArticlesPagination = async (pageNumber, menuName) => {
    const data = {}

    if (pageNumber) data["pageNumber"] = pageNumber
    if (menuName) data["menuName"] = menuName
    data["searchKeyword"] = $('#blog-search-input').val()

    showSkeletonLoading('#article-list')

    const options = {
        method: 'GET'
    };

    const queryString = new URLSearchParams(data).toString()

    async function success(response) {
        const data = response.data;
        const articlesAppendHtml = articlesTemplate(data.articles, data.totalPages)
        $('#article-list').append(articlesAppendHtml)
        isRequesting = data.last
        $('.skeleton-card').remove()
    }

    setTimeout(async () => {
        await httpRequest( `/api/articles?${queryString}`, options, success)
        //await ajaxGetRequest('GET', '/api/articles', data, success, fail)
    }, 500)


    // hideSkeletonLoading();
}

