// Variables globales
let currentPage = 0;
const pageSize = 10;
let totalPages = 1;
let roles = [];
let turnos = [];

function debounce(func, timeout = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    cargarRoles();
    cargarTurnos();
    cargarEmpleados();

    // Event listeners
    document.getElementById('searchInput').addEventListener('input', () => {
        currentPage = 0;
        cargarEmpleados();
    });

    document.getElementById('rolFilter').addEventListener('change', () => {
        currentPage = 0;
        cargarEmpleados();
    });

    document.getElementById('statusFilter').addEventListener('change', () => {
        currentPage = 0;
        cargarEmpleados();
    });

    document.getElementById('guardarEmpleadoBtn').addEventListener('click', crearEmpleado);
    document.getElementById('actualizarEmpleadoBtn').addEventListener('click', actualizarEmpleado);
      
    const telefonoInput = document.getElementById('telefono');
    if (telefonoInput) {
        telefonoInput.addEventListener('input', function(e){
            this.value = this.value.replace(/\D/g, '');
        });

        telefonoInput.addEventListener('blur', function(e) {
            if (!/^\d{10}$/.test(this.value)) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });

        telefonoInput.addEventListener('paste', function(e) {
            const textoPegado = e.clipboardData.getData('text');
            if (!/^\d+$/.test(textoPegado)) {
                e.preventDefault();
            }
        })
    }

    const telefonoEdicionInput = document.querySelector('#editarEmpleadoModal [name="telefono"]');
    if (telefonoEdicionInput) {
        telefonoEdicionInput.addEventListener('input', function(e){
            this.value = this.value.replace(/\D/g, '');
        });

        telefonoEdicionInput.addEventListener('blur', function(e) {
            if (!/^\d{10}$/.test(this.value)) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });

        telefonoEdicionInput.addEventListener('paste', function(e) {
            const textoPegado = e.clipboardData.getData('text');
            if (!/^\d+$/.test(textoPegado)) {
                e.preventDefault();
            }
        })
    }
});

// Cargar lista de empleados
async function cargarEmpleados() {
    try {
        const search = document.getElementById('searchInput').value.trim();
        const nombreRol = document.getElementById('rolFilter').value.trim();
        const status = document.getElementById('statusFilter').value;

        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', Math.max(pageSize, 1));

        if (search) params.append('search', search);
        if (nombreRol) params.append('nombreRol', nombreRol);
        if (status !== 'all') params.append('activo', status === 'true');

        console.log('Solicitando con params:', params.toString());

        const response = await fetch(`/api/empleados?${params.toString()}`, {
            credentials: 'include'
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const data = await response.json();
        console.log('Datos recibidos:', data);

        // Adaptación para PagedModel
        const empleados = data.content || data._embedded?.respuestaEmpleadoDTOs || [];
        totalPages = data.totalPages || 1;
        const totalElements = data.totalElements || empleados.length;

        renderizarEmpleados(data.content);
        actualizarPaginacion();

    } catch (error) {
        console.error('Error al cargar empleados:', error);
        Swal.fire('Error', 'No se pudieron cargar los empleados', 'error');
    }
}

// Renderizar empleados en la tabla
function renderizarEmpleados(empleados) {
    const tbody = document.getElementById('empleadosTableBody');
    tbody.innerHTML = '';

    if (empleados.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center">No se encontraron empleados</td></tr>`;
        return;
    }

    empleados.forEach(empleado => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${empleado.idEmpleado}</td>
            <td>${empleado.nombre}</td>
            <td>${empleado.apellido}</td>
            <td>${empleado.correo}</td>
            <td>${empleado.telefono}</td>
            <td>${empleado.usuario}</td>
            <td>${empleado.nombreRol}</td>
            <td>${empleado.activo ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inactivo</span>'}</td>
            <td class="action-buttons">
                <button class="btn btn-sm btn-custom editar-btn" data-id="${empleado.idEmpleado}">
                    <i class="fas fa-edit"></i>
                </button>
                ${empleado.activo ? `
                <button class="btn btn-sm btn-custom desactivar-btn" data-id="${empleado.idEmpleado}">
                    <i class="fas fa-user-slash"></i>
                </button>
                ` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });

    // Agregar event listeners a los botones
    document.querySelectorAll('.editar-btn').forEach(btn => {
        btn.addEventListener('click', () => abrirModalEdicion(btn.dataset.id));
    });

    document.querySelectorAll('.desactivar-btn').forEach(btn => {
        btn.addEventListener('click', () => desactivarEmpleado(btn.dataset.id));
    });
}

// Actualizar controles de paginación
function actualizarPaginacion() {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    // Info de paginación
    document.getElementById('paginationInfo').textContent = `Mostrando página ${currentPage + 1} de ${totalPages}`;

    // Botón Anterior
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#">Anterior</a>`;
    prevLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (currentPage > 0) {
            currentPage--;
            cargarEmpleados();
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
            cargarEmpleados();
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
            cargarEmpleados();
        }
    });
    pagination.appendChild(nextLi);
}

// Cargar roles para filtros y formularios
async function cargarRoles() {
    try {
        // Simulación de datos (deberías reemplazar esto con tu llamada real a la API)
        roles = [
            { idRol: 1, nombreRol: 'ADMIN' },
            { idRol: 2, nombreRol: 'LIDER' },
            { idRol: 3, nombreRol: 'AGENTE' }
        ];

        // Llenar filtro de roles
        const rolFilter = document.getElementById('rolFilter');
        roles.forEach(rol => {
            const option = document.createElement('option');
            option.value = rol.nombreRol;
            option.textContent = rol.nombreRol;
            rolFilter.appendChild(option);
        });

        // Llenar selectores en modales
        const rolSelects = document.querySelectorAll('select[name="idRol"]');
        rolSelects.forEach(select => {
            roles.forEach(rol => {
                const option = document.createElement('option');
                option.value = rol.idRol;
                option.textContent = rol.nombreRol;
                select.appendChild(option);
            });
        });

    } catch (error) {
        console.error('Error al cargar roles:', error);
    }
}

// Cargar turnos para formularios
async function cargarTurnos() {
    try {
        turnos = [
            { idTurno: 1, nombre: 'Mañana' },
            { idTurno: 2, nombre: 'Tarde' },
            { idTurno: 3, nombre: 'Noche' }
        ];

        // Llenar selectores en modales
        const turnoSelects = document.querySelectorAll('select[name="idTurno"]');
        turnoSelects.forEach(select => {
            turnos.forEach(turno => {
                const option = document.createElement('option');
                option.value = turno.idTurno;
                option.textContent = turno.nombre;
                select.appendChild(option);
            });
        });

    } catch (error) {
        console.error('Error al cargar turnos:', error);
    }
}

// Crear nuevo empleado
async function crearEmpleado() {
    try {
        const form = document.getElementById('nuevoEmpleadoForm');
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

        const empleadoData = {
            nombre: formData.get('nombre'),
            apellido: formData.get('apellido'),
            correo: formData.get('correo'),
            telefono: formData.get('telefono'),
            usuario: formData.get('usuario'),
            contrasena: formData.get('contrasena'),
            idRol: parseInt(formData.get('idRol')),
            idTurno: parseInt(formData.get('idTurno'))
        };

        const response = await fetch('/api/empleados', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(empleadoData)
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
                throw new Error(responseData.error || 'Error al crear empleado');
            }
            return
        }

        // Cerrar modal y recargar lista
        bootstrap.Modal.getInstance(document.getElementById('nuevoEmpleadoModal')).hide();
        form.reset();
        currentPage = 0;
        cargarEmpleados();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Empleado creado correctamente',
            timer: 2000,
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });

    } catch (error) {
        console.error('Error:', error);
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

//Funcion para validar datos en el front para crear empleado
function validarFormulario(formData) {
    const errores = {}

    //Validaciones de campos requeridos
    if(!formData.get('nombre')?.trim()) errores.nombre = "El nombre es obligatorio";
    if(!formData.get('apellido')?.trim()) errores.apellido = "El apellido es obligatorio";
    if(!formData.get('correo')?.trim()) errores.correo = "El correo es obligatorio";
    if(!formData.get('telefono')?.trim()) errores.telefono = "El telefono es obligatorio";
    if(!formData.get('usuario')?.trim()) errores.usuario = "El Usuario es obligatorio";
    if(!formData.get('contrasena')?.trim()) errores.contrasena = "La contraseña es obligatoria";
    if(!formData.get('idRol')?.trim()) errores.idRol = "Selecciona un Rol";
    if(!formData.get('idTurno')?.trim()) errores.idTurno = "Selecciona un Turno";

    //validaciones de formato
    //Correo erroneo
    const correo = formData.get('correo')?.trim();
    if( correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/. test(correo)) {
        errores.correo = "Ingresa un correo valido"
    }

    //Telefono con cantidad de caracteres incorrecto
    const telefono = formData.get('telefono')?.trim();
    if( telefono && !/^\d{10}$/. test(telefono)) {
        errores.telefono = "El telefono debe tener 10 digitos"
    }

    //Contraseña con numero de caracteres incorrectos
    const contrasena = formData.get('contrasena')?.trim();
    if( contrasena && contrasena.length < 8) {
        errores.contrasena = "La contraseña debe tener al menos 8 caracteres"
    }
    return errores;
}

function mostrarErrores(errores, formId = 'nuevoEmpleadoForm') {
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

  // Abrir modal de edición
async function abrirModalEdicion(idEmpleado) {
    try {

        const form = document.getElementById('editarEmpleadoForm');
        form.querySelectorAll('.error-message').forEach(el => el.remove());
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

        console.log(`Intentando cargar empleado con ID: ${idEmpleado}`);

        const response = await fetch(`/api/empleados/${idEmpleado}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error('Error del servidor:', errorData);
            throw new Error(errorData.message || 'Error al cargar empleado');
        }

        const empleado = await response.json();
        console.log('Datos del empleado recibidos:', empleado);

        // Llenar formulario
        form.querySelector('input[name="idEmpleado"]').value = empleado.idEmpleado;
        form.querySelector('input[name="nombre"]').value = empleado.nombre || '';
        form.querySelector('input[name="apellido"]').value = empleado.apellido || '';
        form.querySelector('input[name="correo"]').value = empleado.correo || '';
        form.querySelector('input[name="telefono"]').value = empleado.telefono || '';
        form.querySelector('input[name="usuario"]').value = empleado.usuario || '';

        // Seleccionar rol y turno correctos
        if (empleado.idRol) {
            form.querySelector('select[name="idRol"]').value = empleado.idRol;
        }
        if (empleado.idTurno) {
            form.querySelector('select[name="idTurno"]').value = empleado.idTurno;
        }

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('editarEmpleadoModal'));
        modal.show();

    } catch (error) {
        console.error('Error al cargar empleado:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || 'No se pudo cargar el empleado para edición',
            footer: 'Verifica la consola para más detalles',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

// Actualizar empleado
async function actualizarEmpleado() {
    try {
        const form = document.getElementById('editarEmpleadoForm');
        const formData = new FormData(form);

        const erroresFront = validarFormularioEdicion(formData);
        if (Object.keys(erroresFront).length > 0) {
            mostrarErrores(erroresFront, 'editarEmpleadoForm');
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor completa todos los campos requeridos',
                footer: 'Revisa que todos los campos estén correctos',
                confirmButtonColor: '#23A7C1',
                background: '#edf3f4',
                iconColor: '#23A7C1'
            });
            return;
        }

        const idEmpleado = formData.get('idEmpleado');
        const empleadoData = {
            nombre: formData.get('nombre'),
            apellido: formData.get('apellido'),
            correo: formData.get('correo'),
            telefono: formData.get('telefono'),
            usuario: formData.get('usuario'),
            idRol: parseInt(formData.get('idRol')),
            idTurno: parseInt(formData.get('idTurno'))
        };

        const response = await fetch(`/api/empleados/${idEmpleado}/actualizar`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(empleadoData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            if (errorData.type === 'VALIDATION_ERROR') {
                mostrarErrores(errorData.errors, 'editarEmpleadoForm');
                Swal.fire({
                    icon: 'error',
                    title: 'Errores de validación',
                    text: 'El servidor encontró problemas con los datos',
                    footer: 'Por favor corrige los campos marcados',
                    background: '#edf3f4',
                    iconColor: '#23A7C1'
                });
            } else {
                throw new Error(errorData.message || 'Error al actualizar empleado');
            }
            return;
        }

        // Cerrar modal y recargar lista
        bootstrap.Modal.getInstance(document.getElementById('editarEmpleadoModal')).hide();
        cargarEmpleados();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Empleado actualizado correctamente',
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

//Validar Formulario de edicion
function validarFormularioEdicion(formData) {
    const errores = {};

    //Validaciones de campos requeridos
    if(!formData.get('nombre')?.trim()) errores.nombre = "El nombre es obligatorio";
    if(!formData.get('apellido')?.trim()) errores.apellido = "El apellido es obligatorio";
    if(!formData.get('correo')?.trim()) errores.correo = "El correo es obligatorio";
    if(!formData.get('telefono')?.trim()) errores.telefono = "El telefono es obligatorio";
    if(!formData.get('usuario')?.trim()) errores.usuario = "El Usuario es obligatorio";
    if(!formData.get('idRol')?.trim()) errores.idRol = "Selecciona un Rol";
    if(!formData.get('idTurno')?.trim()) errores.idTurno = "Selecciona un Turno";

    const correo = formData.get('correo')?.trim();
    if( correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/. test(correo)) {
        errores.correo = "Ingresa un correo valido"
    }

    //Telefono con cantidad de caracteres incorrecto
    const telefono = formData.get('telefono')?.trim();
    if( telefono && !/^\d{10}$/. test(telefono)) {
        errores.telefono = "El telefono debe tener 10 digitos"
    }
    return errores;
}

// Desactivar empleado
async function desactivarEmpleado(idEmpleado) {
    try {
        const result = await Swal.fire({
            title: '¿Desactivar empleado?',
            text: "El empleado ya no podrá acceder al sistema",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#23a7c1',
            cancelButtonColor: '#176e7f',
            background: '#edf3f4',
            confirmButtonText: 'Sí, desactivar',
            cancelButtonText: 'Cancelar',
            iconColor: '#23A7C1'
        });

        if (!result.isConfirmed) return;

        const response = await fetch(`/api/empleados/${idEmpleado}/desactivar`, {
            method: 'PATCH',
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Error al desactivar empleado');

        cargarEmpleados();
        Swal.fire('Éxito', 'Empleado desactivado correctamente', 'success');

    } catch (error) {
        console.error('Error:', error);
        Swal.fire('Error', 'No se pudo desactivar el empleado', 'error');
    }
  }

