package roomescape.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.interceptor.JwtAuthInterceptor;
import roomescape.auth.interceptor.RequiredAuthInterceptor;
import roomescape.auth.interceptor.SessionAuthInterceptor;
import roomescape.auth.resolver.LoginMemberArgumentResolver;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final RequiredAuthInterceptor requiredAuthInterceptor;

    private static final List<String> UI_WHITE_LIST = List.of("/", "/index.html", "/admin", "/admin.html", "/login", "/login.html", "/signup", "/signup.html", "/css/**", "/js/**");
    private static final List<String> API_WHITE_LIST = List.of("/members", "/auth/web/login", "/auth/web/logout", "/themes/**", "/times/**");
    private static final List<String> WHITE_LIST = Stream.concat(UI_WHITE_LIST.stream(), API_WHITE_LIST.stream()).toList();

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);

        registry.addInterceptor(requiredAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}
