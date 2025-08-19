let empresas = [];
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
    cargarEmpresas();

    document.getElementById('searchNombre').addEventListener('input', () => {
        currentPage = 0;
        cargarEmpresas();
    });
    document.getElementById('searchSector').addEventListener('input', () => {
        currentPage = 0;
        cargarEmpresas();
    });
    document.getElementById('estadoFilter').addEventListener('change', () => {
        currentPage = 0;
        cargarEmpresas();
    });

    document.getElementById('guardarEmpresaBtn').addEventListener('click', crearEmpresa);
    document.getElementById('actualizarEmpresaBtn').addEventListener('click', actualizarEmpresa)

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

    const nitInput = document.getElementById('nitEmpresa');
    if (nitInput) {
        nitInput.addEventListener('input', function(e) {
            this.value = this.value
                .replace(/[^\d-]/g, '')
                .replace(/^([\d]{0,9})([\d-]{0,1})([\d]{0,1})/, '$1$2$3')
                .replace(/(\d{9})-?(\d)/, '$1-$2')
                .substring(0,11);
        });

        nitInput.addEventListener('blur', function(e) {
            if(!/^\d{9}-\d$/.test(this.value)) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        })

        nitInput.addEventListener('paste', function(e) {
            const textoPegado = e.clipboardData.getData('text');
            if(!/^[\d-]+$/.test(textoPegado)) {
                e.preventDefault();
            }
        })
    }
});

//Cargar lista de empresas
async function cargarEmpresas() {
    try {
        const nombre = document.getElementById('searchNombre').value;
        const sector = document.getElementById('searchSector').value;
        const estado = document.getElementById('estadoFilter').value;

        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', Math.max(pageSize,1));

        if (nombre) params.append('nombreEmpresa', nombre);
        if (sector) params.append('sector', sector);
        if (estado !== 'all') params.append('activo', estado === 'true');

        const res = await fetch(`/api/empresa?${params.toString()}`);
        const data = await res.json();


        const empresas = data.content || data._embedded?.respuestaEmpresaDTOs || [];
        totalPages = data.totalPages || 1;
        const totalElements = data.totalElements || empresas.length;

        renderizarEmpresas(data.content);
        actualizarPaginacion();
    } catch (error) {
        console.error('Error al cargar empresas: ', error);
        Swal.fire('Error', 'No se pudieron cargar las empresas', 'error')
    }
}

function renderizarEmpresas(empresas) {
    const tbody = document.getElementById('tablaEmpresas');
    tbody.innerHTML = '';

    if (empresas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center">No se encontraron empresas</td></tr>`;
        return;
    }

    empresas.forEach(empresa => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${empresa.idEmpresa}</td>
            <td>${empresa.nombreEmpresa}</td>
            <td>${empresa.nitEmpresa}</td>
            <td>${empresa.direccion}</td>
            <td>${empresa.telefono}</td>
            <td>${empresa.sector}</td>
            <td>${empresa.representante}</td>
            <td>${empresa.activo ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inactivo</span>'}</td>
            <td class="action-buttons">
                <button class="btn btn-sm btn-custom editar-btn" data-id="${empresa.idEmpresa}">
                    <i class="fas fa-edit"></i>
                </button>
                ${empresa.activo ? `
                <button class="btn btn-sm btn-custom desactivar-btn" data-id="${empresa.idEmpresa}">
                    <i class="fas fa-user-slash"></i>
                </button>
                ` : ''}
            </td>
        `;
        console.log('HTML generado para empresa:', empresa.idEmpresa, tr.innerHTML);
        tbody.appendChild(tr);
    });

    document.querySelectorAll('.editar-btn').forEach(btn => {
        btn.addEventListener('click', () => abrirModalEdicion(btn.dataset.id));
    });

    document.querySelectorAll('.desactivar-btn').forEach(btn => {
        btn.addEventListener('click', () => desactivarEmpresa(btn.dataset.id));
    });
}

function actualizarPaginacion() {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    document.getElementById('paginationInfo').textContent = `Mostrando página ${currentPage + 1} de ${totalPages}`;

    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#">Anterior</a>`;
    prevLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (currentPage > 0) {
            currentPage--;
            cargarEmpresas();
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
            cargarEmpresas();
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
            cargarEmpresas();
        }
    });
    pagination.appendChild(nextLi);
}

async function crearEmpresa() {
    try {
        const form = document.getElementById('nuevaEmpresaForm');
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

        const empresaData = {
            nombreEmpresa: formData.get('nombreEmpresa'),
            nitEmpresa: formData.get('nitEmpresa'),
            direccion: formData.get('direccion'),
            telefono: formData.get('telefono'),
            sector: formData.get('sector'),
            representante: formData.get('representante'),
        };

        const response = await fetch('/api/empresa', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(empresaData)
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
                throw new Error(responseData.error || 'Error al crear empresa');
            }
            return
        }

        bootstrap.Modal.getInstance(document.getElementById('nuevaEmpresaModal')).hide();
        form.reset();
        currentPage = 0;
        cargarEmpresas();

        Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Empresa creada correctamente',
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

function validarFormulario(formData) {
    const errores= {}

    if(!formData.get('nombreEmpresa')?.trim()) errores.nombreEmpresa = "El nombre de la empresa es obligatorio";
    if(!formData.get('nitEmpresa')?.trim()) errores.nitEmpresa = "El NIT de la empresa es obligatorio";
    if(!formData.get('direccion')?.trim()) errores.direccion = "La dirección de la empresa es obligatoria";
    if(!formData.get('telefono')?.trim()) errores.telefono = "El telefono de la empresa es obligatorio";
    if(!formData.get('sector')?.trim()) errores.sector = "El sector de la empresa es obligatorio";
    if(!formData.get('representante')?.trim()) errores.representante = "El representante de la empresa es obligatorio";


    //Telefono con cantidad de caracteres incorrecto
    const telefono = formData.get('telefono')?.trim();
    if( telefono && !/^\d{10}$/. test(telefono)) {
        errores.telefono = "El telefono debe tener 10 digitos"
    }

    //Telefono con cantidad de caracteres incorrecto
    const nitEmpresa = formData.get('nitEmpresa')?.trim();
    if( nitEmpresa && !/^\d{9}-\d$/. test(nitEmpresa)) {
        errores.nitEmpresa = "El NIT debe tener formato 000000000-0";
    }
    return errores;
}

function mostrarErrores(errores, formId = 'nuevaEmpresaForm') {
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

async function abrirModalEdicion(idEmpresa) {
    try {
        const form = document.getElementById('editarEmpresaForm');
        form.querySelectorAll('.error-message').forEach(el => el.remove());
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

        const response = await fetch(`/api/empresa/${idEmpresa}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error('Error del servidor:', errorData);
            throw new Error(errorData.message || 'Error al cargar empresa');
        }

        const empresa = await response.json();
        console.log('Datos de la empresa recibidos:', empresa);

        form.querySelector('input[name="idEmpresa"]').value = empresa.idEmpresa;
        form.querySelector('input[name="nombreEmpresa"]').value = empresa.nombreEmpresa;
        form.querySelector('input[name="nitEmpresa"]').value = empresa.nitEmpresa;
        form.querySelector('input[name="direccion"]').value = empresa.direccion;
        form.querySelector('input[name="telefono"]').value = empresa.telefono;
        form.querySelector('input[name="sector"]').value = empresa.sector;
        form.querySelector('input[name="representante"]').value = empresa.representante;

        const modal = new bootstrap.Modal(document.getElementById('editarEmpresaModal'));
        modal.show()

    } catch (error) {
        console.error('Error al cargar empresa:', error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.message || 'No se pudo cargar la empresa para edición',
            footer: 'Verifica la consola para más detalles',
            background: '#edf3f4',
            iconColor: '#23A7C1'
        });
    }
}

async function actualizarEmpresa() {
    try {
        const form = document.getElementById('editarEmpresaForm');
        const formData = new FormData(form);

        const erroresFront = validarFormulario(formData);
        if (Object.keys(erroresFront).length > 0) {
            mostrarErrores(erroresFront, 'editarEmpresaForm');
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

        const idEmpresa = formData.get('idEmpresa');
        const empresaData = {
            nombreEmpresa: formData.get('nombreEmpresa'),
            nitEmpresa: formData.get('nitEmpresa'),
            direccion: formData.get('direccion'),
            telefono: formData.get('telefono'),
            sector: formData.get('sector'),
            representante: formData.get('representante')
        };

        const response = await fetch(`/api/empresa/${idEmpresa}/actualizar`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(empresaData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            if (errorData.type === 'VALIDATION_ERROR') {
                mostrarErrores(errorData.errors, 'editarEmpresaForm');
                Swal.fire({
                    icon: 'error',
                    title: 'Errores de validación',
                    text: 'El servidor encontró problemas con los datos',
                    footer: 'Por favor corrige los campos marcados',
                    background: '#edf3f4',
                    iconColor: '#23A7C1'
                });
            } else {
                throw new Error(errorData.message || 'Error al actualizar empresa');
            }
            return;
        }

        bootstrap.Modal.getInstance(document.getElementById('editarEmpresaModal')).hide();
        cargarEmpresas();

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

async function desactivarEmpresa(idEmpresa) {
    try {
        const result = await Swal.fire({
            title: '¿Desactivar empresa?',
            text: "La empresa ya no puede ser usada otra vez",
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

        const response = await fetch(`/api/empresa/${idEmpresa}/desactivar`, {
            method: 'PATCH',
            credentials: 'include',
        });

        if (!response.ok) throw new Error('Error al desactivar empresa');

        cargarEmpresas();
        Swal.fire('Éxito', 'Empresa desactivado correctamente', 'success');
    } catch (error) {
        console.error('Error:', error);
        Swal.fire('Error', 'No se pudo desactivar la empresa', 'error');
    }
}