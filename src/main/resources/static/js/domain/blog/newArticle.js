// 수정 기능
const modifyButton = document.getElementById('modify-btn');
const getSelectedVisibility = () => document.querySelector('input[name="visibility"]:checked')?.value || 'PRIVATE';

if (modifyButton) {
    modifyButton.addEventListener('click', async event => {
        if (!isValidated()) return
        if (!await confirm("수정 하시겠습니까?")) {
            return
        }

        let params = new URLSearchParams(location.search);
        let id = params.get('id');
        const content = easyMDE.value();
        const selectElement = document.getElementById('menu');
        const selectedMenu = selectElement.options[selectElement.selectedIndex];
        const menuName = selectedMenu.text;
        const thumbnailUrl = document.getElementById('selected-thumbnail-hidden')?.value || '';
        const logicalFileName = document.getElementById('selected-thumbnail-name-hidden')?.value || '';

        const body = JSON.stringify({
            title: document.getElementById('title').value,
            subTitle: document.getElementById('subTitle').value,
            content: content,
            menuId: selectElement.value,
            visibility: getSelectedVisibility(),
            thumbnailLogicalFileName: logicalFileName || null,
            thumbnailUrl: thumbnailUrl || null
        });

        async function success() {
            await alert('수정 완료되었습니다.');
            $(window).unbind('beforeunload');
            location.replace(`/blog/${menuName}/${id}`);
        }

        const options = {
            method: 'PUT',
            body: body
        };

        httpRequest(`/api/articles/${id}`, options, success);
    });
}

// 생성 기능
const createButton = document.getElementById('create-btn');

if (createButton) {
    // 등록 버튼을 클릭하면 /api/articles로 요청을 보낸다
    createButton.addEventListener('click', async event => {
        if (!isValidated()) return
        if (!(await confirm("저장 하시겠습니까?"))) return

        const content = easyMDE.value();
        const thumbnailUrl = document.getElementById('selected-thumbnail-hidden')?.value || '';
        const logicalFileName = document.getElementById('selected-thumbnail-name-hidden')?.value || '';
        const body = JSON.stringify({
            title: document.getElementById('title').value,
            subTitle: document.getElementById('subTitle').value,
            content: content,
            menuId: document.getElementById('menu').value,
            draftId: document.getElementById('draft-id').value,
            visibility: getSelectedVisibility(),
            thumbnailLogicalFileName: logicalFileName || null,
            thumbnailUrl: thumbnailUrl || null
        });

        async function success() {
            await alert('등록 완료되었습니다.');
            $(window).unbind('beforeunload');
            location.replace('/blog');
        }

        const options = {
            method: 'POST',
            body: body
        };

        httpRequest('/api/articles', options, success)
    });
}

const draftButton = document.getElementById('draft-btn');
let isDraftRequestPending = false;

if (draftButton) {
    draftButton.addEventListener('click', async event => {
        if (!isValidated()) return
        if (isDraftRequestPending) {
            await alert('임시저장 요청을 처리 중입니다.', {type: 'info'});
            return;
        }
        if (!(await confirm("임시저장 하시겠습니까?"))) return
        const draftId = document.getElementById('draft-id').value;
        const content = easyMDE.value();
        const thumbnailUrl = document.getElementById('selected-thumbnail-hidden')?.value || '';
        const logicalFileName = document.getElementById('selected-thumbnail-name-hidden')?.value || '';
        const data = {
            title: document.getElementById('title').value,
            subTitle: document.getElementById('subTitle').value,
            content: content,
            menuId: document.getElementById('menu').value,
            thumbnailUrl: thumbnailUrl || null,
            thumbnailLogicalFileName: logicalFileName || null
        };


        // draft id가 있다면 update
        if (draftId) {
            await updateDraft(data, draftId);
        } else {
            await createDraft(data);
        }
    });
}

async function createDraft(data) {
    const options = {
        method: 'POST',
        body: JSON.stringify(data)
    };

    async function success(response) {
        closeDraftLoadingAlert();
        await alert('게시글이 임시로 저장되었습니다.');
        $('#draft-id').val(response.data.id);
        $('#draft-version').val(response.data.version);
    };

    await submitDraftRequest('/api/draft', options, success);
}

async function updateDraft(data, draftId) {
    data.id = draftId;
    data.version = document.getElementById('draft-version').value;
    const options = {
        method: 'PUT',
        body: JSON.stringify(data)
    };

    async function success(response) {
        closeDraftLoadingAlert();
        await alert('게시글이 임시로 저장되었습니다.');
        $('#draft-version').val(response.data.version);
        isDraftSaved = true;
    };

    await submitDraftRequest('/api/draft', options, success);
}

async function submitDraftRequest(url, options, success) {
    if (isDraftRequestPending) {
        await alert('임시저장 요청을 처리 중입니다.', {type: 'info'});
        return;
    }

    isDraftRequestPending = true;
    showDraftLoadingAlert();

    async function fail(errorData) {
        closeDraftLoadingAlert();
        if (errorData.code === 'E409') {
            await alert(errorData.message, {type: 'warning'});
            await getDrafts();
            return true;
        }
        return false;
    }

    try {
        await httpRequest(url, options, success, fail);
    } finally {
        closeDraftLoadingAlert();
        isDraftRequestPending = false;
    }
}

function showDraftLoadingAlert() {
    closeDraftLoadingAlert();

    const overlay = document.createElement('div');
    overlay.id = 'draft-loading-alert-overlay';
    overlay.className = 'custom-alert-overlay show';

    const alertBox = document.createElement('div');
    alertBox.className = 'custom-alert-box custom-alert-info show';
    alertBox.innerHTML = `
        <div class="custom-alert-header">
            <div class="custom-alert-icon"><i class="bi bi-hourglass-split"></i></div>
        </div>
        <div class="custom-alert-body">
            <h3 class="custom-alert-title">임시저장 중</h3>
            <p class="custom-alert-message">게시글을 임시저장하고 있습니다.</p>
        </div>
    `;

    overlay.appendChild(alertBox);
    document.body.appendChild(overlay);
}

function closeDraftLoadingAlert() {
    const loadingAlert = document.getElementById('draft-loading-alert-overlay');
    if (loadingAlert) {
        loadingAlert.remove();
    }
}

function isValidated() {
    const forms = document.querySelectorAll('.needs-validation')
    for (let i = 0; i < forms.length; i++) {
        if (!forms[i].checkValidity()) {
            forms[i].classList.add('was-validated')
            return false
        }
    }
    return true
}

async function alertValidation(json) {
    for (const error of json.errors) {
        await alert(`유효성 검증에 실패하였습니다. ${error.field} : ${error.defaultMessage}`)
    }
}

let draftList = {};

function setDraftList(json) {
    let html = "";
    let index = 0;
    if (json.length <= 0) return html = `<li class="draft-item"><div class="draft-no-item-box"><span class="dropdown-no-item-text">임시로 저장된 게시글이 없습니다.</span></div></li>`

    for (let draft of json) {
        const safeTitle = escapeHtml(draft.title);
        const safeDraftId = Number(draft.id);
        html +=
            `
        <li class="draft-item">
            <a class="" onclick="loadDraft(${index++})">
                <div class="draft-item-container">
                    <div class="draft-item-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-file-earmark-text" viewBox="0 0 16 16">
                          <path d="M5.5 7a.5.5 0 0 0 0 1h5a.5.5 0 0 0 0-1zM5 9.5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5m0 2a.5.5 0 0 1 .5-.5h2a.5.5 0 0 1 0 1h-2a.5.5 0 0 1-.5-.5"/>
                          <path d="M9.5 0H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V4.5zm0 1v2A1.5 1.5 0 0 0 11 4.5h2V14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1z"/>
                        </svg>
                    </div>
                    <div class="draft-item-wrapper">
                        <span class="draft-item-title" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-custom-class="custom-tooltip" title="${safeTitle}">
                            ${safeTitle}
                        </span>
                    </div>
                    <div class="draft-item-action fs-3">
                        <!-- 업로드 아이콘 -->
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="50" fill="currentColor" class="bi bi-upload" viewBox="0 0 16 16">
                            <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                            <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z"/>
                        </svg>
                        <!-- 삭제 버튼(아이콘) 추가 -->
                        <button type="button" class="btn ms-2 draft-delete-btn" onclick="deleteDraft(${safeDraftId}, event)">
                            <i class="bi bi-trash3"></i>
                        </button>
                    </div>
                </div>
            </a>
        </li>
        `
    }
    return html;
}

async function loadDraft(index, event) {
    const draft = draftList[index];
    if (!(await confirm(`"${draft.title}" \n을 불러오시겠습니까?`))) {
        return false;
    }

    resetForm();

    $('#draft-id').val(draft.id);
    $('#draft-version').val(draft.version);
    $('#menu').val(draft.menuId);
    $('#title').val(draft.title);
    $('#subTitle').val(draft.subTitle);
    easyMDE.value(draft.content);
    $('#selected-thumbnail-hidden').val(draft.thumbnailUrl || '');
    $('#selected-thumbnail-name-hidden').val(draft.thumbnailLogicalFileName || '');
    $('#selected-thumbnail-name').text(draft.thumbnailLogicalFileName || '');
    bootstrap.Modal.getInstance($('#draftModal')).hide()
}

async function deleteDraft(draftId, event) {
    event.stopPropagation(); // 부모 a 태그 클릭 방지
    if (!(await confirm('이 임시글을 삭제하시겠습니까?'))) return;


    const options = {
        method: 'DELETE'
    };

    await httpRequest(`/api/draft/${draftId}`, options, () => {
        getDrafts();
    })

}

async function handleResetForm() {
    const result = await confirm('새 글을 작성 하시겠습니까?');
    if (result) {
        resetForm();
    }
}

function resetForm() {
    $('form').each(function () {
        this.reset();
    });
    $('#draft-id').val("");
    $('#draft-version').val("");
    easyMDE.value("");
}

async function getDrafts() {
    const options = {
        method: 'GET'
    };

    const success = async (json) => {
        draftList = json.data;
        html = setDraftList(draftList);

        $('#draft-list').html(html);
        isDraftSaved = false;

        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
        const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
    }

    await httpRequest('/api/draft', options, success)
}

$(document).ready(function () {
    $("#draft-button").click(async function () {
        await getDrafts();
    })

    $("form :input").on("input", function () {
        isDraftSaved = true;
    });

    $(window).bind('beforeunload', function () {
        if (isDraftSaved)
            return '페이지를 나가시겠습니까? 기존에 작성한 글은 복구할 수 없습니다.';
    });
});
