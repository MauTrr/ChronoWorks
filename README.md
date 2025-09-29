# ChronoWork

ChronoWork es un sistema web orientado a la **gestión logística de un call center**, permitiendo administrar empleados, empresas, campañas y tareas de manera organizada.  
El objetivo es centralizar la información y mejorar la trazabilidad de los procesos internos.

---

## Tecnologías utilizadas
- **Backend:** Java con Spring Boot + Maven  
- **Frontend:** HTML, CSS, JavaScript  
- **Base de datos:** MySQL (o la que estén usando)  

---

##  Funcionalidades principales
- **Gestión de usuarios:**  
  - Registro de empleados con rol, turno y credenciales de acceso.  
  - Edición y eliminación de usuarios.  
  - Control de acceso mediante login seguro.  

- **Gestión de empresas:**  
  - Creación y administración de empresas con su información.  
  - Edición, consulta y eliminación.  

- **Gestión de campañas:**  
  - Creación de campañas con asignación de líder y múltiples agentes.  
  - Control de estados (activa, en progreso, finalizada, etc.).  
  - Posibilidad de consultar, modificar y eliminar campañas.  

- **Gestión de tareas:**  
  - Asignación de tareas según el rol del usuario.  
  - Control de estados de cada tarea (pendiente, en ejecución, completada).  

---

## Instalación y ejecución
1. Clonar el repositorio:  
   ```bash
   git clone https://github.com/MauTrr/ChronoWork.git
   
2. Abrir el proyecto en un IDE compatible (IntelliJ IDEA, Eclipse, VS Code con extensiones Java).

3. Configurar la base de datos en el archivo application.properties.

4. Ejecutar la aplicación con Spring Boot (mvn spring-boot:run).

5. Acceder desde el navegador en http://localhost:8080/.

Próximamente: despliegue en servidor para acceso público.

## Autores

Mauro Torres (@MauTrr) → Backend (Spring Boot), seguridad, gestión de usuarios, módulo de campañas, exportación de reportes, refactorización y optimización de código.

Nailis Abril (@Nailis7305) → Frontend, ajustes visuales, módulos de empresa/campaña, apoyo en validaciones y pruebas de usabilidad.
