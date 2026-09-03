$(document).ready(function() {
    // Code block copy functionality
    const codeBlocks = document.querySelectorAll('pre code');

    codeBlocks.forEach(function(codeBlock) {
        const pre = codeBlock.parentElement;
        if (!pre || pre.tagName !== 'PRE') {
            return;
        }

        if (pre.parentElement && pre.parentElement.classList.contains('code-block-wrapper')) {
            return;
        }

        const copyButton = document.createElement('button');
        copyButton.className = 'copy-code-btn';
        copyButton.type = 'button';
        copyButton.innerHTML = '<i class="bi bi-clipboard"></i>';
        copyButton.title = 'Copy code';

        // Wrap pre with non-scroll container and keep scroll only on pre.
        const wrapper = document.createElement('div');
        wrapper.className = 'code-block-wrapper';
        pre.parentNode.insertBefore(wrapper, pre);
        wrapper.appendChild(pre);
        pre.classList.add('code-block-scroll');
        wrapper.appendChild(copyButton);

        // Add hover effect with signature color
        copyButton.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(106, 17, 203, 0.2)';
            this.style.color = '#5a0fb1';
        });

        copyButton.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(106, 17, 203, 0.1)';
            this.style.color = '#6a11cb';
        });

        // Copy functionality
        copyButton.addEventListener('click', function() {
            const text = codeBlock.textContent;

            // Use modern clipboard API if available
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(text).then(function() {
                    showCopySuccess(copyButton);
                }).catch(function() {
                    fallbackCopyTextToClipboard(text, copyButton);
                });
            } else {
                fallbackCopyTextToClipboard(text, copyButton);
            }
        });
    });

// Fallback copy function for older browsers
    function fallbackCopyTextToClipboard(text, button) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
            const successful = document.execCommand('copy');
            if (successful) {
                showCopySuccess(button);
            } else {
                showCopyError(button);
            }
        } catch (err) {
            showCopyError(button);
        }

        document.body.removeChild(textArea);
    }

// Show copy success feedback with signature color
    function showCopySuccess(button) {
        const originalHTML = button.innerHTML;
        button.innerHTML = '<i class="bi bi-check"></i>';
        button.style.background = 'rgba(106, 17, 203, 0.2)';
        button.style.color = '#6a11cb';

        setTimeout(function() {
            button.innerHTML = originalHTML;
            button.style.background = 'rgba(106, 17, 203, 0.1)';
            button.style.color = '#6a11cb';
        }, 2000);
    }

// Show copy error feedback
    function showCopyError(button) {
        const originalHTML = button.innerHTML;
        button.innerHTML = '<i class="bi bi-x"></i>';
        button.style.background = 'rgba(239, 68, 68, 0.2)';
        button.style.color = '#ef4444';

        setTimeout(function() {
            button.innerHTML = originalHTML;
            button.style.background = 'rgba(106, 17, 203, 0.1)';
            button.style.color = '#6a11cb';
        }, 2000);
    }

})
