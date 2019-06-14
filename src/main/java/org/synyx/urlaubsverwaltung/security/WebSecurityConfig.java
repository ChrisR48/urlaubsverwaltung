package org.synyx.urlaubsverwaltung.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private boolean isOauth2Enabled;

    public WebSecurityConfig(SecurityConfigurationProperties properties) {

        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf()
                .disable()
            .authorizeRequests()
                // TODO move to common url static or resources
                .antMatchers("/css/**").permitAll()
                .antMatchers("/fonts/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/assets/**").permitAll()
                // WEB
                .antMatchers("/web/overview").hasAuthority("USER")
                .antMatchers("/web/application/**").hasAuthority("USER")
                .antMatchers("/web/sicknote/**").hasAuthority("USER")
                .antMatchers("/web/person/**").hasAuthority("USER")
                .antMatchers("/web/overtime/**").hasAuthority("USER")
                .antMatchers("/web/department/**").hasAnyAuthority("BOSS", "OFFICE")
                .antMatchers("/web/settings/**").hasAuthority("OFFICE")
                .antMatchers("/web/google-api-handshake/**").hasAuthority("OFFICE")
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
                // TODO muss konfigurierbar werden!
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ADMIN")
                .anyRequest()
                    .authenticated()
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login");

                if(isOauth2Enabled) {
                    http.oauth2Login();
                } else {
                    http.formLogin()
                        .loginPage("/login").permitAll()
                            .defaultSuccessUrl("/web/overview")
                            .failureUrl("/login?login_error=1");
                }
    }
}
