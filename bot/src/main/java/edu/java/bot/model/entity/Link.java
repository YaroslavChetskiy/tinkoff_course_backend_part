package edu.java.bot.model.entity;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private String protocol;
    private String hostname;
    private String path;
    private String query;
    private String fragment;

    @SneakyThrows @Override
    public String toString() {
        return new URI(protocol, hostname, path, query, fragment).toString();
    }

}
