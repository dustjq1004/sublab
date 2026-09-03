/**
 * Thumbnail Upload & Select Module
 * Handles image cropping, uploading, and selection functionality
 */
class ThumbnailCropper {
    constructor() {
        this.elements = this.initializeElements();
        this.state = this.initializeState();
        
        if (!this.isValidPage()) {
            return;
        }
        
        this.initializeEventListeners();
        this.initializeExistingItems();
    }

    /**
     * Initialize DOM elements
     */
    initializeElements() {
        return {
            grid: document.getElementById('thumbnail-grid'),
            plusTile: document.getElementById('thumb-plus'),
            fileInput: document.getElementById('thumb-file-input'),
            cropperImage: document.getElementById('cropper-image'),
            cropperDoneBtn: document.getElementById('cropper-done-btn'),
            confirmBtn: document.getElementById('thumbnail-select-confirm'),
            modalEl: document.getElementById('thumbnailSelectModal'),
            cropperModalEl: document.getElementById('cropperModal'),
            selectedNameSpan: document.getElementById('selected-thumbnail-name'),
            selectedHidden: document.getElementById('selected-thumbnail-hidden'),
            selectedNameHidden: document.getElementById('selected-thumbnail-name-hidden')
        };
    }

    /**
     * Initialize application state
     */
    initializeState() {
        return {
            cropper: null,
            selectedFilename: '',
            originalFile: null,
            imageUrl: '',
            config: {
                validFileTypes: ['image/jpeg', 'image/png'],
                thumbnailSize: { width: 320, height: 180 },
                aspectRatio: 16 / 9,
                cropperOptions: {
                    aspectRatio: 16 / 9,
                    minContainerWidth: 600,
                    minContainerHeight: 400,
                    minCropBoxWidth: 300,
                    minCropBoxHeight: 200,
                    viewMode: 0,
                    dragMode: 'move',
                    autoCropArea: 0.8,
                    responsive: true,
                    zoomable: true,
                    scalable: true,
                    rotatable: false,
                    movable: true,
                    background: true,
                    center: true,
                    highlight: true,
                    cropBoxMovable: true,
                    cropBoxResizable: false,
                    imageSmoothingEnabled: false,
                    imageSmoothingQuality: 'high'
                }
            }
        };
    }

    /**
     * Check if we're on a valid page with required elements
     */
    isValidPage() {
        return !!(this.elements.grid && this.elements.plusTile && this.elements.fileInput);
    }

    /**
     * Initialize event listeners
     */
    initializeEventListeners() {
        this.elements.plusTile?.addEventListener('click', () => this.handlePlusTileClick());
        this.elements.fileInput?.addEventListener('change', (e) => this.handleFileInputChange(e));
        this.elements.cropperDoneBtn?.addEventListener('click', () => this.handleCropperDone());
        this.elements.confirmBtn?.addEventListener('click', () => this.handleConfirmSelection());
        this.elements.modalEl?.addEventListener('shown.bs.modal', () => this.handleModalShown());
    }

    /**
     * Initialize existing thumbnail items
     */
    initializeExistingItems() {
        this.elements.grid?.querySelectorAll('.thumb-item').forEach(el => {
            this.attachItemClickHandler(el);
        });
    }

    /**
     * Handle plus tile click
     */
    handlePlusTileClick() {
        this.elements.fileInput?.click();
    }

    /**
     * Handle file input change
     */
    handleFileInputChange(event) {
        const file = event.target.files?.[0];
        if (!file) return;

        if (!this.validateFile(file)) {
            this.showAlert('jpg 또는 png 파일만 업로드할 수 있습니다.');
            this.elements.fileInput.value = '';
            return;
        }

        // Store original file information
        this.state.originalFile = file;
        this.loadImageForCropping(file);
    }

    /**
     * Validate uploaded file
     */
    validateFile(file) {
        return this.state.config.validFileTypes.includes(file.type);
    }

    /**
     * Load image for cropping
     */
    loadImageForCropping(file) {
        const reader = new FileReader();
        reader.onload = (event) => {
            this.setupCropper(event.target.result);
        };
        reader.readAsDataURL(file);
    }

    /**
     * Setup cropper with image
     */
    setupCropper(imageSrc) {
        this.elements.cropperImage.src = imageSrc;
        this.elements.cropperImage.style.display = 'block';
        
        this.showCropperModal();
        this.destroyExistingCropper();
        
        this.elements.cropperImage.onload = () => {
            this.initializeCropper();
        };
    }

    /**
     * Show cropper modal
     */
    showCropperModal() {
        const modal = bootstrap.Modal.getOrCreateInstance(this.elements.cropperModalEl);
        modal.show();
    }

    /**
     * Destroy existing cropper instance
     */
    destroyExistingCropper() {
        if (this.state.cropper) {
            this.state.cropper.destroy();
            this.state.cropper = null;
        }
    }

    /**
     * Initialize cropper instance
     */
    initializeCropper() {
        this.state.cropper = new Cropper(this.elements.cropperImage, this.state.config.cropperOptions);
    }

    /**
     * Handle cropper done button click
     */
    async handleCropperDone() {
        if (!this.state.cropper) return;

        try {
            const canvas = this.getCroppedCanvas();
            if (!canvas) {
                this.showAlert('이미지를 자를 수 없습니다.');
                return;
            }

            await this.uploadCroppedImage(canvas);
        } catch (error) {
            this.showAlert(`이미지 처리 중 오류가 발생했습니다: ${error.message}`);
        }
    }

    /**
     * Get cropped canvas
     */
    getCroppedCanvas() {
        return this.state.cropper.getCroppedCanvas({
            width: this.state.config.thumbnailSize.width,
            height: this.state.config.thumbnailSize.height,
            imageSmoothingEnabled: true,
            imageSmoothingQuality: 'high'
        });
    }

    /**
     * Upload cropped image
     */
    async uploadCroppedImage(canvas) {
        return new Promise((resolve, reject) => {
            // Use original file type or fallback to jpeg
            const mimeType = this.state.originalFile?.type || 'image/jpeg';
            const quality = 0.9;
            
            canvas.toBlob(async (blob) => {
                if (!blob) {
                    reject(new Error('이미지 변환에 실패했습니다.'));
                    return;
                }

                try {
                    const uploadResult = await this.performUpload(blob, mimeType);
                    this.handleUploadSuccess(uploadResult.url, uploadResult.logicalName);
                    resolve(uploadResult.url);
                } catch (error) {
                    reject(error);
                }
            }, mimeType, quality);
        });
    }

    /**
     * Perform file upload
     */
    async performUpload(blob, mimeType) {
        // Use original filename with thumb prefix, or fallback to timestamp
        const originalName = this.state.originalFile?.name || 'image.jpg';
        const nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.')) || 'image';
        const extension = originalName.substring(originalName.lastIndexOf('.')) || '.jpg';
        const filename = `thumb_${nameWithoutExt}_${Date.now()}${extension}`;
        
        const file = new File([blob], filename, { type: mimeType });
        const fileSize = file.size;

        // 1. S3 presigned URL 조회
        const result = await getResignedUrl(file.name);
        console.log(result)
        const presignedUrl = result.signedUrl;
        const physicalName = result.fileName; // S3 key name
        
        // S3 URL에서 최종 업로드 URL 생성 (쿼리 파라미터 제거)
        const finalUrl = presignedUrl.split("?")[0];

        // 2. 업로드할 파일 선택 후 image 메타 데이터 저장 API 호출
        let imageId;
        try {
            const saveResponse = await this.saveImageMetadata({
                type: 'image',
                url: finalUrl,
                logicalName: originalName,
                size: fileSize
            });
            imageId = saveResponse.data.id;
        } catch (error) {
            throw new Error(`이미지 메타데이터 저장 실패: ${error.message}`);
        }

        // 3. S3에 파일 업로드
        const s3Data = await requestS3PresignedImageUpload(presignedUrl, file.type, file);
        
        // 4. 업로드 완료 후 성공/실패에 대한 상태 업데이트 API 호출
        if (s3Data.ok || s3Data.status === 200 || s3Data.status === 201) {
            try {
                await this.updateUploadStatus(imageId, physicalName, true);
            } catch (error) {
                console.error('업로드 상태 업데이트 실패:', error);
                // 상태 업데이트 실패해도 업로드는 성공했으므로 계속 진행
            }
            return { url: finalUrl, logicalName: originalName };
        } else {
            // 업로드 실패 시 상태 업데이트
            try {
                await this.updateUploadStatus(imageId, null, false, 'S3 업로드 실패');
            } catch (error) {
                console.error('업로드 실패 상태 업데이트 실패:', error);
            }
            throw new Error('S3 업로드 실패');
        }
    }

    /**
     * Save image metadata API call
     */
    async saveImageMetadata(metadata) {
        const options = {
            method: 'POST',
            body: JSON.stringify(metadata)
        };

        function success(response) {
            return response;
        }

        return await httpRequest('/api/upload/image', options, success);
    }

    /**
     * Update upload status API call
     */
    async updateUploadStatus(imageId, physicalName, isSuccess, failMessage = null) {
        const requestBody = {
            id: imageId
        };

        if (isSuccess) {
            requestBody.physicalName = physicalName;
            requestBody.isSuccess = true;
        } else {
            requestBody.failMessage = failMessage || '업로드 실패';
            requestBody.isSuccess = false;
        }

        const options = {
            method: 'PUT',
            body: JSON.stringify(requestBody)
        };

        function success(response) {
            return response;
        }

        return await httpRequest('/api/upload/image', options, success);
    }

    /**
     * Handle successful upload
     */
    handleUploadSuccess(savedUrl, logicalName) {
        this.addThumbnailToGrid(savedUrl, logicalName);
        this.closeCropperModal();
        this.resetFileInput();
    }

    /**
     * Add thumbnail to grid
     */
    addThumbnailToGrid(savedUrl, logicalName) {
        const item = this.createThumbnailElement(savedUrl, logicalName);
        this.elements.grid.insertBefore(item, this.elements.plusTile.nextSibling);
        this.attachItemClickHandler(item);
    }

    /**
     * Create thumbnail element
     */
    createThumbnailElement(savedUrl, logicalName) {
        const item = document.createElement('div');
        item.className = 'thumb-item';
        item.dataset.logicalname = logicalName;
        item.dataset.filename = savedUrl; // URL은 filename에 유지 (기존 호환성)
        item.title = logicalName;

        const img = document.createElement('img');
        img.className = 'thumb-img';
        img.alt = 'thumbnail';
        img.src = savedUrl;

        const check = document.createElement('div');
        check.className = 'thumb-check d-none';
        check.textContent = '✓';

        item.appendChild(img);
        item.appendChild(check);
        return item;
    }

    /**
     * Close cropper modal
     */
    closeCropperModal() {
        const modal = bootstrap.Modal.getInstance(this.elements.cropperModalEl);
        modal?.hide();
        this.destroyExistingCropper();
    }

    /**
     * Reset file input
     */
    resetFileInput() {
        this.elements.fileInput.value = '';
    }

    /**
     * Handle confirm selection
     */
    handleConfirmSelection() {
        if (!this.state.selectedFilename) {
            this.showAlert('썸네일을 선택하세요.');
            return;
        }

        this.updateSelectedThumbnail();
        this.closeSelectionModal();
    }

    /**
     * Update selected thumbnail display
     */
    updateSelectedThumbnail() {
        if (this.elements.selectedNameSpan) {
            this.elements.selectedNameSpan.textContent = this.state.selectedFilename;
        }
        if (this.elements.selectedHidden) {
            this.elements.selectedHidden.value = this.state.imageUrl;
        }
        if (this.elements.selectedNameHidden) {
            this.elements.selectedNameHidden.value = this.state.selectedFilename;
        }
    }

    /**
     * Close selection modal
     */
    closeSelectionModal() {
        const modal = bootstrap.Modal.getInstance(this.elements.modalEl);
        modal?.hide();
    }

    /**
     * Handle modal shown event
     */
    async handleModalShown() {
        // 1. 썸네일 업로드 버튼 클릭시 이미지 조회
        await this.loadUploadedImages();
        
        if (!this.state.selectedFilename) return;
        
        // logicalName으로 먼저 찾고, 없으면 filename으로 찾기 (기존 호환성)
        const element = this.elements.grid.querySelector(`.thumb-item[data-logicalname="${this.state.selectedFilename}"]`) ||
                        this.elements.grid.querySelector(`.thumb-item[data-filename="${this.state.selectedFilename}"]`);
        if (element) {
            this.markSelected(element);
        }
    }

    /**
     * Load uploaded images from API
     */
    async loadUploadedImages() {
        try {
            const images = await this.fetchUploadedImages();
            this.renderUploadedImages(images.data);
        } catch (error) {
            console.error('이미지 조회 실패:', error);
        }
    }

    /**
     * Fetch uploaded images from API
     */
    async fetchUploadedImages() {
        const options = {
            method: 'GET'
        };

        function success(response) {
            return response;
        }

        return await httpRequest('/api/upload/image', options, success);
    }

    /**
     * Render uploaded images to grid
     */
    renderUploadedImages(images) {
        if (!images || images.length === 0) return;

        // 기존 동적으로 추가된 썸네일 제거 (플러스 타일은 유지)
        const existingItems = this.elements.grid.querySelectorAll('.thumb-item:not(.thumb-plus)');
        existingItems.forEach(item => item.remove());

        // API에서 받은 이미지들을 그리드에 추가
        images.forEach(image => {
            if (image.url && image.status === '성공') {
                const logicalName = image.logicalName || image.url;
                const item = this.createThumbnailElement(image.url, logicalName);
                this.elements.grid.insertBefore(item, this.elements.plusTile.nextSibling);
                this.attachItemClickHandler(item);
            }
        });
    }

    /**
     * Clear all selections
     */
    clearSelection() {
        this.elements.grid?.querySelectorAll('.thumb-item').forEach(el => {
            el.classList.remove('selected');
            const check = el.querySelector('.thumb-check');
            if (check) check.classList.add('d-none');
        });
    }

    /**
     * Mark item as selected
     */
    markSelected(itemEl) {
        this.clearSelection();
        itemEl.classList.add('selected');
        const check = itemEl.querySelector('.thumb-check');
        if (check) check.classList.remove('d-none');
        const imageSrc = itemEl.dataset.filename || '';
        this.state.selectedFilename = itemEl.dataset.logicalname || imageSrc;
        this.state.imageUrl = imageSrc;
        
        // hidden input에 logicalName 저장
        // if (this.elements.selectedHidden) {
        //     this.elements.selectedHidden.value = imageSrc;
        // }
        // if (this.elements.selectedNameHidden) {
        //     this.elements.selectedNameHidden.value = imageSrc;
        // }
    }

    /**
     * Attach click handler to thumbnail item
     */
    attachItemClickHandler(itemEl) {
        if (!itemEl || itemEl.classList.contains('thumb-plus')) return;
        
        itemEl.addEventListener('click', () => {
            this.markSelected(itemEl);
        });
    }

    /**
     * Show alert message
     */
    async showAlert(message) {
        await alert(message);
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new ThumbnailCropper();
});
