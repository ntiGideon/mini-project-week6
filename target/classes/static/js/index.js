// Update file name display
function updateFileName(input) {
    const fileName = input.files[0] ? input.files[0].name : "No file selected";
    document.getElementById('file-name').textContent = fileName;
}

// Navbar burger menu
document.addEventListener('DOMContentLoaded', () => {
    // Get all "navbar-burger" elements
    const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);

    // Add a click event on each of them
    $navbarBurgers.forEach(el => {
        el.addEventListener('click', () => {
            // Get the target from the "data-target" attribute
            const target = el.dataset.target;
            const $target = document.getElementById(target);

            // Toggle the "is-active" class on both the "navbar-burger" and the "navbar-menu"
            el.classList.toggle('is-active');
            $target.classList.toggle('is-active');
        });
    });

    // HTMX upload indicators
    document.body.addEventListener('htmx:configRequest', (evt) => {
        if (evt.detail.elt.id === 'upload-form') {
            const uploadResult = document.getElementById('upload-result');
            uploadResult.innerHTML = `
                <div class="notification is-info">
                    <p>Uploading your image, please wait...</p>
                    <progress class="progress is-small is-primary" max="100">0%</progress>
                </div>
            `;
        }
    });

    document.body.addEventListener('htmx:xhr:progress', (evt) => {
        if (evt.detail.elt.id === 'upload-form') {
            const progress = evt.detail.lengthComputable ?
                Math.round((evt.detail.loaded / evt.detail.total) * 100) : 0;
            const progressBar = document.querySelector('#upload-result progress');
            if (progressBar) {
                progressBar.value = progress;
                progressBar.textContent = `${progress}%`;
            }
        }
    });
});