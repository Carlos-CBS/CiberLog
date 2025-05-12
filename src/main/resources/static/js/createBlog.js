//
//     const simplemde = new SimpleMDE({
//     element: document.getElementById("markdown-editor"),
//     spellChecker: false,
//     previewRender: renderPreview,
//     toolbar: [
//     "bold", "italic", "heading", "|",
//     "code", "quote", "unordered-list", "ordered-list", "|",
//     "link", "image", "table", "|",
//     "preview", "side-by-side", "fullscreen", "|",
// {
//     name: "insert-color",
//     action: () => {
//     document.getElementById('color-picker').style.display = 'block';
// },
//     className: "fa fa-paint-brush",
//     title: "Cambiar Color de Texto",
// }
//     ]
// });
//
//     function renderPreview(plainText) {
//     const reader = new commonmark.Parser();
//     const writer = new commonmark.HtmlRenderer();
//     const parsed = reader.parse(plainText);
//     const dirtyHTML = writer.render(parsed);
//
//     return DOMPurify.sanitize(dirtyHTML, {
//     ALLOWED_TAGS: ['span', 'strong', 'em', 'pre', 'code', 'a', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'p', 'blockquote'],
//     ALLOWED_ATTR: ['class', 'href']
// });
// }
//     // Insertar span con clase de color
//     function applyColor(colorClass) {
//     const cm = simplemde.codemirror;
//     const selectedText = cm.getSelection();
//     const coloredText = `<span class="${colorClass}">${selectedText}</span>`;
//     cm.replaceSelection(coloredText);
//     document.getElementById('color-picker').style.display = 'none';
// }
//
//     // Cerrar el selector si haces clic fuera
//     document.body.addEventListener('click', function(event) {
//     const picker = document.getElementById('color-picker');
//     if (picker && !picker.contains(event.target) && !event.target.classList.contains('fa-paint-brush')) {
//     picker.style.display = 'none';
// }
// });
//
//     // HTML para el selector de colores
//     const colorPickerHtml = `
//     <div id="color-picker" class="color-picker">
//         <label>Elige un color:</label><br />
//         <button onclick="applyColor('text-red')">Rojo</button>
//         <button onclick="applyColor('text-blue')">Azul</button>
//         <button onclick="applyColor('text-green')">Verde</button>
//         <button onclick="applyColor('text-yellow')">Amarillo</button>
//         <button onclick="applyColor('text-cyan')">Cian</button>
//     </div>
//     `;
//     document.body.insertAdjacentHTML('beforeend', colorPickerHtml);

const simplemde = new SimpleMDE({
    element: document.getElementById("markdown-editor"),
    spellChecker: false,
    previewRender: renderPreview,
    toolbar: [
        "bold", "italic", "heading", "|",
        "code", "quote", "unordered-list", "ordered-list", "|",
        "link",
        {
            name: "custom-image",
            action: showImageUploader,
            className: "fa fa-picture-o",
            title: "Insertar Imagen Local",
        },
        "table", "|",
        "preview", "side-by-side", "fullscreen", "|",
        {
            name: "insert-color",
            action: () => {
                document.getElementById('color-picker').style.display = 'block';
            },
            className: "fa fa-paint-brush",
            title: "Cambiar Color de Texto",
        }
    ]
});

function renderPreview(plainText) {
    // Procesar el texto para reemplazar los marcadores de imagen con las imágenes reales
    let processedText = plainText;

    // Buscar referencias a imágenes locales y reemplazarlas
    const imageRegex = /!\[(.*?)\]\((local-image-\d+)\)/g;
    processedText = processedText.replace(imageRegex, function(match, altText, imageId) {
        if (localImages[imageId]) {
            return `![${altText}](${localImages[imageId]})`;
        }
        return match;
    });

    const reader = new commonmark.Parser();
    const writer = new commonmark.HtmlRenderer();
    const parsed = reader.parse(processedText);
    const dirtyHTML = writer.render(parsed);

    return DOMPurify.sanitize(dirtyHTML, {
        ALLOWED_TAGS: ['span', 'strong', 'em', 'pre', 'code', 'a', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'p', 'blockquote', 'img'],
        ALLOWED_ATTR: ['class', 'href', 'src', 'alt', 'width', 'height']
    });
}

// Función para mostrar el cargador de imágenes
function showImageUploader() {
    const imageUploader = document.getElementById('image-uploader');
    if (imageUploader) {
        imageUploader.style.display = 'block';
    }
}

// Almacenamiento para imágenes locales
const localImages = {};
let imageCounter = 1;

// Insertar imagen en el editor
function insertImage() {
    const fileInput = document.getElementById('image-file');
    const file = fileInput.files[0];

    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            const imageUrl = e.target.result;
            const altText = document.getElementById('image-alt').value || 'Imagen';

            // Generar un ID único para la imagen
            const imageId = `local-image-${imageCounter++}`;

            // Almacenar la imagen por su ID
            localImages[imageId] = imageUrl;

            // Insertar un marcador simplificado en el editor
            const imageMarkdown = `![${altText}](${imageId})`;
            const cm = simplemde.codemirror;
            cm.replaceSelection(imageMarkdown);

            // Cerrar el diálogo
            document.getElementById('image-uploader').style.display = 'none';

            // Limpiar la vista previa y los campos
            document.getElementById('image-preview').src = '';
            document.getElementById('image-preview').style.display = 'none';
            fileInput.value = '';
            document.getElementById('image-alt').value = '';
        };

        reader.readAsDataURL(file);
    }
}

// Mostrar vista previa de la imagen
function previewImage() {
    const fileInput = document.getElementById('image-file');
    const file = fileInput.files[0];
    const preview = document.getElementById('image-preview');

    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        };

        reader.readAsDataURL(file);
    } else {
        preview.src = '';
        preview.style.display = 'none';
    }
}

// Insertar span con clase de color
function applyColor(colorClass) {
    const cm = simplemde.codemirror;
    const selectedText = cm.getSelection();
    const coloredText = `<span class="${colorClass}">${selectedText}</span>`;
    cm.replaceSelection(coloredText);
    document.getElementById('color-picker').style.display = 'none';
}

// Cerrar los selectores si haces clic fuera
document.body.addEventListener('click', function(event) {
    const picker = document.getElementById('color-picker');
    const uploader = document.getElementById('image-uploader');

    // Cerrar el selector de colores
    if (picker && !picker.contains(event.target) && !event.target.classList.contains('fa-paint-brush')) {
        picker.style.display = 'none';
    }

    // Cerrar el uploader de imágenes
    if (uploader && !uploader.contains(event.target) &&
        !event.target.classList.contains('fa-picture-o') &&
        !event.target.closest('.fa-picture-o')) {
        uploader.style.display = 'none';
    }
});

// HTML para el selector de colores
const colorPickerHtml = `
    <div id="color-picker" class="color-picker">
        <label>Elige un color:</label><br />
        <button onclick="applyColor('text-red')">Rojo</button>
        <button onclick="applyColor('text-blue')">Azul</button>
        <button onclick="applyColor('text-green')">Verde</button>
        <button onclick="applyColor('text-yellow')">Amarillo</button>
        <button onclick="applyColor('text-cyan')">Cian</button>
    </div>
    `;

// HTML para el uploader de imágenes
const imageUploaderHtml = `
    <div id="image-uploader" class="image-upload">
        <label>Subir Imagen:</label>
        <input type="file" id="image-file" accept="image/*" onchange="previewImage()" />
        <input type="text" id="image-alt" placeholder="Texto alternativo (Alt)" />
        <div class="preview-container">
            <img id="image-preview" style="display:none;" alt="Vista previa" />
        </div>
        <button type="button" onclick="insertImage()">Insertar Imagen</button>
        <button type="button" onclick="document.getElementById('image-uploader').style.display='none'">Cancelar</button>
    </div>
    `;

// Insertar HTML de ambos componentes
document.body.insertAdjacentHTML('beforeend', colorPickerHtml);
document.body.insertAdjacentHTML('beforeend', imageUploaderHtml);

// Función para preparar el formulario antes de enviarlo
function prepareFormSubmission() {
    // Obtener el contenido actual del editor
    const editorContent = simplemde.value();

    // Reemplazar todos los marcadores de imagen con las imágenes reales en Base64
    let processedContent = editorContent;
    const imageRegex = /!\[(.*?)\]\((local-image-\d+)\)/g;
    processedContent = processedContent.replace(imageRegex, function(match, altText, imageId) {
        if (localImages[imageId]) {
            return `![${altText}](${localImages[imageId]})`;
        }
        return match;
    });

    // Actualizar el contenido del editor con los datos reales
    simplemde.value(processedContent);

    // Añadir un campo oculto para almacenar las imágenes en formato JSON por si se necesita
    const hiddenField = document.createElement('input');
    hiddenField.type = 'hidden';
    hiddenField.name = 'localImagesData';
    hiddenField.value = JSON.stringify(localImages);
    document.getElementById('blogForm').appendChild(hiddenField);

    return true;
}

// Guardar las imágenes en localStorage para persistencia entre sesiones (opcional)
window.addEventListener('beforeunload', function() {
    localStorage.setItem('localBlogImages', JSON.stringify(localImages));
    localStorage.setItem('imageCounter', imageCounter);
});

// Cargar imágenes guardadas en localStorage al iniciar (opcional)
(function loadSavedImages() {
    const savedImages = localStorage.getItem('localBlogImages');
    const savedCounter = localStorage.getItem('imageCounter');

    if (savedImages) {
        try {
            const parsedImages = JSON.parse(savedImages);
            Object.assign(localImages, parsedImages);
        } catch (e) {
            console.error('Error al cargar imágenes guardadas:', e);
        }
    }

    if (savedCounter) {
        imageCounter = parseInt(savedCounter, 10) || 1;
    }
})();