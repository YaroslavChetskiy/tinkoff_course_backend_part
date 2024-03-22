package edu.java.dto.entity.jdbc;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat {

    private Long id;
    private OffsetDateTime createdAt;
}
