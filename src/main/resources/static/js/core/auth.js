document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm'); // Corregido el ID

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => { // Corregido "submit"
            e.preventDefault();

            // Mostrar estado de carga
            const submitBtn = loginForm.getElementedBy('button[type="submit"]');
            const errorElement = document.getElementedBy('mensajeError')

            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando...';
            errorElement.classList.add('d-none');

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        usuario: loginForm.usuario.value,
                        contrasena: loginForm.contrasena.value
                    })
                });

                const data = await response.json();

                if (data.success) {
                    // La redirección será manejada por Spring Security
                    window.location.href = '/redirect-by-role';
                } else {
                    throw new Error(data.message);
                }
            } catch (error) {
                errorElement.textContent = error.message;
                errorElement.classList.remove('d-none');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Iniciar sesión';
            }
        });
    }
});;