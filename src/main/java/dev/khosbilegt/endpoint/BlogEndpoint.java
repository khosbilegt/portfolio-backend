package dev.khosbilegt.endpoint;

import dev.khosbilegt.service.BlogService;
import dev.khosbilegt.utilities.Utilities;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/blog")
public class BlogEndpoint {
    @Inject
    BlogService blogService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getBlogs(@QueryParam("title") @DefaultValue("") String title,
                                  @QueryParam("tags") @DefaultValue("") String tagString) {
        return blogService.fetchBlogs(title, tagString)
                .onItem().transform(Utilities::successResponse);
    }

    @POST
    public Uni<Response> createBlog(JsonObject jsonObject) {
        return blogService.createBlogPost(jsonObject)
                .onItem().transform(unused -> Utilities.successResponse());
    }

    @PATCH
    public Uni<Response> updateBlog(JsonObject jsonObject) {
        return blogService.updateBlogPost(jsonObject)
                .onItem().transform(unused -> Utilities.successResponse());
    }
}
