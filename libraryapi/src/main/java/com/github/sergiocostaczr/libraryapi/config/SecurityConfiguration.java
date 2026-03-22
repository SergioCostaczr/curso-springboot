package com.github.sergiocostaczr.libraryapi.config;

import com.github.sergiocostaczr.libraryapi.security.CustomUserDetailsService;
import com.github.sergiocostaczr.libraryapi.security.LoginSocialSucessHandler;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true,jsr250Enabled = true)//habilita nos controllers
public class SecurityConfiguration {

    @Bean //resource server
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginSocialSucessHandler loginSocialSucessHandler) throws Exception {

        return http
//                .formLogin(configurer -> configurer.loginPage("/login.html").successForwardUrl("/home.html"))
                // header Authorization
//                .httpBasic(Customizer.withDefaults())

//                .formLogin(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)

//                 Diz ao Spring que a gente tem a nossa propia pagina de login
                .formLogin(configurer -> configurer.loginPage("/login").permitAll())
//                .formLogin(Customizer.withDefaults())

                .httpBasic(Customizer.withDefaults())

                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/teste").permitAll();
                    authorize.requestMatchers("/login/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST,"/usuarios/**").permitAll();
//                    authorize.requestMatchers(HttpMethod.POST,"/autores/**").hasAuthority("CADASTRAR_USUARIO");
//                    authorize.requestMatchers(HttpMethod.POST,"/autores/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.PUT,"/autores/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.DELETE,"/autores/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.GET,"/autores/**").hasAnyRole("USER","ADMIN");
//                    authorize.requestMatchers("/autores/**").hasRole("ADMIN");
//                    authorize.requestMatchers("/livros/**").hasAnyRole("USER","ADMIN");

                    //Caso nao defina uma role em um endpoint basta estar autenticado para usar
                    authorize.anyRequest().authenticated();
                })
                .oauth2Login(oauth2-> {
                    oauth2
                        .loginPage("/login")
                        .successHandler(loginSocialSucessHandler);
                })
                .oauth2ResourceServer(oauth2RS -> Customizer.withDefaults())
                .build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
//    }

//    @Bean
//    public UserDetailsService userDetailsService(UsuarioService usuarioService) {
//
//        UserDetails user1 = User.builder()
//                .username("username")
//                .password(encoder.encode("123"))
//                .roles("USER")
//                .build();
//
//        UserDetails user2 = User.builder()
//                .username("admin")
//                .password(encoder.encode("321"))
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(user1, user2);
//        return new CustomUserDetailsService(usuarioService);
//    }

    // Configura o prefixo ROLE
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults(){
        return new GrantedAuthorityDefaults(""); //tira o ROLE_
    }


    //SCOPE_xxx tem prefixo
    // Configura no token JWT o prefixo SCOPE
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }
}
