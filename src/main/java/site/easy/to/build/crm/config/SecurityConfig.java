package site.easy.to.build.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import site.easy.to.build.crm.config.oauth2.CustomOAuth2UserService;
import site.easy.to.build.crm.config.oauth2.OAuthAPILoginSuccessHandler;
import site.easy.to.build.crm.config.oauth2.OAuthLoginSuccessHandler;
import site.easy.to.build.crm.service.user.OAuthUserService;
import site.easy.to.build.crm.util.StringUtils;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuthAPILoginSuccessHandler oAuth2APILoginSuccessHandler;
    private final CustomOAuth2UserService oauthUserService;
    private final CrmUserDetails crmUserDetails;
    private final CustomerUserDetails customerUserDetails;
    private final Environment environment;
    private final ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;

    @Autowired
    public SecurityConfig(OAuthLoginSuccessHandler oAuth2LoginSuccessHandler, 
                        CustomOAuth2UserService oauthUserService, 
                        CrmUserDetails crmUserDetails,
                        CustomerUserDetails customerUserDetails, 
                        Environment environment,
                        ApiTokenAuthenticationFilter apiTokenAuthenticationFilter,
                        OAuthAPILoginSuccessHandler l) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.oauthUserService = oauthUserService;
        this.crmUserDetails = crmUserDetails;
        this.customerUserDetails = customerUserDetails;
        this.environment = environment;
        this.apiTokenAuthenticationFilter = apiTokenAuthenticationFilter;
        this.oAuth2APILoginSuccessHandler = l;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(crmUserDetails);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/oauth2/**").permitAll()
                .requestMatchers("/api/auth/google").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/api/auth/google")
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/auth/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/auth/oauth2/google"))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauthUserService))
                .successHandler(oAuth2APILoginSuccessHandler)
            )
            .addFilterBefore(apiTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setContentType("application/json");
                    res.setStatus(401);
                    res.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
                .accessDeniedHandler((req, res, ex) -> {
                    res.setContentType("application/json");
                    res.setStatus(403);
                    res.getWriter().write("{\"error\": \"Access Denied\"}");
                })
            );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
                .ignoringRequestMatchers("/api/**")
        );

        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/delete-all/**").permitAll()
                .requestMatchers("/csv/**").permitAll()
                .requestMatchers("/register/**").permitAll()
                .requestMatchers("/set-employee-password/**").permitAll()
                .requestMatchers("/set-password").permitAll()
                .requestMatchers("/change-password/**").permitAll()
                .requestMatchers("/font-awesome/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/save").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                .requestMatchers("/employee/**").hasAnyRole("MANAGER", "EMPLOYEE")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/imports").hasAnyRole("EMPLOYEE","MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login")
                .permitAll()
            )
            .userDetailsService(crmUserDetails)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauthUserService))
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll())
            .exceptionHandling(exception -> {
                exception.accessDeniedHandler(accessDeniedHandler());
            });

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
                .ignoringRequestMatchers("/csv/**")
                .ignoringRequestMatchers("/api/**")
        );

        http.securityMatcher("/customer-login/**")
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/csv/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/set-password/**").permitAll()
                .requestMatchers("/font-awesome/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/customer-login")
                .loginProcessingUrl("/customer-login")
                .failureUrl("/customer-login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .userDetailsService(customerUserDetails)
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/customer-login")
                .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
