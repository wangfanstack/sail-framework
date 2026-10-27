package cn.sailstack.framework.common.util.http;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HTTP 工具类
 *
 * @author 农E链源码
 */
public class HttpUtils {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 编码 URL 参数
     *
     * @param value 参数
     * @return 编码后的参数
     */
    public static String encodeUtf8(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 解码 URL 参数（query parameter）
     * 注意：此方法会将 + 解码为空格，适用于 query parameter，不适用于 URL path
     *
     * @see #decodeUrlPath(String)
     * @param value 参数
     * @return 解码后的参数
     */
    public static String decodeUtf8(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * 解码 URL 路径
     * 与 {@link #decodeUtf8(String)} 不同，此方法不会将 + 解码为空格，保持 + 为字面字符
     * 适用于 URL path 部分的解码
     *
     * @param path URL 路径
     * @return 解码后的路径
     */
    public static String decodeUrlPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        // 先将 + 替换为 %2B，避免被 URLDecoder 解码为空格
        String encoded = path.replace("+", "%2B");
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    /**
     * 编码 URL 路径，按路径段编码，保留 / 分隔符
     *
     * @param path URL 路径，例如 20250602/xxx.pdf
     * @return 编码后的路径
     */
    public static String encodeUrlPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        String[] segments = path.split("/", -1);
        StringBuilder result = new StringBuilder(path.length());
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                result.append("/");
            }
            result.append(encodeUrlPathSegment(segments[i]));
        }
        return result.toString();
    }

    /**
     * 编码 URL 路径段
     *
     * @param segment URL 路径段
     * @return 编码后的路径段
     */
    public static String encodeUrlPathSegment(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    public static String removeUrlPathQueryAndFragment(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        int endIndex = path.length();
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            endIndex = queryIndex;
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0 && fragmentIndex < endIndex) {
            endIndex = fragmentIndex;
        }
        return path.substring(0, endIndex);
    }

    /**
     * 替换 URL 中的指定 query 参数：先移除原有同名参数，再添加新参数
     *
     * @param url URL
     * @param key 参数名
     * @param value 参数值
     * @return 替换后的 URL
     */
    public static String replaceUrlQuery(String url, String key, String value) {
        UriComponents components = UriComponentsBuilder.fromUriString(url).build();
        // 复制原有参数（跳过同名参数），统一交给 UriComponentsBuilder 编码构造
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        components.getQueryParams().forEach((name, values) -> {
            if (!name.equals(key)) {
                values.forEach(v -> params.add(name, v));
            }
        });
        params.add(key, value);
        return UriComponentsBuilder.fromUriString(url)
                .queryParams(params)
                .build()
                .toUriString();
    }

    /**
     * 移除 URL 中的 query、fragment 部分
     *
     * @param url URL
     * @return 移除后的 URL
     */
    public static String removeUrlQuery(String url) {
        if (!StringUtils.contains(url, "?")) {
            return url;
        }
        return UriComponentsBuilder.fromUriString(url)
                .query(null)
                .fragment(null)
                .build().toUriString();
    }

    /**
     * 拼接 URL
     *
     * copy from Spring Security OAuth2 的 AuthorizationEndpoint 类的 append 方法
     *
     * @param base 基础 URL
     * @param query 查询参数
     * @param keys query 的 key，对应的原本的 key 的映射。例如说 query 里有个 key 是 xx，实际它的 key 是 extra_xx，则通过 keys 里添加这个映射
     * @param fragment URL 的 fragment，即拼接到 # 中
     * @return 拼接后的 URL
     */
    public static String append(String base, Map<String, ?> query, Map<String, String> keys, boolean fragment) {
        UriComponentsBuilder template = UriComponentsBuilder.newInstance();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base);
        URI redirectUri;
        try {
            // assume it's encoded to start with (if it came in over the wire)
            redirectUri = builder.build(true).toUri();
        } catch (Exception e) {
            // ... but allow client registrations to contain hard-coded non-encoded values
            redirectUri = builder.build().toUri();
            builder = UriComponentsBuilder.fromUri(redirectUri);
        }
        template.scheme(redirectUri.getScheme()).port(redirectUri.getPort()).host(redirectUri.getHost())
                .userInfo(redirectUri.getUserInfo()).path(redirectUri.getPath());

        // 生成 name={key} 模板片段，之后统一交给 UriComponentsBuilder 的 expand 展开
        List<String> templates = new ArrayList<>();
        for (String key : query.keySet()) {
            String name = key;
            if (keys != null && keys.containsKey(key)) {
                name = keys.get(key);
            }
            templates.add(String.format("%s={%s}", name, key));
        }

        if (fragment) {
            String existingFragment = redirectUri.getFragment();
            String joined = String.join("&", templates);
            String fragmentValue = existingFragment == null || existingFragment.isEmpty()
                    ? joined : String.join("&", existingFragment, joined);
            if (!fragmentValue.isEmpty()) {
                template.fragment(fragmentValue);
            }
            UriComponents encoded = template.build().expand(query).encode();
            builder.fragment(encoded.getFragment());
        } else {
            templates.forEach(template::queryParam);
            template.fragment(redirectUri.getFragment());
            UriComponents encoded = template.build().expand(query).encode();
            builder.query(encoded.getQuery());
        }
        return builder.build().toUriString();
    }

    /**
     * 从请求中解析 Basic Authorization 的 clientId、clientSecret
     *
     * @param request 请求
     * @return [clientId, clientSecret]；不存在时返回 null
     */
    public static String[] obtainBasicAuthorization(HttpServletRequest request) {
        String clientId;
        String clientSecret;
        // 先从 Header 中获取
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, "Basic ".length())) {
            authorization = authorization.substring("Basic ".length());
            authorization = new String(Base64.getDecoder().decode(authorization), StandardCharsets.UTF_8);
            clientId = StringUtils.substringBefore(authorization, ":");
            clientSecret = StringUtils.substringAfter(authorization, ":");
            // 再从 Param 中获取
        } else {
            clientId = request.getParameter("client_id");
            clientSecret = request.getParameter("client_secret");
        }

        // 如果两者非空，则返回
        if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
            return new String[]{clientId, clientSecret};
        }
        return null;
    }

    /**
     * HTTP post 请求，基于 JDK HttpClient 实现
     *
     * @param url URL
     * @param headers 请求头
     * @param requestBody 请求体
     * @return 请求结果
     */
    public static String post(String url, Map<String, String> headers, String requestBody) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
        headers.forEach(requestBuilder::header);
        return sendRequest(requestBuilder.build());
    }

    /**
     * HTTP get 请求，基于 JDK HttpClient 实现
     *
     * @param url URL
     * @param headers 请求头
     * @return 请求结果
     */
    public static String get(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url)).GET();
        headers.forEach(requestBuilder::header);
        return sendRequest(requestBuilder.build());
    }

    /**
     * 发送 HTTP 请求并返回响应体
     */
    private static String sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * WebSocket URL 切换成 HTTP URL：ws:// → http://；wss:// → https://；其它格式原样保留
     *
     * @param url 原始 URL
     * @return 切换协议后的 URL
     */
    public static String wsUrlToHttp(String url) {
        return StringUtils.startsWithIgnoreCase(url, "ws") ? "http" + url.substring(2) : url;
    }

}
