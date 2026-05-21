package roomescape.auth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.exception.DomainException;
import roomescape.auth.domain.LoginMemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;

import static roomescape.auth.domain.LoginMemberInfo.LOGIN_MEMBER_INFO;
import static roomescape.auth.exception.AuthErrorCode.AUTHENTICATION_ERROR;


@RequiredArgsConstructor
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(roomescape.auth.annotation.LoginMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        LoginMemberInfo loginMemberInfo = (LoginMemberInfo) request.getAttribute(LOGIN_MEMBER_INFO);
        return getMember(loginMemberInfo);
    }

    private Member getMember(LoginMemberInfo loginMemberInfo) {
        if(loginMemberInfo == null) {
            throw new DomainException(AUTHENTICATION_ERROR);
        }

        return memberRepository.findById(loginMemberInfo.id())
                .orElseThrow(() -> new DomainException(AUTHENTICATION_ERROR));
    }
}
