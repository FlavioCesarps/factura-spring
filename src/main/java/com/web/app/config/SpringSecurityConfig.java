package com.web.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.web.app.authHandler.LoginSuccessHandler;
import com.web.app.service.JpaUserDetailsService;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {


	@Autowired
	private LoginSuccessHandler successHandler;
	
	@Autowired
	private JpaUserDetailsService userDetailsService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	/** establece ruta publica **/
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/", "/css/**", "/js/**", "/images/**", "/listar**", "/locale", "/api/clientes/**").permitAll()
			/** .antMatchers("/ver/**").hasAnyRole("USER") **/
		/** .antMatchers("/uploads/**").hasAnyRole("USER")  **/
			/** . .antMatchers("/form/**").hasAnyRole("ADMIN") **/
		/** ..antMatchers("/eliminar/**").hasAnyRole("ADMIN") **/
		/** .	.antMatchers("/factura/**").hasAnyRole("ADMIN") **/
			.anyRequest().authenticated()
			.and()
			.formLogin()
			.successHandler(successHandler)
			.loginPage("/login")
			.permitAll()
			.and()
			.logout().permitAll()
			.and()
			.exceptionHandling().accessDeniedPage("/error_403");
	}

	
	
	@Autowired
	public void configurerGlobal(AuthenticationManagerBuilder builder) throws Exception {
		
		
		builder.userDetailsService(userDetailsService)
		.passwordEncoder(passwordEncoder);
		
		
		/*
		
		PasswordEncoder encoder =  this.passwordEncoder;
		UserBuilder users = User.builder().passwordEncoder(password -> encoder.encode(password));
		
		builder.inMemoryAuthentication()
				.withUser(users.username("admin").password("12345").roles("ADMIN", "USER"))
				.withUser(users.username("andres").password("12345").roles("USER"));  */
	}




	
}
