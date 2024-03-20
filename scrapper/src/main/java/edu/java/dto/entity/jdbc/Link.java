package edu.java.dto.entity.jdbc;

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
