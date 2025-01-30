package S12P11D110.ssacle.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration // 스프링 실행시 설정 파일 일어드리기 위한 어노테이션
//@EnableWebMvc //Spring MVC를 사용하는 애플리케이션에 필요한 설정을 활성화

public class SwaggerConfig {

    @Bean
    //OpenAPI 객체를 생성하여 설정합니다. OpenAPI 객체는 API 문서의 전체적인 구조를 나타냅
    public OpenAPI openAPI(){
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());


    }
    //API 문서의 정보를 설정하기 위한 Info 객체 생성
    private Info apiInfo(){
        return new Info()
                .title("API Title")
                .description("This is my Swagger UI")
                .version("1.0.0");
    }

}
