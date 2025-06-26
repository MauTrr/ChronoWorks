function mostrarContraseña(idcontraseña, idicono) {
    let inputcontraseña = document.getElementById(idcontraseña);
    let icon = document.getElementById(idicono);
    if (inputcontraseña.type == "password" && icon.classList.contains("fa-eye")) {
        inputcontraseña.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash")
    }
    else {
        inputcontraseña.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye")
    }

} 
function expandirObservaciones(selector) {
    document.querySelectorAll(`${selector} .ver-mas`).forEach((button) => {
        button.addEventListener("click", () => {
            const cellText = button.previousElementSibling;
            const isCollapsed = cellText.getAttribute("data-collapsed") === "true";
            cellText.setAttribute("data-collapsed", isCollapsed ? "false" : "true");
            button.textContent = isCollapsed ? "Ver menos" : "Ver más";
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    // Mapeo de vistas a selectores específicos
    const vistaSelectorMap = {
        "listaasignacion-vista": ".celdaobservaciones",
        "listacampaña-vista": ".celdadescripcion",
        "listacontrol-vista": ".celdaobservacion",
        "listatarea-vista": ".celdadetalles",
    };

    // Obtén el ID del body
    const bodyId = document.body.id;

    // Verifica si el ID del body tiene un selector mapeado
    if (vistaSelectorMap[bodyId]) {
        expandirObservaciones(vistaSelectorMap[bodyId]);
    }
});
