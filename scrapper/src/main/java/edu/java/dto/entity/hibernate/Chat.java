package edu.java.dto.entity.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

// решил разделить реализации сущностей на jdbc/jooq и hibernate, так как
// они отличаются довольно сильно
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "chatLinks")
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "chat", schema = "scrapper_schema")
public class Chat {

    @Id
    private Long id;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "chat")
    private List<ChatLink> chatLinks = new ArrayList<>();
}
