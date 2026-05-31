package kh.ddeonabom.common.config.handler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import kh.ddeonabom.member.model.exception.MemberException;

@ControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler({MemberException.class}) // 특정 예외가 발생했을 때 처리할 메소드 지정
public String handlerException(RuntimeException e, Model model) {
	model.addAttribute("message", e.getMessage());
	return "error/500";
}
}