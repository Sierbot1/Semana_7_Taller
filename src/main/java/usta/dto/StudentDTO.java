package usta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO inmutable para crear/actualizar estudiantes
public record StudentDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String name,

        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 120, message = "El correo admite máximo 120 caracteres")
        @Email(message = "El correo debe ser válido")
        String email,

        @NotBlank(message = "El programa académico es obligatorio")
        @Size(max = 100, message = "El programa admite máximo 100 caracteres")
        String program
) {}
