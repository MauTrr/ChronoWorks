let tareas = [];
let currentPage = 0;
const pageSize = 10;
let totalPages = 1;

function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

document.addEventListener('DOMContentLoaded', () => {
    cargarAsignaciones();
    cargarEmpleados();
    cargarCampanas();
    cargarTareas();

    document.querySelector('[data-bs-target="#asignacionModal"]').addEventListener('click', async function() {
    try {
        // Mostrar carga mientras se obtienen los datos
        const guardarBtn = document.getElementById('guardarAsignacionBtn');
        guardarBtn.disabled = true;
        guardarBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Cargando...';

        // Recargar datos necesarios
        await Promise.all([
            cargarEmpleados(),
            cargarCampanas(),
            cargarTareas()
        ]);

        // Limpiar formulario
        document.getElementById('asignacionForm').reset();
    } catch (error) {
        console.error("Error al cargar datos:", error);
        Swal.fire('Error', 'No se pudieron cargar los datos necesarios', 'error');
    } finally {
        const guardarBtn = document.getElementById('guardarAsignacionBtn');
        guardarBtn.disabled = false;
        guardarBtn.textContent = 'Guardar';
    }
});

    document.getElementById('searchEmpleado').addEventListener('input', () => {
        currentPage = 0;
        cargarAsignaciones();
    });

    document.getElementById('searchCampana').addEventListener('input', () => {
        currentPage = 0;
        cargarAsignaciones();
    });

    document.getElementById('searchFecha').addEventListener('change', () => {
        currentPage = 0;
        cargarAsignaciones();
    });

    document.getElementById('estadoFilter').addEventListener('change', () => {
        currentPage = 0;
        cargarAsignaciones();
    });

    document.getElementById('guardarAsignacionBtn').addEventListener('click', guardarAsignacion);
    document.getElementById('generarReporteBtn').addEventListener('click', generarReporteExcel);
});


// ======Aca se trabajan las funciones de Tareas=======
// ===============CARGAR LISTA DE TAREAS===============
async function cargarTareas() {
    try {
        const response = await fetch('/api/tareas?page=0&size=1000');
        if (!response.ok) throw new Error('Error al cargar tareas');

        const data = await response.json();
        tareas = data.content || [];

        renderizarTareas(tareas);
    } catch (error) {
        console.error("Error al cargar tareas:", error);
        Swal.fire('Error', 'No se pudieron cargar las tareas', 'error');
    }
}

function renderizarTareas(tareas) {
    const tbody = document.getElementById('tablaTareas');
    tbody.innerHTML = '';
    if (!tareas || tareas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">No se encontraron tareas</td></tr>';
        return;
    }
    
    tareas.forEach(t => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${t.idTarea}</td>
            <td>${t.nombreTarea}</td>
            <td>${t.tipo || 'N/A'}</td> <!-- Cambié de tipoTarea a tipo -->
            <td class="text-nowrap">
                <button class="btn btn-sm btn-custom editar-btn" data-id="${t.idTarea}" title="Editar">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-custom eliminar-btn ms-1" data-id="${t.idTarea}" title="Eliminar">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// ==================== CARGAR DATOS ====================
async function cargarAsignaciones() {
    try {
        const filtro = {
            nombreEmpleado: document.getElementById('searchEmpleado').value.trim(),
            nombreCampana: document.getElementById('searchCampana').value.trim(),
            fechaAsignacion: document.getElementById('searchFecha').value,
            estado: document.getElementById('estadoFilter').value
        };

        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize,
            sort: 'fecha,desc',
            ...Object.fromEntries(Object.entries(filtro).filter(([_, v]) => v !== '' && v !== undefined))
        });

        const response = await fetch(`/api/asignaciones?${params.toString()}`);
        if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

        const data = await response.json();
        const asignaciones = data.content || [];
        totalPages = data.totalPages || 1;

        renderizarAsignaciones(asignaciones);
        actualizarPaginacion();

    } catch (error) {
        console.error("Error al cargar asignaciones:", error);
        Swal.fire('Error', 'No se pudieron cargar las asignaciones', 'error');
    }
}

async function cargarEmpleados() {
    try {
        const response = await fetch('/api/empleados?page=0&size=1000?activo=true');
        if (!response.ok) throw new Error('Error al cargar empleados');

        const data = await response.json();
        const empleados = data.content || [];

        const selectEmpleado = document.getElementById('selectEmpleado');
        selectEmpleado.innerHTML = '<option value="">Seleccione empleado...</option>';

        empleados.forEach(e => {
            const option = document.createElement('option');
            option.value = e.idEmpleado;
            option.textContent = `${e.nombre} ${e.apellido}`;
            selectEmpleado.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar empleados:", error);
        Swal.fire('Error', 'No se pudieron cargar los empleados', 'error');
    }
}

async function cargarCampanas() {
    try {
        const response = await fetch('/api/campanas?page=0&size=1000&estado=ACTIVA');
        const data = await response.json();
        const campanas = data.content || [];

        // Llenar filtro de campañas
        const searchCampana = document.getElementById('searchCampana');
        searchCampana.innerHTML = '<option value="">Todas las campañas</option>';
        campanas.forEach(c => {
            const option = document.createElement('option');
            option.value = c.nombreCampana;
            option.textContent = c.nombreCampana;
            searchCampana.appendChild(option);
        });

        // Llenar selector en modal
        const selectCampana = document.getElementById('selectCampana');
        selectCampana.innerHTML = '<option value="">Seleccione campaña...</option>';
        campanas.forEach(c => {
            const option = document.createElement('option');
            option.value = c.idCampana;
            option.textContent = c.nombreCampana;
            selectCampana.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar campañas:", error);
        Swal.fire('Error', 'No se pudieron cargar las campañas', 'error');
    }
}

async function cargarTareas() {
    try {
        const response = await fetch('/api/tareas?page=0&size=1000');
        const data = await response.json();
        const tareas = data.content || [];

        const selectTarea = document.getElementById('selectTarea');
        selectTarea.innerHTML = '<option value="">Seleccione tarea...</option>';

        tareas.forEach(t => {
            const option = document.createElement('option');
            option.value = t.idTarea;
            option.textContent = t.nombreTarea;
            selectTarea.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar tareas:", error);
        Swal.fire('Error', 'No se pudieron cargar las tareas', 'error');
    }
}

// ==================== RENDERIZADO ====================
function renderizarAsignaciones(asignaciones) {
    const tbody = document.getElementById('tablaAsignaciones');
    tbody.innerHTML = '';

    if (!asignaciones || asignaciones.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No se encontraron asignaciones</td></tr>';
        return;
    }

    asignaciones.forEach(a => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${a.idAsignacion}</td>
            <td>${a.nombre} ${a.apellido}</td>
            <td>${a.nombreCampana}</td>
            <td>${a.nombreTarea}</td>
            <td>${a.fecha}</td>
            <td>${a.observaciones || '-'}</td>
            <td>
                <span class="badge ${getBadgeClass(a.estado)}">
                    ${a.estado}
                </span>
            </td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-primary editar-btn" data-id="${a.idAsignacion}">
                    <i class="fa-solid fa-pen-to-square"></i>
                </button>
                ${a.estado === 'ACTIVA' ? `
                <button class="btn btn-sm btn-success iniciar-btn" data-id="${a.idAsignacion}">
                    <i class="fa-solid fa-play"></i>
                </button>` : ''}
                ${a.estado === 'EN_PROCESO' ? `
                <button class="btn btn-sm btn-info finalizar-btn" data-id="${a.idAsignacion}">
                    <i class="fa-solid fa-flag-checkered"></i>
                </button>` : ''}
                ${a.estado !== 'FINALIZADA' && a.estado !== 'ARCHIVADA' ? `
                <button class="btn btn-sm btn-warning cancelar-btn" data-id="${a.idAsignacion}">
                    <i class="fa-solid fa-ban"></i>
                </button>` : ''}
                ${(a.estado === 'FINALIZADA' || a.estado === 'CANCELADA') ? `
                <button class="btn btn-sm btn-secondary archivar-btn" data-id="${a.idAsignacion}">
                    <i class="fa-solid fa-box-archive"></i>
                </button>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });

    // Event listeners para botones dinámicos
    document.querySelectorAll('.editar-btn').forEach(btn => {
        btn.addEventListener('click', () => abrirModalEdicion(btn.dataset.id));
    });

    document.querySelectorAll('.iniciar-btn').forEach(btn => {
        btn.addEventListener('click', () => confirmarAccion(btn.dataset.id, 'iniciar', '¿Iniciar esta asignación?'));
    });

    document.querySelectorAll('.finalizar-btn').forEach(btn => {
        btn.addEventListener('click', () => confirmarAccion(btn.dataset.id, 'finalizar', '¿Finalizar esta asignación?'));
    });

    document.querySelectorAll('.cancelar-btn').forEach(btn => {
        btn.addEventListener('click', () => confirmarAccion(btn.dataset.id, 'cancelar', '¿Cancelar esta asignación?'));
    });

    document.querySelectorAll('.archivar-btn').forEach(btn => {
        btn.addEventListener('click', () => confirmarAccion(btn.dataset.id, 'archivar', '¿Archivar esta asignación?'));
    });
}

function getBadgeClass(estado) {
    const classes = {
        'ACTIVA': 'bg-success',
        'EN_PROCESO': 'bg-primary',
        'FINALIZADA': 'bg-info',
        'CANCELADA': 'bg-warning',
        'ARCHIVADA': 'bg-secondary'
    };
    return classes[estado] || 'bg-light text-dark';
}

// ==================== REPORTE EXCEL ====================
async function generarReporteExcel() {
    try {
        // Mostrar loading en el botón
        const btnExcel = document.getElementById('generarReporteBtn');
        const originalHtml = btnExcel.innerHTML;
        btnExcel.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando...';
        btnExcel.disabled = true;

        // Usar los mismos filtros que en cargarAsignaciones()
        const filtro = {
            nombreEmpleado: document.getElementById('searchEmpleado').value.trim(),
            nombreCampana: document.getElementById('searchCampana').value.trim(),
            fechaAsignacion: document.getElementById('searchFecha').value,
            estado: document.getElementById('estadoFilter').value
        };

        // Construir URL con filtros
        const params = new URLSearchParams({
            ...Object.fromEntries(Object.entries(filtro).filter(([_, v]) => v !== '' && v !== undefined)
        });

        // Hacer la petición al endpoint de Excel
        const response = await fetch(`/api/asignaciones/reporte-excel?${params.toString()}`);

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        // Crear y descargar el archivo
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_asignaciones_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Notificación de éxito
        Swal.fire({
            icon: 'success',
            title: 'Reporte generado',
            text: 'El archivo Excel se ha descargado correctamente',
            timer: 2000,
            showConfirmButton: false
        });

    } catch (error) {
        console.error("Error al generar reporte Excel:", error);
        Swal.fire('Error', 'No se pudo generar el reporte: ' + error.message, 'error');
    } finally {
        // Restaurar el botón a su estado original
        const btnExcel = document.getElementById('generarReporteBtn');
        btnExcel.innerHTML = originalHtml;
        btnExcel.disabled = false;
    }
}

// ==================== PAGINACIÓN ====================
function actualizarPaginacion() {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    // Info de paginación
    document.getElementById('paginationInfo').textContent =
        `Mostrando página ${currentPage + 1} de ${totalPages}`;

    // Botón Anterior
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#">Anterior</a>`;
    prevLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (currentPage > 0) {
            currentPage--;
            cargarAsignaciones();
        }
    });
    pagination.appendChild(prevLi);

    // Números de página
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item ${i === currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        pageLi.addEventListener('click', (e) => {
            e.preventDefault();
            currentPage = i;
            cargarAsignaciones();
        });
        pagination.appendChild(pageLi);
    }

    // Botón Siguiente
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#">Siguiente</a>`;
    nextLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (currentPage < totalPages - 1) {
            currentPage++;
            cargarAsignaciones();
        }
    });
    pagination.appendChild(nextLi);
}

// ==================== MODALES ====================
function abrirModal(asignacion = {}) {
    const form = document.getElementById('asignacionForm');

    form.idAsignacion.value = asignacion.idAsignacion || '';
    form.idEmpleado.value = asignacion.idEmpleado || '';
    form.idCampana.value = asignacion.idCampana || '';
    form.idTarea.value = asignacion.idTarea || '';
    form.fecha.value = asignacion.fecha || '';
    form.observaciones.value = asignacion.observaciones || '';
    form.estado.value = asignacion.estado || 'ACTIVA';

    new bootstrap.Modal('#asignacionModal').show();
}

async function abrirModalEdicion(idAsignacion) {
    try {
        const response = await fetch(`/api/asignaciones/${idAsignacion}`);
        if (!response.ok) throw new Error('Asignación no encontrada');

        const asignacion = await response.json();
        const form = document.getElementById('asignacionForm');

        // Llenar formulario
        form.idAsignacion.value = asignacion.idAsignacion || '';
        form.idEmpleado.value = asignacion.idEmpleado || '';
        form.idCampana.value = asignacion.idCampana || '';
        form.idTarea.value = asignacion.idTarea || '';
        form.fecha.value = asignacion.fecha || '';
        form.observaciones.value = asignacion.observaciones || '';
        form.estado.value = asignacion.estado || 'ACTIVA';

        // Mostrar modal
        new bootstrap.Modal('#asignacionModal').show();

    } catch (error) {
        console.error("Error al abrir modal:", error);
        Swal.fire('Error', 'No se pudo cargar la asignación para edición', 'error');
    }
}

async function guardarAsignacion() {
    try {
        const form = document.getElementById('asignacionForm');
        const dto = {
            idEmpleado: parseInt(form.idEmpleado.value),
            idCampana: parseInt(form.idCampana.value),
            idTarea: parseInt(form.idTarea.value),
            fecha: form.fecha.value,
            observaciones: form.observaciones.value,
            estado: form.estado.value
        };

        if (form.idAsignacion.value) dto.idAsignacion = parseInt(form.idAsignacion.value);

        const method = form.idAsignacion.value ? 'PUT' : 'POST';
        const url = form.idAsignacion.value
            ? `/api/asignaciones/${form.idAsignacion.value}/actualizar`
            : '/api/asignaciones';

        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Error al guardar');
        }

        // Cerrar modal y recargar
        bootstrap.Modal.getInstance('#asignacionModal').hide();
        cargarAsignaciones();
        Swal.fire('Éxito', 'Asignación guardada correctamente', 'success');

    } catch (error) {
        console.error("Error al guardar:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

// ==================== ACCIONES DE ESTADO ====================
function confirmarAccion(idAsignacion, accion, mensaje) {
    const modal = new bootstrap.Modal('#accionModal');
    const modalTitle = document.getElementById('accionModalTitle');
    const modalBody = document.getElementById('accionModalBody');
    const confirmBtn = document.getElementById('confirmarAccionBtn');

    // Configurar modal
    modalTitle.textContent = mensaje;
    modalBody.textContent = '¿Está seguro de realizar esta acción?';

    // Limpiar eventos anteriores y asignar nuevo
    const oldConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(oldConfirmBtn, confirmBtn);
    oldConfirmBtn.addEventListener('click', async () => {
        try {
            const response = await fetch(`/api/asignaciones/${idAsignacion}/${accion}`, {
                method: 'PUT'
            });

            if (!response.ok) throw new Error('Error al realizar la acción');

            modal.hide();
            cargarAsignaciones();
            Swal.fire('Éxito', `Asignación ${accion}da correctamente`, 'success');
        } catch (error) {
            console.error(`Error al ${accion} asignación:`, error);
            Swal.fire('Error', `No se pudo ${accion} la asignación`, 'error');
        }
    });
    modal.show();
}