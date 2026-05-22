package roomescape.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.interceptor.AdminAuthorizeInterceptor;
import roomescape.auth.interceptor.JwtAuthInterceptor;
import roomescape.auth.interceptor.RequiredAuthInterceptor;
import roomescape.auth.interceptor.SessionAuthInterceptor;
import roomescape.auth.resolver.LoginMemberArgumentResolver;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final RequiredAuthInterceptor requiredAuthInterceptor;
    private final AdminAuthorizeInterceptor adminAuthorizeInterceptor;

    private static final List<String> AUTH_REQUIRED_PATHS = List.of(
            "/reservations",
            "/reservations/**",
            "/store-managers",
            "/store-managers/**",
            "/admin/**"
    );

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns(AUTH_REQUIRED_PATHS);

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(AUTH_REQUIRED_PATHS);

        registry.addInterceptor(requiredAuthInterceptor)
                .addPathPatterns(AUTH_REQUIRED_PATHS);

        registry.addInterceptor(adminAuthorizeInterceptor)
                .addPathPatterns("/admin/**");
    }
}
