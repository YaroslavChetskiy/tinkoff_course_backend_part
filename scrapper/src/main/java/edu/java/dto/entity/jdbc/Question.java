package edu.java.dto.entity.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {

    private Long id;
    private Integer answerCount;
    private Long linkId;
}
