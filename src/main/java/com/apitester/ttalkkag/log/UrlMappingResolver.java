package com.apitester.ttalkkag.log;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class UrlMappingResolver {

    private final List<UrlMapping> urlMappings;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public UrlMappingResolver(UrlMappingLoader urlMappingLoader) throws IOException {
        this.urlMappings = urlMappingLoader.loadMappings();
    }

    public Optional<UrlMapping> resolveMapping(HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();

        return urlMappings.stream()
                .filter(mapping -> pathMatcher.match(mapping.getUrl(), requestUrl)
                        && mapping.getMethod().equalsIgnoreCase(requestMethod))
                .findFirst();
    }
}