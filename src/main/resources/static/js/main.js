// Gestion des commentaires
function editComment(commentId) {
    const contentElement = document.getElementById(`comment-content-${commentId}`);
    const editForm = document.getElementById(`edit-form-${commentId}`);
    const editTextarea = document.getElementById(`edit-content-${commentId}`);
    
    contentElement.style.display = 'none';
    editForm.style.display = 'block';
    editTextarea.value = contentElement.textContent.trim();
    editTextarea.focus();
}

function cancelEdit(commentId) {
    const contentElement = document.getElementById(`comment-content-${commentId}`);
    const editForm = document.getElementById(`edit-form-${commentId}`);
    
    contentElement.style.display = 'block';
    editForm.style.display = 'none';
}

// Animation des étoiles
document.addEventListener('DOMContentLoaded', function() {
    const ratingLabels = document.querySelectorAll('.rating-input label');
    ratingLabels.forEach(label => {
        label.addEventListener('mouseover', function() {
            label.querySelector('i').classList.remove('far');
            label.querySelector('i').classList.add('fas');
        });
        
        label.addEventListener('mouseout', function() {
            if (!label.previousElementSibling.checked) {
                label.querySelector('i').classList.remove('fas');
                label.querySelector('i').classList.add('far');
            }
        });
    });
});document.addEventListener('DOMContentLoaded', function() {
    // Gestion des ingrédients dynamiques
    const ingredientsList = document.getElementById('ingredientsList');
    const addIngredientBtn = document.getElementById('addIngredient');

    if (addIngredientBtn) {
        addIngredientBtn.addEventListener('click', function() {
            const index = ingredientsList.children.length;
            const ingredientDiv = document.createElement('div');
            ingredientDiv.className = 'input-group mb-2';
            ingredientDiv.innerHTML = `
                <input type="text" class="form-control" name="ingredients[${index}]" required>
                <button type="button" class="btn btn-danger remove-ingredient">Supprimer</button>
            `;
            ingredientsList.appendChild(ingredientDiv);
        });

        ingredientsList.addEventListener('click', function(e) {
            if (e.target.classList.contains('remove-ingredient')) {
                e.target.closest('.input-group').remove();
            }
        });
    }

    // Preview de l'image
    const imageInput = document.getElementById('image');
    if (imageInput) {
        imageInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const preview = document.getElementById('imagePreview');
                    if (preview) {
                        preview.src = e.target.result;
                        preview.style.display = 'block';
                    }
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // Confirmation de suppression
    const deleteButtons = document.querySelectorAll('.delete-confirm');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) {
                e.preventDefault();
            }
        });
    });

    // Auto-dismiss des alertes
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.classList.add('fade');
            setTimeout(() => {
                alert.remove();
            }, 150);
        }, 5000);
    });
});