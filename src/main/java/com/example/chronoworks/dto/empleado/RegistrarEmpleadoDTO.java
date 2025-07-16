package com.example.chronoworks.dto.empleado;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class RegistrarEmpleadoDTO {

    @NotBlank(message = "El nombre no puede estar vacio")
    @Size(max = 25, message = "El nombre no puede exceder 25 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacio")
    @Size(max = 25, message = "El apellido no puede exceder 25 caracteres")
    private String apellido;

    @NotBlank(message = "El nombre no puede estar vacio")
    @Email(message = "El formato de correo es invalido")
    @Size(max = 50, message = "El correo no puede exceder 50 caracteres")
    private String correo;

    @NotBlank(message = "El numero de telefono no puede estar vacio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El numero de telefono no puede exceder 10 caracteres")
    private String telefono;

    @NotNull(message = "El ID de turno no puede estar vacio")
    private Integer idTurno;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaIngreso;

    @NotBlank(message = "El usuario no puede estar vacio")
    @Size(min = 4, max = 25, message = "El usuario debe tener entre 4 y 25 caracteres")
    private String usuario;

    @NotBlank(message = "La contraseña no puede estar vacia")
    @Size(min = 8, max = 25, message = "La contraseña debe tener entre 8 y 25 caracteres")
    private String contrasena;

    @NotNull(message = "El ID de rol no puede estar vacio")
    private Integer idRol;
}
