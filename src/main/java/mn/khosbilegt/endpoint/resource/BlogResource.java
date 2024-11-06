package mn.khosbilegt.endpoint.resource;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import mn.khosbilegt.service.BlogService;
import mn.khosbilegt.service.dto.Tag;

import java.util.List;

@Dependent
public class BlogResource {
    @Inject
    BlogService blogService;

    @GET
    @Path("/tag")
    public Uni<List<Tag>> fetchTags(@QueryParam("id") @DefaultValue("") String id) {
        return blogService.searchTags(id);
    }
}
