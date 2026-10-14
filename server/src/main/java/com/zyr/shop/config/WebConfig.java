package com.zyr.shop.config;

import com.zyr.shop.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * <p>统一配置：跨域放行、JWT 拦截器、上传文件的静态资源映射</p>
 *
 * @author zyr
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /** JWT 认证拦截器 */
    private final JwtInterceptor jwtInterceptor;

    /** 项目自定义配置（读取上传目录） */
    private final ZyrProperties properties;

    /**
     * 全局跨域配置，方便前后端分离开发联调（管理员 Vue 后台 5174 端口访问后端 8081）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册 JWT 拦截器：拦截 /api 下全部接口，登录、支付异步回调等免鉴权接口除外
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 登录接口免鉴权
                        "/api/auth/login",
                        // 支付宝异步通知靠 SDK 验签，不走 JWT
                        "/api/payment/notify",
                        // 前台商品列表/详情无需登录可浏览（购物车、下单才需登录）
                        "/api/product/list",
                        "/api/product/detail/**"
                );
    }

    /**
     * 将磁盘上传目录映射为可通过 HTTP 访问的静态资源
     * 例如 D:/uploads4/202610/a.pdf 可通过 /uploads/202610/a.pdf 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = properties.getUpload().getDir().replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }

    /**
     * 上传文件访问专用 UTF-8 编码过滤器
     *
     * <p>静态资源默认返回的 Content-Type（如 text/plain）不携带 charset，
     * 中文 Windows 下浏览器会按本地默认编码 GBK 去解码 UTF-8 文件，导致中文乱码。
     * 这里强制 /uploads 响应的字符编码为 UTF-8，
     * 使响应头变为 text/plain;charset=UTF-8，浏览器即可正确解码</p>
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> uploadsUtf8Filter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceResponseEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/uploads/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
