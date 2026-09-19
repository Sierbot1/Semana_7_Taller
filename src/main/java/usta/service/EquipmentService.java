package usta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import usta.dto.EquipmentDTO;
import usta.model.Equipment;
import usta.model.EquipmentRepository;
import usta.model.Loan;
import usta.model.LoanRepository;

import java.util.List;

@ApplicationScoped
public class EquipmentService {
    @Inject
    EquipmentRepository repository;

    @Inject
    LoanRepository loanRepository;

    // Lista todos
    public List<Equipment> findAll() {
        return repository.listAllEquipment();
    }

    // Busca por id
    public Equipment findById(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));
    }

    @Transactional
    // Crea desde DTO (validado con @Valid en el Resource)
    public Equipment create(EquipmentDTO dto) {
        repository.findByCode(dto.code()).ifPresent(e -> {
            throw new BadRequestException("Ya existe un equipo con el código: " + dto.code());
        });
        Equipment equipment = new Equipment(dto.code(), dto.name(), dto.stock());
        repository.persist(equipment);
        return equipment;
    }

    @Transactional
    // Actualiza desde DTO
    public Equipment update(Long id, EquipmentDTO dto) {
        Equipment existing = findById(id);
        repository.findByCode(dto.code()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BadRequestException("Ya existe un equipo con el código: " + dto.code());
            }
        });
        existing.setCode(dto.code());
        existing.setName(dto.name());
        existing.setStock(dto.stock());
        repository.persist(existing);
        return existing;
    }

    @Transactional
    // Elimina por id (no se puede si está prestado; borra primero el historial devuelto)
    public void delete(Long id) {
        Equipment equipment = findById(id);
        if (loanRepository.countActiveByEquipment(id) > 0) {
            throw new BadRequestException("No se puede eliminar el equipo: tiene préstamos activos");
        }
        for (Loan loan : loanRepository.listByEquipment(id)) {
            loanRepository.delete(loan);
        }
        repository.delete(equipment);
    }
}
