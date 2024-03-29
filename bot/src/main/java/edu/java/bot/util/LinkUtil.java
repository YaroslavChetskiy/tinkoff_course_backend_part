package edu.java.bot.util;

import edu.java.bot.model.entity.Link;
import java.net.URI;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkUtil {

    private static final List<String> SUPPORTED_HOSTNAMES_REGEXES = List.of(
        "(https?://)?(www\\.)?github\\.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/?",
        "(https?://)?(www\\.)?stackoverflow\\.com/questions/(\\d+)(/[^/]*)?"
    );

    public static Link parse(URI url) {
        return new Link(
            url.getScheme(),
            url.getHost(),
            url.getPath(),
            url.getRawQuery(),
            url.getRawFragment()
        );
    }

    public static boolean supports(URI url) {
        return url.getScheme() != null
               && url.getHost() != null
               && url.getPath() != null
               && SUPPORTED_HOSTNAMES_REGEXES.stream()
                   .anyMatch(regex -> url.toString().matches(regex));
    }

}
