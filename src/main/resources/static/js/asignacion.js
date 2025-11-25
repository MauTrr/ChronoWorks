let tareas = [];
let asignaciones = [];
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
    // Cargar datos iniciales
    cargarAsignaciones();
    cargarEmpleados();
    cargarCampanas();
    cargarTareas();

    // Eventos de búsqueda y filtros
    const searchTareas = document.getElementById('searchTareas');
    if (searchTareas) {
        searchTareas.addEventListener('input', debounce(() => {
            const filtro = searchTareas.value.trim().toLowerCase();
            const tareasFiltradas = tareas.filter(t =>
                t.nombreTarea.toLowerCase().includes(filtro) ||
                (t.tipos && t.tipos.toLowerCase().includes(filtro))
            );
            renderizarTareas(tareasFiltradas);
        }, 300));
    }

    const searchEmpleado = document.getElementById('searchEmpleado');
    if (searchEmpleado) {
        searchEmpleado.addEventListener('input', debounce(() => {
            currentPage = 0;
            cargarAsignaciones();
        }));
    }

    const searchCampana = document.getElementById('searchCampana');
    if (searchCampana) {
        searchCampana.addEventListener('input', debounce(() => {
            currentPage = 0;
            cargarAsignaciones();
        }));
    }

    const searchFecha = document.getElementById('searchFecha');
    if (searchFecha) {
        searchFecha.addEventListener('change', () => {
            currentPage = 0;
            cargarAsignaciones();
        });
    }

    const estadoFilter = document.getElementById('estadoFilter');
    if (estadoFilter) {
        estadoFilter.addEventListener('change', () => {
            currentPage = 0;
            cargarAsignaciones();
        });
    }

    // Resetear formularios al cerrar modales
    const tareaModal = document.getElementById('tareaModal');
    if (tareaModal) {
        tareaModal.addEventListener('hidden.bs.modal', () => {
            document.getElementById('tareaForm').reset();
        });
    }

    const asignacionModal = document.getElementById('asignacionModal');
    if (asignacionModal) {
        asignacionModal.addEventListener('hidden.bs.modal', () => {
            document.getElementById('asignacionForm').reset();
        });
    }

    // CORREGIDO: Botón nueva asignación - usar querySelector en lugar de getElementById
    const nuevaAsignacionBtn = document.querySelector('[data-bs-target="#asignacionModal"]');
    if (nuevaAsignacionBtn) {
        nuevaAsignacionBtn.addEventListener('click', () => {
            abrirModalAsignacion();
        });
    }

    // Delegación de eventos para la tabla de asignaciones
    const tablaAsignaciones = document.getElementById('tablaAsignaciones');
    if (tablaAsignaciones) {
        tablaAsignaciones.addEventListener('click', async (e) => {
            const btn = e.target.closest('button');
            if (!btn) return;
            
            const idAsignacion = btn.dataset.id;
            const idEmpleado = btn.dataset.empleado;
            
            console.log('Botón clickeado:', btn.className, 'Asignación:', idAsignacion, 'Empleado:', idEmpleado);
            
            if (btn.classList.contains('editar-btn')) {
                if (!btn.disabled) {
                    await abrirModalEdicionAsignacion(idAsignacion);
                } else {
                    Swal.fire('No permitido', 'No puedes editar esta asignación porque no está activa', 'warning');
                }
            }
            else if (btn.classList.contains('iniciar-btn')) {
                await cambiarEstadoEmpleado(idAsignacion, idEmpleado, 'EN_PROCESO');
            }
            else if (btn.classList.contains('finalizar-btn')) {
                await cambiarEstadoEmpleado(idAsignacion, idEmpleado, 'FINALIZADA');
            }
            else if (btn.classList.contains('cancelar-btn')) {
                await cambiarEstadoEmpleado(idAsignacion, idEmpleado, 'CANCELADA');
            }
        });
    }

    const guardarAsignacionBtn = document.getElementById('guardarAsignacionBtn');
    if (guardarAsignacionBtn) {
        guardarAsignacionBtn.addEventListener('click', guardarAsignacion);
    }
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

        const selectTarea = document.getElementById('selectTarea');
        if (selectTarea) {
            selectTarea.innerHTML = '<option value="">Seleccione tarea...</option>';
            tareas.forEach(t => {
                const option = document.createElement('option');
                option.value = t.idTarea;
                option.textContent = t.nombreTarea;
                selectTarea.appendChild(option);
            });
        }
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
            <td>${t.tipos || 'N/A'}</td>
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

    document.querySelectorAll('.editar-btn').forEach(btn => {
        btn.addEventListener('click', () => abrirModalEdicionTarea(btn.dataset.id));
    });

    document.querySelectorAll('.eliminar-btn').forEach(btn => {
        btn.addEventListener('click', () => eliminarTarea(btn.dataset.id));
    });
}

async function crearTarea() {
    try {
        const form = document.getElementById('tareaForm');
        const formData = new FormData(form);

        const erroresFront = validarFormularioTarea(formData);
        if(Object.keys(erroresFront).length > 0) {
            mostrarErrores(erroresFront);
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor completa todos los campos requeridos',
                footer: 'Revisa que todos los campos esten correctos',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            })
            return;
        }

        const tareaData = {
            nombreTarea: formData.get('nombreTarea').trim(),
            tipo: formData.get('tipoTarea')
        }

        const response = await fetch('/api/tareas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tareaData)
        });

        const responseData = await response.json();
        if (!response.ok) {
            if (responseData.type === 'VALIDATION_ERROR') {
                mostrarErrores(responseData.errors);
                Swal.fire({
                    icon: 'error',
                    title: 'Errores de validación',
                    text: 'El servidor encontró problemas con los datos',
                    footer: 'Por favor corrige los campos marcados'
                });
            } else {
                throw new Error(responseData.message || 'Error al crear tarea');
            }
            return;
        }

        bootstrap.Modal.getInstance(document.getElementById('tareaModal')).hide();
        form.reset();
        currentPage = 0;
        cargarTareas();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Tarea creada correctamente',
            timer: 2000,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error('Error', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message,
            footer: 'Inténtalo nuevamente',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

async function abrirModalEdicionTarea(idTarea) {
    try {
        const form = document.getElementById('editarTareaForm');
        form.querySelectorAll('.error-message').forEach(em => em.remove());
        form.querySelectorAll('is-invalid').forEach(el => el.classList.remove('is-invalid'));

        const response = await fetch(`/api/tareas/${idTarea}`);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al obtener tarea');
        
        }
        const tarea = await response.json();
        
    form.querySelector('input[name="idTarea"]').value = tarea.idTarea;
    form.querySelector('input[name="nombreTarea"]').value = tarea.nombreTarea;
    form.querySelector('select[name="tipoTarea"]').value = tarea.tipos;

    const modal = new bootstrap.Modal(document.getElementById('editarTareaModal'));
    modal.show();

    } catch (error) {
        console.error('Error al cargar tarea:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || 'No se pudo cargar la tarea para edición',
            footer: 'Verifica la consola para más detalles',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

async function actualizarTarea() {
    try {
        const form = document.getElementById('editarTareaForm');

        const formData = new FormData(form);
        console.log('Formulario encontrado:', form);

        console.log('Todos los campos del formulario:');
        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value}`);
        }

        const erroresFront = validarFormularioTarea(formData);
        console.log('Errores encontrados:', erroresFront)

        if(Object.keys(erroresFront).length > 0) {
            console.log('Campos con errores:', Object.keys(erroresFront));
            mostrarErrores(erroresFront, 'editarTareaForm');
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor completa todos los campos requeridos',
                footer: 'Revisa que todos los campos esten correctos',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            })
            return;
        }

        const idTarea = formData.get('idTarea');
        const tareaData = {
            nombreTarea: formData.get('nombreTarea').trim(),
            tipo: formData.get('tipoTarea')
        };

        const response = await fetch(`/api/tareas/${idTarea}/actualizar`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tareaData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            if (errorData.type === 'VALIDATION_ERROR') {
                mostrarErrores(errorData.errors, 'editarTareaForm');
                Swal.fire({
                    icon: 'error',
                    title: 'Errores de validación',
                    text: 'El servidor encontró problemas con los datos',
                    footer: 'Por favor corrige los campos marcados',
                    background: '#edf3f4',
                    iconColor: '#23A7C1'
                });
            } else {
                throw new Error(errorData.message || 'Error al actualizar tarea');
            }
            return;
        }

        bootstrap.Modal.getInstance(document.getElementById('editarTareaModal')).hide();
        cargarTareas();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Empresa actualizada correctamente',
            timer: 2000,
            confirmButtonColor: '#23a7c1',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error('Error:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message,
            footer: 'Intentalo nuevamente',
            background: '#edf3f4',
            iconColor: '#23a7c1'
        });
    }
}

async function eliminarTarea(idTarea) {
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

        if (result.isConfirmed) {
            const response = await fetch(`/api/tareas/${idTarea}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error al eliminar tarea');
            }

            await cargarTareas();

            Swal.fire({
                icon: 'success',
                title: 'Eliminada',
                text: 'Tarea eliminada correctamente',
                timer: 2000,
                confirmButtonColor: '#23a7c1',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            });
        }
        
    } catch (error) {
        console.error('Error al eliminar tarea:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || 'No se pudo eliminar la tarea',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

function validarFormularioTarea(formData) {
    const errores = {};
    if(!formData.get('nombreTarea')?.trim()) errores.nombreTarea = 'El nombre de la tarea es obligatorio';
    if(!formData.get('tipoTarea')) errores.tipoTarea = 'El tipo de tarea es obligatorio';

    return errores;
}

function mostrarErrores(errores, formId = 'tareaForm') {
    const form = document.getElementById(formId);
    form.querySelectorAll('.error-message').forEach(em => em.remove());
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    Object.entries(errores).forEach(([campo, mensaje]) => {
        const input = form.querySelector(`[name="${campo}"]`);
        if (input) {
            input.classList.add('is-invalid');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback error-message';
            errorDiv.textContent = mensaje;
            input.parentNode.appendChild(errorDiv);
        }
    });
}

// ======Aca se trabajan las funciones de Asignaciones=======
// ====== FUNCIONES DE ASIGNACIONES ======
async function cargarAsignaciones() {
    try {
        const filtro = {
            nombreEmpleado: document.getElementById('searchEmpleado')?.value.trim() || '',
            nombreCampana: document.getElementById('searchCampana')?.value.trim() || '',
            fechaAsignacion: document.getElementById('searchFecha')?.value || '',
            estado: document.getElementById('estadoFilter')?.value || ''
        };

        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize,
            sort: 'fecha',
            direction: 'desc',
        });

        Object.entries(filtro).forEach(([key, value]) => {
            if (value !== '' && value !== undefined && value !== null) {
                params.append(key, value);
            }
        });

        const response = await fetch(`/api/asignaciones?${params.toString()}`);
        if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

        const data = await response.json();
        asignaciones = data.content || [];
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
        const response = await fetch('/api/empleados?page=0&size=1000&activo=true');
        if (!response.ok) throw new Error('Error al cargar empleados');

        const data = await response.json();
        const empleados = data.content || [];

        const selectEmpleado = document.getElementById('selectEmpleado');
        if (selectEmpleado) {
            selectEmpleado.innerHTML = '<option value="">Seleccione empleado...</option>';
            empleados.forEach(e => {
                const option = document.createElement('option');
                option.value = e.idEmpleado;
                option.textContent = `${e.nombre} ${e.apellido}`;
                selectEmpleado.appendChild(option);
            });
        }
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
        if (searchCampana) {
            searchCampana.innerHTML = '<option value="">Todas las campañas</option>';
            campanas.forEach(c => {
                const option = document.createElement('option');
                option.value = c.nombreCampana;
                option.textContent = c.nombreCampana;
                searchCampana.appendChild(option);
            });
        }

        // Llenar selector en modal
        const selectCampana = document.getElementById('selectCampana');
        if (selectCampana) {
            selectCampana.innerHTML = '<option value="">Seleccione campaña...</option>';
            campanas.forEach(c => {
                const option = document.createElement('option');
                option.value = c.idCampana;
                option.textContent = c.nombreCampana;
                selectCampana.appendChild(option);
            });
        }
    } catch (error) {
        console.error("Error al cargar campañas:", error);
        Swal.fire('Error', 'No se pudieron cargar las campañas', 'error');
    }
}

// ==================== RENDERIZADO CORREGIDO ====================
function renderizarAsignaciones(asignaciones) {
    const tbody = document.getElementById('tablaAsignaciones');
    if (!tbody) return;
    
    tbody.innerHTML = '';

    if (!asignaciones || asignaciones.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No se encontraron asignaciones</td></tr>';
        return;
    }

    console.log('Renderizando asignaciones:', asignaciones);

    asignaciones.forEach(a => {
        const empleado = a.empleados && a.empleados.length > 0 ? a.empleados[0] : {};
        
        // Estados
        const estadoEmpleado = empleado.estado || 'ASIGNADA';
        const estadoAsignacion = a.estado || 'ACTIVA';

        console.log(`Asignación ${a.idAsignacion}:`, {
            estadoEmpleado: estadoEmpleado,
            estadoAsignacion: estadoAsignacion,
            empleado: empleado
        });

        // Verificar si se puede editar (solo asignaciones ACTIVAS)
        const puedeEditar = estadoAsignacion === 'ACTIVA';

        const tr = document.createElement('tr');
        tr.setAttribute('data-id', a.idAsignacion);
        tr.innerHTML = `
            <td>${a.idAsignacion}</td>
            <td>${empleado.nombreEmpleado || ''} ${empleado.apellidoEmpleado || ''}</td>
            <td>${a.nombreCampana}</td>
            <td>${a.nombreTarea}</td>
            <td>${a.fecha ? a.fecha.split('T')[0] : ''}</td>
            <td>${a.observaciones || '-'}</td>
            <td>
                <span class="badge ${getBadgeClassAsignacion(estadoEmpleado)}">
                    ${estadoEmpleado}
                </span>
            </td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-custom editar-btn" 
                        data-id="${a.idAsignacion}" 
                        ${!puedeEditar ? 'disabled' : ''}
                        title="${!puedeEditar ? 'No se puede editar asignaciones no activas' : 'Editar'}">
                    <i class="fa-solid fa-pen-to-square"></i>
                </button>
                ${getEstadoBtnsAsignacion(a, estadoEmpleado, estadoAsignacion)}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function getEstadoBtnsAsignacion(asignacion, estadoEmpleado, estadoAsignacion) {
    console.log(`Generando botones para asignación ${asignacion.idAsignacion}:`, {
        estadoEmpleado: estadoEmpleado,
        estadoAsignacion: estadoAsignacion,
        tieneEmpleados: asignacion.empleados && asignacion.empleados.length > 0
    });

    // Si la asignación no está ACTIVA, no mostrar botones de estado
    if (estadoAsignacion !== 'ACTIVA') {
        console.log(`Asignación ${asignacion.idAsignacion} no está ACTIVA, sin botones`);
        return '<span class="text-muted small">No disponible</span>';
    }

    const primerEmpleado = asignacion.empleados && asignacion.empleados.length > 0 ? asignacion.empleados[0] : null;
    if (!primerEmpleado) {
        console.log(`Asignación ${asignacion.idAsignacion} no tiene empleados`);
        return '<span class="text-muted small">Sin empleado</span>';
    }

    const idEmpleado = primerEmpleado.idEmpleado;
    let buttons = '';

    console.log(`Estado del empleado: ${estadoEmpleado} para asignación ${asignacion.idAsignacion}`);

    // CORREGIDO: Lógica mejorada para mostrar botones
    switch(estadoEmpleado) {
        case 'ASIGNADA':
            buttons = `
                <button class="btn btn-sm btn-custom iniciar-btn mx-1" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Iniciar tarea">
                    <i class="fa-solid fa-play"></i>
                </button>
                <button class="btn btn-sm btn-custom cancelar-btn" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Cancelar tarea">
                    <i class="fa-solid fa-ban"></i>
                </button>
            `;
            break;
            
        case 'EN_PROCESO':
            buttons = `
                <button class="btn btn-sm btn-custom finalizar-btn mx-1" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Finalizar tarea">
                    <i class="fa-solid fa-flag-checkered"></i>
                </button>
                <button class="btn btn-sm btn-custom cancelar-btn mx-1" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Cancelar tarea">
                    <i class="fa-solid fa-ban"></i>
                </button>
            `;
            break;
            
        case 'FINALIZADA':
        case 'CANCELADA':
            buttons = `<span class="text-muted small">Completada</span>`;
            break;
            
        default:
            buttons = `
                <button class="btn btn-sm btn-custom iniciar-btn mx-1" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Iniciar tarea">
                    <i class="fa-solid fa-play"></i>
                </button>
                <button class="btn btn-sm btn-custom cancelar-btn" 
                        data-id="${asignacion.idAsignacion}" 
                        data-empleado="${idEmpleado}" 
                        title="Cancelar tarea">
                    <i class="fa-solid fa-ban"></i>
                </button>
            `;
            break;
    }

    console.log(`Botones generados para ${asignacion.idAsignacion}:`, buttons);
    return buttons;
}

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

// ==================== MODALES ASIGNACIONES ====================
async function abrirModalAsignacion(asignacion = null) {
    const form = document.getElementById('asignacionForm');
    if (!form) return;

    // Limpiar errores previos
    form.querySelectorAll('.error-message').forEach(em => em.remove());
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    if (asignacion && asignacion.idAsignacion) {
        console.log('Modo edición - Datos:', asignacion);
        
        // Modo edición - mostrar datos existentes
        form.querySelector('input[name="idAsignacion"]').value = asignacion.idAsignacion || '';
        
        // CORREGIDO: Usar el primer empleado de la lista
        const primerEmpleado = asignacion.empleados && asignacion.empleados.length > 0 ? asignacion.empleados[0] : null;
        if (primerEmpleado) {
            form.querySelector('select[name="idEmpleado"]').value = primerEmpleado.idEmpleado || '';
        }
        
        form.querySelector('select[name="idCampana"]').value = asignacion.idCampana || '';
        form.querySelector('select[name="idTarea"]').value = asignacion.idTarea || '';
        form.querySelector('input[name="fecha"]').value = asignacion.fecha ? asignacion.fecha.split('T')[0] : '';
        form.querySelector('textarea[name="observaciones"]').value = asignacion.observaciones || '';

        document.querySelector('#asignacionModal .modal-title').textContent = 'Editar Asignación';
        document.getElementById('guardarAsignacionBtn').textContent = 'Actualizar';
        document.getElementById('guardarAsignacionBtn').dataset.mode = 'edit';
    } else {
        // Modo creación - resetear formulario
        form.reset();
        document.querySelector('#asignacionModal .modal-title').textContent = 'Nueva Asignación';
        document.getElementById('guardarAsignacionBtn').textContent = 'Guardar';
        document.getElementById('guardarAsignacionBtn').dataset.mode = 'create';
    }

    const modalElement = document.getElementById('asignacionModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
}

async function abrirModalEdicionAsignacion(idAsignacion) {
    try {
        console.log('Abriendo modal de edición para asignación:', idAsignacion);
        const response = await fetch(`/api/asignaciones/${idAsignacion}`);
        if (!response.ok) throw new Error('Asignación no encontrada');

        const asignacion = await response.json();
        console.log('Datos de asignación recibidos:', asignacion);
        
        // CORREGIDO: Pasar el objeto asignación completo
        await abrirModalAsignacion(asignacion);

    } catch (error) {
        console.error("Error al abrir modal:", error);
        Swal.fire('Error', 'No se pudo cargar la asignación para edición', 'error');
    }
}

// ==================== GUARDAR ASIGNACIÓN CORREGIDO ====================
async function guardarAsignacion() {
    try {
        const form = document.getElementById('asignacionForm');
        if (!form) return;

        const formData = new FormData(form);
        const modo = document.getElementById('guardarAsignacionBtn').dataset.mode;

        // Validaciones básicas
        if (!formData.get('idEmpleado') || !formData.get('idCampana') || !formData.get('idTarea') || !formData.get('fecha')) {
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor completa todos los campos requeridos',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            });
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
        } else {
            // MODO CREACIÓN
            url = '/api/asignaciones';
            method = 'POST';
        }

        console.log('Enviando datos:', dto);

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al guardar asignación');
        }

        // Cerrar modal y recargar
        const modalElement = document.getElementById('asignacionModal');
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) modal.hide();
        }
        
        cargarAsignaciones();
        
        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: `Asignación ${modo === 'edit' ? 'actualizada' : 'creada'} correctamente`,
            timer: 2000,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error("Error al guardar:", error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || 'Error al guardar la asignación',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

// ==================== CAMBIOS DE ESTADO ====================
async function cambiarEstadoEmpleado(idAsignacion, idEmpleado, nuevoEstado) {
    try {
        const actionText = {
            'EN_PROCESO': 'iniciar',
            'FINALIZADA': 'finalizar', 
            'CANCELADA': 'cancelar'
        }[nuevoEstado];

        const result = await Swal.fire({
            title: `¿${actionText.charAt(0).toUpperCase() + actionText.slice(1)} tarea?`,
            text: "Esta acción cambiará el estado de la tarea",
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#23a7c1',
            cancelButtonColor: '#d33',
            confirmButtonText: `Sí, ${actionText}`,
            cancelButtonText: 'Cancelar',
            background: '#edf3f4'
        });

        if (!result.isConfirmed) return;

        // CORREGIDO: Usar el endpoint correcto
        const response = await fetch(`/api/asignaciones/${idAsignacion}/empleados/${idEmpleado}/estado?nuevoEstado=${nuevoEstado}`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `Error al cambiar estado a ${nuevoEstado}`);
        }

        await cargarAsignaciones();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: `Tarea ${actionText}da correctamente`,
            timer: 2000,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error(`Error al cambiar estado:`, error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || `No se pudo cambiar el estado de la tarea`,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}


async function cambiarEstadoAsignacion(idAsignacion, nuevoEstado) {
    try {
        const result = await Swal.fire({
            title: `¿${nuevoEstado === 'EN_PROCESO' ? 'Iniciar' : nuevoEstado === 'FINALIZADA' ? 'Finalizar' : 'Cancelar'} asignación?`,
            text: "Esta acción cambiará el estado de la asignación",
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#23a7c1',
            cancelButtonColor: '#d33',
            confirmButtonText: `Sí, ${nuevoEstado === 'EN_PROCESO' ? 'iniciar' : nuevoEstado === 'FINALIZADA' ? 'finalizar' : 'cancelar'}`,
            cancelButtonText: 'Cancelar',
            background: '#edf3f4'
        });

        if (!result.isConfirmed) return;

        // Usar el endpoint específico según el estado
        let url = '';
        if (nuevoEstado === 'EN_PROCESO') {
            url = `/api/asignaciones/${idAsignacion}/iniciar`;
        } else if (nuevoEstado === 'FINALIZADA') {
            url = `/api/asignaciones/${idAsignacion}/finalizar`;
        } else if (nuevoEstado === 'CANCELADA') {
            url = `/api/asignaciones/${idAsignacion}/cancelar`;
        }

        console.log('Cambiando estado. URL:', url);

        const response = await fetch(url, {
            method: 'PUT'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `Error al cambiar estado a ${nuevoEstado}`);
        }

        cargarAsignaciones();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: `Asignación ${nuevoEstado === 'EN_PROCESO' ? 'iniciada' : nuevoEstado === 'FINALIZADA' ? 'finalizada' : 'cancelada'} correctamente`,
            timer: 2000,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error(`Error al cambiar estado:`, error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || `No se pudo cambiar el estado de la asignación`,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

// ==================== PAGINACIÓN ====================
function actualizarPaginacion() {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    pagination.innerHTML = '';

    // Info de paginación
    const paginationInfo = document.getElementById('paginationInfo');
    if (paginationInfo) {
        paginationInfo.textContent = `Mostrando página ${currentPage + 1} de ${totalPages}`;
    }

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