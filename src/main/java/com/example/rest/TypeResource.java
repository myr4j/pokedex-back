package com.example.rest;

import com.example.domain.Type;
import com.example.security.Secured;
import com.example.service.TypeService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class TypeResource {

    @Inject
    private TypeService typeService;

    @POST
    public Response createType(Type type) {
        Type created = typeService.createType(type.getName());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public Response getAllTypes() {
        List<Type> types = typeService.findAllTypes();
        return Response.ok(types).build();
    }

    @GET
    @Path("/{id}")
    public Response getTypeById(@PathParam("id") Long id) {
        Type type = typeService.findTypeById(id);
        if (type == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(type).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateType(@PathParam("id") Long id, Type type) {
        Type updated = typeService.updateType(id, type.getName());
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteType(@PathParam("id") Long id) {
        typeService.deleteType(id);
        return Response.noContent().build();
    }
}

