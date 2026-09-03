const categoriesTemplate = (categories) => {
    if (!categories || !categories.categories || categories.categories.length === 0) {
        return `
            <li class="list-group-item admin-empty-state">
                <strong>등록된 카테고리가 없습니다</strong>
                <small>카테고리를 추가하면 메뉴를 연결할 수 있습니다.</small>
            </li>
        `;
    }

    const template = categories.categories.map(category => `
        <li class="list-group-item admin-list-item" data-active="${category.id}">
            <div class="d-flex flex-row align-items-center">
                <a class="p-3 lh-sm w-100 text-decoration-none admin-list-item__link" 
                    onclick="loadMenusTemplate(${category.id})">
                    <div class="d-flex align-items-start justify-content-between gap-3">
                        <div class="admin-list-item__title">
                            <strong class="mb-1">${!category.emoji || category.emoji.length === 0 ? '' : category.emoji}${category.name}</strong>
                        </div>
                        <small class="admin-list-item__date">수정 ${category.updatedAt}</small>
                    </div>
                </a>
                <div class="btn-group admin-list-item__actions" role="group" aria-label="First group">
                    <button type="button" class="btn btn-light admin-list-item__action" data-bs-toggle="modal" data-bs-target="#categoryUpdateDetail" onclick="showCategoryUpdateModal(${category.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button type="button" class="btn btn-light admin-list-item__action" onclick="sendDeleteCategoryRequest(${category.id})">
                        <i class="bi bi-trash3-fill"></i>
                    </button>
                </div>
            </div>
        </li>
    `).join("");

    return template;
}

const menusTemplate = (menus) => {
    if (!menus) {
        return getMenusInitialState();
    }

    if (!menus.menus || menus.menus.length === 0) {
        return `
            <li class="list-group-item admin-empty-state">
                <strong>등록된 메뉴가 없습니다</strong>
                <small>카테고리를 선택하면 연결된 메뉴가 표시됩니다. 메뉴가 없다면 위 추가 버튼을 사용하세요.</small>
            </li>
        `;
    }

    const template = menus.menus.map(menu => `
        <li class="list-group-item list-group-item-action admin-list-item">
            <div class="d-flex flex-row align-items-center">
                <a onclick="loadMenuDetailTemplate(${menu.id})" aria-current="true" class="p-3 d-flex w-100 align-items-center
                 mx-2 text-decoration-none link-dark admin-list-item__link"
                    data-bs-toggle="modal" data-bs-target="#menuDetailModal">    
                    <div class="admin-list-item__title">
                        <strong class="mb-1">${menu.name}</strong>
                        <small>${menu.description || '메뉴 설명이 아직 없습니다.'}</small>
                    </div>
                    <small class="ms-auto admin-list-item__date">수정 ${menu.updatedAt}</small>
                </a>
                <button type="button" class="btn btn-light admin-list-item__action" onclick="deleteMenu(${menu.id})">
                    <i class="bi bi-trash3-fill"></i>
                </button>
            </div>
        </li>
    `).join("");

    return template;
}

const getMenusInitialState = () => `
    <li class="list-group-item admin-empty-state">
        <strong>메뉴 패널을 준비하고 있어요</strong>
        <small>왼쪽에서 카테고리를 선택하면 연결된 메뉴 목록이 여기에 표시됩니다.</small>
    </li>
`;

const menuDetailModalTemplate = (menu) => {
    const template = `
            <div class="modal-header admin-modal-card__header">
                <div>
                    <span class="admin-modal-card__eyebrow">Menu</span>
                    <h1 class="modal-title fs-5 admin-modal-card__title" id="exampleModalLabel">메뉴 상세 정보</h1>
                    <p class="admin-modal-card__description mb-0">현재 메뉴의 카테고리, 설명, 생성 및 수정 시점을 확인할 수 있습니다.</p>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body admin-modal-card__body">
                <div class="admin-detail-card">
                    <div class="admin-detail-card__row">
                        <span class="admin-detail-card__label">카테고리</span>
                        <span class="admin-detail-card__value">${menu.categoryEmoji} ${menu.categoryName}</span>
                    </div>
                    <div class="admin-detail-card__row">
                        <span class="admin-detail-card__label">메뉴 이름</span>
                        <span class="admin-detail-card__value">${menu.emoji} ${menu.name}</span>
                    </div>
                    <div class="admin-detail-card__row admin-detail-card__row--block">
                        <span class="admin-detail-card__label">설명</span>
                        <div class="admin-detail-card__value admin-detail-card__description-box">
                            ${menu.description || '-'}
                        </div>
                    </div>
                    <div class="admin-detail-card__meta-grid">
                        <div class="admin-detail-card__meta-item">
                            <span class="admin-detail-card__label">생성일</span>
                            <strong class="admin-detail-card__value">${menu.createdAt || '-'}</strong>
                        </div>
                        <div class="admin-detail-card__meta-item">
                            <span class="admin-detail-card__label">수정일</span>
                            <strong class="admin-detail-card__value">${menu.updatedAt || '-'}</strong>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer admin-modal-card__footer">
                <button type="button" class="btn btn-light admin-secondary-button" data-bs-dismiss="modal">닫기</button>
                <button id="menuUpdateBtn" type="button" class="btn btn-primary admin-primary-button">수정</button>
            </div>
    `
    return template
}

const menuDetailUpdateTemplate = (menu, categories) => {
    const selectOptions = categories.categories.map(category => `
        <option value="${category.id}" ${menu.categoryId == category.id ? "selected" : ""}>${category.name}</option>
    `)
    const categoriesSelectTemplate = `
        <select class="form-select" name="categoryId" aria-label="Default select categories">
          ${selectOptions}
        </select>
    `

    const template = `
        <div class="modal-header admin-modal-card__header">
            <div>
                <span class="admin-modal-card__eyebrow">Menu</span>
                <h1 class="modal-title fs-5 admin-modal-card__title" id="menuUpdateLabel">메뉴 수정</h1>
                <p class="admin-modal-card__description mb-0">메뉴 이름, 설명, 연결 카테고리를 한 번에 수정합니다.</p>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div id="menuUpdateContent" class="modal-body admin-modal-card__body">
            <form id="menuUpdateForm">
                <input name="id" type="hidden" value="${menu.id}"/>
                <input name="categoryId" type="hidden" value="${menu.categoryId}"/>
                <div class="admin-form-field mb-3">
                    <label for="category-name" class="col-form-label admin-form-field__label">카테고리</label>
                    ${categoriesSelectTemplate.replace('class="form-select"', 'class="form-select admin-form-field__control"')}
                    <div class="admin-form-field__help">메뉴가 표시될 상위 카테고리를 선택합니다.</div>
                </div>
                <div class="admin-form-field mb-3">
                    <label for="menu-name" class="col-form-label admin-form-field__label">메뉴 이름</label>
                    <input name="name" type="text" class="form-control admin-form-field__control" id="menu-name" value="${menu.name}"/>
                </div>
                <div class="admin-form-field">
                    <label for="menu-description" class="col-form-label admin-form-field__label">설명</label>
                    <input name="description" type="text" class="form-control admin-form-field__control" id="menu-description" value="${menu.description}"/>
                    <div class="admin-form-field__help">목록과 탐색 맥락에서 함께 보여질 설명입니다.</div>
                </div>
            </form>

        </div>
        <div class="modal-footer admin-modal-card__footer">
            <button type="button" class="btn btn-light admin-secondary-button" data-bs-dismiss="modal">닫기</button>
            <button onclick="updateMenu()" type="button" class="btn btn-primary admin-primary-button">수정</button>
        </div>
    `
    return template;
}


const categoryDetailUpdateTemplate = async (category) => {
    const template = `
        <div class="modal-header admin-modal-card__header">
            <div>
                <span class="admin-modal-card__eyebrow">Category</span>
                <h1 class="modal-title fs-5 admin-modal-card__title" id="menuUpdateLabel">카테고리 수정</h1>
                <p class="admin-modal-card__description mb-0">선택한 카테고리의 이름을 바로 수정합니다.</p>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div id="categoryUpdateContent" class="modal-body admin-modal-card__body">
            <form id="categoryModifyForm">
                <input name="id" type="hidden" value="${category.id}"/>
                <div class="admin-form-field">
                    <label for="category-name" class="col-form-label admin-form-field__label">카테고리 이름</label>
                    <input name="name" type="text" class="form-control admin-form-field__control" id="category-name" value="${category.name}"/>
                    <div class="admin-form-field__help">기존 게시글 분류에 연결된 이름도 함께 반영됩니다.</div>
                </div>
            </form>
        </div>
        <div class="modal-footer admin-modal-card__footer">
            <button type="button" class="btn btn-light admin-secondary-button" data-bs-dismiss="modal">닫기</button>
            <button onclick="sendModifyCategoryRequest()" type="button" class="btn btn-primary admin-primary-button">수정</button>
        </div>
    `
    return template;
}

const getVisibilityBadgeLabel = (visibility) => visibility === 'PUBLIC' ? '공개' : '비공개';

const getVisibilityBadgeClass = (visibility) => visibility === 'PUBLIC'
        ? 'text-bg-success'
        : 'text-bg-secondary';

const articleVisibilityBadgeTemplate = (article) => {
    const currentVisibility = article.visibility || 'PRIVATE';
    return `
        <span class="badge rounded-pill ${getVisibilityBadgeClass(currentVisibility)} article-visibility-badge"
              role="button"
              tabindex="0"
              data-article-id="${article.id}"
              data-visibility="${currentVisibility}">
            ${getVisibilityBadgeLabel(currentVisibility)}
        </span>
    `;
};

const articlesTemplate = (articles) => {
    if (!articles || articles.length === 0) {
        return `
            <tr>
                <td colspan="8" class="text-center admin-table-empty">조회된 데이터가 없습니다</td>
            </tr>
        `;
    }

    const template = articles.map(article =>
        `
            <tr>
                <td style="width: 0px">
                    <div class="form-check d-none article-checkbox-container">
                        <input class="form-check-input article-checkbox" type="checkbox" 
                               data-article-id="${article.id}" id="tableCheckBox">
                        <label class="form-check-label" for="tableCheckBox"></label>
                    </div>
                </td>
                <td>
                    ${articleVisibilityBadgeTemplate(article)}
                </td>
                <td>
                    <span class="admin-menu-chip">${article.menuName}</span>
                </td>
                <td class="admin-title-column">
                    <div class="admin-article-title-cell">
                        <div class="admin-article-title text-truncate w-100" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-title="${article.title}">
                            ${article.title}
                        </div>
                        <small class="admin-article-meta">게시글 ID #${article.id}</small>
                    </div>
                </td>
                <td class="admin-subtitle-column">
                    <div class="text-truncate admin-article-subtitle" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-title="${article.subTitle}">
                        <div class="fs-sm text-body-secondary">
                            ${article.subTitle}
                        </div>
                    </div>
                </td>
                <td>
                    <span class="admin-author-chip">${article.author}</span>
                </td>
                <td>
                    <div class="admin-date-cell">
                        <span>등록</span>
                        <strong>${article.createdAt}</strong>
                    </div>
                </td>
                <td>
                    <div class="admin-date-cell">
                        <span>수정</span>
                        <strong>${article.updatedAt}</strong>
                    </div>
                </td>
            </tr>
        `
    ).join("");
    return template;
}
