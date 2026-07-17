package com.film.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Component
public class TmdbApiUtil {

    @Value("${tmdb.access-token}")
    private String accessToken;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    @Value("${tmdb.proxy-host:}")
    private String proxyHost;

    @Value("${tmdb.proxy-port:0}")
    private int proxyPort;

    private RestTemplate restTemplate;

    public TmdbApiUtil() {}

    public RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(10000);
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            }
            restTemplate = new RestTemplate(factory);
            // 使用 Bearer Token 认证（TMDb 推荐方式）
            ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
                request.getHeaders().setBearerAuth(accessToken);
                return execution.execute(request, body);
            };
            restTemplate.setInterceptors(List.of(authInterceptor));
        }
        return restTemplate;
    }

    public String buildUrl(String path) {
        return baseUrl + path + "?language=zh-CN";
    }

    public String buildUrl(String path, String extraParams) {
        return baseUrl + path + "?language=zh-CN&" + extraParams;
    }

    public String getImageUrl(String posterPath, String size) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return imageBaseUrl + "/" + size + posterPath;
    }
}
