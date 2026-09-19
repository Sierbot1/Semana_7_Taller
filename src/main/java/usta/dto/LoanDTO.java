package usta.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// DTO inmutable para registrar/actualizar préstamos
public record LoanDTO(
        @NotNull(message = "El id del estudiante es obligatorio")
        Long studentId,

        @NotNull(message = "El id del equipo es obligatorio")
        Long equipmentId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
        Integer quantity,

        // Opcional: si no se envía, se usa la fecha de hoy
        LocalDate loanDate
) {}
