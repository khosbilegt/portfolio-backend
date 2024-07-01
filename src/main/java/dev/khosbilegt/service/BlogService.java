package dev.khosbilegt.service;

import dev.khosbilegt.exception.KnownException;
import dev.khosbilegt.jooq.generated.tables.records.BlogPostRecord;
import dev.khosbilegt.service.dto.BlogPost;
import dev.khosbilegt.utilities.Utilities;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jooq.*;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.jooq.impl.DSL;
import org.postgresql.util.PSQLException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.khosbilegt.jooq.generated.Tables.BLOG_POST;
import static org.jooq.impl.DSL.field;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
@ApplicationScoped
public class BlogService {
    private static final Logger LOG = Logger.getLogger("BlogService");
    @Inject
    DSLContext context;
    private final ExecutorService QUERY_THREAD = Executors.newSingleThreadExecutor();
    private final Set<String> CACHED_TAGS = new HashSet<>();

    public void init(@Observes StartupEvent ignored) {
        queryUniqueTags()
                .onItem().invoke(CACHED_TAGS::addAll)
                .onItem().invoke(unused -> LOG.infov("Completed caching [Tags]: {0}", CACHED_TAGS.size()))
                .subscribe().with(unused -> LOG.infov("Completed caching [Blog Service]..."),
                        throwable -> LOG.errorv(throwable, "Failed to cache [Tags]: {0}", throwable.getMessage()));
    }

    private Uni<List<BlogPostRecord>> queryBlogs(String title, String type, List<String> tags) {
        return Uni.createFrom().item(() -> {
                    SelectConditionStep<BlogPostRecord> firstCondition = context.selectFrom(BLOG_POST)
                            .where(BLOG_POST.BLOG_TITLE.like("%" + title + "%"))
                            .and(BLOG_POST.TYPE.like("%" + type + "%"));
                    if (tags.isEmpty()) {
                        return firstCondition.fetch();
                    } else {
                        return context
                                .fetch("SELECT * FROM blog_post WHERE blog_title LIKE ? AND tags @> ARRAY[?]::varchar[]", "%" + title + "%", tags.toArray(new String[0]))
                                .into(BlogPostRecord.class);
                    }
                })
                .runSubscriptionOn(QUERY_THREAD);
    }

    public Uni<JsonObject> fetchBlogs(String title, String type, String tagString) {
        List<String> tags;
        if (tagString.contains(",")) {
            tags = new ArrayList<>(List.of(tagString.split(",")));
        } else if (!tagString.isEmpty()) {
            tags = new ArrayList<>(List.of(tagString));
        } else {
            tags = new ArrayList<>();
        }
        return queryBlogs(title, type.toUpperCase(), tags)
                .onItem().transform(blogPostRecords -> {
                    JsonArray jsonArray = new JsonArray();
                    for (BlogPostRecord postRecord : blogPostRecords) {
                        jsonArray.add(new BlogPost(postRecord).toJson());
                    }
                    return new JsonObject()
                            .put("data", jsonArray);
                })
                .onFailure().invoke(throwable -> {
                    LOG.errorv(throwable, "ERROR: {0}", throwable.getMessage());
                });
    }

    private Uni<BlogPost> queryBlogById(String id) {
        return Uni.createFrom().item(() -> context.selectFrom(BLOG_POST)
                .where(BLOG_POST.BLOG_ID.eq(id))
                .fetchOptional()
                .map(BlogPost::new)
                .orElseThrow(() -> new KnownException("BLOG_NOT_FOUND")));
    }

    public Uni<Void> createBlogPost(JsonObject jsonObject) {
        return Uni.createFrom().voidItem()
                .onItem().invoke(Unchecked.consumer(unused -> {
                    BlogPost blogPost = new BlogPost(jsonObject);
                    try {
                        context.insertInto(BLOG_POST)
                                .set(blogPost.toRecord())
                                .execute();
                        CACHED_TAGS.addAll(blogPost.getTags());
                    } catch (IntegrityConstraintViolationException e) {
                        if (e.getCause() instanceof PSQLException psqlException) {
                            String constraintName = psqlException.getServerErrorMessage() == null ? "" : psqlException.getServerErrorMessage().getConstraint();
                            if (constraintName != null) {
                                if (constraintName.equals("blog_post_title")) {
                                    throw new KnownException("BLOG_TITLE_ALREADY_EXISTS", psqlException);
                                }
                            }
                        }
                    }
                }))
                .runSubscriptionOn(QUERY_THREAD);
    }

    public Uni<Void> updateBlogPost(JsonObject jsonObject) {
        return Uni.createFrom().voidItem()
                .onItem().transformToUni(unused -> {
                    Utilities.validateMissingParams("Update Blog Post", jsonObject, List.of("id"));
                    return queryBlogById(jsonObject.getString("id"));
                })
                .onItem().invoke(blogPost -> {
                    String[] tagArray;
                    if (jsonObject.containsKey("tags")) {
                        List<String> tags = new ArrayList<>();
                        for (Object tag : jsonObject.getJsonArray("tags")) {
                            tags.add(tag.toString());
                            CACHED_TAGS.add(tags.toString());
                        }
                        tagArray = tags.toArray(new String[0]);
                        CACHED_TAGS.addAll(tags);
                    } else {
                        tagArray = blogPost.getTags().toArray(new String[0]);
                    }
                    context.update(BLOG_POST)
                            .set(BLOG_POST.BLOG_TITLE, jsonObject.containsKey("title") ? jsonObject.getString("title") : blogPost.getTitle())
                            .set(BLOG_POST.BLOG_BLURB, jsonObject.containsKey("blurb") ? jsonObject.getString("blurb") : blogPost.getBlurb())
                            .set(BLOG_POST.THUMBNAIL_URL, jsonObject.containsKey("thumbnail") ? jsonObject.getString("thumbnail") : blogPost.getThumbnail())
                            .set(BLOG_POST.TYPE, jsonObject.containsKey("type") ? jsonObject.getString("type") : blogPost.getType())
                            .set(BLOG_POST.IS_PUBLIC, jsonObject.containsKey("is_public") ? jsonObject.getBoolean("is_public") : blogPost.isPublic())
                            .set(BLOG_POST.READ_DURATION, jsonObject.containsKey("read_duration") ? jsonObject.getInteger("read_duration") : blogPost.getReadDuration())
                            .set(BLOG_POST.TAGS, tagArray)
                            .set(BLOG_POST.BODY, jsonObject.containsKey("body") ? JSONB.valueOf(jsonObject.getJsonObject("body").encode()) : JSONB.valueOf(blogPost.getBody().encode()))
                            .where(BLOG_POST.BLOG_ID.eq(blogPost.getId()))
                            .execute();
                })
                .replaceWithVoid();
    }

    public Uni<List<String>> queryUniqueTags() {
        return Uni.createFrom().item(() -> context
                .fetch("SELECT DISTINCT unnest(tags) AS tag FROM blog_post")
                .into(String.class));
    }

    public Uni<JsonObject> fetchUniqueTags() {
        return Uni.createFrom().item(() -> {
            JsonArray jsonArray = new JsonArray();
            for (String tag : CACHED_TAGS) {
                jsonArray.add(tag);
            }
            return new JsonObject()
                    .put("tags", jsonArray);
        });
    }
}
