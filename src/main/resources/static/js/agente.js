// ==================== VARIABLES GLOBALES ====================
let campanaActual = null;
let misAsignaciones = [];
let empleadoActualId = null;

// ==================== INICIALIZACIÓN PRINCIPAL ====================
document.addEventListener('DOMContentLoaded', async () => {
    console.log("=== INICIANDO DASHBOARD AGENTE ===");

    try {
        // Verificar autenticación
        const authCheck = await fetch('/api/auth/validate', { credentials: 'include' });
        if (!authCheck.ok) {
            console.error("❌ Error de autenticación");
            return gracefulLogout();
        }

        // Verificar rol
        const roleCheck = await fetch('/api/auth/current-role', { credentials: 'include' });
        if (!roleCheck.ok) {
            console.error("❌ Error verificando rol");
            return gracefulLogout();
        }

        const { normalizedRole } = await roleCheck.json();
        if (normalizedRole !== 'AGENTE') {
            console.error("❌ Usuario no es agente:", normalizedRole);
            return gracefulLogout();
        }

        console.log("✅ Autenticación y rol verificados correctamente");

        // Inicializar dashboard
        await initializeAgenteDashboard();

    } catch (error) {
        console.error('❌ Error en inicialización:', error);
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

// ==================== INICIALIZACIÓN DASHBOARD AGENTE ====================
async function initializeAgenteDashboard() {
    console.log("🚀 Inicializando dashboard del agente...");

    // Primero obtener el ID del empleado actual
    empleadoActualId = await getCurrentEmployeeId();
    if (!empleadoActualId) {
        console.error("❌ No se pudo obtener el ID del empleado actual");
        alert("No se pudo identificar al empleado");
        return;
    }

    console.log("✅ Empleado ID:", empleadoActualId);

    await cargarDashboardData();
    setupEventListeners();
    setupControlAcceso();

    console.log("✅ Dashboard agente inicializado correctamente");
}

// ==================== CARGA DE DATOS ====================
async function cargarDashboardData() {
    console.log("Cargando datos del dashboard agente...");
    try {
        // 1. Primero cargar la campaña del agente
        await cargarCampanaActual();

        // 2. Luego cargar las asignaciones del agente
        await cargarMisAsignaciones();

        actualizarVistaDashboard();
    } catch (error) {
        console.error('❌ Error cargando datos del dashboard:', error);
        alert('No se pudieron cargar los datos del dashboard');
    }
}

async function cargarCampanaActual() {
    try {
        console.log("Cargando campaña actual del agente...");

        // CORREGIDO: Usar endpoint existente para agente
        const response = await fetch(`/api/campanas/agente/${empleadoActualId}`, {
            credentials: 'include'
        });

        if (response.ok) {
            campanaActual = await response.json();
            console.log("Campaña actual encontrada:", campanaActual);
        } else if (response.status === 404) {
            campanaActual = null;
            console.log("El agente no tiene campaña asignada");
        } else {
            console.error(` Error HTTP al cargar campaña: ${response.status}`);
            campanaActual = null;
        }

    } catch (error) {
        console.error('Error cargando campaña actual:', error);
        campanaActual = null;
    }
}

async function cargarMisAsignaciones() {
    try {
        console.log("📋 Cargando mis asignaciones como agente...");

        // CORREGIDO: Usar endpoint existente para empleado
        const response = await fetch(`/api/asignaciones/empleado/${empleadoActualId}`, {
            credentials: 'include'
        });

        if (response.ok) {
            misAsignaciones = await response.json();
            console.log(`✅ Asignaciones del agente: ${misAsignaciones.length}`, misAsignaciones);
        } else if (response.status === 404) {
            misAsignaciones = [];
            console.log("ℹ️ No se encontraron asignaciones para el agente");
        } else {
            console.error(`❌ Error HTTP: ${response.status}`);
            misAsignaciones = [];
        }
    } catch (error) {
        console.error('❌ Error cargando mis asignaciones:', error);
        misAsignaciones = [];
    }
}

// ==================== ACTUALIZAR VISTA ====================
function actualizarVistaDashboard() {
    console.log("🎨 Actualizando vista del dashboard agente...");

    // Actualizar información de campaña
    if (campanaActual) {
        document.getElementById('empresa-actual').textContent = campanaActual.nombreEmpresa || 'Sin empresa asignada';
        document.getElementById('campana-actual').textContent = campanaActual.nombreCampana;
        document.getElementById('nombre-campana-actual').textContent = campanaActual.nombreCampana;
        document.getElementById('descripcion-campana').textContent = campanaActual.descripcion || 'Sin descripción';
        console.log("✅ Campaña actualizada en la vista");
    } else {
        document.getElementById('empresa-actual').textContent = 'Sin empresa asignada';
        document.getElementById('campana-actual').textContent = 'Sin campaña asignada';
        document.getElementById('nombre-campana-actual').textContent = 'Sin campaña asignada';
        document.getElementById('descripcion-campana').textContent = 'No tienes una campaña asignada actualmente';
        console.log("❌ No hay campaña asignada");
    }

    // Actualizar tabla de mis asignaciones
    renderizarMisAsignaciones();
}

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
        const tr = document.createElement('tr');

        // CORREGIDO: Obtener información del empleado específico (el agente actual)
        const miAsignacion = asignacion.empleados?.find(emp => emp.idEmpleado === empleadoActualId) || {};

        // Determinar clase del badge según el estado del EMPLEADO
        let badgeClass = 'badge-pendiente';
        let estadoTexto = miAsignacion.estado || 'Asignada';

        if (miAsignacion.estado === 'EN_PROCESO') {
            badgeClass = 'badge-en-curso';
            estadoTexto = 'En proceso';
        } else if (miAsignacion.estado === 'FINALIZADA') {
            badgeClass = 'badge-completada';
            estadoTexto = 'Completada';
        } else if (miAsignacion.estado === 'CANCELADA') {
            badgeClass = 'badge-pendiente';
            estadoTexto = 'Cancelada';
        }

        tr.innerHTML = `
            <td>${asignacion.nombreTarea || 'Sin nombre'}</td>
            <td>${asignacion.observaciones || 'Sin descripción'}</td>
            <td>${asignacion.fecha ? asignacion.fecha.split('T')[0] : 'N/A'}</td>
            <td>${miAsignacion.fechaFin ? miAsignacion.fechaFin.split('T')[0] : 'Pendiente'}</td>
            <td><span class="badge ${badgeClass}">${estadoTexto}</span></td>
            <td class="text-nowrap">
                ${miAsignacion.estado === 'ASIGNADA' || miAsignacion.estado === 'EN_PROCESO' ? `
                <button class="btn btn-sm btn-success" onclick="cambiarEstadoAsignacionAgente(${asignacion.idAsignacion}, 'EN_PROCESO')" 
                        ${miAsignacion.estado === 'EN_PROCESO' ? 'disabled' : ''}>
                    <i class="fas fa-play"></i> Iniciar
                </button>
                <button class="btn btn-sm btn-info" onclick="cambiarEstadoAsignacionAgente(${asignacion.idAsignacion}, 'FINALIZADA')">
                    <i class="fas fa-check"></i> Completar
                </button>` : ''}
                ${miAsignacion.estado === 'ASIGNADA' || miAsignacion.estado === 'EN_PROCESO' ? `
                <button class="btn btn-sm btn-danger" onclick="cambiarEstadoAsignacionAgente(${asignacion.idAsignacion}, 'CANCELADA')">
                    <i class="fas fa-times"></i> Cancelar
                </button>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });

    console.log(`✅ Renderizadas ${misAsignaciones.length} asignaciones`);
}

// ==================== FUNCIONALIDADES AGENTE ====================
async function cambiarEstadoAsignacionAgente(idAsignacion, nuevoEstado) {
    try {
        console.log(`🔄 Cambiando estado de asignación ${idAsignacion} a ${nuevoEstado}`);

        // CORREGIDO: Usar el endpoint correcto para cambiar estado del EMPLEADO
        const response = await fetch(`/api/asignaciones/${idAsignacion}/empleados/${empleadoActualId}/estado?nuevoEstado=${nuevoEstado}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al cambiar estado');
        }

        await cargarDashboardData();

        const mensajes = {
            'EN_PROCESO': 'Tarea iniciada correctamente',
            'FINALIZADA': 'Tarea completada correctamente',
            'CANCELADA': 'Tarea cancelada correctamente'
        };

        mostrarExito(mensajes[nuevoEstado] || 'Estado actualizado correctamente');

    } catch (error) {
        console.error('❌ Error cambiando estado:', error);
        mostrarError(error.message);
    }
}

// ==================== EVENT LISTENERS ====================
function setupEventListeners() {
    console.log("🔗 Configurando event listeners agente...");

    // Búsqueda en tiempo real para mis asignaciones
    const searchAsignaciones = document.getElementById('searchMisAsignaciones');
    if (searchAsignaciones) {
        searchAsignaciones.addEventListener('input', debounce(() => {
            filtrarMisAsignaciones();
        }, 300));
        console.log("✅ Listener de búsqueda configurado");
    }

    // Filtro por estado
    const estadoFilter = document.getElementById('estadoFilterAsignaciones');
    if (estadoFilter) {
        estadoFilter.addEventListener('change', () => {
            filtrarMisAsignaciones();
        });
        console.log("✅ Listener de filtro por estado configurado");
    }
}

// ==================== FILTRADO Y BÚSQUEDA ====================
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
        asignacionesFiltradas = asignacionesFiltradas.filter(a => {
            const miAsignacion = a.empleados?.find(emp => emp.idEmpleado === empleadoActualId) || {};
            return miAsignacion.estado === estadoFilter;
        });
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

    const tempAsignaciones = misAsignaciones;
    misAsignaciones = asignaciones;
    renderizarMisAsignaciones();
    misAsignaciones = tempAsignaciones;
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

// ==================== UTILIDADES ====================
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

function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

async function getCurrentEmployeeId() {
    try {
        console.log("🔍 Obteniendo ID del empleado actual...");
        const r = await fetch('/api/control-acceso/me', { credentials: 'include' });
        if (!r.ok) {
            console.warn("❌ No se pudo obtener el empleado actual");
            return null;
        }
        const data = await r.json();
        console.log("✅ Empleado autenticado:", data);
        return data.idEmpleado;
    } catch (e) {
        console.error("❌ Error al obtener empleado:", e);
        return null;
    }
}

// ==================== EXPORTAR FUNCIONES ====================
window.cambiarEstadoAsignacionAgente = cambiarEstadoAsignacionAgente;