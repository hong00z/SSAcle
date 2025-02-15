package S12P11D110.ssacle.global.config;

import S12P11D110.ssacle.global.OctetStreamReadMsgConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String imageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imageDir+ "/");
    }

    private OctetStreamReadMsgConverter octetStreamReadMsgConverter;

    @Autowired
    public WebMvcConfig(OctetStreamReadMsgConverter octetStreamReadMsgConverter) {
        this.octetStreamReadMsgConverter = octetStreamReadMsgConverter;
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // application/octet-stream을 처리할 수 있도록 ByteArrayHttpMessageConverter 추가
        converters.add(new ByteArrayHttpMessageConverter());
    }
}
