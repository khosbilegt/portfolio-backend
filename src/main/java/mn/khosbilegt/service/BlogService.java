package mn.khosbilegt.service;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.JsonObject;
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
                .chain(this::cacheStoredTags)
                .chain(this::cacheStoredBlogs)
                .chain(this::cacheStoredExperiences)
                .subscribe().with(unused -> LOG.infov("Completed initializing [BlogService]..."));
    }

    private Uni<Void> cacheStoredTags() {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    context.selectFrom(PF_TAG)
                            .fetch()
                            .forEach(record -> {
                                Tag tag = new Tag(record.getTagId(), record.getTagName(), record.getTagType(), record.getTagColor());
                                CACHED_TAGS.put(tag.getId(), tag);
                            });
                }));
    }

    private Uni<Void> cacheStoredBlogs() {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    context.selectFrom(PF_BLOG)
                            .fetch()
                            .forEach(record -> {
                                Blog blog = new Blog(
                                        record.getBlogId(),
                                        record.getBlogTitle(),
                                        record.getBlogSubtitle(),
                                        record.getBlogThumbnail(),
                                        record.getCreateDate(),
                                        record.getModifiedDate(),
                                        new JsonObject(record.getBlogContent().data())
                                );
                                CACHED_BLOGS.put(blog.getId(), blog);
                                context.selectFrom(PF_BLOG_TAGS)
                                        .where(PF_BLOG_TAGS.BLOG_ID.eq(blog.getId()))
                                        .fetch()
                                        .forEach(tagRecord -> {
                                            Tag tag = CACHED_TAGS.get(tagRecord.getTagId());
                                            if (tag != null) {
                                                blog.addTag(tag);
                                            }
                                        });
                            });
                }));
    }

    private Uni<Void> cacheStoredExperiences() {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    context.selectFrom(PF_EXPERIENCE)
                            .fetch()
                            .forEach(record -> {});
                }));
    }
}
