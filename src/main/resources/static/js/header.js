// Mapeo de roles a rutas físicas
const ROLE_PATHS = {
    'ADMIN': '/admin.html',
    'LIDER': '/lider.html',
    'AGENTE': '/agente.html'
};

// Función para navegar a la página de inicio según el rol
async function navigateToRoleHome() {
    const homeButton = document.getElementById('home-button');
    if (!homeButton) return;

    try {
        // 1. Mostrar estado de carga
        homeButton.disabled = true;
        const originalContent = homeButton.innerHTML;
        homeButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

        // 2. Obtener información del rol
        const response = await fetch('/api/auth/current-role', {
            credentials: 'include',
            headers: { 'Cache-Control': 'no-cache' }
        });

        if (!response.ok) {
            throw new Error('Error al verificar rol');
        }

        const roleData = await response.json();

        // 3. Determinar la ruta adecuada
        const homePath = ROLE_PATHS[roleData.normalizedRole] || '/login.html';

        // 4. Redirección completa
        window.location.href = window.location.origin + homePath;

    } catch (error) {
        console.error('Error en navegación:', error);
        window.location.href = '/login.html';
    }
}

// Configurar el evento al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    const homeButton = document.getElementById('home-button');
    if (homeButton) {
        homeButton.addEventListener('click', (e) => {
            e.preventDefault();
            navigateToRoleHome();
        });
    }
});