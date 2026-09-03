let currentCategoryId = "";
let currentPageNumber = 1;

// 편집 모드 상태 관리
let isEditMode = false;

/* 카테고리 Function */
const loadCategoriesTemplate = async (element) => {
    const fragment = await getCategoriesFragment(element)
    $('#admin-content').html(fragment)

    await setCategoriesTemplate();

    $('#category-add-id').val("")
    $('#category-add-name').val("")
}

const setCategoriesTemplate = async () => {
    const categories = await getAllCategory()
    const template = await categoriesTemplate(categories)
    $('#categories-content').html(template)
}

const showCategoryUpdateModal = async (categoryId) => {
    const category = await getCategoryById(categoryId);
    const template = await categoryDetailUpdateTemplate(category)
    $('#category-update-modal').html(template)
}

const saveAndRenderCategory = async () => {
    const categoryFormData = $('#categoryAddForm').serializeFormToJSON()
    const isSuccess = await createCategory(categoryFormData);
    if (!isSuccess) {
        return;
    }
    await setCategoriesTemplate()

    bootstrap.Modal.getInstance($('#categoryAddDetail')).hide()
    $('.modal-backdrop').remove()

    $("#categoryAddForm")[0].reset()
}

const sendModifyCategoryRequest = async () => {
    const categoryFormData = $('#categoryModifyForm').serializeFormToJSON()
    await putCategoryUpdate(categoryFormData)
    const categories = await getAllCategory()

    const template = categoriesTemplate(categories)
    $('#categories-content').html(template)

    $('.modal-backdrop').remove();
    bootstrap.Modal.getInstance($('#categoryUpdateDetail')).hide()
}

const sendDeleteCategoryRequest = async (categoryId) => {
    if (!(await confirm("카테고리를 삭제하시겠습니까?"))) {
        return
    }

    await deleteCategory(categoryId)

    const categories = await getAllCategory()
    const template = await categoriesTemplate(categories)
    $('#categories-content').html(template)
}

/* 카테고리 Function */
const loadMenusTemplate = async (categoryId) => {
    const menus = await getMenus(categoryId)

    const template = await menusTemplate(menus)
    $('#menus-content').html(template)

    const category = await getCategoryById(categoryId);
    $('#category-add-id').val(categoryId)
    $('#category-add-name').val(category.emoji + category.name);
}

const loadMenuDetailTemplate = async (menuId) => {
    const menu = await getMenuDetailById(menuId)
    const template = menuDetailModalTemplate(menu)
    $('#menu-detail-modal').html(template)
    initializeUpdateModalEvent(menu)
}

const updateMenu = async () => {
    const menuFormData = $('#menuUpdateForm').serializeFormToJSON()
    await putMenuUpdate(menuFormData)
    await loadMenusTemplate(currentCategoryId)

    bootstrap.Modal.getInstance($('#menuUpdateDetail')).hide()
}

const deleteMenu = async (menuId) => {
    if (!(await confirm("메뉴를 삭제하시겠습니까?"))) {
        return
    }

    await deleteMenuRemove(menuId)
}

const initializeUpdateModalEvent = (menu) => {
    document.getElementById('menuUpdateBtn').addEventListener('click', async function () {
        const categories = await getAllCategory()
        const template = menuDetailUpdateTemplate(menu, categories);

        $('#menu-update-modal').html(template);

        // 첫 번째 모달 닫기
        const firstModal = bootstrap.Modal.getInstance($('#menuDetailModal'))
        firstModal.hide()

        // backdrop 제거
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open'); // body의 'modal-open' 클래스 제거
        $('body').css('padding-right', ''); // 추가된 padding-right 제거

        // 두 번째 모달 열기
        const secondModal = new bootstrap.Modal($('#menuUpdateDetail'))
        secondModal.show()
    })
}

const showMenuAddModal = async () => {
    const categoryId = $('#category-add-id').val()
    if (!categoryId) {
        await alert("카테고리를 선택해주세요.")
        return false
    }

    const modal = new bootstrap.Modal($('#menuAddDetail'))
    modal.show()
}

const sendSaveMenuRequest = async () => {
    const menuFormData = $('#menuAddForm').serializeFormToJSON()

    if (!(await verifyMenuInput(menuFormData))) {
        return false;
    }

    await postMenuAdd(menuFormData)
    bootstrap.Modal.getInstance($('#menuAddDetail')).hide()
    $('#menuAddForm')[0].reset()
}

async function verifyMenuInput(jsonFormData) {
    const categoryId = jsonFormData['categoryId']
    if (categoryId == null || categoryId == 'undefined') {
        await alert("카테고리를 선택해주세요.")
        return false;
    }

    return true;
}

/* Article Transaction */
const loadArticlesFragment = async (pageNumber) => {
    $('#admin-content').html("")
    const fragment = await getArticlesFragment()
    $('#admin-content').html(fragment)

    // 초기 카테고리 데이터 로드
    loadCategories();

    await loadArticles(pageNumber)
}

async function loadArticles(pageNumber, options = {}) {
    const { preserveEditMode = false } = options;
    currentPageNumber = pageNumber;
    const maxPageNumber = 20
    const pageSize = 20;
    const categoryId = $("#filterCategory").val();
    const menuId = $("#filterMenu").val();
    const visibility = $("#filterVisibility").val();
    const sortBy = $("#sortBy").val()
    const sortDirection = $("#sortDirection").val()
    const articleSearchFormData = $('#articleSearchForm').serializeFormToJSON()

    articleSearchFormData.categoryId = categoryId;
    articleSearchFormData.menuId = menuId;
    articleSearchFormData.visibility = visibility;
    articleSearchFormData.pageSize = pageSize;
    articleSearchFormData.sortBy = sortBy
    articleSearchFormData.direction = sortDirection

    const articlesPaging = await getArticlesPaging(articleSearchFormData, pageNumber)
    const template = await articlesTemplate(articlesPaging.articles)
    const pageNumberTemplate = await articlesPageNumberTemplate(articlesPaging.pageNumber + 1, maxPageNumber, articlesPaging.totalPages)
    const pageSummary = getPageSummary(articlesPaging)

    // 페이징 시 편집모드 처리
    if (isEditMode) {
        if (preserveEditMode) {
            updateButtonVisibility();
            showCheckboxes();
        } else {
            isEditMode = false;
            updateButtonVisibility();
            hideCheckboxes();
            uncheckAllCheckboxes();
        }
    }
    updateSelectedCount();

    $('#articlePagination').html(pageNumberTemplate)
    $('#articlePageSummary').html(pageSummary)
    $('#article-tbody').html(template)

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
}

// 카테고리 데이터 로드
const loadCategories = async () => {
    const options = {
        method: 'GET'
    };

    async function success(response) {
        let options = '<option value="">Select Category</option>';
        options += response.data.categories.map(category =>
            `<option value="${category.id}">${category.name}</option>`
        ).join('');
        $("#filterCategory").html(options);
        return
    }

    return await httpRequest('/api/categories', options, success);
}

// 메뉴 데이터 로드
const loadMenus = async (categoryId) => {
    const options = {
        method: 'GET'
    };

    async function success(response) {
        let options = '<option value="">Select Menu</option>';
        response.data.menus.forEach(menu => {
            options += `<option value="${menu.id}">${menu.name}</option>`;
        });
        $("#filterMenu").html(options);
    }

    await httpRequest(`/api/admin/menus/${categoryId}`, options, success);
}

// 버튼 가시성 업데이트
function updateButtonVisibility() {
    if (isEditMode) {
        $('#editButton').addClass('d-none');
        $('#actionButtons').removeClass('d-none');
    } else {
        $('#editButton').removeClass('d-none');
        $('#actionButtons').addClass('d-none');
    }
}

// 체크박스 표시
function showCheckboxes() {
    $('#tableCheckAllContainer').removeClass('d-none');
    $('.article-checkbox-container').removeClass('d-none');
}

// 체크박스 숨김
function hideCheckboxes() {
    $('#tableCheckAllContainer').addClass('d-none');
    $('.article-checkbox-container').addClass('d-none');
}

// 모든 체크박스 해제
function uncheckAllCheckboxes() {
    $('#tableCheckAll').prop('checked', false);
    $('.article-checkbox').prop('checked', false);
}

// 선택된 게시글 ID 가져오기
function getSelectedArticleIds() {
    return $('.article-checkbox:checked').map(function () {
        return $(this).data('article-id');
    }).get();
}

// 선택된 항목 수 업데이트
function updateSelectedCount() {
    const selectedCount = $('.article-checkbox:checked').length;

    if (selectedCount === 0) {
        $('#selectedCountText').text('선택된 항목 없음');
    } else {
        $('#selectedCountText').text(`${selectedCount}개 선택됨`);
    }

    toggleActionButtonsState(selectedCount > 0);
}

function toggleActionButtonsState(hasSelection) {
    $('#deleteButton, #changePublicButton, #changePrivateButton').prop('disabled', !hasSelection);
}

async function handleBatchVisibilityChange(targetVisibility) {
    const selectedIds = getSelectedArticleIds();
    if (selectedIds.length === 0) {
        await alert('변경할 항목을 선택해주세요.');
        return;
    }

    const updatedCount = await updateArticlesVisibility(selectedIds, targetVisibility);
    await alert(`${updatedCount}개의 게시글이 ${targetVisibility === 'PUBLIC' ? '공개' : '비공개'}로 변경되었습니다.`);
    await loadArticles(currentPageNumber, {preserveEditMode: true});
    uncheckAllCheckboxes();
    updateSelectedCount();
}

async function handleSingleVisibilityToggle(element) {
    const badge = $(element);
    if (badge.data('loading')) {
        return;
    }

    const articleId = badge.data('article-id');
    const currentVisibility = badge.data('visibility');
    if (!articleId || !currentVisibility) {
        return;
    }
    const nextVisibility = currentVisibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC';

    badge.data('loading', true).addClass('disabled');
    try {
        await updateArticlesVisibility([articleId], nextVisibility);
        await loadArticles(currentPageNumber, {preserveEditMode: isEditMode});
    } finally {
        badge.data('loading', false).removeClass('disabled');
    }
}


/* document.ready  */
$(document).ready(function () {
    $(document).on("click", ".admin-nav", (event) => {
        $(".admin-nav").each((index, element) => {
            $(element).removeClass("active")
        })
        $(event.currentTarget).addClass("active")
    })

    $(document).on("click", "#categories-content .list-group-item", (event) => {
        $("#categories-content .list-group-item").removeClass("active") // 모든 항목에서 active 제거
        $(event.currentTarget).addClass("active")  // 클릭한 항목에 active 추가
        currentCategoryId = $(event.currentTarget).data("active")
    })

    $(document).on("click", "#adminArticlesSearch", () => {
        loadArticles(1);
    })

    // 카테고리 선택 시 메뉴 데이터 로드
    $(document).on("change", "#filterCategory", function () {
        const categoryId = $(this).val();
        if (categoryId) {
            loadMenus(categoryId);
            $("#filterMenu").prop("disabled", false);
        } else {
            $("#filterMenu").prop("disabled", true);
            $("#filterMenu").html('<option value="">Select Menu</option>');
        }
    });

    // 필터 적용 버튼 클릭
    $(document).on("click", "#applyFilter", () => {
        loadArticles(1);

        // 드롭다운 닫기
        const dropdownElement = document.getElementById('articleFilterButton');
        const dropdown = bootstrap.Dropdown.getInstance(dropdownElement);
        if (dropdown) {
            dropdown.hide();
        }
    });

    // Sort 적용 버튼 클릭
    $(document).on("click", "#applySort", () => {
        const sortBy = $("#sortBy").val();
        const sortDirection = $("#sortDirection").val();
        const articleSearchFormData = $('#articleSearchForm').serializeFormToJSON();
        articleSearchFormData.sortBy = sortBy;
        articleSearchFormData.direction = sortDirection;
        loadArticles(1);

        // 드롭다운 닫기
        const dropdownElement = document.getElementById('articleSortButton');
        const dropdown = bootstrap.Dropdown.getInstance(dropdownElement);
        if (dropdown) {
            dropdown.hide();
        }
    });

    // 편집 버튼 클릭 이벤트
    $(document).on("click", "#editButton", function () {
        isEditMode = true;
        updateButtonVisibility();
        showCheckboxes();
    });

    // 취소 버튼 클릭 이벤트
    $(document).on("click", "#cancelButton", function () {
        isEditMode = false;
        updateButtonVisibility();
        hideCheckboxes();
        uncheckAllCheckboxes();
    });

    // 삭제 버튼 클릭 이벤트
    $(document).on('click', "#deleteButton", async function () {
        const selectedIds = getSelectedArticleIds();
        if (selectedIds.length === 0) {
            await alert('삭제할 항목을 선택해주세요.');
            return;
        }

        if (!(await confirm('선택한 항목을 삭제하시겠습니까?'))) {
            return;
        }

        const deletedCount = await deleteSelectedArticles(selectedIds);
        loadArticles(currentPageNumber)
    });

    // 전체 선택 체크박스 이벤트
    $(document).on('change', "#tableCheckAll", function () {
        const isChecked = $(this).prop('checked');
        $('.article-checkbox').prop('checked', isChecked);
    });

    // 체크박스 선택 이벤트 추가
    $(document).on('change', '.article-checkbox', function () {
        updateSelectedCount();
    });

    // 편집 버튼 클릭 이벤트 수정
    $(document).on('click', '#editButton', function () {
        isEditMode = true;
        updateButtonVisibility();
        showCheckboxes();
        updateSelectedCount(); // 초기 상태 업데이트
        document.querySelector('table').classList.toggle('show-col');
    });

    // 취소 버튼 클릭 이벤트 수정
    $(document).on('click', '#cancelButton', function () {
        isEditMode = false;
        updateButtonVisibility();
        hideCheckboxes();
        uncheckAllCheckboxes();
        updateSelectedCount();
        document.querySelector('table').classList.toggle('show-col');
    });

    // 전체 선택 체크박스 이벤트 수정
    $(document).on('change', '#tableCheckAll', function () {
        const isChecked = $(this).prop('checked');
        $('.article-checkbox').prop('checked', isChecked);
        updateSelectedCount();
    });

    $(document).on('click', '#changePublicButton', async function () {
        await handleBatchVisibilityChange('PUBLIC');
    });

    $(document).on('click', '#changePrivateButton', async function () {
        await handleBatchVisibilityChange('PRIVATE');
    });

    $(document).on('click', '.article-visibility-badge', async function () {
        await handleSingleVisibilityToggle(this);
    });

    $('.side-link .admin-nav').first().trigger('click');
})
