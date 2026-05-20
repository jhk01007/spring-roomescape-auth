package roomescape.test_config;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

public class TestWebConfig implements WebMvcConfigurer {

    private final FakeLoginMemberArgumentResolver fakeLoginMemberArgumentResolver;

    public TestWebConfig(FakeLoginMemberArgumentResolver fakeLoginMemberArgumentResolver) {
        this.fakeLoginMemberArgumentResolver = fakeLoginMemberArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(fakeLoginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new AuthInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns(
//                        "/members",
//                        "/auth/login",
//                        "/auth/logout",
//                        "/themes/**",
//                        "/times/**"
//                );
    }


}
