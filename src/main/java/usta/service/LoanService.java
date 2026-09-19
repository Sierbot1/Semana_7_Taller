package usta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import usta.dto.LoanDTO;
import usta.model.Equipment;
import usta.model.EquipmentRepository;
import usta.model.Loan;
import usta.model.LoanRepository;
import usta.model.Student;
import usta.model.StudentRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LoanService {
    @Inject
    LoanRepository repository;

    @Inject
    EquipmentRepository equipmentRepository;

    @Inject
    StudentRepository studentRepository;

    // Lista todos
    public List<Loan> findAll() {
        return repository.listAllLoans();
    }

    // Lista solo los préstamos sin devolver
    public List<Loan> findActive() {
        return repository.listActive();
    }

    // Busca por id
    public Loan findById(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado"));
    }

    // Préstamos de un estudiante (404 si el estudiante no existe)
    public List<Loan> findByStudent(Long studentId) {
        findStudent(studentId);
        return repository.listByStudent(studentId);
    }

    // Préstamos de un equipo (404 si el equipo no existe)
    public List<Loan> findByEquipment(Long equipmentId) {
        findEquipment(equipmentId);
        return repository.listByEquipment(equipmentId);
    }

    @Transactional
    // Registra el préstamo y descuenta del stock del equipo
    public Loan create(LoanDTO dto) {
        Student student = findStudent(dto.studentId());
        Equipment equipment = findEquipment(dto.equipmentId());

        if (!equipment.hasStock(dto.quantity())) {
            throw new BadRequestException("Stock insuficiente para el equipo: " + equipment.getName()
                    + " (disponibles: " + equipment.getStock() + ")");
        }

        LocalDate loanDate = dto.loanDate() != null ? dto.loanDate() : LocalDate.now();
        Loan loan = new Loan(loanDate, dto.quantity(), equipment, student);

        equipment.decreaseStock(dto.quantity());
        equipmentRepository.persist(equipment);
        repository.persist(loan);
        return loan;
    }

    @Transactional
    // Actualiza un préstamo activo (reajusta el stock de los equipos implicados)
    public Loan update(Long id, LoanDTO dto) {
        Loan existing = findById(id);
        if (existing.isReturned()) {
            throw new BadRequestException("No se puede modificar un préstamo ya devuelto");
        }

        Student student = findStudent(dto.studentId());
        Equipment newEquipment = findEquipment(dto.equipmentId());

        // Devuelve al stock lo que tenía reservado el préstamo actual
        Equipment oldEquipment = existing.getEquipment();
        oldEquipment.increaseStock(existing.getQuantity());
        equipmentRepository.persist(oldEquipment);

        // El equipo puede ser el mismo: se vuelve a leer el stock ya restaurado
        if (!newEquipment.hasStock(dto.quantity())) {
            throw new BadRequestException("Stock insuficiente para el equipo: " + newEquipment.getName()
                    + " (disponibles: " + newEquipment.getStock() + ")");
        }
        newEquipment.decreaseStock(dto.quantity());
        equipmentRepository.persist(newEquipment);

        existing.setStudent(student);
        existing.setEquipment(newEquipment);
        existing.setQuantity(dto.quantity());
        if (dto.loanDate() != null) {
            existing.setLoanDate(dto.loanDate());
        }
        repository.persist(existing);
        return existing;
    }

    @Transactional
    // Registra la devolución: pone la fecha y suma al stock
    public Loan returnLoan(Long id) {
        Loan loan = findById(id);
        if (loan.isReturned()) {
            throw new BadRequestException("El préstamo ya fue devuelto el " + loan.getReturnDate());
        }
        loan.setReturnDate(LocalDate.now());

        Equipment equipment = loan.getEquipment();
        equipment.increaseStock(loan.getQuantity());
        equipmentRepository.persist(equipment);

        repository.persist(loan);
        return loan;
    }

    @Transactional
    // Elimina el préstamo (si seguía activo, devuelve las unidades al stock)
    public void delete(Long id) {
        Loan loan = findById(id);
        if (!loan.isReturned()) {
            Equipment equipment = loan.getEquipment();
            equipment.increaseStock(loan.getQuantity());
            equipmentRepository.persist(equipment);
        }
        repository.delete(loan);
    }

    // Busca el estudiante o avisa con 404
    private Student findStudent(Long studentId) {
        return studentRepository.findByIdOptional(studentId)
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));
    }

    // Busca el equipo o avisa con 404
    private Equipment findEquipment(Long equipmentId) {
        return equipmentRepository.findByIdOptional(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));
    }
}
