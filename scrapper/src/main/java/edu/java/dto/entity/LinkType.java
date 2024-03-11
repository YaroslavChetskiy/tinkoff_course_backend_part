package edu.java.dto.entity;

import java.util.Arrays;

public enum LinkType {

    GITHUB_REPO("https://github.com"),
    STACKOVERFLOW_QUESTION("https://stackoverflow.com"),

    UNKNOWN("Unknown url pattern");

    private final String urlPattern;

    LinkType(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public static LinkType find(String type) {
        return Arrays.stream(values())
            .filter(it -> it.name().equals(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Неверное название типа" + type));
    }

    public static LinkType resolve(String url) {
        if (url.startsWith(GITHUB_REPO.urlPattern)) {
            return GITHUB_REPO;
        } else if (url.startsWith(STACKOVERFLOW_QUESTION.urlPattern)) {
            return STACKOVERFLOW_QUESTION;
        }
        return UNKNOWN;
    }
}
