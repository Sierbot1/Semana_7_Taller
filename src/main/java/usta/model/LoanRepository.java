package usta.model;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {
    // Lista todos
    public List<Loan> listAllLoans() {
        return listAll();
    }

    // Busca por id
    public Optional<Loan> findByIdOptional(Long id) {
        return find("id", id).firstResultOptional();
    }

    // Préstamos activos (sin fecha de devolución)
    public List<Loan> listActive() {
        return list("returnDate is null");
    }

    // Préstamos de un estudiante
    public List<Loan> listByStudent(Long studentId) {
        return list("student.id", studentId);
    }

    // Préstamos de un equipo
    public List<Loan> listByEquipment(Long equipmentId) {
        return list("equipment.id", equipmentId);
    }

    // Cuenta préstamos activos de un estudiante (para no borrarlo si tiene equipos)
    public long countActiveByStudent(Long studentId) {
        return count("student.id = ?1 and returnDate is null", studentId);
    }

    // Cuenta préstamos activos de un equipo (para no borrarlo si está prestado)
    public long countActiveByEquipment(Long equipmentId) {
        return count("equipment.id = ?1 and returnDate is null", equipmentId);
    }

    // Guarda
    public Loan save(Loan loan) {
        persist(loan);
        return loan;
    }

    // Elimina por id
    public boolean delete(Long id) {
        return deleteById(id);
    }
}
