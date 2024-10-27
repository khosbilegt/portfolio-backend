package mn.khosbilegt.endpoint.resource;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;

@Dependent
public class BlogResource {
    @GET
    public String get() {
        return "Hello from BlogResource";
    }
}
