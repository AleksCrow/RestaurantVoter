package com.voronkov.cafevoiter.configuration;

import com.voronkov.cafevoiter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    //для данных из бд
    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                    .antMatchers("/users/register").anonymous()
                    .antMatchers("/cafes/**").authenticated()
                .and()
                    .csrf().disable()
                    .formLogin().defaultSuccessUrl("/cafes").permitAll()
                .and()
                    .logout().permitAll();

    }

    //для связи юзеров из бд
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)    //чтобы менеджер входил в бд и искал роли
                .passwordEncoder(NoOpPasswordEncoder.getInstance());
//                .usersByUsernameQuery("select email, password, enabled from users where email=?")
//                //позволяет получить юзеров с их ролями
//                .authoritiesByUsernameQuery("select u.email, ur.role from users u inner join user_roles ur on u.id=ur.user_id where u.email=?");
    }
}
