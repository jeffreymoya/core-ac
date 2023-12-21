package com.synpulse8.pulse8.core.accesscontrolsvc;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Map;

public class ResourceTest {
    @Test
    void testEcho() {
        String route = "/echo";
        UriTemplate uriTemplate = new UriTemplate("/{resourceType}{/?}{resourceId:.*}");
        Map<String, String> matches = uriTemplate.match(route);
        Assertions.assertEquals("echo", matches.get("resourceType"));
        Assertions.assertTrue(StringUtils.isEmpty(matches.get("resourceId")));
    }

    @Test
    void testEchoWithParams() {
        String route = "/echo/someParam";
        UriTemplate uriTemplate = new UriTemplate("/{resourceType}{/?}{resourceId:.*}");
        Map<String, String> matches = uriTemplate.match(route);
        Assertions.assertEquals("echo", matches.get("resourceType"));
        Assertions.assertEquals("/someParam", matches.get("resourceId"));
    }

    @Test
    void testEchoWithQueryParams() {
        String route = "/echo/someParam?query=1&query2=2";
        URI uri = UriComponentsBuilder.fromUriString(route).build().toUri();
        UriTemplate uriTemplate = new UriTemplate("/{resourceType}/{resourceId}");
        Map<String, String> matches = uriTemplate.match(uri.getPath());
        Assertions.assertEquals("echo", matches.get("resourceType"));
        Assertions.assertEquals("someParam", matches.get("resourceId"));
    }
}
