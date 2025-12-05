//Variables globales
let currentPage = 0;
const pageSize = 10;
let totalPages = 1;
let empleados = [];
let empresas = [];

let liderSeleccionado = null;
let agentesSeleccionados = [];
let empleadosDisponibles = [];
let empresaSeleccionadaId = null;
let seleccionActual = '';
let ultimoIdEmpresa = null;

let liderSeleccionadoEditar = null;
let agentesSeleccionadosEditar = [];
let empleadosDisponiblesEditar = [];
let empresaSeleccionadaIdEditar = null;
let seleccionActualEditar = '';

//Funcion del timer de respuesta
function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

function getBadgeClass(estado) {
    const classes = {
        'ACTIVA': 'bg-success',
        'EN_PROCESO': 'bg-warning text-dark',
        'FINALIZADA': 'bg-info',
        'CANCELADA': 'bg-danger',
        'ARCHIVADA': 'bg-secondary'
    };
    return classes[estado] || 'bg-secondary';
}

async function guardarCampana() {
    try {
        const form = document.getElementById('campanaForm');
        const formData = new FormData(form);
        
        
        const erroresFront = validarFormulario(formData);
        if (Object.keys(erroresFront).length > 0) {
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
        if (!form.idEmpresa.value) {
            throw new Error('Debe seleccionar una empresa');
        }

        const idEmpresa = parseInt(form.idEmpresa.value);
        if (isNaN(idEmpresa)) {
            throw new Error('ID de empresa inválido');
        }

        if (!liderSeleccionado || !liderSeleccionado.id) {
            throw new Error('Debe seleccionar un líder para la campaña');
        }

        const liderId = Number(liderSeleccionado.id);
        if (!liderId || isNaN(liderId)) {
            throw new Error('El líder seleccionado no es válido');
        }

        if (agentesSeleccionados.length === 0) {
            throw new Error('Debe seleccionar al menos un agente para la campaña');
        }

        const agentesIds = agentesSeleccionados.map(a => Number(a.id));
        if (agentesIds.some(id => !id || isNaN(id))) {
            throw new Error('Todos los agentes seleccionados deben ser válidos');
        }

        const idCampana = form.idCampana.value;

        const campanaDTO = {
            nombreCampana: form.nombreCampana.value.trim(),
            descripcion: form.descripcion.value.trim(),
            fechaInicio: form.fechaInicio.value,
            fechaFin: form.fechaFin.value,
            idEmpresa: idEmpresa,
            estado: "ACTIVA",
            idLider: liderId,
            idsAgentes: agentesIds
        };

        if (idCampana) {
            campanaDTO.idCampana = parseInt(idCampana);
        }

        const asignaciones = [
            {
                idEmpleado: liderId,
                esLider: true
            },
            ...agentesIds.map(id => ({
                idEmpleado: id,
                esLider: false
            }))
        ];

        const method = idCampana ? 'PUT' : 'POST';
        const url = idCampana ? `/api/campanas/${idCampana}` : '/api/campanas';

        const response = await fetch(url, {
            method: method,
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                campana: campanaDTO,
                asignaciones: asignaciones
            })
        });

        if (!response.ok) {
            let errorDetail = '';
            try {
                const errorData = await response.json();
                errorDetail = JSON.stringify(errorData);
                console.error('Error detallado del servidor:', errorData);
            } catch (e) {
                errorDetail = await response.text();
            }
            throw new Error(`Error ${response.status}: ${response.statusText}\nDetalles: ${errorDetail}`);
        }

        const result = await response.json();
        console.log('Respuesta exitosa:', result);

        bootstrap.Modal.getInstance(document.getElementById('campanaModal')).hide();
        cargarCampanas();
        Swal.fire({
            icon: 'success',
            title: 'Éxito', 
            text: 'Campaña guardada correctamente',
            confirmButtonColor: '#23A7C1',
            background: '#edf3f4',
            iconColor: '#23A7C1'
         });

        resetearSelecciones();

    } catch (error) {
        console.error("Error al guardar:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

function mostrarErrores(errores, formId = 'campanaForm') {
    const form = document.getElementById(formId);
    form.querySelectorAll('.error-message').forEach(el => el.remove());
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    for (const [campo, mensaje] of Object.entries(errores)) {
        const input = form.querySelector(`[name="${campo}"]`);
        const formGroup = input?.closest('.form-group') || input?.parentElement;

        if (formGroup) {
            const errorElement = document.createElement('div');
            errorElement.className = 'error-message text-danger mt-1 small';
            errorElement.textContent = mensaje;

            formGroup.appendChild(errorElement);

            input.classList.add('is-invalid')

            input.addEventListener('input', function() {
                this.classList.remove('is-invalid');
                errorElement.remove();
            }, {once: true});
        }
    }
}

function validarFormulario(formData) {
    const errores = [];

    if (!formData.get('nombreCampana')?.trim()) errores.nombreCampana = 'El nombre de la campaña es obligatorio';
    if (!formData.get('fechaInicio')) errores.fechaInicio = 'La fecha de inicio es obligatoria';
    if (!formData.get('fechaFin')) errores.fechaFin = 'La fecha de fin es obligatoria';
    if (!formData.get('idEmpresa')) errores.idEmpresa = 'Debe seleccionar una empresa';

    if (formData.get('fechaInicio') && formData.get('fechaFin')) {
        const inicio = new Date(formData.get('fechaInicio'));
        const fin = new Date(formData.get('fechaFin'));
        if (inicio > fin) errores.fechaFin = 'La fecha de fin debe ser posterior a la fecha de inicio';
    }

    return errores;


}

async function actualizarCampana() {
    try {
        const form = document.getElementById('campanaForm');
        const idCampana = form.idCampana.value;

        const ErroresFront = validarFormularioEdicion(new FormData(form));
        if (Object.keys(ErroresFront).length > 0) {
            mostrarErrores(ErroresFront, 'editarCampanaForm');
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor completa todos los campos requeridos',
                footer: 'Revisa que todos los campos esten correctos',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            });
            return;
        }

        const idEmpresa = parseInt(form.idEmpresa.value);
        const liderId = Number(liderSeleccionado.id);
        const agentesIds = agentesSeleccionados.map(a => Number(a.id));

        const campanaDTO = {
            idCampana: parseInt(idCampana),
            nombreCampana: form.nombreCampana.value.trim(),
            descripcion: form.descripcion.value.trim(),
            fechaInicio: form.fechaInicio.value,
            fechaFin: form.fechaFin.value,
            idEmpresa: idEmpresa,
            estado: "ACTIVA", 
            idLider: liderId,
            idsAgentes: agentesIds
        };

        const asignaciones = [
            { idEmpleado: liderId, esLider: true },
            ...agentesIds.map(id => ({ idEmpleado: id, esLider: false }))
        ];

        const response = await fetch(`/api/campanas/${idCampana}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ campana: campanaDTO, asignaciones })
        });

        if (!response.ok) {
            let errorDetail = '';
            try {
                const errorData = await response.json();
                errorDetail = JSON.stringify(errorData);
                console.error('Error detallado del servidor:', errorData);
            } catch (e) {
                errorDetail = await response.text();
            }
            throw new Error(`Error ${response.status}: ${response.statusText}\nDetalles: ${errorDetail}`);
        }

        const result = await response.json();
        console.log('Respuesta exitosa:', result);

        bootstrap.Modal.getInstance(document.getElementById('campanaModal')).hide();
        cargarCampanas();
        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Campaña actualizada correctamente',
            confirmButtonColor: '#23A7C1',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

        resetearSelecciones();

    } catch (error) {
        console.error("Error al actualizar campaña:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

function validarFormularioEdicion(formData) {
    const errores = {};

    if (!formData.get('nombreCampana')?.trim()) errores.nombreCampana = 'El nombre de la campaña es obligatorio';
    if (!formData.get('fechaFin')) errores.fechaFin = 'La fecha de fin es obligatoria';

    return errores;
}

//Inicializacion
document.addEventListener('DOMContentLoaded', () => {
    cargarCampanas();
    cargarEmpresas();

    document.getElementById('searchNombre').addEventListener('input', debounce(() => {
        currentPage = 0;
        cargarCampanas();
    }, 300));

    document.getElementById('searchEmpresa').addEventListener('input', debounce(() => {
        currentPage = 0;
        cargarCampanas();
    }, 300));

    document.getElementById('estadoFilter').addEventListener('change', () => {
        currentPage = 0;
        cargarCampanas();
    });

    document.getElementById('tablaCampanas').addEventListener('click', async (e) => {
        const idCampana = e.target.closest('[data-id]')?.dataset.id;
        if(!idCampana) return;
        
        if (e.target.closest('.editar-btn')) {
            const fila = e.target.closest('tr');
            const estado = fila.querySelector('.badge').textContent.trim();
            if (estado !== 'ACTIVA' && estado !== 'EN_PROCESO') {
                Swal.fire('No permitido', 'Solo puedes editar campañas ACTIVAS o EN PROCESO', 'warning');
                return;
            }
            await abrirModalEdicion(idCampana);
        }
        if (e.target.closest('.iniciar-btn')) {
            await cambiarEstadoCampana(idCampana, 'EN_PROCESO');
        }
        else if (e.target.closest('.finalizar-btn')) {
            await cambiarEstadoCampana(idCampana, 'FINALIZADA');
        }
        else if (e.target.closest('.cancelar-btn')) {
            await cambiarEstadoCampana(idCampana, 'CANCELADA');
        }

    });

    document.getElementById('guardarCampanasBtn').addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();

        this.disabled = true;
        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';

        const form = document.getElementById('campanaForm');
        const idCampana = form.idCampana.value;

        const accion = idCampana ? actualizarCampana : guardarCampana;  

        accion().finally(() => {
            setTimeout(() => {
                this.disabled = false;
                this.innerHTML = 'Guardar';
            }, 2000);
        });
    });

    document.getElementById('guardarEdicionBtn').addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();

        this.disabled = true;
        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';

        guardarCambiosEdicion().finally(() => {
            setTimeout(() => {
                this.disabled = false;
                this.innerHTML = 'Guardar Cambios';
            }, 2000);
        });
    });

    document.getElementById('generarReporteBtn').addEventListener('click', generarReporteExcel);

});

// Funcion para resetear las selecciones
function resetearSelecciones() {
    liderSeleccionado = null;
    agentesSeleccionados = [];
    document.getElementById('inputLider').value = '';
    document.getElementById('listaAgentes').innerHTML = '';
    document.getElementById('contadorAgentes').textContent = '0 agentes seleccionados';

    if (empresaSeleccionadaId) {
        cargarEmpleadosporRol('LIDER')
        cargarEmpleadosporRol('AGENTE')
    }
}

// ==================== CARGAR DATOS ====================
async function cargarCampanas() {
    try {
        const searchNombre = document.getElementById('searchNombre').value.trim();
        const searchEmpresa = document.getElementById('searchEmpresa').value.trim();
        const estado = document.getElementById('estadoFilter').value;

        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', Math.max(pageSize, 1));
        params.append('sort', 'idCampana');
        params.append('direction', 'desc');

        if (searchNombre) params.append('nombreCampana', searchNombre);
        if (searchEmpresa) params.append('nombreEmpresa', searchEmpresa);
        if (estado && estado !==  'ALL') {
            params.append('estados', estado)

            if (estado !== 'ARCHIVADA') {
                params.append('excluirArchivadas', 'true')
            }
        }

        const response = await fetch(`/api/campanas?${params.toString()}`);

        if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

        const data = await response.json();
        const campanas = data.content || [];
        totalPages = data.totalPages || 1;

        renderizarCampanas(campanas);
        actualizarPaginacion();

    } catch (error) {
        console.error("Error al cargar campañas:", error);
        Swal.fire('Error', 'No se pudieron cargar las campañas', 'error');
    }
}

async function cargarEmpresas() {
    try {
        document.getElementById('selectEmpresa').addEventListener('focus', (e) => {
            ultimoIdEmpresa = e.target.value;
        });

        document.getElementById('selectEmpresa').addEventListener('change', async (e) => {
            empresaSeleccionadaId = parseInt(e.target.value); 
            resetearSelecciones();
            if (empresaSeleccionadaId) {
                await cargarLideresDisponibles('LIDER');
                await cargarAgentesDisponibles('AGENTE');
            }
        })

        const response = await fetch('/api/empresa?page=0&size=1000');
        const data = await response.json();
        const empresas = data.content || [];

        // Llenar filtro de empresas
        const searchEmpresa = document.getElementById('searchEmpresa');
        searchEmpresa.innerHTML = '<option value="">Todas las empresas</option>';
        empresas.forEach(e => {
            const option = document.createElement('option');
            option.value = e.nombreEmpresa;
            option.textContent = e.nombreEmpresa;
            searchEmpresa.appendChild(option);
        });

        // Llenar selector en modal
        const selectEmpresa = document.getElementById('selectEmpresa');
        selectEmpresa.innerHTML = '<option value="">Seleccione empresa...</option>';
        empresas.forEach(e => {
            const option = document.createElement('option');
            option.value = e.idEmpresa;
            option.textContent = e.nombreEmpresa;
            selectEmpresa.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar empresas:", error);
        Swal.fire('Error', 'No se pudieron cargar las empresas', 'error');
    }
}

// =========== CARGAR LIDERES DISPONIBLES ============ 
async function cargarLideresDisponibles() {
    try {
        if (!empresaSeleccionadaId) {
            Swal.fire('Advertencia', 'Selecciona una empresa primero', 'warning');
            return;
        }

        const url = `/api/campanas/empleados/disponibles?rol=LIDER&idEmpresa=${empresaSeleccionadaId}`;
        const response = await fetch(url, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Accept': 'application/json' }
        });

        empleadosDisponibles = await response.json();
        console.log("Líderes disponibles cargados:", empleadosDisponibles);

        // Renderizar opciones en el modal
        renderizarLideresModal();

    } catch (error) {
        console.error("Error cargando líderes", error);
        Swal.fire('Error', 'No se pudieron cargar los líderes disponibles', 'error');
    }
}

// =========== CARGAR AGENTES DISPONIBLES ============ 
async function cargarAgentesDisponibles() {
    try {
        if (!empresaSeleccionadaId) {
            Swal.fire('Advertencia', 'Selecciona una empresa primero', 'warning');
            return;
        }

        const response = await fetch(
            `/api/campanas/empleados/disponibles?rol=AGENTE&idEmpresa=${empresaSeleccionadaId}`
        );

        if (!response.ok) {
            throw new Error("Error al cargar agentes disponibles");
        }

        empleadosDisponibles = await response.json();
        console.log("Agentes disponibles cargados:", empleadosDisponibles);

        // Renderizar opciones en el modal
        renderizarAgentesModal();

    } catch (error) {
        console.error("Error cargando agentes", error);
        Swal.fire('Error', 'No se pudieron cargar agentes disponibles', 'error');
    }
}

// ===========CARGAR AGENTES DISPONIBLES============ 
async function cargarAsignacionesCampana(idCampana) {
    try {
        const response = await fetch(`/api/campanas/${idCampana}/asignaciones`);
        if (!response.ok) throw new Error("Error al cargar asignaciones"); 

        const asignaciones = await response.json();

        const lider = asignaciones.find(a => a.esLider);
        if (lider) {
            liderSeleccionado = {
                id: lider.idEmpleado,
                nombre: `${lider.nombreEmpleado || ''} ${lider.apellidoEmpleado || ''}`.trim()
            };
            document.getElementById('inputLider').value = liderSeleccionado.nombre;
        }

        agentesSeleccionados = asignaciones
            .filter(a => !a.esLider)
            .map(a => ({
                id: a.idEmpleado,
                nombre: `${a.nombreEmpleado || ''} ${a.apellidoEmpleado || ''}`.trim()
            }));

        actualizarVistaAgentes();    

    } catch (error) {
        console.error("Error cargando asignaciones:", error);
        Swal.fire('Error', 'No se pudieron cargar las asignaciones', 'error');
    }
}

// ==================== RENDERIZADO ====================
function renderizarCampanas(campanas) {
    const tbody = document.getElementById('tablaCampanas');
    tbody.innerHTML = '';

    if (!campanas || campanas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No se encontraron campañas</td></tr>';
        return;
    }

    campanas.forEach(c => {
        const puedeEditar = c.estado === 'ACTIVA' || c.estado === 'EN_PROCESO';
        const tr = document.createElement('tr');
        tr.style.cursor = 'pointer';
        tr.innerHTML = `
            <td>${c.idCampana}</td>
            <td>${c.nombreCampana}</td>
            <td>${c.nombreEmpresa}</td>
            <td>${c.descripcion || '-'}</td>
            <td>${c.fechaInicio}</td>
            <td>${c.fechaFin}</td>
            <td>
                <span class="badge ${getBadgeClass(c.estado)}">
                    ${c.estado}
                </span>
            </td>
            <td class="text-nowrap">
                <button class="btn btn-sm btn-custom editar-btn" data-id="${c.idCampana}">
                    <i class="fa-solid fa-pen-to-square"></i>
                </button>
                ${getEstadoBtn(c)}
            </td>
        `;
        
        tr.addEventListener('click', async (e) => {
            if (!e.target.classList.contains('btn')) {
                await mostrarEmpleadosAsignados(c.idCampana);
            }
        })

        tbody.appendChild(tr);
    });
}

async function mostrarEmpleadosAsignados(idCampana) {
    try {
        const response = await fetch(`/api/campanas/${idCampana}/asignaciones`);
        if (!response.ok) throw new Error("Error al cargar asignaciones");

        const asignaciones = await response.json();
        console.log("Asignaciones recibidas:", asignaciones);

        const lider = asignaciones.find(a => a.esLider);
        const agentes = asignaciones.filter(a => !a.esLider);

        let content = '';

        if(lider) {
            const nombreLider = lider.nombreEmpleado || 'Nombre no disponible';
            const apellidoLider = lider.apellidoEmpleado || '';
            const estadoLider = lider.estadoAsignacion || 'DESCONOCIDO';

            content += `
                <div class="mb-3">
                    <h6 class="text-primary mb-2">Líder de la Campaña</h6>
                    <div class="d-flex align-items-center p-3 border rounded bg-light">
                        <div class="flex-grow-1">
                            <div class="fw-semibold">${nombreLider} ${apellidoLider}</div>
                            <div class="text-muted small">ID: ${lider.idEmpleado}</div>
                        </div>
                        <span class="badge ${estadoLider === 'ACTIVA' ? 'bg-success' : 'bg-secondary'}">
                            ${estadoLider}
                        </span>
                    </div>
                </div>
            `;
        }

        if (agentes.length > 0) {
            content += `
                <div class="mb-3">
                    <h6 class="text-primary mb-2">Agentes Asignados (${agentes.length})</h6>
            `;

            agentes.forEach(agente => {
                const nombreAgente = agente.nombreEmpleado || 'Nombre no disponible';
                const apellidoAgente = agente.apellidoEmpleado || '';
                const estadoAgente = agente.estadoAsignacion || 'DESCONOCIDO';
                
                content += `
                    <div class="d-flex align-items-center p-2 border rounded mb-2">
                        <div class="flex-grow-1">
                            <div class="fw-medium">${nombreAgente} ${apellidoAgente}</div>
                            <div class="text-muted small">ID: ${agente.idEmpleado}</div>
                        </div>
                        <span class="badge ${estadoAgente === 'ACTIVA' ? 'bg-success' : 'bg-secondary'}">
                            ${estadoAgente}
                        </span>
                    </div>
                `;
            });

            content += `</div>`;
        }

        if (asignaciones.length === 0) {
            content = '<div class="text-center text-muted py-4"><i class="fas fa-users fa-2x mb-2"></i><p>No hay asignaciones</p></div>';
        }

        Swal.fire({
            title: `<div class="d-flex align-items-center">
                      <i class="fas fa-users me-2 text-primary"></i>
                      <span>Empleados Asignados</span>
                    </div>`,
            html: content,
            width: '600px',
            padding: '1.5rem',
            showCloseButton: true,
            showConfirmButton: false,
            customClass: {
                popup: 'rounded-3',
                title: 'mb-3'
            },
            background: '#ffffff',
            didOpen: () => {
                const popup = document.querySelector('.swal2-popup');
                popup.style.transform = 'scale(0.9)';
                popup.style.opacity = '0';
                popup.style.transition = 'all 0.3s ease';
                
                setTimeout(() => {
                    popup.style.transform = 'scale(1)';
                    popup.style.opacity = '1';
                }, 10);
            }
        });

    } catch (error) {
        console.error("Error:", error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudieron cargar las asignaciones',
            confirmButtonColor: '#23A7C1',
            background: '#edf3f4'
        });
    }
}

function getEstadoBtn(campana) {
    let buttons = '';

    if (campana.estado === 'ACTIVA') {
        buttons = `
            <button class="btn btn-sm btn-custom iniciar-btn mx-1" data-id="${campana.idCampana}">
                <i class="fa-solid fa-play"></i>
            </button>
            <button class="btn btn-sm btn-custom archivar-btn" data-id="${campana.idCampana}">
                <i class="fa-solid fa-box-archive"></i>
            </button>
        `;
    }
    else if (campana.estado === 'EN_PROCESO') {
        buttons = `
            <button class="btn btn-sm btn-custom finalizar-btn mx-1" data-id="${campana.idCampana}">
                <i class="fa-solid fa-flag-checkered"></i>
            </button>
            <button class="btn btn-sm btn-custom cancelar-btn mx-1" data-id="${campana.idCampana}">
                <i class="fa-solid fa-ban"></i>
            </button>
        `;
    }
    else if (campana.estado === 'ARCHIVADA') {
        buttons = `
            <button class="btn btn-sm btn-secondary archivar-btn" data-id="${campana.idCampana}">
                <i class="fa-solid fa-box-archive"></i>
            </button>
        `;
    }

    return buttons;
}

async function cambiarEstadoCampana(idCampana, nuevoEstado) {
    try {
        const response = await fetch(`/api/campanas/${idCampana}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                estado: nuevoEstado,
                liberarEmpleados: ['FINALIZADA', 'CANCELADA'].includes(nuevoEstado)
            })
        });

        if (!response) throw new Error(await response.text());

        Swal.fire('Éxito', `Campaña ${nuevoEstado.toLowerCase()} correctamente`, 'success');
        cargarCampanas();
    } catch (error) {
        console.error("Error cambiando estado:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

// ==================== REPORTE EXCEL ====================
async function generarReporteExcel() {
    try {
        // Mostrar loading en el botón
        const btnExcel = document.getElementById('generarReporteBtn');
        const originalHtml = btnExcel.innerHTML;
        btnExcel.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando...';
        btnExcel.disabled = true;

        // Usar los mismos filtros que en cargarCampanas()
        const filtro = {
            nombreCampana: document.getElementById('searchNombre').value.trim(),
            nombreEmpresa: document.getElementById('searchEmpresa').value.trim(),
            estado: document.getElementById('estadoFilter').value,
            excluirArchivadas: document.getElementById('estadoFilter').value === ''
        };

        // Construir URL con filtros
        const params = new URLSearchParams({
            ...Object.fromEntries(Object.entries(filtro).filter(([_, v]) => v !== '' && v !== undefined))
        });

        // Hacer la petición al endpoint de Excel
        const response = await fetch(`/api/campanas/reporte-excel?${params.toString()}`);

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        // Crear y descargar el archivo
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_campanas_${new Date().toISOString().split('T')[0]}.xlsx`;
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
            cargarCampanas();
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
            cargarCampanas();
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
            cargarCampanas();
        }
    });
    pagination.appendChild(nextLi);
}

// ==================== MODALES ====================
function abrirModal(campana = {}) {
    const form = document.getElementById('campanaForm');

    form.idCampana.value     = campana.idCampana || '';
    form.nombreCampana.value = campana.nombreCampana || '';
    form.descripcion.value   = campana.descripcion || '';
    form.fechaInicio.value   = campana.fechaInicio || '';
    form.fechaFin.value      = campana.fechaFin || '';
    form.idEmpresa.value     = campana.idEmpresa || '';

    empresaSeleccionadaId = campana.idEmpresa;

    new bootstrap.Modal('#campanaModal').show();
}

// ==================== MODALES ====================
async function abrirModalEdicion(idCampana) {
    try {
        const response = await fetch(`/api/campanas/${idCampana}`);
        if (!response.ok) throw new Error('Campaña no encontrada');

        const campana = await response.json();
         const form = document.getElementById('editarCampanaForm');

        // Llenar formulario
        form.idCampana.value = campana.idCampana || '';
        form.nombreCampana.value = campana.nombreCampana || '';
        form.descripcion.value = campana.descripcion || '';
        form.fechaFin.value = campana.fechaFin || '';
        form.idEmpresa.value = campana.idEmpresa || '';

        document.getElementById('empresaDisplay').value = campana.nombreEmpresa || '';

        liderSeleccionadoEditar = null;
        agentesSeleccionadosEditar = [];

        await cargarAsignacionesCampanaEditar(idCampana);

        // Mostrar modal
        new bootstrap.Modal('#editarCampanaModal').show();

    } catch (error) {
        console.error("Error al abrir modal:", error);
        Swal.fire('Error', 'No se pudo cargar la campaña para edición', 'error');
    }
}

async function cargarAsignacionesCampanaEditar(idCampana) {
    try {
        const response = await fetch(`/api/campanas/${idCampana}/asignaciones`);
        if (!response.ok) throw new Error("Error al cargar asignaciones");

        const asignaciones = await response.json();

        const lider = asignaciones.find(a => a.esLider);
        if (lider) {
            liderSeleccionadoEditar = {
                id: lider.idEmpleado,
                nombre: `${lider.nombreEmpleado || ''} ${lider.apellidoEmpleado || ''}`.trim()
            };
            document.getElementById('inputLiderEditar').value = liderSeleccionadoEditar.nombre;
        }

        agentesSeleccionadosEditar = asignaciones
            .filter(a => !a.esLider)
            .map(a => ({
                id: a.idEmpleado,
                nombre: `${a.nombreEmpleado || ''} ${a.apellidoEmpleado || ''}`.trim()
            }));

            actualizarVistaAgentesEditar();

    } catch (error) {
        console.error("Error cargando asignaciones:", error);
        Swal.fire('Error', 'No se pudieron cargar las asignaciones', 'error');
    }
}

function actualizarVistaAgentesEditar() {
    const contador = document.getElementById('contadorAgentesEditar');
    const lista = document.getElementById('listaAgentesEditar');

    contador.textContent = `${agentesSeleccionadosEditar.length} agente(s) seleccionado(s)`;
    lista.innerHTML = agentesSeleccionadosEditar.map(agente => `
        <span class="d-inline-flex align-items-center rounded-pill border px-2 py-1 me-2 mb-1" 
                style="font-size: 0.95em; background-color: #23A7C1; color: white;">
            <span>${agente.nombre}</span>
            <button type="button" class="btn-close btn-close-white btn-close-sm ms-2" 
                onclick="eliminarAgenteEditar(${agente.id})" aria-label="Eliminar"></button>
        </span>
    `).join('');
}

function eliminarAgenteEditar(id) {
    agentesSeleccionadosEditar = agentesSeleccionadosEditar.filter(a =>parseInt(a.id) !== parseInt(id));
    actualizarVistaAgentesEditar();
}

function abrirModalSeleccionEditar(tipo) {
    seleccionActualEditar = tipo;
    const modal = new bootstrap.Modal(document.getElementById('modalSeleccionEditar'));
    const titulo = tipo === 'lider' ? 'Seleccionar Lider' : 'Seleccionar Agentes';
    document.getElementById('modalSeleccionTituloEditar').textContent = titulo;

    const btnConfirmar = document.getElementById('btnConfirmarSeleccionEditar');
    if (tipo === 'lider') {
        btnConfirmar.style.display = 'none';
        cargarEmpleadosporRolEditar('LIDER');
    } else {
        btnConfirmar.style.display = 'block';
        btnConfirmar.onclick = confirmarSeleccionAgentesEditar;
        cargarEmpleadosporRolEditar('AGENTE');
    }

    modal.show();
}

async function cargarEmpleadosporRolEditar(rol) {
    try {
        const form = document.getElementById('editarCampanaForm');
        const idEmpresa = form.idEmpresa.value;

        if (!idEmpresa) {
            Swal.fire({
                icon: 'warning',
                title:'Advertencia', 
                text: 'Primero seleccione una empresa',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1' 
            });
            return;
        }
        const response = await fetch(`/api/campanas/empleados/disponibles?rol=${rol}&idEmpresa=${idEmpresa}&activo=true`);
        if(!response.ok) throw new Error('Error al cargar empleados disponibles');

        empleadosDisponiblesEditar = await response.json();
        console.log(`Empleados ${rol} disponibles:`, empleadosDisponiblesEditar);

        renderizarEmpleadosModalEditar(rol === 'LIDER');
    } catch (error) {
        console.error("Error al cargar empleados:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

function renderizarEmpleadosModalEditar(esParaLider, lista = empleadosDisponiblesEditar) {
    const tbody = document.getElementById('tablaEmpleadosEditar');
    tbody.innerHTML = '';

    if (esParaLider && liderSeleccionadoEditar && liderSeleccionadoEditar.id) {
        const existe = lista.some(e => parseInt(e.idEmpleado) === parseInt(liderSeleccionadoEditar.id));
        if (!existe) {
            lista = [
                ...lista,
                {
                    idEmpleado: liderSeleccionadoEditar.id,
                    nombre: liderSeleccionadoEditar.nombre.split(' ')[0] || '',
                    apellido: liderSeleccionadoEditar.nombre.split(' ').slice(1).join(' ') || '',
                    email: 'N/A'
                }
            ];
        }
    }
    if (!lista || lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">No hay empleados disponibles</td></tr>';
        return;
    }
    lista.forEach(empleado => {
        const tr = document.createElement('tr');

        const estaSeleccionado = esParaLider ? 
            liderSeleccionadoEditar && parseInt(liderSeleccionadoEditar.id) === parseInt(empleado.idEmpleado) : 
            agentesSeleccionadosEditar.some(a => parseInt(a.id) === parseInt(empleado.idEmpleado));
        tr.innerHTML = `
            <td>${empleado.nombre} ${empleado.apellido}</td>
            <td>${empleado.correo || 'N/A'}</td>
            <td>
                ${esParaLider 
                    ? `<button class="btn btn-sm ${estaSeleccionado ? 'btn-success' : 'btn-custom'}" 
                         onclick="seleccionarLiderModalEditar(${empleado.idEmpleado}, '${empleado.nombre} ${empleado.apellido}')">
                        ${estaSeleccionado ? 'Seleccionado' : 'Seleccionar'}
                       </button>`
                    : `<label class="custom-checkbox d-flex justify-content-center align-items-center">
                        <input type="checkbox" 
                            ${estaSeleccionado ? 'checked' : ''}
                            onchange="toggleSeleccionAgenteEditar(${empleado.idEmpleado}, '${empleado.nombre} ${empleado.apellido}', this.checked)">
                        <span></span>
                      </label>`
                }
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function filtrarEmpleadosModalEditar() {
    const termino = document.getElementById('buscarEmpleadosEditar').value.toLowerCase();
    const empleadosFiltrados = empleadosDisponiblesEditar.filter(emp => 
        `${emp.nombre} ${emp.apellido}`.toLowerCase().includes(termino) ||
        (emp.correo || '').toLowerCase().includes(termino)
    );
    renderizarEmpleadosModalEditar(seleccionActualEditar === 'lider', empleadosFiltrados);
}

function seleccionarLiderModalEditar(id, nombre) {
    const numericId = parseInt(id);
    if (!numericId || isNaN(numericId)) {
        Swal.fire('Error', 'El líder seleccionado no es válido', 'error');
        return;
    }
    liderSeleccionadoEditar = {
        id: numericId,
        nombre: nombre
    };
    document.getElementById('inputLiderEditar').value = nombre;
    const modal = bootstrap.Modal.getInstance(document.getElementById('modalSeleccionEditar'));
    if (modal) modal.hide();
}

function toggleSeleccionAgenteEditar(id, nombre, seleccionado) {
    const numericId = parseInt(id);
    if(seleccionado) {
        if (!agentesSeleccionadosEditar.some(a => a.id === numericId)) {
            agentesSeleccionadosEditar.push ({
                id: numericId, 
                nombre: nombre
            });
        }
    } else {
        agentesSeleccionadosEditar = agentesSeleccionadosEditar.filter(a => parseInt(a.id) !== parseInt(numericId));
    }
}

function confirmarSeleccionAgentesEditar() {
    actualizarVistaAgentesEditar();
    bootstrap.Modal.getInstance('#modalSeleccionEditar').hide();
}

async function guardarCambiosEdicion() {
    try {
        const form = document.getElementById('editarCampanaForm');
        const idCampana = form.idCampana.value;

        if (!idCampana) {
            Swal.fire('Error', 'ID de campaña no válido', 'error');
            return;
        }

        // Obtener los datos actuales para completar los campos que no se editan
        const responseCampana = await fetch(`/api/campanas/${idCampana}`);
        if (!responseCampana.ok) throw new Error('Error al obtener campaña');
        const campanaActual = await responseCampana.json();

        if(!liderSeleccionadoEditar || !liderSeleccionadoEditar.id) {
            Swal.fire('Error', 'Debe seleccionar un líder', 'error');
            return;
        }

        if(agentesSeleccionadosEditar.length === 0) {
            Swal.fire('Error', 'Debe seleccionar al menos un agente', 'error');
            return;
        }

        const campanaDTO = {
            idCampana: parseInt(idCampana),
            nombreCampana: form.nombreCampana.value.trim(),
            descripcion: form.descripcion.value.trim(),
            fechaInicio: campanaActual.fechaInicio, // Mantener el valor actual
            fechaFin: form.fechaFin.value,
            idEmpresa: parseInt(form.idEmpresa.value),
            estado: campanaActual.estado, // Mantener el estado actual
            idLider: liderSeleccionadoEditar.id,
            idsAgentes: agentesSeleccionadosEditar.map(a => a.id)
        };

        const asignaciones = [
            { idEmpleado: liderSeleccionadoEditar.id, esLider: true },
            ...agentesSeleccionadosEditar.map(a => ({ idEmpleado: a.id, esLider: false }))
        ];

        const response = await fetch(`/api/campanas/${idCampana}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json', 
                'Accept': 'application/json' 
            },
            body: JSON.stringify({ 
                campana: campanaDTO, 
                asignaciones: asignaciones 
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error al actualizar campaña');
        }

        const result = await response.json();
        console.log('Campaña actualizada:', result);

        bootstrap.Modal.getInstance(document.getElementById('editarCampanaModal')).hide();
        cargarCampanas();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Campaña actualizada correctamente',
            confirmButtonColor: '#23A7C1',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

        liderSeleccionadoEditar = null;
        agentesSeleccionadosEditar = [];

    } catch (error) {
        console.error("Error al actualizar campaña:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

// ==================== MODALES ====================
function abrirModalSeleccion(tipo) {
    seleccionActual = tipo;
    const modal = new bootstrap.Modal(document.getElementById('modalSeleccion'));
    const titulo = tipo === 'lider' ? 'Seleccionar Lider' : 'Seleccionar Agentes';
    document.getElementById('modalSeleccionTitulo').textContent = titulo

    const btnConfirmar = document.getElementById('btnConfirmarSeleccion');
    if (tipo === 'lider') {
        btnConfirmar.style.display = 'none';
        cargarEmpleadosporRol('LIDER');
    } else {
        btnConfirmar.style.display = 'block';
        btnConfirmar.onclick = confirmarSeleccionAgentes;
        cargarEmpleadosporRol('AGENTE');
    }

    modal.show();
}

async function cargarEmpleadosporRol(rol) {
    try {
        if (!empresaSeleccionadaId) {
            Swal.fire({
                icon: 'warning',
                title:'Advertencia', 
                text: 'Primero seleccione una empresa',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1' 
            });
            return;
        }
        
        const response = await fetch(`/api/campanas/empleados/disponibles?rol=${rol}&idEmpresa=${empresaSeleccionadaId}&activo=true`);
        if(!response.ok) throw new Error('Error al cargar empleados disponibles');

        empleadosDisponibles = await response.json();
        console.log(`Empleados ${rol} disponibles:`, empleadosDisponibles);

        renderizarEmpleadosModal(rol === 'LIDER');
    } catch (error) {
        console.error("Error al cargar empleados:", error);
        Swal.fire('Error', error.message, 'error');
    }
}

function renderizarLideresModal() {
    const selectLider = document.getElementById('selectLider');
    if (!selectLider) return;

    selectLider.innerHTML = '<option value="">-- Seleccionar Líder --</option>';

    empleadosDisponibles.forEach(empleado => {
        const option = document.createElement('option');
        option.value = empleado.idEmpleado;
        option.textContent = `${empleado.nombre} ${empleado.apellido} (${empleado.correo})`;
        selectLider.appendChild(option);
    });
}

function renderizarAgentesModal() {
    const selectAgentes = document.getElementById('selectAgentes');
    if (!selectAgentes) return;

    selectAgentes.innerHTML = '<option value="">-- Seleccionar Agentes --</option>';

    empleadosDisponibles.forEach(empleado => {
        const option = document.createElement('option');
        option.value = empleado.idEmpleado;
        option.textContent = `${empleado.nombre} ${empleado.apellido} (${empleado.correo})`;
        selectAgentes.appendChild(option);
    });
}

function renderizarEmpleadosModal(esParaLider, lista = empleadosDisponibles) {
    const tbody = document.getElementById('tablaEmpleados');
    tbody.innerHTML = '';

    if (esParaLider && liderSeleccionado && liderSeleccionado.id) {
        const existe = lista.some(e => parseInt(e.idEmpleado) === parseInt(liderSeleccionado.id));
        if (!existe) {
            lista = [
                ...lista,
                {
                    idEmpleado: liderSeleccionado.id,
                    nombre: liderSeleccionado.nombre.split(' ')[0] || '',
                    apellido: liderSeleccionado.nombre.split(' ').slice(1).join(' ') || '',
                    email: 'N/A'
                }   
            ];
        }
    }
    if (!lista || lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">No hay empleados disponibles</td></tr>';
        return;
    }

    lista.forEach(empleado => {
        const tr = document.createElement('tr');
        
        const estaSeleccionado = esParaLider ? 
            liderSeleccionado && parseInt(liderSeleccionado.id) === parseInt(empleado.idEmpleado) : 
            agentesSeleccionados.some(a => parseInt(a.id) === parseInt(empleado.idEmpleado));

        tr.innerHTML = `
            <td>${empleado.nombre} ${empleado.apellido}</td>
            <td>${empleado.email || empleado.correo || 'N/A'}</td>
            <td>
                ${esParaLider 
                    ? `<button class="btn btn-sm btn-custom ${estaSeleccionado ? 'btn-seleccionado' : ''}" 
                         onclick="seleccionarLiderModal(${empleado.idEmpleado}, '${empleado.nombre} ${empleado.apellido}')">
                        ${estaSeleccionado ? 'Seleccionado' : 'Seleccionar'}
                       </button>`
                    : `<label class="custom-checkbox d-flex justify-content-center align-items-center">
                        <input type="checkbox" 
                            ${estaSeleccionado ? 'checked' : ''}
                            onchange="toggleSeleccionAgente(${empleado.idEmpleado}, '${empleado.nombre} ${empleado.apellido}', this.checked)">
                        <span></span>
                      </label>`
                }
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function seleccionarLiderModal(id, nombre) {
    const numericId = parseInt(id);
    if (!numericId || isNaN(numericId)) {
        Swal.fire('Error', 'El líder seleccionado no es válido', 'error');
        return;
    }
    liderSeleccionado = { 
        id: numericId,
        nombre: nombre
    };
    document.getElementById('inputLider').value = nombre;
    const modal = bootstrap.Modal.getInstance(document.getElementById('modalSeleccion'));
    if (modal) modal.hide();
}

function toggleSeleccionAgente(id, nombre, seleccionado) {
    const numericId = parseInt(id);
    if (!numericId || isNaN(numericId)) {
        Swal.fire('Error', 'El agente seleccionado no es válido', 'error');
        return;
    }
    if (seleccionado) {
        if (!agentesSeleccionados.some(a => a.id === numericId)) {
            agentesSeleccionados.push ({
                id: numericId, 
                nombre: nombre
            });
        }
    } else {
        agentesSeleccionados = agentesSeleccionados.filter(a => a.id !== numericId);
    }
}

function confirmarSeleccionAgentes() {
    actualizarVistaAgentes();
    bootstrap.Modal.getInstance('#modalSeleccion').hide();
}

function actualizarVistaAgentes() {
    const contador = document.getElementById('contadorAgentes');
    const lista = document.getElementById('listaAgentes');

    contador.textContent = `${agentesSeleccionados.length} agente(s) seleccionado(s)`;
    lista.innerHTML = agentesSeleccionados.map(agente => `
        <span class="d-inline-flex align-items-center rounded-pill border px-2 py-1 me-2 mb-1" 
                style="font-size: 0.95em; background-color: #23A7C1; color: white;">
            <span>${agente.nombre}</span>
            <button type="button" class="btn-close btn-close-white btn-close-sm ms-2" 
                onclick="eliminarAgente(${agente.id})" aria-label="Eliminar"></button>
        </span>
    `).join('');
}

function eliminarAgente(id) {
    agentesSeleccionados = agentesSeleccionados.filter(a => parseInt(a.id) !== parseInt(id));
    actualizarVistaAgentes();
}

function filtrarEmpleadosModal() {
    const termino = document.getElementById('buscarEmpleados').value.toLowerCase();
    const empleadosFiltrados = empleadosDisponibles.filter(emp => 
        `${emp.nombre} ${emp.apellido}`.toLowerCase().includes(termino) ||
        (emp.email || emp.correo).toLowerCase().includes(termino)
    );
    renderizarEmpleadosModal(seleccionActual === 'lider', empleadosFiltrados);
}

// ==================== EXPORTAR PDF ====================
// ==================== EXPORTAR PDF ====================
async function generarReportePDF() {
    let btnPdf = document.getElementById('generarPdfBtn');
    
    // Si no encuentra el botón, busca alternativas
    if (!btnPdf) {
        console.warn('No se encontró el botón con id "generarPdfBtn", buscando alternativas...');
        
        // Buscar por texto del botón
        const botones = document.querySelectorAll('.btn');
        for (let boton of botones) {
            if (boton.textContent.includes('PDF') || boton.innerHTML.includes('file-pdf')) {
                btnPdf = boton;
                break;
            }
        }
        
        // Si aún no lo encuentra, usar un botón temporal
        if (!btnPdf) {
            console.warn('Creando botón temporal para la operación...');
            btnPdf = { innerHTML: '', disabled: false };
        }
    }
    
    try {
        // Guardar el HTML original solo si es un elemento real
        const originalHtml = btnPdf.innerHTML || '';
        
        // Mostrar loading solo si es un elemento real del DOM
        if (btnPdf instanceof HTMLElement) {
            btnPdf.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando...';
            btnPdf.disabled = true;
        }

        // Obtener los mismos filtros que en cargarCampanas()
        const filtro = {
            nombreCampana: document.getElementById('searchNombre').value.trim(),
            nombreEmpresa: document.getElementById('searchEmpresa').value.trim(),
            estado: document.getElementById('estadoFilter').value,
            excluirArchivadas: document.getElementById('estadoFilter').value === ''
        };

        // Construir URL con filtros
        const params = new URLSearchParams();
        if (filtro.nombreCampana) params.append('nombreCampana', filtro.nombreCampana);
        if (filtro.nombreEmpresa) params.append('nombreEmpresa', filtro.nombreEmpresa);
        if (filtro.estado && filtro.estado !== '') params.append('estados', filtro.estado);
        if (filtro.excluirArchivadas) params.append('excluirArchivadas', 'true');

        console.log('Solicitando PDF con filtros:', Object.fromEntries(params));

        // Hacer la petición al endpoint de PDF
        const response = await fetch(`/api/campanas/reporte-pdf?${params.toString()}`);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error HTTP ${response.status}: ${errorText}`);
        }

        // Crear y descargar el archivo
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_campanas_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Notificación de éxito
        await Swal.fire({
            icon: 'success',
            title: 'PDF Generado',
            text: 'El reporte PDF se ha descargado correctamente',
            timer: 2000,
            showConfirmButton: false
        });

    } catch (error) {
        console.error("Error al generar PDF:", error);
        await Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo generar el PDF: ' + error.message,
            confirmButtonColor: '#23A7C1'
        });
    } finally {
        // Restaurar el botón solo si es un elemento real del DOM
        if (btnPdf instanceof HTMLElement && originalHtml) {
            btnPdf.innerHTML = originalHtml;
            btnPdf.disabled = false;
        }
    }
}

// ==================== CARGA MASIVA ====================
async function procesarCargaMasiva() {
    const archivoInput = document.getElementById('archivoCargaMasiva');
    const archivo = archivoInput.files[0];
    
    if (!archivo) {
        Swal.fire('Error', 'Por favor selecciona un archivo CSV', 'error');
        return;
    }
    
    // Validar extensión
    if (!archivo.name.toLowerCase().endsWith('.csv')) {
        Swal.fire('Error', 'Solo se permiten archivos CSV', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('archivo', archivo);
    
    try {
        // Mostrar loading
        Swal.fire({
            title: 'Procesando archivo...',
            text: 'Por favor espera mientras se cargan las campañas',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
        
        const response = await fetch('/api/campanas/carga-masiva', {
            method: 'POST',
            body: formData
        });
        
        const resultado = await response.json();
        
        if (!response.ok) {
            throw new Error('Error en la carga masiva');
        }
        
        // Mostrar resultados
        mostrarResultadoCargaMasiva(resultado);
        
        // Limpiar archivo
        archivoInput.value = '';
        
        // Recargar tabla
        cargarCampanas();
        
    } catch (error) {
        console.error('Error en carga masiva:', error);
        Swal.fire('Error', 'Error al procesar el archivo: ' + error.message, 'error');
    }
}

function mostrarResultadoCargaMasiva(resultado) {
    let contenido = `
        <div class="alert alert-success">
            <h6>Resumen de Carga Masiva</h6>
            <p><strong>Total registros:</strong> ${resultado.totalRegistros}</p>
            <p><strong>Éxitos:</strong> ${resultado.exitosos}</p>
            <p><strong>Fallidos:</strong> ${resultado.fallidos}</p>
        </div>
    `;
    
    if (resultado.fallidos > 0 && resultado.errores.length > 0) {
        contenido += `
            <div class="mt-3">
                <h6>Errores encontrados:</h6>
                <div style="max-height: 300px; overflow-y: auto;">
                    <table class="table table-sm table-bordered">
                        <thead>
                            <tr>
                                <th>Línea</th>
                                <th>Error</th>
                                <th>Registro</th>
                            </tr>
                        </thead>
                        <tbody>
        `;
        
        resultado.errores.forEach(error => {
            contenido += `
                <tr>
                    <td>${error.numeroLinea}</td>
                    <td class="text-danger">${error.mensajeError}</td>
                    <td><small>${error.registro || 'N/A'}</small></td>
                </tr>
            `;
        });
        
        contenido += `
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    }
    
    Swal.fire({
        title: 'Carga Masiva Completada',
        html: contenido,
        width: '800px',
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#23A7C1'
    });
}

// Función para descargar plantilla CSV
function descargarPlantillaCSV() {
    const plantilla = `nombre_campana,descripcion,fecha_inicio,fecha_fin,empresa,lider,agentes
Campaña Ejemplo 1,Descripción de ejemplo 1,2024-01-01,2024-12-31,Empresa Ejemplo,lider@empresa.com,agente1@empresa.com,agente2@empresa.com
Campaña Ejemplo 2,Descripción de ejemplo 2,2024-02-01,2024-11-30,Empresa Ejemplo,lider@empresa.com,agente3@empresa.com`;

    const blob = new Blob([plantilla], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'plantilla_campanas.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
}

// ==================== SOFT DELETE (ARCHIVADO) ====================
async function archivarCampana(idCampana) {
    try {
        const result = await Swal.fire({
            title: '¿Archivar campaña?',
            text: "La campaña se marcará como archivada y no aparecerá en los listados por defecto",
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, archivar',
            cancelButtonText: 'Cancelar'
        });

        if (!result.isConfirmed) return;

        const response = await fetch(`/api/campanas/${idCampana}/archivar`, {
            method: 'PUT'
        });

        if (!response.ok) throw new Error('Error al archivar');

        cargarCampanas();
        Swal.fire('Éxito', 'Campaña archivada correctamente', 'success');

    } catch (error) {
        console.error("Error al archivar:", error);
        Swal.fire('Error', 'No se pudo archivar la campaña', 'error');
    }
}

// ==================== FUNCION PARA RESETEAR SELECCIONES ====================
function resetearSelecciones() {
    liderSeleccionado = null;
    agentesSeleccionados = [];
    document.getElementById('inputLider').value = '';
    document.getElementById('listaAgentes').innerHTML = '';
    document.getElementById('contadorAgentes').textContent = '0 agentes seleccionados';
}