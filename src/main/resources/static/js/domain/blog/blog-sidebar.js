document.addEventListener('DOMContentLoaded', function () {
    initSideBarMenuActive(decodeURIComponent(window.location.href));
});

function initSideBarMenuActive(decodedUrl) {
    // 현재 페이지 URL
    let menuItems = document.querySelectorAll('.side-link a');

    // 각 메뉴 항목에 대해 현재 페이지와 비교하여 active 클래스 추가
    menuItems.forEach(function (menuItem) {
        let menuName = menuItem.getAttribute('data-menu-name');
        if (decodedUrl.includes(menuName)) {
            menuItem.classList.add('active');
        }
    })
}