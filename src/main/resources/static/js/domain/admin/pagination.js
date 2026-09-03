const articlesPageNumberTemplate = (pageNumber, maxPageNumber, totalPages) => {
    const [startPage, endPage] = getPageRange(pageNumber, maxPageNumber, totalPages);
    const previousNumber = startPage > 0 ? startPage : 0;
    const endNumber = endPage < totalPages ? endPage : totalPages;
    console.log(startPage + "  " + endNumber)
    const previousPageString = `
            <li class="page-item">
                <a class="page-link" onclick="loadArticles(${previousNumber})" aria-label="Previous">
                    <span aria-hidden="true">«</span>
                </a>
            </li>
    `
    const nextPageString = `
            <li class="page-item">
                <a class="page-link" onclick="loadArticles(${endNumber})" aria-label="Next">
                    <span aria-hidden="true">»</span>
                </a>
            </li>
    `
    let pageNumbersString = ``;
    for (let i = startPage; i <= endPage; i++) {
        pageNumbersString += `
                  <li class="page-item">
                    <a class="page-link ${i === pageNumber ? 'active' : ''}" onclick="loadArticles(${i})">${i}</a>
                  </li>
        `
    }
    return previousPageString + pageNumbersString + nextPageString
}

function getPageSummary(articles) {
    if (articles.totalCount == 0) {
        return '(0 total)'
    }
    return `1 – ${articles.totalPages} (${articles.totalCount} total)`;
}


function getPageRange(pageNumber, maxPageNumber, totalPages) {
    let startPage = pageNumber - (pageNumber % maxPageNumber) + 1
    let endPage = maxPageNumber - (pageNumber % maxPageNumber) + pageNumber

    if (endPage > totalPages) {
        endPage = totalPages
    }

    return [startPage, endPage]
}