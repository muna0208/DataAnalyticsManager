package com.daumsoft.dataAnalyticsManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.daumsoft.dataAnalyticsManager.common.service.UserAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserAuthenticationProvider authenticationProvider;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/css/**", "/fonts/**", "/images/**", "/js/**", "/dist/**", "/login",
						"/mainTest", "/gentelella/**", "/users/{^[\\d]$}")
				.permitAll().antMatchers(HttpMethod.POST, "/users/signUp", "/users/find").permitAll()
				.antMatchers("/userManage").hasAnyRole("ADMIN").antMatchers("/**").hasAnyRole("USER", "ADMIN") // 내부적으로
																												// 접두어
																												// "ROLE_"가
																												// 붙는다.
				.anyRequest().authenticated();

		http.csrf().disable(); // rest 통신에서 POST 허용

		http.formLogin().loginPage("/login") // default
				.loginProcessingUrl("/authenticate").failureUrl("/login?error=accountError")
				// default
				.defaultSuccessUrl("/dashBoard").usernameParameter("userId").passwordParameter("userPw").permitAll();

		http.logout().logoutUrl("/logout") // default
				.logoutSuccessUrl("/login") // 로그아웃 성공시 페이지
				.invalidateHttpSession(true) // session invalidate
				.deleteCookies("JSESSIONID") // cookie
				.permitAll();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider);
	}
}
