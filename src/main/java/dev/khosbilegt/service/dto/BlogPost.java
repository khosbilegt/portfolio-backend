package dev.khosbilegt.service.dto;

import dev.khosbilegt.jooq.generated.tables.records.BlogPostRecord;
import dev.khosbilegt.utilities.Utilities;
import io.vertx.core.json.JsonObject;
import org.jooq.JSONB;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.khosbilegt.jooq.generated.Tables.BLOG_POST;

public class BlogPost {
    private final String id;
    private final String title;
    private final String blurb;
    private final String thumbnail;
    private final String type;
    private final boolean isPublic;
    private final LocalDateTime createDate;
    private final int readDuration;
    private final JsonObject body;
    private final List<String> tags = new ArrayList<>();

    public BlogPost(JsonObject jsonObject) {
        Utilities.validateMissingParams("Create Blog Post", jsonObject, List.of("title", "blurb", "thumbnail", "type", "body"));
        this.id = UUID.randomUUID().toString();
        this.title = jsonObject.getString("title");
        this.blurb = jsonObject.getString("blurb");
        this.thumbnail = jsonObject.getString("thumbnail");
        this.type = jsonObject.getString("type");
        this.body = jsonObject.getJsonObject("body");
        this.isPublic = jsonObject.containsKey("is_public") ? jsonObject.getBoolean("is_public") : false;
        this.readDuration = 10;
        this.createDate = LocalDateTime.now();
        if (jsonObject.containsKey("tags")) {
            for (Object tag : jsonObject.getJsonArray("tags")) {
                tags.add((String) tag);
            }
        }
    }

    public BlogPost(BlogPostRecord record) {
        this.id = record.get(BLOG_POST.BLOG_ID);
        this.title = record.get(BLOG_POST.BLOG_TITLE);
        this.blurb = record.get(BLOG_POST.BLOG_BLURB);
        this.thumbnail = record.get(BLOG_POST.THUMBNAIL_URL);
        this.type = record.get(BLOG_POST.TYPE);
        this.isPublic = record.get(BLOG_POST.IS_PUBLIC);
        this.readDuration = record.get(BLOG_POST.READ_DURATION);
        this.createDate = record.get(BLOG_POST.CREATE_DATE);
        this.tags.addAll(List.of(record.get(BLOG_POST.TAGS)));
        this.body = new JsonObject(record.getBody().data());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBlurb() {
        return blurb;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getType() {
        return type;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public int getReadDuration() {
        return readDuration;
    }

    public JsonObject getBody() {
        return body;
    }

    public List<String> getTags() {
        return tags;
    }

    public BlogPostRecord toRecord() {
        BlogPostRecord postRecord = new BlogPostRecord();
        postRecord.setBlogId(id);
        postRecord.setBlogTitle(title);
        postRecord.setThumbnailUrl(thumbnail);
        postRecord.setBlogBlurb(blurb);
        postRecord.setType(type);
        postRecord.setIsPublic(isPublic);
        postRecord.setCreateDate(createDate);
        postRecord.setLastModifiedDate(createDate);
        postRecord.setReadDuration(readDuration);
        postRecord.setBody(JSONB.valueOf(body.encode()));
        postRecord.setTags(tags.toArray(new String[0]));
        return postRecord;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("blurb", blurb)
                .put("title", title)
                .put("thumbnail", thumbnail)
                .put("is_public", isPublic)
                .put("create_date", createDate)
                .put("type", type)
                .put("tags", tags)
                .put("body", body);
    }
}
