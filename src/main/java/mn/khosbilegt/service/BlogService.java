package mn.khosbilegt.service;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import mn.khosbilegt.service.dto.Blog;
import mn.khosbilegt.service.dto.Experience;
import mn.khosbilegt.service.dto.Tag;
import org.jboss.logging.Logger;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public Uni<List<Tag>> searchTags(String id) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .map(Unchecked.function(unused -> {
                    if (id.isEmpty()) {
                        return new ArrayList<>(CACHED_TAGS.values());
                    } else if (CACHED_TAGS.containsKey(id)) {
                        return List.of(CACHED_TAGS.get(id));
                    } else {
                        throw new NotFoundException("Tag not found: " + id);
                    }
                }));
    }

    public Uni<Void> createTag(Tag tag) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    context.insertInto(PF_TAG)
                            .set(PF_TAG.TAG_ID, tag.getId())
                            .set(PF_TAG.TAG_NAME, tag.getName())
                            .set(PF_TAG.TAG_TYPE, tag.getType())
                            .set(PF_TAG.TAG_COLOR, tag.getColor())
                            .execute();
                    CACHED_TAGS.put(tag.getId(), tag);
                }));
    }

    public Uni<Void> updateTag(Tag tag) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    if (CACHED_TAGS.containsKey(tag.getId())) {
                        context.update(PF_TAG)
                                .set(PF_TAG.TAG_NAME, tag.getName())
                                .set(PF_TAG.TAG_TYPE, tag.getType())
                                .set(PF_TAG.TAG_COLOR, tag.getColor())
                                .where(PF_TAG.TAG_ID.eq(tag.getId()))
                                .execute();
                        CACHED_TAGS.put(tag.getId(), tag);
                    } else {
                        throw new NotFoundException("Tag not found: " + tag.getId());
                    }
                }));
    }

    public Uni<Void> deleteTag(String id) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    if (CACHED_TAGS.containsKey(id)) {
                        context.deleteFrom(PF_TAG)
                                .where(PF_TAG.TAG_ID.eq(id))
                                .execute();
                        context.deleteFrom(PF_BLOG_TAGS)
                                .where(PF_BLOG_TAGS.TAG_ID.eq(id))
                                .execute();
                        CACHED_TAGS.remove(id);
                    } else {
                        throw new NotFoundException("Tag not found: " + id);
                    }
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

    public Uni<List<Blog>> searchBlogs(String blogName) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .map(unused -> {
                    if (blogName.isEmpty()) {
                        return new ArrayList<>(CACHED_BLOGS.values());
                    } else {
                        List<Blog> blogs = new ArrayList<>();
                        CACHED_BLOGS.values().forEach(blog -> {
                            if (blog.getTitle().contains(blogName)) {
                                blogs.add(blog);
                            }
                        });
                        return blogs;
                    }
                });
    }

    public Uni<Void> updateBlog(Blog blog) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    if (CACHED_BLOGS.containsKey(blog.getId())) {
                        context.update(PF_BLOG)
                                .set(PF_BLOG.BLOG_TITLE, blog.getTitle())
                                .set(PF_BLOG.BLOG_SUBTITLE, blog.getSubtitle())
                                .set(PF_BLOG.BLOG_THUMBNAIL, blog.getThumbnail())
                                .set(PF_BLOG.CREATE_DATE, blog.getCreateDate())
                                .set(PF_BLOG.MODIFIED_DATE, blog.getModifiedDate())
                                .set(PF_BLOG.BLOG_CONTENT, JSONB.jsonb(blog.getContent().toJson().encode()))
                                .where(PF_BLOG.BLOG_ID.eq(blog.getId()))
                                .execute();
                        context.deleteFrom(PF_BLOG_TAGS)
                                .where(PF_BLOG_TAGS.BLOG_ID.eq(blog.getId()))
                                .execute();
                        blog.getTags().forEach(tag -> {
                            context.insertInto(PF_BLOG_TAGS)
                                    .set(PF_BLOG_TAGS.BLOG_ID, blog.getId())
                                    .set(PF_BLOG_TAGS.TAG_ID, tag.getId())
                                    .execute();
                        });
                        CACHED_BLOGS.put(blog.getId(), blog);
                    } else {
                        throw new NotFoundException("Blog not found: " + blog.getId());
                    }
                }));
    }

    public Uni<Void> deleteBlog(String id) {
        return Uni.createFrom().voidItem()
                .emitOn(QUERY_THREAD)
                .invoke(Unchecked.consumer(unused -> {
                    if (CACHED_BLOGS.containsKey(id)) {
                        context.deleteFrom(PF_BLOG)
                                .where(PF_BLOG.BLOG_ID.eq(id))
                                .execute();
                        context.deleteFrom(PF_BLOG_TAGS)
                                .where(PF_BLOG_TAGS.BLOG_ID.eq(id))
                                .execute();
                        CACHED_BLOGS.remove(id);
                    } else {
                        throw new NotFoundException("Blog not found: " + id);
                    }
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
