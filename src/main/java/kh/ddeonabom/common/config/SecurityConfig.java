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
			// 1. 모든 정적 자원 및 URL 경로에 대해 시큐리티의 간섭을 완전히 차단 (전면 허용)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			
			// 2. AJAX 및 Form 통신을 방해하는 CSRF 보안 필터 비활성화
			.csrf(csrf -> csrf.disable())
			
			// 3. 💡 [핵심 교정]: 시큐리티 자동 로그인(formLogin) 기능을 완전히 제거합니다.
			// 이를 통해 질문자님이 만든 MemberController의 @PostMapping("/login")이 정상 작동하게 됩니다.
			.formLogin(form -> form.disable());
			
		return http.build();
	}
	
	@Bean
	public BCryptPasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}