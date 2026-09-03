// 삭제 기능
const deleteButton = document.getElementById('delete-btn');
let isDraftSaved = false;

if (deleteButton) {
    deleteButton.addEventListener('click', async event => {
        if (!(await confirm("삭제 하시겠습니까?"))) return

        const id = document.getElementById('article-id').value;

        async function success() {
            await alert('삭제가 완료되었습니다.')
            $(window).unbind('beforeunload');
            location.replace('/blog');
        }

        const options = {
            method: 'DELETE'
        };

        httpRequest(`/api/articles/${id}`, options, success);
    });
}

const modifyButton = document.getElementById('modify-btn');
if (modifyButton) {
    modifyButton.addEventListener('click', async event => {
        if (!await confirm("수정 하시겠습니까?")) {
            return
        }
        const articleId = document.getElementById('article-id').value;
        location.href = `/blog/new-article?id=${articleId}`;
    });
}

// Mobile FAB toggle
const fabToggle = document.getElementById('article-fab-toggle');
const fabMenu = document.getElementById('article-fab-menu');

if (fabToggle && fabMenu) {
    fabToggle.addEventListener('click', () => {
        const isOpen = fabMenu.classList.toggle('open');
        fabToggle.setAttribute('aria-expanded', String(isOpen));
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('#article-fab')) {
            fabMenu.classList.remove('open');
            fabToggle.setAttribute('aria-expanded', 'false');
        }
    });

    const mobileModify = document.getElementById('mobile-modify-btn');
    const mobileDelete = document.getElementById('mobile-delete-btn');

    if (mobileModify && modifyButton) {
        mobileModify.addEventListener('click', () => {
            fabMenu.classList.remove('open');
            modifyButton.click();
        });
    }

    if (mobileDelete && deleteButton) {
        mobileDelete.addEventListener('click', () => {
            fabMenu.classList.remove('open');
            deleteButton.click();
        });
    }
}

