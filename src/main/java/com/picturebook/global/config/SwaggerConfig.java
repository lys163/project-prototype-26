package com.picturebook.global.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import com.picturebook.global.response.ErrorResponse;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "Bearer Authentication";

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // 문서 전용 에러 스키마는 컨트롤러에서 참조되지 않으므로 직접 등록한다
        ModelConverters.getInstance().readAll(ErrorResponse.class).forEach(components::addSchemas);

        return new OpenAPI()
                .info(new Info()
                        .title("AI 그림책 제작 플랫폼 API")
                        .description("AI 그림책 제작 플랫폼 백엔드 API 문서")
                        .version("v0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(components);
    }

    /**
     * 4xx·5xx 응답이 성공 스키마(ApiResponseXxx)를 그대로 재사용해 예시에 data가 섞여 나오는 것을 막는다.
     * 컨트롤러에 선언된 @ExampleObject 는 그대로 두고 스키마만 ErrorResponse 로 바꾼다.
     */
    @Bean
    public OperationCustomizer errorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getResponses() == null) {
                return operation;
            }

            operation.getResponses().forEach((statusCode, response) -> {
                if (!isErrorStatus(statusCode)) {
                    return;
                }

                Content content = response.getContent();
                if (content == null || content.isEmpty()) {
                    response.setContent(new Content().addMediaType(
                            MediaType.APPLICATION_JSON_VALUE,
                            new io.swagger.v3.oas.models.media.MediaType().schema(errorSchema())
                    ));
                    return;
                }

                content.values().forEach(mediaType -> mediaType.setSchema(errorSchema()));
            });

            return operation;
        };
    }

    private Schema<?> errorSchema() {
        return new Schema<>().$ref(ERROR_SCHEMA_REF);
    }

    private boolean isErrorStatus(String statusCode) {
        return statusCode.length() == 3
                && (statusCode.charAt(0) == '4' || statusCode.charAt(0) == '5');
    }
}
