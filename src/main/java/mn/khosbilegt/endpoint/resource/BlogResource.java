package mn.khosbilegt.endpoint.resource;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import mn.khosbilegt.service.BlogService;
import mn.khosbilegt.service.blog.dto.Tag;
import mn.khosbilegt.util.Utilities;

import java.util.List;

@Dependent
public class BlogResource {
    @Inject
    BlogService blogService;

    @GET
    @Path("/tag")
    public Uni<List<Tag>> fetchTags(@QueryParam("id") @DefaultValue("") String id,
                                    @QueryParam("name") @DefaultValue("") String name,
                                    @QueryParam("type") @DefaultValue("") String type) {
        return blogService.searchTags(id, name, type);
    }

    @POST
    @Path("/tag")
    public Uni<JsonObject> createTag(Tag tag) {
        return blogService.createTag(tag)
                .map(unused -> Utilities.createSuccessResponse());
    }

    @PATCH
    @Path("/tag/{id}")
    public Uni<JsonObject> updateTag(@PathParam("id") String id, Tag tag) {
        return blogService.updateTag(id, tag)
                .map(unused -> Utilities.createSuccessResponse());
    }

    @DELETE
    @Path("/tag")
    public Uni<JsonObject> deleteTag(@QueryParam("id") @DefaultValue("") String id) {
        return blogService.deleteTag(id)
                .map(unused -> Utilities.createSuccessResponse());
    }
}
