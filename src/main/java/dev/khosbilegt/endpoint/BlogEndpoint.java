package dev.khosbilegt.endpoint;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/blog")
public class BlogEndpoint {
    @Path("/")
    @GET
    public Uni<Response> getBlogs() {
        return Uni.createFrom().item(Response.ok("Hello from Quarkus REST").build());
    }
}
