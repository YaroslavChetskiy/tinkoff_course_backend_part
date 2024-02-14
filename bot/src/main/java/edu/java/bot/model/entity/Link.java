package edu.java.bot.model.entity;

import java.net.URI;
import lombok.SneakyThrows;

public record Link(String protocol, String hostname, String path, String query, String fragment) {

    @SneakyThrows @Override
    public String toString() {
        return new URI(protocol, hostname, path, query, fragment).toString();
    }

}
