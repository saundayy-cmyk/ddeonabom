package kh.ddeonabom.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")

public class AdminController {
	
	@GetMapping("dash")
		public String selectdash() {
		return "views/admin/dash";
	}
	
	@GetMapping("member")
		public String selectadmember() {
		return "views/admin/member";
	}
}
