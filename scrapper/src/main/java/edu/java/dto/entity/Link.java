package edu.java.dto.entity;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private Long id;
    private String url;
    private LinkType type;
    private OffsetDateTime checkedAt;
    private OffsetDateTime lastUpdatedAt;
    private OffsetDateTime createdAt;
}
