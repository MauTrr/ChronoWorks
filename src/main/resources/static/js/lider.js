// ==================== VARIABLES GLOBALES ====================
let currentPage = 0;
const pageSize = 10;
let totalPages = 1;
let campanaActual = null;
let misAsignaciones = []; // Asignaciones asignadas A MÍ
let tareasCreadas = []; // Tareas que YO creé
let asignacionesCreadas = []; // Asignaciones que YO creé
let empleadoActualId = null;

// ==================== INICIALIZACIÓN PRINCIPAL ====================
document.addEventListener('DOMContentLoaded', async () => {
    console.log("=== INICIANDO DASHBOARD LIDER ===");
    
    try {
        // Verificar autenticación
        const authCheck = await fetch('/api/auth/validate', { credentials: 'include' });
        if (!authCheck.ok) {
            console.error("Error de autenticación");
            return gracefulLogout();
        }

        // Verificar rol
        const roleCheck = await fetch('/api/auth/current-role', { credentials: 'include' });
        if (!roleCheck.ok) {
            console.error("Error verificando rol");
            return gracefulLogout();
        }
        
        const { normalizedRole } = await roleCheck.json();
        if (normalizedRole !== 'LIDER') {
            console.error("Usuario no es líder:", normalizedRole);
            return gracefulLogout();
        }

        console.log("Autenticación y rol verificados correctamente");
        
        // Inicializar dashboard
        await initializeLeaderDashboard();

    } catch (error) {
        console.error('Error en inicialización:', error);
        gracefulLogout();
    }
});

async function gracefulLogout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch (e) { 
        console.warn('Error en logout:', e); 
    }
    window.location.replace('/login.html');
}

// ==================== INICIALIZACIÓN DASHBOARD ====================
async function initializeLeaderDashboard() {
    console.log("Inicializando dashboard del líder...");
    
    // Primero obtener el ID del empleado actual
    empleadoActualId = await getCurrentEmployeeId();
    if (!empleadoActualId) {
        console.error("No se pudo obtener el ID del empleado actual");
        mostrarError("No se pudo identificar al empleado");
        return;
    }
    
    console.log("Empleado ID:", empleadoActualId);
    
    await cargarDashboardData();
    setupEventListeners();
    setupControlAcceso();
    
    console.log("Dashboard inicializado correctamente");
}

// ==================== CARGA DE DATOS ====================
async function cargarDashboardData() {
    console.log("Cargando datos del dashboard...");
    try {
        // 1. Primero cargar la campaña del líder
        await cargarCampanaActual();
        
        // 2. Luego cargar el resto de datos que dependen de la campaña
        if (campanaActual && campanaActual.idCampana) {
            await Promise.all([
                cargarMisAsignaciones(), // Asignaciones asignadas A MÍ
                cargarAsignacionesCreadas(), // Asignaciones que YO creé
                cargarTareasCreadas() // Tareas que YO creé
            ]);
        } else {
            // Si no tiene campaña, mostrar datos vacíos
            console.log("No hay campaña activa, mostrando datos vacíos");
            misAsignaciones = [];
            tareasCreadas = [];
            asignacionesCreadas = [];
        }
        
        actualizarVistaDashboard();
    } catch (error) {
        console.error('Error cargando datos del dashboard:', error);
        mostrarError('No se pudieron cargar los datos del dashboard');
    }
}

async function cargarCampanaActual() {
    try {
        console.log("Cargando campaña actual del líder...");
        
        // Usar el nuevo endpoint específico para líder
        const response = await fetch(`/api/campanas/lider/${empleadoActualId}`, { 
            credentials: 'include' 
        });
        
        if (response.ok) {
            campanaActual = await response.json();
            console.log("Campaña actual encontrada:", campanaActual);
        } else if (response.status === 404) {
            campanaActual = null;
            console.log("El líder no tiene campaña asignada");
        } else {
            console.error(`Error HTTP al cargar campaña: ${response.status}`);
            campanaActual = null;
        }
        
    } catch (error) {
        console.error('Error cargando campaña actual:', error);
        campanaActual = null;
    }
}

async function cargarMisAsignaciones() {
    try {
        console.log("Cargando asignaciones asignadas A MI...");
        
        // Usar endpoint para asignaciones donde YO soy el empleado asignado
        const response = await fetch(`/api/asignaciones/empleado/${empleadoActualId}`, {
            credentials: 'include'
        });

        if (response.ok) {
            misAsignaciones = await response.json();
            console.log(`Asignaciones asignadas a mi: ${misAsignaciones.length}`);
        } else if (response.status === 404) {
            misAsignaciones = [];
            console.log("No se encontraron asignaciones para este empleado");
        } else {
            // Fallback al endpoint original si el nuevo no existe
            const fallbackResponse = await fetch(`/api/asignaciones/lider/${empleadoActualId}`, {
                credentials: 'include'
            });
            if (fallbackResponse.ok) {
                misAsignaciones = await fallbackResponse.json();
                console.log(`Asignaciones cargadas (fallback): ${misAsignaciones.length}`);
            } else {
                misAsignaciones = [];
            }
        }
    } catch (error) {
        console.error('Error cargando mis asignaciones:', error);
        misAsignaciones = [];
    }
}

async function cargarAsignacionesCreadas() {
    try {
        console.log("Cargando asignaciones que YO cree...");
        
        // Cargar asignaciones de la campaña del líder (las que él creó)
        if (campanaActual && campanaActual.idCampana) {
            const response = await fetch(`/api/asignaciones/campana/${campanaActual.idCampana}`, {
                credentials: 'include'
            });

            if (response.ok) {
                asignacionesCreadas = await response.json();
                console.log(`Asignaciones que cree: ${asignacionesCreadas.length}`);
            } else if (response.status === 404) {
                asignacionesCreadas = [];
                console.log("No se encontraron asignaciones creadas por el líder");
            } else {
                asignacionesCreadas = [];
                console.log("Error cargando asignaciones creadas");
            }
        } else {
            asignacionesCreadas = [];
            console.log("No hay campaña, no se pueden cargar asignaciones creadas");
        }
    } catch (error) {
        console.error('Error cargando asignaciones creadas:', error);
        asignacionesCreadas = [];
    }
}

async function cargarTareasCreadas() {
    try {
        console.log("Cargando tareas que YO cree...");
        
        // Cargar tareas creadas por el líder
        const response = await fetch(`/api/tareas/creador/${empleadoActualId}`, {
            credentials: 'include'
        });

        if (response.ok) {
            tareasCreadas = await response.json();
            console.log(`Tareas que cree: ${tareasCreadas.length}`);
        } else if (response.status === 404) {
            // Fallback: cargar todas las tareas
            const fallbackResponse = await fetch('/api/tareas?page=0&size=1000', {
                credentials: 'include'
            });
            if (fallbackResponse.ok) {
                const data = await fallbackResponse.json();
                tareasCreadas = data.content || [];
                console.log(`Tareas cargadas (fallback): ${tareasCreadas.length}`);
            } else {
                tareasCreadas = [];
            }
        } else {
            throw new Error(`Error HTTP: ${response.status}`);
        }
    } catch (error) {
        console.error('Error cargando tareas:', error);
        tareasCreadas = [];
    }
}

// ==================== ACTUALIZAR VISTA ====================
function actualizarVistaDashboard() {
    console.log("Actualizando vista del dashboard...");
    
    // Actualizar información de campaña
    if (campanaActual) {
        document.getElementById('empresa-actual').textContent = campanaActual.nombreEmpresa || 'Sin empresa asignada';
        document.getElementById('campana-actual').textContent = campanaActual.nombreCampana;
        document.getElementById('nombre-campana-actual').textContent = campanaActual.nombreCampana;
        document.getElementById('descripcion-campana').textContent = campanaActual.descripcion || 'Sin descripción';
        console.log("Campaña actualizada en la vista");
    } else {
        document.getElementById('empresa-actual').textContent = 'Sin empresa asignada';
        document.getElementById('campana-actual').textContent = 'Sin campaña asignada';
        document.getElementById('nombre-campana-actual').textContent = 'Sin campaña asignada';
        document.getElementById('descripcion-campana').textContent = 'No tienes una campaña asignada actualmente';
        console.log("No hay campaña asignada");
    }

    // Actualizar estadísticas
    document.getElementById('total-asignaciones').textContent = misAsignaciones.length;
    document.getElementById('total-tareas').textContent = tareasCreadas.length;
    console.log(`Estadísticas: ${misAsignaciones.length} asignaciones, ${tareasCreadas.length} tareas`);

    // Actualizar todas las tablas
    renderizarAsignacionesCreadas();
    renderizarTareasCreadas();
    renderizarMisAsignaciones();
}

// 1. RENDERIZAR ASIGNACIONES QUE YO CREÉ
function renderizarAsignacionesCreadas() {
    const tbody = document.getElementById('tabla-asignaciones-creadas');
    if (!tbody) {
        console.error("No se encontró la tabla de asignaciones creadas");
        return;
    }
    
    tbody.innerHTML = '';

    if (asignacionesCreadas.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="fas fa-tasks fa-2x text-muted mb-2"></i>
                    <p class="text-muted">No has creado asignaciones</p>
                </td>
            </tr>
        `;
        return;
    }

    asignacionesCreadas.forEach(asignacion => {
        const empleado = asignacion.empleados && asignacion.empleados.length > 0 ? asignacion.empleados[0] : {};
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${empleado.nombreEmpleado || ''} ${empleado.apellidoEmpleado || 'Sin asignar'}</td>
            <td>${asignacion.nombreTarea || 'Sin nombre'}</td>
            <td>${asignacion.fecha ? asignacion.fecha.split('T')[0] : 'N/A'}</td>
            <td>${asignacion.observaciones || 'Sin observaciones'}</td>
            <td><span class="badge ${getBadgeClassAsignacion(asignacion.estado)}">${asignacion.estado || 'ASIGNADA'}</span></td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-primary" onclick="editarAsignacionLider(${asignacion.idAsignacion})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="eliminarAsignacionLider(${asignacion.idAsignacion})">
                    <i class="fas fa-trash"></i>
                </button>
                ${getBotonesEstadoAsignacionCreada(asignacion)}
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    console.log(`Renderizadas ${asignacionesCreadas.length} asignaciones creadas`);
}

// 2. RENDERIZAR TAREAS QUE YO CREÉ
function renderizarTareasCreadas() {
    const tbody = document.getElementById('tabla-tareas-creadas');
    if (!tbody) {
        console.error("No se encontró la tabla de tareas creadas");
        return;
    }
    
    tbody.innerHTML = '';

    if (tareasCreadas.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4">
                    <i class="fas fa-tasks fa-2x text-muted mb-2"></i>
                    <p class="text-muted">No has creado tareas</p>
                </td>
            </tr>
        `;
        return;
    }

    tareasCreadas.forEach(tarea => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${tarea.idTarea}</td>
            <td>${tarea.nombreTarea}</td>
            <td>${tarea.tipos || 'N/A'}</td>
            <td>${tarea.fechaCreacion ? tarea.fechaCreacion.split('T')[0] : 'N/A'}</td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-primary" onclick="editarTareaLider(${tarea.idTarea})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="eliminarTareaLider(${tarea.idTarea})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    console.log(`Renderizadas ${tareasCreadas.length} tareas creadas`);
}

// 3. RENDERIZAR ASIGNACIONES ASIGNADAS A MÍ (existente)
function renderizarMisAsignaciones() {
    const tbody = document.getElementById('tabla-mis-asignaciones');
    tbody.innerHTML = '';

    if (misAsignaciones.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="fas fa-tasks fa-2x text-muted mb-2"></i>
                    <p class="text-muted">No tienes asignaciones actualmente</p>
                </td>
            </tr>
        `;
        return;
    }

    misAsignaciones.forEach(asignacion => {
        // Determinar clase del badge según el estado
        let badgeClass = 'badge-pendiente';
        let estadoTexto = asignacion.estado || 'Asignada';
        
        if (asignacion.estado === 'EN_PROCESO' || asignacion.estado === 'ACTIVA') {
            badgeClass = 'badge-en-curso';
            estadoTexto = 'En curso';
        } else if (asignacion.estado === 'FINALIZADA' || asignacion.estado === 'LIBERADA') {
            badgeClass = 'badge-completada';
            estadoTexto = 'Completada';
        } else if (asignacion.estado === 'CANCELADA' || asignacion.estado === 'INACTIVA') {
            badgeClass = 'badge-pendiente';
            estadoTexto = 'Cancelada';
        }

        // Obtener información del empleado (primer empleado de la lista)
        const empleadoInfo = asignacion.empleados && asignacion.empleados.length > 0 ? 
            asignacion.empleados[0] : {};

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${asignacion.nombreTarea || 'Sin nombre'}</td>
            <td>${asignacion.observaciones || 'Sin descripción'}</td>
            <td>${asignacion.fecha ? asignacion.fecha.split('T')[0] : 'N/A'}</td>
            <td>${empleadoInfo.fechaFin ? empleadoInfo.fechaFin.split('T')[0] : 'Pendiente'}</td>
            <td><span class="badge ${badgeClass}">${estadoTexto}</span></td>
            <td class="text-nowrap">
                ${getBotonesEstadoAsignacionPropia(asignacion)}
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    console.log(`Renderizadas ${misAsignaciones.length} asignaciones asignadas a mi`);
}

// ==================== FUNCIONES AUXILIARES PARA BOTONES ====================
function getBadgeClassAsignacion(estado) {
    const classes = {
        'ASIGNADA': 'bg-secondary',
        'EN_PROCESO': 'bg-warning text-dark',
        'FINALIZADA': 'bg-success',
        'CANCELADA': 'bg-danger',
        'ACTIVA': 'bg-primary',
        'LIBERADA': 'bg-info',
        'INACTIVA': 'bg-dark'
    };
    return classes[estado] || 'bg-secondary';
}

function getBotonesEstadoAsignacionCreada(asignacion) {
    let buttons = '';
    const estado = asignacion.estado || 'ASIGNADA';

    if (estado === 'ASIGNADA' || estado === 'INACTIVA') {
        buttons = `
            <button class="btn btn-sm btn-success" onclick="cambiarEstadoAsignacion(${asignacion.idAsignacion}, 'ACTIVA')">
                <i class="fas fa-play"></i>
            </button>`;
    } else if (estado === 'EN_PROCESO' || estado === 'ACTIVA') {
        buttons = `
            <button class="btn btn-sm btn-info" onclick="cambiarEstadoAsignacion(${asignacion.idAsignacion}, 'LIBERADA')">
                <i class="fas fa-flag-checkered"></i>
            </button>`;
    }

    return buttons;
}

function getBotonesEstadoAsignacionPropia(asignacion) {
    let buttons = '';
    const estado = asignacion.estado || 'ASIGNADA';

    if (estado === 'ASIGNADA' || estado === 'INACTIVA') {
        buttons = `
            <button class="btn btn-sm btn-success" onclick="cambiarEstadoAsignacion(${asignacion.idAsignacion}, 'ACTIVA')">
                <i class="fas fa-play"></i>
            </button>`;
    } else if (estado === 'EN_PROCESO' || estado === 'ACTIVA') {
        buttons = `
            <button class="btn btn-sm btn-info" onclick="cambiarEstadoAsignacion(${asignacion.idAsignacion}, 'LIBERADA')">
                <i class="fas fa-flag-checkered"></i>
            </button>`;
    }

    return buttons;
}

// ==================== EVENT LISTENERS ====================
function setupEventListeners() {
    console.log("Configurando event listeners...");
    
    // Botón nueva asignación
    const btnNuevaAsignacion = document.getElementById('btn-nueva-asignacion');
    if (btnNuevaAsignacion) {
        btnNuevaAsignacion.addEventListener('click', abrirModalNuevaAsignacion);
        console.log("Listener de nueva asignación configurado");
    } else {
        console.error("No se encontró el botón de nueva asignación");
    }

    // Botón nueva tarea
    const btnNuevaTarea = document.getElementById('btn-nueva-tarea');
    if (btnNuevaTarea) {
        btnNuevaTarea.addEventListener('click', abrirModalNuevaTarea);
        console.log("Listener de nueva tarea configurado");
    } else {
        console.error("No se encontró el botón de nueva tarea");
    }

    // Botón crear tarea
    const btnCrearTarea = document.getElementById('crearTareaBtn');
    if (btnCrearTarea) {
        btnCrearTarea.addEventListener('click', crearTareaLider);
        console.log("Listener de crear tarea configurado");
    } else {
        console.error("No se encontró el botón de crear tarea");
    }

    // Botón guardar asignación
    const btnGuardarAsignacion = document.getElementById('guardarAsignacionBtn');
    if (btnGuardarAsignacion) {
        btnGuardarAsignacion.addEventListener('click', guardarAsignacionLider);
        console.log("Listener de guardar asignación configurado");
    } else {
        console.error("No se encontró el botón de guardar asignación");
    }

    // Búsqueda en tiempo real para asignaciones creadas
    const searchAsignacionesCreadas = document.getElementById('searchAsignacionesCreadas');
    if (searchAsignacionesCreadas) {
        searchAsignacionesCreadas.addEventListener('input', debounce(() => {
            filtrarAsignacionesCreadas();
        }, 300));
        console.log("Listener de búsqueda asignaciones creadas configurado");
    }

    // Filtro por estado para asignaciones creadas
    const estadoFilterCreadas = document.getElementById('estadoFilterCreadas');
    if (estadoFilterCreadas) {
        estadoFilterCreadas.addEventListener('change', () => {
            filtrarAsignacionesCreadas();
        });
        console.log("Listener de filtro por estado asignaciones creadas configurado");
    }

    // Búsqueda en tiempo real para mis asignaciones
    const searchAsignaciones = document.getElementById('searchMisAsignaciones');
    if (searchAsignaciones) {
        searchAsignaciones.addEventListener('input', debounce(() => {
            filtrarMisAsignaciones();
        }, 300));
        console.log("Listener de búsqueda configurado");
    }

    // Filtro por estado para mis asignaciones
    const estadoFilter = document.getElementById('estadoFilterAsignaciones');
    if (estadoFilter) {
        estadoFilter.addEventListener('change', () => {
            filtrarMisAsignaciones();
        });
        console.log("Listener de filtro por estado configurado");
    }
}

// ==================== FILTRADO Y BÚSQUEDA ====================
function filtrarAsignacionesCreadas() {
    const searchTerm = document.getElementById('searchAsignacionesCreadas')?.value.toLowerCase() || '';
    const estadoFilter = document.getElementById('estadoFilterCreadas')?.value || '';

    let asignacionesFiltradas = asignacionesCreadas;

    if (searchTerm) {
        asignacionesFiltradas = asignacionesFiltradas.filter(a =>
            (a.nombreTarea || '').toLowerCase().includes(searchTerm) ||
            (a.observaciones || '').toLowerCase().includes(searchTerm) ||
            (a.empleados && a.empleados.some(e => 
                (e.nombreEmpleado || '').toLowerCase().includes(searchTerm) ||
                (e.apellidoEmpleado || '').toLowerCase().includes(searchTerm)
            ))
        );
    }

    if (estadoFilter && estadoFilter !== 'TODAS') {
        asignacionesFiltradas = asignacionesFiltradas.filter(a => 
            a.estado === estadoFilter
        );
    }

    renderizarAsignacionesCreadasFiltradas(asignacionesFiltradas);
}

function renderizarAsignacionesCreadasFiltradas(asignaciones) {
    const tbody = document.getElementById('tabla-asignaciones-creadas');
    tbody.innerHTML = '';

    if (asignaciones.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="fas fa-search fa-2x text-muted mb-2"></i>
                    <p class="text-muted">No se encontraron asignaciones con los filtros aplicados</p>
                </td>
            </tr>
        `;
        return;
    }

    asignaciones.forEach(asignacion => {
        const empleado = asignacion.empleados && asignacion.empleados.length > 0 ? asignacion.empleados[0] : {};
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${empleado.nombreEmpleado || ''} ${empleado.apellidoEmpleado || 'Sin asignar'}</td>
            <td>${asignacion.nombreTarea || 'Sin nombre'}</td>
            <td>${asignacion.fecha ? asignacion.fecha.split('T')[0] : 'N/A'}</td>
            <td>${asignacion.observaciones || 'Sin observaciones'}</td>
            <td><span class="badge ${getBadgeClassAsignacion(asignacion.estado)}">${asignacion.estado || 'ASIGNADA'}</span></td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-primary" onclick="editarAsignacionLider(${asignacion.idAsignacion})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="eliminarAsignacionLider(${asignacion.idAsignacion})">
                    <i class="fas fa-trash"></i>
                </button>
                ${getBotonesEstadoAsignacionCreada(asignacion)}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function filtrarMisAsignaciones() {
    const searchTerm = document.getElementById('searchMisAsignaciones')?.value.toLowerCase() || '';
    const estadoFilter = document.getElementById('estadoFilterAsignaciones')?.value || '';

    let asignacionesFiltradas = misAsignaciones;

    if (searchTerm) {
        asignacionesFiltradas = asignacionesFiltradas.filter(a =>
            (a.nombreTarea || '').toLowerCase().includes(searchTerm) ||
            (a.observaciones || '').toLowerCase().includes(searchTerm)
        );
    }

    if (estadoFilter && estadoFilter !== 'TODAS') {
        // Mapear los estados del filtro a los estados reales
        const estadoMap = {
            'ASIGNADA': ['ASIGNADA', 'INACTIVA'],
            'EN_PROCESO': ['EN_PROCESO', 'ACTIVA'],
            'FINALIZADA': ['FINALIZADA', 'LIBERADA'],
            'CANCELADA': ['CANCELADA', 'INACTIVA']
        };
        
        const estadosReales = estadoMap[estadoFilter] || [estadoFilter];
        asignacionesFiltradas = asignacionesFiltradas.filter(a => 
            estadosReales.includes(a.estado)
        );
    }

    renderizarAsignacionesFiltradas(asignacionesFiltradas);
}

function renderizarAsignacionesFiltradas(asignaciones) {
    const tbody = document.getElementById('tabla-mis-asignaciones');
    tbody.innerHTML = '';

    if (asignaciones.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="fas fa-search fa-2x text-muted mb-2"></i>
                    <p class="text-muted">No se encontraron asignaciones con los filtros aplicados</p>
                </td>
            </tr>
        `;
        return;
    }

    // Reutilizar la lógica de renderizado pero con los datos filtrados
    const tempAsignaciones = misAsignaciones;
    misAsignaciones = asignaciones;
    renderizarMisAsignaciones();
    misAsignaciones = tempAsignaciones;
}

// ==================== FUNCIONALIDADES DE ASIGNACIONES ====================
async function abrirModalNuevaAsignacion() {
    console.log("📝 Abriendo modal de nueva asignación...");
    
    // Verificar que tenga una campaña asignada
    if (!campanaActual) {
        mostrarError('No puedes crear asignaciones sin tener una campaña asignada');
        return;
    }

    try {
        // Cargar empleados y tareas disponibles
        await Promise.all([
            cargarEmpleadosParaAsignacion(),
            cargarTareasParaAsignacion()
        ]);

        // Resetear formulario completamente
        const form = document.getElementById('asignacionForm');
        form.reset();
        
        // Configurar valores por defecto
        form.querySelector('input[name="idCampana"]').value = campanaActual.idCampana;
        form.querySelector('input[name="fecha"]').value = new Date().toISOString().split('T')[0];
        
        // Asegurarse de que no hay ID de asignación (modo creación)
        form.querySelector('input[name="idAsignacion"]').value = '';
        
        // Cambiar título del modal y texto del botón
        document.querySelector('#asignacionModal .modal-title').textContent = 'Nueva Asignación';
        document.getElementById('guardarAsignacionBtn').textContent = 'Guardar';
        document.getElementById('guardarAsignacionBtn').dataset.mode = 'create';

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('asignacionModal'));
        modal.show();
        
        console.log("Modal de asignación abierto correctamente en modo creación");

    } catch (error) {
        console.error('Error abriendo modal de asignación:', error);
        mostrarError('No se pudo cargar los datos para la asignación');
    }
}

async function cargarEmpleadosParaAsignacion() {
    try {
        console.log("Cargando empleados para asignación...");
        
        // Cargar agentes disponibles para la empresa de la campaña actual
        const response = await fetch(`/api/campanas/empleados/disponibles?rol=AGENTE&idEmpresa=${campanaActual.idEmpresa}`, {
            credentials: 'include'
        });
        
        if (!response.ok) throw new Error('Error al cargar empleados');

        const empleados = await response.json();
        const select = document.getElementById('selectEmpleado');
        
        select.innerHTML = '<option value="">Seleccione empleado...</option>';
        empleados.forEach(e => {
            const option = document.createElement('option');
            option.value = e.idEmpleado;
            option.textContent = `${e.nombre} ${e.apellido}`;
            select.appendChild(option);
        });

        console.log(`Cargados ${empleados.length} empleados`);

    } catch (error) {
        console.error("Error al cargar empleados:", error);
        throw error;
    }
}

async function cargarTareasParaAsignacion() {
    try {
        console.log("Cargando tareas para asignación...");
        const select = document.getElementById('selectTarea');
        select.innerHTML = '<option value="">Seleccione tarea...</option>';
        
        tareasCreadas.forEach(t => {
            const option = document.createElement('option');
            option.value = t.idTarea;
            option.textContent = t.nombreTarea;
            select.appendChild(option);
        });

        console.log(`Cargadas ${tareasCreadas.length} tareas`);

    } catch (error) {
        console.error("Error al cargar tareas:", error);
        throw error;
    }
}

async function guardarAsignacionLider() {
    try {
        const form = document.getElementById('asignacionForm');
        const formData = new FormData(form);
        const modo = document.getElementById('guardarAsignacionBtn').dataset.mode || 'create';

        console.log("Modo:", modo, "Datos del formulario:", Object.fromEntries(formData));

        // Validaciones básicas
        if (!formData.get('idEmpleado') || !formData.get('idTarea') || !formData.get('fecha')) {
            mostrarError('Por favor complete todos los campos obligatorios');
            return;
        }

        const fechaInput = formData.get('fecha');
        let fechaValida = fechaInput + 'T00:00:00';
        
        const dto = {
            idCampana: parseInt(formData.get('idCampana')),
            idTarea: parseInt(formData.get('idTarea')),
            fecha: fechaValida,
            observaciones: formData.get('observaciones'),
            estado: 'ACTIVA', // Estado fijo para la asignación
            empleados: [
                {
                    idEmpleado: parseInt(formData.get('idEmpleado')),
                    estado: 'ASIGNADA', // Estado inicial del empleado
                    fechaInicio: null,
                    fechaFin: null
                }
            ]
        };

        let url, method;

        if (modo === 'edit' && formData.get('idAsignacion')) {
            // MODO EDICIÓN
            const idAsignacion = formData.get('idAsignacion');
            url = `/api/asignaciones/${idAsignacion}/actualizar`;
            method = 'PUT';
            dto.idAsignacion = parseInt(idAsignacion);
            
            console.log("Actualizando asignación existente:", dto);
        } else {
            // MODO CREACIÓN
            url = '/api/asignaciones';
            method = 'POST';
            console.log("Creando nueva asignación:", dto);
        }

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(dto)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `Error al ${modo === 'edit' ? 'actualizar' : 'crear'} asignación`);
        }

        // Cerrar modal y recargar datos
        const modal = bootstrap.Modal.getInstance(document.getElementById('asignacionModal'));
        if (modal) modal.hide();
        
        await cargarDashboardData();
        
        mostrarExito(`Asignación ${modo === 'edit' ? 'actualizada' : 'creada'} correctamente`);

    } catch (error) {
        console.error('Error guardando asignación:', error);
        mostrarError(error.message);
    }
}

async function editarAsignacionLider(idAsignacion) {
    try {
        console.log("📝 Editando asignación:", idAsignacion);
        
        // Verificar que la asignación pertenezca al líder actual
        const response = await fetch(`/api/asignaciones/${idAsignacion}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('No tienes permisos para editar esta asignación');
        }

        const asignacion = await response.json();
        console.log("Datos de asignación para editar:", asignacion);
        
        // Verificar que la asignación pertenezca a la campaña del líder
        if (campanaActual && asignacion.idCampana !== campanaActual.idCampana) {
            mostrarError('No puedes editar asignaciones de otras campañas');
            return;
        }

        // Cargar empleados y tareas disponibles
        await Promise.all([
            cargarEmpleadosParaAsignacion(),
            cargarTareasParaAsignacion()
        ]);

        // Llenar el formulario con los datos existentes
        const form = document.getElementById('asignacionForm');
        
        // Datos básicos
        form.querySelector('input[name="idAsignacion"]').value = asignacion.idAsignacion;
        form.querySelector('input[name="idCampana"]').value = asignacion.idCampana;
        form.querySelector('input[name="fecha"]').value = asignacion.fecha ? asignacion.fecha.split('T')[0] : '';
        form.querySelector('textarea[name="observaciones"]').value = asignacion.observaciones || '';

        // Seleccionar empleado (primer empleado de la lista)
        const primerEmpleado = asignacion.empleados && asignacion.empleados.length > 0 ? asignacion.empleados[0] : null;
        if (primerEmpleado) {
            form.querySelector('select[name="idEmpleado"]').value = primerEmpleado.idEmpleado;
        }

        // Seleccionar tarea
        form.querySelector('select[name="idTarea"]').value = asignacion.idTarea;

        // Cambiar título del modal y texto del botón
        document.querySelector('#asignacionModal .modal-title').textContent = 'Editar Asignación';
        document.getElementById('guardarAsignacionBtn').textContent = 'Actualizar';
        document.getElementById('guardarAsignacionBtn').dataset.mode = 'edit';

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('asignacionModal'));
        modal.show();
        
        console.log("Modal de edición abierto correctamente");

    } catch (error) {
        console.error('Error al editar asignación:', error);
        mostrarError(error.message);
    }
}

async function eliminarAsignacionLider(idAsignacion) {
    try {
        const result = await Swal.fire({
            title: '¿Eliminar asignación?',
            text: "Esta acción no se puede deshacer",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#23a7c1',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            background: '#edf3f4'
        });

        if (!result.isConfirmed) return;

        // Cambiar estado a INACTIVA en lugar de eliminar
        const response = await fetch(`/api/asignaciones/${idAsignacion}/estado?nuevoEstado=INACTIVA`, {
            method: 'PATCH',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Error al eliminar asignación');
        }

        await cargarDashboardData();
        
        mostrarExito('Asignación eliminada correctamente');

    } catch (error) {
        console.error('Error al eliminar asignación:', error);
        mostrarError(error.message);
    }
}

async function cambiarEstadoAsignacion(idAsignacion, nuevoEstado) {
    try {
        const estadoValido = nuevoEstado;
        
        const response = await fetch(`/api/asignaciones/${idAsignacion}/estado?nuevoEstado=${estadoValido}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Error al cambiar estado');
        }

        await cargarDashboardData();
        mostrarExito(`Asignación ${nuevoEstado.toLowerCase()} correctamente`);

    } catch (error) {
        console.error('Error cambiando estado:', error);
        mostrarError('Error: ' + error.message);
    }
}

// ==================== FUNCIONALIDADES DE TAREAS ====================
function abrirModalNuevaTarea() {
    console.log("Abriendo modal de nueva tarea...");
    const modal = new bootstrap.Modal(document.getElementById('tareaModal'));
    modal.show();
}

async function crearTareaLider() {
    try {
        console.log("Creando nueva tarea...");
        const form = document.getElementById('tareaForm');
        const formData = new FormData(form);

        // Validaciones básicas
        if (!formData.get('nombreTarea')?.trim() || !formData.get('tipoTarea')) {
            mostrarError('Por favor complete todos los campos obligatorios');
            return;
        }

        const tareaData = {
            nombreTarea: formData.get('nombreTarea').trim(),
            tipo: formData.get('tipoTarea')
        };

        console.log("Enviando datos de tarea:", tareaData);

        const response = await fetch('/api/tareas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(tareaData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al crear tarea');
        }

        bootstrap.Modal.getInstance(document.getElementById('tareaModal')).hide();
        form.reset();
        await cargarDashboardData();

        mostrarExito('Tarea creada correctamente');

    } catch (error) {
        console.error('Error creando tarea:', error);
        mostrarError(error.message);
    }
}

async function editarTareaLider(idTarea) {
    try {
        const response = await fetch(`/api/tareas/${idTarea}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('No se pudo cargar la tarea para edición');
        }

        const tarea = await response.json();
        
        // Llenar el formulario de edición
        document.getElementById('idTareaEditar').value = tarea.idTarea;
        document.getElementById('nombreTareaEditar').value = tarea.nombreTarea;
        document.getElementById('tipoTareaEditar').value = tarea.tipos;

        // Mostrar modal de edición
        const modal = new bootstrap.Modal(document.getElementById('editarTareaModal'));
        modal.show();

    } catch (error) {
        console.error('Error al editar tarea:', error);
        mostrarError(error.message);
    }
}

async function eliminarTareaLider(idTarea) {
    try {
        const result = await Swal.fire({
            title: '¿Eliminar tarea?',
            text: "Esta acción no se puede deshacer",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#23a7c1',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            background: '#edf3f4'
        });

        if (!result.isConfirmed) return;

        const response = await fetch(`/api/tareas/${idTarea}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Error al eliminar tarea');
        }

        await cargarDashboardData();
        mostrarExito('Tarea eliminada correctamente');

    } catch (error) {
        console.error('Error al eliminar tarea:', error);
        mostrarError(error.message);
    }
}

// ==================== CONTROL DE ACCESO ====================
function setupControlAcceso() {
    const btnAcceso = document.getElementById('btnAcceso');
    const estadoAcceso = document.getElementById('estadoAcceso');
    const obsInput = document.getElementById('observacionAcceso');

    let empleadoId = null;
    let estado = "Fuera de turno";

    const modalEl = document.getElementById('controlAccesoModal');
    modalEl.addEventListener('show.bs.modal', async () => {
        try {
            estadoAcceso.textContent = 'Cargando estado...';
            btnAcceso.disabled = true;

            empleadoId = await getCurrentEmployeeId();
            if (!empleadoId) {
                estadoAcceso.textContent = 'No se pudo obtener el id del empleado. Contacte al administrador.';
                btnAcceso.disabled = true;
                return;
            }

            const estadoResp = await fetch(`/api/control-acceso/empleados/${empleadoId}/estado`, { credentials: 'include' });
            if (!estadoResp.ok) {
                estadoAcceso.textContent = 'No se pudo consultar estado';
                btnAcceso.disabled = true;
                return;
            }
            const estadoJson = await estadoResp.json();
            estado = estadoJson.estado;

            actualizarUI();
        } catch (e) {
            console.error('Error cargando estado modal:', e);
            estadoAcceso.textContent = 'Error al obtener estado';
            btnAcceso.disabled = true;
        }
    });

    function actualizarUI() {
        if (estado === "En turno") {
            estadoAcceso.textContent = 'Actualmente en jornada';
            btnAcceso.textContent = 'Marcar Salida';
            btnAcceso.classList.add('btn-danger');
            btnAcceso.classList.remove('btn-primary');
        } else {
            estadoAcceso.textContent = 'No has registrado entrada';
            btnAcceso.textContent = 'Marcar Entrada';
            btnAcceso.classList.add('btn-primary');
            btnAcceso.classList.remove('btn-danger');
        }
        btnAcceso.disabled = false;
    }

    btnAcceso.addEventListener('click', async () => {
        if (!empleadoId) {
            alert('ID de empleado no disponible');
            return;
        }

        const accion = estado === "En turno" ? "salida" : "entrada";
        btnAcceso.disabled = true;
        btnAcceso.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';

        let payload;
        if (accion === 'entrada') {
            payload = {
                idEmpleado: Number(empleadoId),
                observacionEntrada: obsInput ? obsInput.value : ''
            };
        } else {
            payload = {
                idEmpleado: Number(empleadoId),
                observacionSalida: obsInput ? obsInput.value : ''
            };
        }

        try {
            const resp = await fetch(`/api/control-acceso/${accion}`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(payload)
            });

            const txt = await resp.text();
            let json;
            try { json = txt ? JSON.parse(txt) : null; } catch(e){ json = null; }

            if (resp.ok) {
                estado = json.estado || (accion === "entrada" ? "En turno" : "Fuera de turno");
                estadoAcceso.textContent = accion === "entrada" ? "Has marcado ENTRADA" : "Has marcado SALIDA";
                if (json && (json.horaEntrada || json.horaSalida)) {
                    const hora = json.horaEntrada || json.horaSalida;
                    estadoAcceso.textContent += ` (${hora})`;
                }
                if (obsInput) obsInput.value = "";
                actualizarUI();
            } else {
                const msg = (json && (json.message || json.error)) ? (json.message || json.error) : txt || 'Error al registrar acceso';
                alert(msg);
            }
        } catch (e) {
            console.error('Error fetch acceso:', e);
            alert('Error de conexión con el servidor.');
        } finally {
            btnAcceso.disabled = false;
        }
    });
}

// ==================== FUNCIONES DE UTILIDAD ====================
function mostrarError(mensaje) {
    Swal.fire({
        icon: 'error',
        title: 'Error',
        text: mensaje,
        confirmButtonColor: '#23A7C1',
        background: '#edf3f4'
    });
}

function mostrarExito(mensaje) {
    Swal.fire({
        icon: 'success',
        title: 'Éxito',
        text: mensaje,
        timer: 2000,
        confirmButtonColor: '#23A7C1',
        background: '#edf3f4',
        showConfirmButton: false
    });
}

function mostrarInfo(mensaje) {
    Swal.fire({
        icon: 'info',
        title: 'Información',
        text: mensaje,
        confirmButtonColor: '#23A7C1',
        background: '#edf3f4'
    });
}

function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

async function getCurrentEmployeeId() {
    try {
        console.log("Obteniendo ID del empleado actual...");
        const r = await fetch('/api/control-acceso/me', { credentials: 'include' });
        if (!r.ok) {
            console.warn("No se pudo obtener el empleado actual");
            return null;
        }
        const data = await r.json();
        console.log("Empleado autenticado:", data);
        return data.idEmpleado;
    } catch (e) {
        console.error("Error al obtener empleado:", e);
        return null;
    }
}

// ==================== EXPORTAR FUNCIONES ====================
window.editarAsignacionLider = editarAsignacionLider;
window.eliminarAsignacionLider = eliminarAsignacionLider;
window.cambiarEstadoAsignacion = cambiarEstadoAsignacion;
window.crearTareaLider = crearTareaLider;
window.editarTareaLider = editarTareaLider;
window.eliminarTareaLider = eliminarTareaLider;