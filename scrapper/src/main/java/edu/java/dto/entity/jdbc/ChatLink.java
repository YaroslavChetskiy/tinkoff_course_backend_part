package edu.java.dto.entity.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLink {

    private Long id;
    private Long chatId;
    private Long linkId;
}
