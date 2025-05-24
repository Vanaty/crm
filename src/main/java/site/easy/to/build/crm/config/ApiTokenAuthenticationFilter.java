package site.easy.to.build.crm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import site.easy.to.build.crm.repository.ApiTokenRepository;

import java.io.IOException;

@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenRepository apiTokenRepository;
    private final CrmUserDetails userDetailsService;

    public ApiTokenAuthenticationFilter(ApiTokenRepository apiTokenRepository, CrmUserDetails userDetailsService) {
        this.apiTokenRepository = apiTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            apiTokenRepository.findValidToken(token).ifPresent(apiToken -> {
                if (apiToken.isValid()) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(apiToken.getUser().getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(), null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        }

        chain.doFilter(request, response);
    }
}
