// 널 체크 함수
// 널 || 길이가 0 || 'null'
function isNull(checkValue) {
    if (checkValue == null || checkValue === 'undefined') {
        return true;
    }

    if (typeof checkValue === "string") {
        return checkValue.trim().length === 0 || checkValue === 'null'
    }

    return false;
}

function nvl(value) {
    return value ?? "";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        const context = this;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), wait);
    };
}

function getUrlMenuName() {
    let currentUrl = window.location.href;
    let decodedUrl = decodeURIComponent(currentUrl);
    return decodedUrl.split("/")[4];
}

// HTTP 요청을 보내는 함수
async function httpRequest(url, options, success, fail) {
    if (isNull(options.headers)) {
        options.headers = {}
    }

    options.headers['Authorization'] = 'Bearer ' + localStorage.getItem('access_token')
    if (isNull(options.headers['Content-Type'])) {
        options.headers['Content-Type'] = 'application/json'
    }
    addCsrfHeader(options)

    const response = await fetch(url, options)

    if (response.status === 200 || response.status === 201) {
        return handleCommonSuccess(response, success)
    }

    const refresh_token = getCookie('refresh_token')
    if (response.status === 401 && refresh_token) {
        const result = await requestAccessToken(getCookie('refresh_token'))
        if (result) {
            return await httpRequest(url, options, success, fail);
        }
    }
    await handleCommonFail(response, fail);
    return false
}

// 공통 success 처리 함수
async function handleCommonSuccess(response, successCallback) {
    const contentType = response.headers.get('Content-Type');
    if (contentType && contentType.includes("text/html")) {
        const html = await response.text();
        return await successCallback(html);
    }

    const data = await response.json();
    return await successCallback(data);
}

// 공통 fail 처리 함수
async function handleCommonFail(response, failCallback) {
    try {
        const errorData = await response.json();
        let handledByCallback = false;

        if (failCallback && typeof failCallback === 'function') {
            handledByCallback = await failCallback(errorData) === true;
        }

        if (handledByCallback) {
            return;
        }

        if (response.status === 401) {
            await alert("로그인 세션이 만료되었습니다. 다시 로그인 해주세요.");
            window.location.href = '/login';
            return;
        }

        // validation 요류인 경우
        if (response.status === 400) {
            if (errorData.errors && errorData.message === "Validation failed") {
                const errorMessages = errorData.errors.map(err =>
                    `${err.field}: ${err.defaultMessage || err.message || ''}`
                ).join('\n');
                await alert(`유효성 검증에 실패하였습니다:\n ${errorMessages}`);
            }
        }

        if (response.status === 400 && errorData.errors && errorData.message === "Validation failed") {
            return;
        }

        const statusText = response.statusText || '알 수 없는 오류';
        console.error(`요청 처리 중 오류가 발생했습니다. (${response.status}: ${statusText})`);

        // API 응답 형식에 따라 에러 메시지 추출
        if (errorData.message) {
            console.error(`오류가 발생했습니다: (${errorData.code} : ${errorData.message})`);
        }

        const message = errorData.message || `요청 처리 중 오류가 발생했습니다. (${response.status}: ${statusText})`;
        await alert(message, {type: 'error'});
    } catch (e) {
        // JSON 파싱 실패 시 기본 메시지
        const statusText = response.statusText || '알 수 없는 오류';
        console.error(`요청 처리 중 오류가 발생했습니다. (${response.status}: ${statusText})`);
        await alert(`요청 처리 중 오류가 발생했습니다. (${response.status}: ${statusText})`, {type: 'error'});
    }
}

// refresh token 요청
async function requestAccessToken(refresh_token) {
    const options = {
        method: 'POST',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            refreshToken: refresh_token,
        }),
    };
    addCsrfHeader(options)
    const response = await fetch('/api/token', options);

    if (response.ok) {
        const responseData = await response.json();
        localStorage.setItem('access_token', responseData.data.accessToken);
        return response.ok;
    }
}

function addCsrfHeader(options) {
    const method = (options.method || 'GET').toUpperCase();
    if (['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
        return;
    }

    const csrfToken = getCookie('XSRF-TOKEN');
    if (csrfToken) {
        options.headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken);
    }
}

// 쿠키를 가져오는 함수
function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(';');
    cookie.some(function (item) {
        item = item.replace(' ', '');

        var dic = item.split('=');

        if (key === dic[0]) {
            result = dic[1];
            return true;
        }
    });

    return result;
}

// FormData Json 변환
$.fn.serializeFormToJSON = function () {
    const json = {};

    // Select 요소의 선택된 값만 처리
    this.find('select').each(function () {
        const name = $(this).attr('name');
        const selectedValue = $(this).find(':selected').val() || '';
        if (name) {
            json[name] = selectedValue;
        }
    });

    // 기타 입력 요소 처리
    const formData = this.serializeArray();
    $.each(formData, function () {
        if (!json.hasOwnProperty(this.name)) {
            // Select에서 처리되지 않은 다른 요소만 추가
            json[this.name] = this.value || '';
        }
    });

    return json;
}

$(document).on('show.bs.modal shown.bs.modal', function () {
    $('body').css('padding-right', '');

    // body scrollbar 제거
    $('html').css('overflow', 'hidden');
});

$(document).on('hidden.bs.modal', function () {
    $('html').css('overflow', '');
});

// DOM 로딩 완료 후 애니메이션 적용
document.addEventListener('DOMContentLoaded', function () {
    // body에 클래스 추가 (CSS에서 body에 직접 적용했다면 불필요)
    //document.body.classList.add('page-fade-in');

    // 또는 특정 요소들에 순차적으로 적용
    const fadeElements = document.querySelectorAll('.fade-in-element');
    fadeElements.forEach((element, index) => {
        setTimeout(() => {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100); // 각 요소마다 100ms 지연
    });

    // Back to Top button logic
    const backToTopBtn = document.getElementById('backToTop');
    if (backToTopBtn) {
        const toggleBackToTop = () => {
            const y = window.scrollY || document.documentElement.scrollTop;
            if (y > 300) {
                backToTopBtn.classList.add('show');
            } else {
                backToTopBtn.classList.remove('show');
            }
        };

        toggleBackToTop();
        window.addEventListener('scroll', debounce(toggleBackToTop, 50));

        backToTopBtn.addEventListener('click', function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // blog sidebar toggle
    const blogSidebarToggleButton = document.getElementById('blogSidebarToggleButton');
    const blogSidebarContainer = document.querySelector('.content > .offcanvas-body .bd-links-nav')
        ? document.querySelector('.content > .offcanvas-body')
        : null;

    if (blogSidebarToggleButton && blogSidebarContainer) {
        document.body.classList.add('blog-sidebar-page');
        blogSidebarToggleButton.classList.remove('d-none');

        const syncSidebarToggleUI = () => {
            const expanded = document.body.classList.contains('blog-sidebar-open');
            blogSidebarToggleButton.setAttribute('aria-expanded', String(expanded));
            const icon = blogSidebarToggleButton.querySelector('i');
            if (icon) {
                icon.className = expanded ? 'bi bi-x-lg' : 'bi bi-list';
            }
        };

        const closeSidebar = () => {
            document.body.classList.remove('blog-sidebar-open');
            syncSidebarToggleUI();
        };

        blogSidebarToggleButton.addEventListener('click', function (event) {
            event.preventDefault();
            document.body.classList.toggle('blog-sidebar-open');
            syncSidebarToggleUI();
        });

        document.addEventListener('click', function (event) {
            if (!document.body.classList.contains('blog-sidebar-open')) {
                return;
            }
            if (blogSidebarContainer.contains(event.target) || blogSidebarToggleButton.contains(event.target)) {
                return;
            }
            closeSidebar();
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeSidebar();
            }
        });

        syncSidebarToggleUI();
    }
});

/**
 * 커스텀 Alert 함수 (기본 alert 대체)
 * @param {string} message - 표시할 메시지
 * @param {Object} [options] - 옵션 객체 (생략 시 기본 alert와 동일하게 동작)
 * @param {string|HTMLElement} options.focusTarget - alert 후 포커스를 이동할 요소 (CSS selector 또는 HTMLElement)
 * @param {Function} options.onConfirm - 확인 버튼 클릭 시 실행할 콜백 함수
 * @param {string} options.title - Alert 제목 (기본값: '알림')
 * @param {string} options.confirmText - 확인 버튼 텍스트 (기본값: '확인')
 * @param {string} options.type - Alert 타입: 'info', 'success', 'warning', 'error' (기본값: 'info')
 */
function alert(message, options = {}) {
    // 기본 옵션 설정
    const {
        focusTarget = null,
        onConfirm = null,
        title = '알림',
        confirmText = '확인',
        type = 'info'
    } = options;

    // Promise 반환 (이 부분 추가)
    return new Promise((resolve) => {
        // 기존 alert가 있으면 제거
        const existingAlert = document.getElementById('custom-alert-overlay');
        if (existingAlert) {
            existingAlert.remove();
        }

        // Overlay 생성
        const overlay = document.createElement('div');
        overlay.id = 'custom-alert-overlay';
        overlay.className = 'custom-alert-overlay';

        // Alert 박스 생성
        const alertBox = document.createElement('div');
        alertBox.className = `custom-alert-box custom-alert-${type}`;

        // 아이콘 설정
        const iconMap = {
            info: '<i class="bi bi-info-circle"></i>',
            success: '<i class="bi bi-check-circle"></i>',
            warning: '<i class="bi bi-exclamation-triangle"></i>',
            error: '<i class="bi bi-x-circle"></i>'
        };

        const safeTitle = escapeHtml(title);
        const safeMessage = escapeHtml(message);
        const safeConfirmText = escapeHtml(confirmText);

        // Alert 내용 구성
        alertBox.innerHTML = `
            <div class="custom-alert-header">
                <div class="custom-alert-icon">${iconMap[type] || iconMap.info}</div>
            </div>
            <div class="custom-alert-body">
                <h3 class="custom-alert-title">${safeTitle}</h3>
                <p class="custom-alert-message">${safeMessage}</p>
            </div>
            <div class="custom-alert-footer">
                <button class="custom-alert-btn custom-alert-btn-confirm" type="button">${safeConfirmText}</button>
            </div>
        `;

        overlay.appendChild(alertBox);
        document.body.appendChild(overlay);

        // 애니메이션을 위한 약간의 지연
        setTimeout(() => {
            overlay.classList.add('show');
            alertBox.classList.add('show');
        }, 10);

        // 확인 버튼 이벤트
        const confirmBtn = alertBox.querySelector('.custom-alert-btn-confirm');
        let isClosing = false;
        const closeAlert = (e) => {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }

            // 이미 닫히는 중이면 무시
            if (isClosing) return;
            isClosing = true;

            overlay.classList.remove('show');
            alertBox.classList.remove('show');
            setTimeout(() => {
                overlay.remove();

                // 포커스 이동 처리
                if (focusTarget) {
                    let targetElement = null;
                    if (typeof focusTarget === 'string') {
                        targetElement = document.querySelector(focusTarget);
                    } else if (focusTarget instanceof HTMLElement) {
                        targetElement = focusTarget;
                    }

                    if (targetElement) {
                        // 입력 요소인 경우 포커스
                        if (targetElement.tagName === 'INPUT' ||
                            targetElement.tagName === 'TEXTAREA' ||
                            targetElement.tagName === 'SELECT' ||
                            targetElement.isContentEditable) {
                            targetElement.focus();
                            // 입력 요소인 경우 선택
                            if (targetElement.select) {
                                targetElement.select();
                            }
                        } else {
                            // 다른 요소인 경우 클릭 가능하면 포커스
                            if (targetElement.focus) {
                                targetElement.focus();
                            }
                        }
                    }
                }

                // 콜백 함수 실행
                if (onConfirm && typeof onConfirm === 'function') {
                    onConfirm();
                }

                // Promise resolve 추가 (이 부분 추가)
                resolve();
            }, 300); // 애니메이션 시간과 맞춤
        };

        confirmBtn.addEventListener('click', (e) => {
            if (isClosing) {
                e.preventDefault();
                e.stopPropagation();
                return;
            }
            closeAlert(e);
        });

        // ESC 키로 닫기
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                e.preventDefault();
                e.stopPropagation();
                closeAlert(e);
                document.removeEventListener('keydown', handleEscape);
            }
        };
        document.addEventListener('keydown', handleEscape);
    }); // Promise 반환 종료 (이 부분 추가)
}

/**
 * 커스텀 Confirm 함수 (기본 confirm 대체)
 * @param {string} message - 표시할 메시지
 * @param {Object} [options] - 옵션 객체
 * @param {string} options.title - Confirm 제목 (기본값: '확인')
 * @param {string} options.confirmText - 확인 버튼 텍스트 (기본값: '확인')
 * @param {string} options.cancelText - 취소 버튼 텍스트 (기본값: '취소')
 * @param {string} options.type - Confirm 타입: 'info', 'warning', 'error' (기본값: 'warning')
 * @returns {Promise<boolean>} - 확인 시 true, 취소 시 false를 반환하는 Promise
 */
function confirm(message, options = {}) {
    // 기본 옵션 설정
    const {
        title = '확인',
        confirmText = '확인',
        cancelText = '취소',
        type = 'warning'
    } = options;

    // Promise 반환
    return new Promise((resolve) => {
        // 기존 confirm이 있으면 제거
        const existingConfirm = document.getElementById('custom-confirm-overlay');
        if (existingConfirm) {
            existingConfirm.remove();
        }

        // Overlay 생성
        const overlay = document.createElement('div');
        overlay.id = 'custom-confirm-overlay';
        overlay.className = 'custom-alert-overlay';

        // Confirm 박스 생성
        const confirmBox = document.createElement('div');
        confirmBox.className = `custom-alert-box custom-alert-${type}`;

        // 아이콘 설정
        const iconMap = {
            info: '<i class="bi bi-info-circle"></i>',
            warning: '<i class="bi bi-exclamation-triangle"></i>',
            error: '<i class="bi bi-x-circle"></i>'
        };

        const safeTitle = escapeHtml(title);
        const safeMessage = escapeHtml(message);
        const safeConfirmText = escapeHtml(confirmText);
        const safeCancelText = escapeHtml(cancelText);

        // Confirm 내용 구성
        confirmBox.innerHTML = `
            <div class="custom-alert-header">
                <div class="custom-alert-icon">${iconMap[type] || iconMap.warning}</div>
            </div>
            <div class="custom-alert-body">
                <h3 class="custom-alert-title">${safeTitle}</h3>
                <p class="custom-alert-message">${safeMessage}</p>
            </div>
            <div class="custom-alert-footer">
                <button class="custom-alert-btn custom-alert-btn-cancel" type="button">${safeCancelText}</button>
                <button class="custom-alert-btn custom-alert-btn-confirm" type="button">${safeConfirmText}</button>
            </div>
        `;

        overlay.appendChild(confirmBox);
        document.body.appendChild(overlay);

        // 애니메이션을 위한 약간의 지연
        setTimeout(() => {
            overlay.classList.add('show');
            confirmBox.classList.add('show');
        }, 10);

        // 닫기 함수
        let isClosing = false;
        const closeConfirm = (result) => {
            // 이미 닫히는 중이면 무시
            if (isClosing) return;
            isClosing = true;

            overlay.classList.remove('show');
            confirmBox.classList.remove('show');
            setTimeout(() => {
                overlay.remove();
                resolve(result);
            }, 300); // 애니메이션 시간과 맞춤
        };

        // 확인 버튼 이벤트
        const confirmBtn = confirmBox.querySelector('.custom-alert-btn-confirm');
        confirmBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            if (isClosing) return;
            closeConfirm(true);
        });

        // 취소 버튼 이벤트
        const cancelBtn = confirmBox.querySelector('.custom-alert-btn-cancel');
        cancelBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            if (isClosing) return;
            closeConfirm(false);
        });

        // ESC 키로 취소
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                e.preventDefault();
                e.stopPropagation();
                closeConfirm(false);
                document.removeEventListener('keydown', handleEscape);
            }
        };
        document.addEventListener('keydown', handleEscape);
    });
}

// 전역 window 객체에 alert, confirm 함수 오버라이드
window.alert = alert;
window.confirm = confirm;

// DOM 로드 후에도 확실하게 오버라이드 (스크립트 로드 순서 문제 대비)
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        window.alert = alert;
        window.confirm = confirm;
    });
}
