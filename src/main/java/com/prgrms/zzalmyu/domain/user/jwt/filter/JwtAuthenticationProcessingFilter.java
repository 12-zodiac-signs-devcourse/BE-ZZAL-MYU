package com.prgrms.zzalmyu.domain.user.jwt.filter;

import com.prgrms.zzalmyu.core.properties.ErrorCode;
import com.prgrms.zzalmyu.domain.user.application.RedisService;
import com.prgrms.zzalmyu.domain.user.domain.entity.User;
import com.prgrms.zzalmyu.domain.user.exception.UserException;
import com.prgrms.zzalmyu.domain.user.infrastructure.UserJPARepository;
import com.prgrms.zzalmyu.domain.user.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserJPARepository userJPARepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        jwtService.extractRefreshToken(request)
            .ifPresentOrElse(
                refreshToken -> {
                    if(jwtService.isTokenValid(refreshToken)) {
                        checkRefreshTokenAndReissueAccessToken(response, refreshToken);
                    } else {
                        throw new UserException(ErrorCode.SECURITY_INVALID_TOKEN);
                    }
                },
                () -> checkAccessTokenAndAuthentication(request, response, filterChain)
            );
    }

    private void checkRefreshTokenAndReissueAccessToken(HttpServletResponse response,
        String refreshToken) {
        String email = findRefreshTokenAndExtractEmail(refreshToken);
        String reissuedRefreshToken = reissueRefreshToken(email);
        jwtService.sendAccessTokenAndRefreshToken(response, jwtService.createAccessToken(email),
            reissuedRefreshToken);
    }

    private String findRefreshTokenAndExtractEmail(String refreshToken) {
        String email = redisService.getValues(refreshToken);

        if (email.equals("false")) {
            throw new UserException(ErrorCode.SECURITY_INVALID_TOKEN);
        }
        return email;
    }

    private String reissueRefreshToken(String email) {
        String reissuedRefreshToken = jwtService.createRefreshToken();
        redisService.setValues(reissuedRefreshToken, email,
            Duration.ofMillis(jwtService.getRefreshTokenExpirationPeriod()));
        return reissuedRefreshToken;
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain) {
        jwtService.extractAccessToken(request)
            .filter(jwtService::isTokenValid).flatMap(jwtService::extractEmail)
            .flatMap(userJPARepository::findByEmail).ifPresent(this::saveAuthentication);

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw new UserException(ErrorCode.SERVER_ERROR);
        }
    }

    private void saveAuthentication(User myUser) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(myUser, null,
            authoritiesMapper.mapAuthorities(myUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
