package mes.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import mes.domain.security.AjaxAwareLoginUrlAuthenticationEntryPoint;

import mes.domain.security.CustomAccessDeniedHandler;
import mes.domain.security.CustomAuthenticationFailureHandler;
import mes.domain.security.CustomAuthenticationManager;
import mes.domain.security.CustomAuthenticationSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;


@Configuration
@ComponentScan("mes.domain.security")
public class SecurityConfiguration {
	
	@Autowired
	private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

	@Autowired
	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	
		
	@Bean(name="authenticationManager")	
	CustomAuthenticationManager authenticationManager() {
		CustomAuthenticationManager authenticationManager = new CustomAuthenticationManager();
		return authenticationManager;
	}
	
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin(); 
        //http.csrf().disable();
        http.csrf().ignoringAntMatchers("/api/files/upload/**");
		http.csrf().ignoringAntMatchers("/api/sales/**");
		http.csrf().ignoringAntMatchers("/api/gene/**");
		http.csrf().ignoringAntMatchers("/api/QnA/downloader");
        
        http.authorizeRequests().mvcMatchers("/login","/logout", "/useridchk/**", "/Register/save","/mobile/save", "/authentication", "/UserInfo").permitAll()
				.mvcMatchers("/api/sales/upload/**", "/api/gene/**").permitAll()  // 모든 사용자에게 허용 (임시)
				.mvcMatchers("/user-codes/**", "/user-auth/**", "/api/QnA/**").permitAll()
//				.mvcMatchers("/api/sales/upload/**").authenticated()  // 모든 인증된 사용자에게 허용 (임시)
        .mvcMatchers("/setup").hasAuthority("admin")		// hasRole -> hasAuthority로 수정
        .anyRequest().authenticated();

        http.formLogin()
        .loginPage("/login")
        .loginProcessingUrl("/postLogin")
        .successHandler(customAuthenticationSuccessHandler)
		.failureHandler(customAuthenticationFailureHandler)		
        .permitAll();
                
        http.logout().logoutUrl("/logout")
        .logoutSuccessUrl("/login")
        .invalidateHttpSession(true)
        .deleteCookies("mes21_jsessionid")
        .clearAuthentication(true)
        .permitAll();
        
        http.httpBasic().disable();
        http.exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler()).authenticationEntryPoint(new AjaxAwareLoginUrlAuthenticationEntryPoint("/login"));
        

        
        return http.build();
    }

	@Bean
	@Order(0)
	SecurityFilterChain exceptResources(HttpSecurity http) throws Exception {
		http.requestMatchers(matchers -> matchers.antMatchers(
						"/resource/**", "/img/**", "/images/**", "/js/**","/assets_mobile/**", "/auth/**",
						"/css/**", "/font/**", "/robots.txt", "/favicon.ico",
						"/static/**", "/webjars/**", "/intro", "/error",
						"/alive", "/api/das_device", "/import.css"))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.requestCache(RequestCacheConfigurer::disable)
				.securityContext(AbstractHttpConfigurer::disable)
				.sessionManagement(AbstractHttpConfigurer::disable);

		http.headers().frameOptions().disable();
		return http.build();
	}

	@Bean
	public HttpFirewall allowDoubleSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedDoubleSlash(true); // 중복 슬래시 허용
		firewall.setAllowUrlEncodedPercent(true); // 퍼센트 인코딩 허용 (필요 시)
		return firewall;
	}


}

