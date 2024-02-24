package edu.java.bot.model.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// временная заглушка, в дальнейшем будет усовершенствована и перенесена в scrapper
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChat {

    private Long id;

    @Builder.Default
    private List<Link> links = new ArrayList<>();
}
