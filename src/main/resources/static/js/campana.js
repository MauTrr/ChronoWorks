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

async function actualizarCampana() {
    try {
        const form = document.getElementById('campanaForm');
        const idCampana = form.idCampana.value;
        // ...validaciones igual que antes...

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
            estado: "ACTIVA", // O el estado actual si lo editas
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

        const accion = idCampana ? actualizarCampana : crearCampana;  

        accion().finally(() => {
        setTimeout(() => {
            this.disabled = false;
            this.innerHTML = 'Guardar';
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
                await cargarLideresDisponibles();
                await cargarAgentesDisponibles();
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

// ===========CARGAR LIDERES DISPONIBLES============ 
async function cargarLideresDisponibles() {
    try {
        if (!empresaSeleccionadaId) return;

        const response = await fetch (`/api/empleados?nombreRol=LIDER&idEmpresa=${empresaSeleccionadaId}&activo=true&size=1000`);
        if (!response.ok) throw new Error ("Error al cargar lideres");

        const data = await response.json();
        empleadosDisponibles = data.content;
        console.log("Lideres cargados:", empleadosDisponibles);

    } catch (error) {
        console.error("Error cargando lideres", error);
        Swal.fire('Error', 'No se pudieron cargar los lideres disponibles', 'error');
    }
}

// ===========CARGAR AGENTES DISPONIBLES============ 
async function cargarAgentesDisponibles() {
    try {
        if (!empresaSeleccionadaId) return;

        const response = await fetch(`/api/empleados?nombreRol=AGENTE&idEmpresa=${empresaSeleccionadaId}&activo=true&size=1000`);
        if (!response.ok) throw new Error("Error al cargar agentes");

        const data = await response.json();
        empleadosDisponibles = data.content || data || [];
        console.log("Agentes cargados:", empleadosDisponibles);
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

        const content = asignaciones.map(a => {
            const nombre = a.nombreEmpleado || 'Nombre no disponible';
            const apellido = a.apellidoEmpleado || '';
            const estado = a.estadoAsignacion || 'DESCONOCIDO';
            
            return `
                <div class="mb-2 p-2 border rounded">
                    <strong>${a.esLider ? '🧑‍💼 Líder' : '👨‍💻 Agente'}:</strong>
                    ${nombre} ${apellido}
                    <span class="badge ${estado === 'ACTIVA' ? 'bg-success' : 'bg-secondary'} ms-2">
                        ${estado}
                    </span>
                </div>
            `;
        }).join('');

        Swal.fire({
            title: 'Empleados Asignados',
            html: content || '<p>No hay asignaciones</p>',
            confirmButtonText: 'Cerrar',
            confirmButtonColor: '#23A7C1',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error("Error:", error);
        Swal.fire('Error', 'No se pudieron cargar las asignaciones', 'error');
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
         const form = document.getElementById('campanaForm');

        // Llenar formulario
        form.idCampana.value = campana.idCampana || '';
        form.nombreCampana.value = campana.nombreCampana || '';
        form.descripcion.value = campana.descripcion || '';
        form.fechaInicio.value = campana.fechaInicio || '';
        form.fechaFin.value = campana.fechaFin || '';
        form.idEmpresa.value = campana.idEmpresa || '';

        empresaSeleccionadaId = campana.idEmpresa;

        await cargarAsignacionesCampana(idCampana);

        // Mostrar modal
        new bootstrap.Modal('#campanaModal').show();

    } catch (error) {
        console.error("Error al abrir modal:", error);
        Swal.fire('Error', 'No se pudo cargar la campaña para edición', 'error');
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
        
        const response = await fetch(`/api/campanas/empleados/disponibles?rol=${rol}&idEmpresa=${empresaSeleccionadaId}&activo=true&size=1000`);
        if(!response.ok) throw new Error('Error al cargar empleados disponibles');

        empleadosDisponibles = await response.json();
        console.log(`Empleados ${rol} disponibles:`, empleadosDisponibles);

        renderizarEmpleadosModal(rol === 'LIDER');
    } catch (error) {
        console.error("Error al cargar empleados:", error);
        Swal.fire('Error', error.message, 'error');
    }
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