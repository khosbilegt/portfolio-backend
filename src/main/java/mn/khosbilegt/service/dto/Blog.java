package mn.khosbilegt.service.dto;

import io.vertx.core.json.JsonObject;
import mn.khosbilegt.service.blog.BlogContent;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Blog {
    private String id;
    private String title;
    private String subtitle;
    private String thumbnail;
    private LocalDateTime createDate;
    private LocalDateTime modifiedDate;
    private BlogContent content;
    private final Set<Tag> tags = new HashSet<>();

    public Blog() {
        this.id = UUID.randomUUID().toString();
    }

    public Blog(String id, String title, String subtitle, String thumbnail, LocalDateTime createDate, LocalDateTime modifiedDate, JsonObject contentDefinition) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.thumbnail = thumbnail;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
        this.content = new BlogContent(contentDefinition);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public BlogContent getContent() {
        return content;
    }

    public void setContent(JsonObject contentDefinition) {
        this.content = new BlogContent(contentDefinition);
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }
}
