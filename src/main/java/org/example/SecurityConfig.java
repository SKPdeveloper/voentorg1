package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Налаштування безпеки для веб-додатку
 * В даному випадку ми вимикаємо стандартну авторизацію Spring Security,
 * оскільки будемо використовувати власну авторизацію через Vaadin
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Дозволяємо доступ до всіх URL, оскільки керування доступом
     * буде відбуватися на рівні додатку Vaadin
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll();
    }

    /**
     * Вимикаємо безпеку для статичних ресурсів та H2 консолі
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/VAADIN/**",
                "/favicon.ico",
                "/icons/**",
                "/images/**",
                "/frontend/**",
                "/webjars/**",
                "/h2-console/**",
                "/frontend-es5/**",
                "/frontend-es6/**");
    }
}
