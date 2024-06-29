package dev.khosbilegt.service;

import dev.khosbilegt.jooq.generated.tables.records.BlogPostRecord;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

import java.util.List;

import static dev.khosbilegt.jooq.generated.Tables.*;

@ApplicationScoped
public class BlogService {
    @Inject
    DSLContext context;

    public Uni<BlogPostRecord> queryBlogs(String title, List<String> tags) {
        return Uni.createFrom().item(null);
//        return Uni.createFrom().item(context.selectFrom(BLOG_POST)
//                .where(BLOG_POST.BLOG_TITLE.eq(name))
//                .fetchOne());

    }
}
