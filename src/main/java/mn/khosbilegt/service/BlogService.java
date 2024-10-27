package mn.khosbilegt.service;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import mn.khosbilegt.service.dto.Blog;
import mn.khosbilegt.service.dto.Experience;
import mn.khosbilegt.service.dto.Tag;
import org.jboss.logging.Logger;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static mn.khosbilegt.jooq.generated.Tables.*;

@ApplicationScoped
public class BlogService {
    private final static Logger LOG = Logger.getLogger("PortfolioService");
    private final Map<String, Blog> CACHED_BLOGS = new HashMap<>();
    private final Map<String, Tag> CACHED_TAGS = new HashMap<>();
    private final Map<String, Experience> CACHED_EXPERIENCES = new HashMap<>();
    private final ExecutorService QUERY_THREAD = Executors.newFixedThreadPool(3);
    @Inject
    DSLContext context;

    public void init(@Observes StartupEvent ignored) {
        Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .chain(this::cacheStoredBlogs)
                .chain(this::cacheStoredTags)
                .chain(this::cacheStoredExperiences)
                .subscribe().with(unused -> LOG.infov("Completed initializing [BlogService]..."));
    }

    private Uni<Void> cacheStoredBlogs() {
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> cacheStoredTags() {
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> cacheStoredExperiences() {
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> fetchBlogs() {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(unused -> {
                    context.selectFrom(PF_BLOG)
                            .fetch()
                            .forEach(record -> {
                            });
                });
    }
}
