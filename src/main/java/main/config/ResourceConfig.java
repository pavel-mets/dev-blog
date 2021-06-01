package main.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ResourceConfig implements WebMvcConfigurer
{
    @Value("${blog.image.upload.path}")
    String uploadPath;

    private List<String> ClasspathResourceLocations = new ArrayList<>();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        ClasspathResourceLocations.add("classpath:/META-INF/resources/");
        ClasspathResourceLocations.add("classpath:/resources/");
        ClasspathResourceLocations.add("classpath:/static/");
        ClasspathResourceLocations.add("file:" + uploadPath + "/");

        registry.addResourceHandler("/**")
                .addResourceLocations(ClasspathResourceLocations.toArray(String[]::new));

        registry.addResourceHandler("/posts/**", "/post/**")
                .addResourceLocations("file:" + uploadPath + "/avatars/");

    }
}
