package mn.khosbilegt.service.blog;

import io.vertx.core.json.JsonObject;

public class BlogContentBlock {
    public enum Type {
        TEXT,
        IMAGE,
        VIDEO,
        GALLERY,
        CODE,
        QUOTE,
        EMBED
    }
    private final Type type;
    private final String content;

    public BlogContentBlock(JsonObject definition) {
        this.type = Type.valueOf(definition.getString("type"));
        this.content = definition.getString("content");
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "BlogContentBlock{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
