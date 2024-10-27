package mn.khosbilegt.service.blog;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BlogContent {
    private final List<BlogContentBlock> blocks = new ArrayList<>();

    public BlogContent(JsonObject definition) {
        definition.getJsonArray("blocks").forEach(block -> {
            JsonObject blockJson = (JsonObject) block;
            blocks.add(new BlogContentBlock(blockJson));
        });
    }

    public List<BlogContentBlock> getBlocks() {
        return blocks;
    }
}