package com.film.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TmdbApiUtil {

    @Value("${tmdb.api-key}")
    private String apiKey;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    private final RestTemplate restTemplate;

    public TmdbApiUtil() {
        this.restTemplate = new RestTemplate();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String buildUrl(String path) {
        return baseUrl + path + "?api_key=" + apiKey + "&language=zh-CN";
    }

    public String buildUrl(String path, String extraParams) {
        return baseUrl + path + "?api_key=" + apiKey + "&language=zh-CN&" + extraParams;
    }

    public String getImageUrl(String posterPath, String size) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return imageBaseUrl + "/" + size + posterPath;
    }
}
