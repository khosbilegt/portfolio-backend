package mn.khosbilegt.service.dto;

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
    private final Set<Tag> tags = new HashSet<>();

    public Blog() {
        this.id = UUID.randomUUID().toString();
    }
}
