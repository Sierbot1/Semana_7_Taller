package usta.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import usta.dto.LoanDTO;
import usta.model.Loan;
import usta.service.LoanService;

import java.util.List;

@Path("/api/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanResource {

    @Inject
    LoanService service;

    // Lista todos
    @GET
    public List<Loan> list() {
        return service.findAll();
    }

    // Lista los préstamos sin devolver
    @GET
    @Path("/active")
    public List<Loan> listActive() {
        return service.findActive();
    }

    // Busca por id
    @GET
    @Path("/{id}")
    public Loan get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    // Registra un préstamo (JSON validado, descuenta stock)
    @POST
    public Response create(@Valid LoanDTO dto) {
        Loan created = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // Actualiza un préstamo activo (JSON validado)
    @PUT
    @Path("/{id}")
    public Loan update(@PathParam("id") Long id, @Valid LoanDTO dto) {
        return service.update(id, dto);
    }

    // Registra la devolución del equipo (suma al stock)
    @PUT
    @Path("/{id}/return")
    public Loan returnLoan(@PathParam("id") Long id) {
        return service.returnLoan(id);
    }

    // Elimina por id
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
