package kh.ddeonabom.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 1. 모든 페이지 접근 허용 및 CSRF 보안 해제
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.csrf(csrf -> csrf.disable())
			
			// 2. 로그인 설정 및 성공 시 실제 목적지(/member/edit) 지정
			.formLogin(form -> form
				.loginPage("/member/login")
				.loginProcessingUrl("/login") // 💡 유령 공백 제거 완료!
				.defaultSuccessUrl("/member/edit", true) // 로그인 성공하면 무조건 edit 화면으로!
				.permitAll()
			);
			
		return http.build();
	}
	
	@Bean
	public BCryptPasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}