package cn.sailstack.framework.encrypt.core.filter;

import cn.sailstack.framework.common.pojo.CommonResult;
import cn.sailstack.framework.common.util.object.ObjectUtils;
import cn.sailstack.framework.common.util.servlet.ServletUtils;
import cn.sailstack.framework.encrypt.config.ApiEncryptProperties;
import cn.sailstack.framework.encrypt.core.annotation.ApiEncrypt;
import cn.sailstack.framework.web.config.WebProperties;
import cn.sailstack.framework.web.core.filter.ApiRequestFilter;
import cn.sailstack.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.io.IOException;

import static cn.sailstack.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * API 加密过滤器，处理 {@link ApiEncrypt} 注解。
 *
 * 1. 解密请求参数
 * 2. 加密响应结果
 *
 * 疑问：为什么不使用 SpringMVC 的 RequestBodyAdvice 或 ResponseBodyAdvice 机制呢？
 * 回答：考虑到项目中会记录访问日志、异常日志，以及 HTTP API 签名等场景，最好是全局级、且提前做解析！！！
 *
 * @author 农E链源码
 */
@Slf4j
public class ApiEncryptFilter extends ApiRequestFilter {

    private final ApiEncryptProperties apiEncryptProperties;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final ApiEncryptCipher requestDecryptCipher;
    private final ApiEncryptCipher responseEncryptCipher;

    public ApiEncryptFilter(WebProperties webProperties,
                            ApiEncryptProperties apiEncryptProperties,
                            RequestMappingHandlerMapping requestMappingHandlerMapping,
                            GlobalExceptionHandler globalExceptionHandler) {
        super(webProperties);
        this.apiEncryptProperties = apiEncryptProperties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.globalExceptionHandler = globalExceptionHandler;
        if (StringUtils.equalsIgnoreCase(apiEncryptProperties.getAlgorithm(), "AES")) {
            this.requestDecryptCipher = ApiEncryptCipher.aes(apiEncryptProperties.getRequestKey());
            this.responseEncryptCipher = ApiEncryptCipher.aes(apiEncryptProperties.getResponseKey());
        } else if (StringUtils.equalsIgnoreCase(apiEncryptProperties.getAlgorithm(), "RSA")) {
            this.requestDecryptCipher = ApiEncryptCipher.rsaPrivate(apiEncryptProperties.getRequestKey());
            this.responseEncryptCipher = ApiEncryptCipher.rsaPublic(apiEncryptProperties.getResponseKey());
        } else {
            // 补充说明：如果要支持 SM2、SM4 等算法，可在此处增加对应实现，并添加相应的 Maven 依赖即可。
            throw new IllegalArgumentException("不支持的加密算法：" + apiEncryptProperties.getAlgorithm());
        }
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 获取 @ApiEncrypt 注解
        ApiEncrypt apiEncrypt = getApiEncrypt(request);
        boolean requestEnable = apiEncrypt != null && apiEncrypt.request();
        boolean responseEnable = apiEncrypt != null && apiEncrypt.response();
        String encryptHeader = request.getHeader(apiEncryptProperties.getHeader());
        if (!requestEnable && !responseEnable && StringUtils.isBlank(encryptHeader))  {
            chain.doFilter(request, response);
            return;
        }

        // 1. 解密请求
        if (ObjectUtils.equalsAny(HttpMethod.valueOf(request.getMethod()),
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)) {
            try {
                if (StringUtils.isNotBlank(encryptHeader)) {
                    request = new ApiDecryptRequestWrapper(request, requestDecryptCipher);
                } else if (requestEnable) {
                    throw invalidParamException("请求未包含加密标头，请检查是否正确配置了加密标头");
                }
            } catch (Exception ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }

        // 2. 执行过滤器链
        if (responseEnable) {
            // 特殊：仅包装，最后执行。目的：Response 内容可以被重复读取！！！
            response = new ApiEncryptResponseWrapper(response);
        }
        chain.doFilter(request, response);

        // 3. 加密响应（真正执行）
        if (responseEnable) {
            ((ApiEncryptResponseWrapper) response).encrypt(apiEncryptProperties, responseEncryptCipher);
        }
    }

    /**
     * 获取 @ApiEncrypt 注解
     *
     * @param request 请求
     */
    @SuppressWarnings("PatternVariableCanBeUsed")
    private ApiEncrypt getApiEncrypt(HttpServletRequest request) {
        try {
            // 特殊：兼容 SpringBoot 2.X 版本会报错的问题 https://t.zsxq.com/kqyiB
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }

            // 解析 @ApiEncrypt 注解
            HandlerExecutionChain mappingHandler = requestMappingHandlerMapping.getHandler(request);
            if (mappingHandler == null) {
                return null;
            }
            Object handler = mappingHandler.getHandler();
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                ApiEncrypt annotation = handlerMethod.getMethodAnnotation(ApiEncrypt.class);
                if (annotation == null) {
                    annotation = handlerMethod.getBeanType().getAnnotation(ApiEncrypt.class);
                }
                return annotation;
            }
        } catch (Exception e) {
            log.error("[getApiEncrypt][url({}/{}) 获取 @ApiEncrypt 注解失败]",
                    request.getRequestURI(), request.getMethod(), e);
        }
        return null;
    }

}
