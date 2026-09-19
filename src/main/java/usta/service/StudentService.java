package usta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import usta.dto.StudentDTO;
import usta.model.Loan;
import usta.model.LoanRepository;
import usta.model.Student;
import usta.model.StudentRepository;

import java.util.List;

@ApplicationScoped
public class StudentService {
    @Inject
    StudentRepository repository;

    @Inject
    LoanRepository loanRepository;

    // Lista todos
    public List<Student> findAll() {
        return repository.listAllStudents();
    }

    // Busca por id
    public Student findById(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));
    }

    @Transactional
    // Crea desde DTO
    public Student create(StudentDTO dto) {
        repository.findByEmail(dto.email()).ifPresent(s -> {
            throw new BadRequestException("Ya existe un estudiante con el correo: " + dto.email());
        });
        Student student = new Student(dto.name(), dto.email(), dto.program());
        repository.persist(student);
        return student;
    }

    @Transactional
    // Actualiza desde DTO
    public Student update(Long id, StudentDTO dto) {
        Student existing = findById(id);
        repository.findByEmail(dto.email()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BadRequestException("Ya existe un estudiante con el correo: " + dto.email());
            }
        });
        existing.setName(dto.name());
        existing.setEmail(dto.email());
        existing.setProgram(dto.program());
        repository.persist(existing);
        return existing;
    }

    @Transactional
    // Elimina por id (no se puede si tiene equipos sin devolver)
    public void delete(Long id) {
        Student student = findById(id);
        if (loanRepository.countActiveByStudent(id) > 0) {
            throw new BadRequestException("No se puede eliminar el estudiante: tiene equipos sin devolver");
        }
        for (Loan loan : loanRepository.listByStudent(id)) {
            loanRepository.delete(loan);
        }
        repository.delete(student);
    }
}
