package mn.khosbilegt.service.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Experience {
    private String id;
    private String title;
    private String icon;
    private String company;
    private String blurb;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private final Set<String> tags = new HashSet<>();
    private final Set<String> projects = new HashSet<>();
}
