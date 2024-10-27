package mn.khosbilegt.endpoint.endpoint;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import mn.khosbilegt.endpoint.resource.BlogResource;

@Path("/api")
public class PublicEndpoint {
    @Inject
    BlogResource blogResource;

    @Path("/blog")
    public BlogResource blogResource() {
        return blogResource;
    }
}
