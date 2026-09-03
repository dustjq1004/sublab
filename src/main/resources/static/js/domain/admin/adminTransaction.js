const getCategoriesFragment = async () => {
    const options = {
        method: 'GET'
    }

    async function success(response) {
        return response;
    }

    return await httpRequest('/admin/frag/categories', options, success);
}

const getCategoryById = async (categoryId) => {
    const options = {
        method: 'GET'
    }

    async function success(response) {
        return response.data
    }

    return await httpRequest(`/api/category?categoryId=${categoryId}`, options, success);
}

const getAllCategory = async () => {
    const options = {
        method: 'GET'
    }

    async function success(response) {
        return response.data;
    }

    return await httpRequest('/api/categories', options, success);
}

const createCategory = async (categoryFormData) => {

    const options = {
        method: 'POST',
        body: JSON.stringify(categoryFormData)
    }

    async function success(response) {
        await alert("카테고리 정보가 추가 됐습니다.")
        return true;
    }

    return await httpRequest(`/api/category`, options, success);
}

const putCategoryUpdate = async (categoryFormData) => {
    const options = {
        method: 'PUT',
        body: JSON.stringify(categoryFormData)
    }

    async function success(response) {
        await alert("카테고리 정보가 수정 됐습니다.")
    }

    await httpRequest(`/api/category/update`, options, success)
}

const deleteCategory = async (categoryId) => {
    const options = {
        method: 'DELETE',
        body: JSON.stringify({"categoryId": categoryId})
    }

    async function success(response) {
        await alert("카테고리가 삭제 됐습니다.")
    }

    await httpRequest(`/api/category/delete`, options, success)
}

/*
*
* Menu Transaction
*
*/
const getMenus = async (categoryId) => {
    const options = {
        method: 'GET'
    };

    function success(response) {
        return response.data
    }

    return await httpRequest(`/api/admin/menus/${categoryId}`, options, success);
}

const getMenuDetailById = async (menuId) => {
    let menu = {}
    const options = {
        method: 'GET'
    };

    function success(response) {
        return response.data;
    }


    return await httpRequest(`/api/admin/menu/${menuId}`, options, success);
}

const putMenuUpdate = async (menuFormData) => {
    const options = {
        method: 'PUT',
        body: JSON.stringify(menuFormData)
    }

    async function success(response) {
        await alert("메뉴 정보가 수정 됐습니다.")
    }

    await httpRequest(`/api/admin/menu/update`, options, success)
}

const deleteMenuRemove = async (menuId) => {
    const options = {
        method: 'DELETE',
        body: JSON.stringify({"menuId": menuId})
    }

    async function success(response) {
        await alert("메뉴가 삭제 됐습니다.")
        loadMenusTemplate(response.data.categoryId)
    }

    httpRequest(`/api/admin/menu/delete`, options, success)
}

const postMenuAdd = async (menuFormData) => {
    const options = {
        method: 'POST',
        body: JSON.stringify(menuFormData)
    }

    async function success(response) {
        await alert("메뉴 정보가 추가 됐습니다.")
        loadMenusTemplate(response.data.categoryId)
    }

    await httpRequest(`/api/admin/menu`, options, success)
}


const getArticlesFragment = async () => {
    const options = {
        method: 'GET'
    }

    async function success(response) {
        return response;
    }

    return await httpRequest('/admin/frag/articles', options, success);
}

const getArticlesPaging = async (articleSearchFormData, pageNumber) => {
    const options = {
        method: 'GET'
    }

    articleSearchFormData.pageNumber = pageNumber

    const queryString = new URLSearchParams(articleSearchFormData).toString()

    async function success(response) {
        return response.data;
    }

    return await httpRequest(`/api/admin/articles?${queryString}`, options, success)
}

// 선택된 게시글 삭제
// 선택된 게시글 삭제
async function deleteSelectedArticles(articleIds) {
    const options = {
        method: 'DELETE',
        body: JSON.stringify({articleIds})
    };

    async function success(response) {
        await alert('선택한 항목이 삭제되었습니다.');
        return response.data.deletedCount || 0; // 삭제된 항목 수를 가져옵니다.
    }

    return await httpRequest('/api/admin/articles/batch', options, success);
}

async function updateArticlesVisibility(articleIds, visibility) {
    const options = {
        method: 'PUT',
        body: JSON.stringify({articleIds, visibility})
    };

    async function success(response) {
        return response.data.updatedCount || 0;
    }

    return await httpRequest('/api/admin/articles/batch/visibility', options, success);
}
