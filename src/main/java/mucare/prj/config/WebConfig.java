package mucare.prj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들링 제외
        // registry
        // .addResourceHandler("/api/**")
        // .addResourceLocations("classpath:/nothing/");

        // 정적 리소스 프로필 사진 업로드
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

}