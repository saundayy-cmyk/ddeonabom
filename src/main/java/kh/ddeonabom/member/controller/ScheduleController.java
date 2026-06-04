package kh.ddeonabom.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleController {
	@GetMapping("schedule/list")
	public String scheduleList() {
		return "views/schedule/my-list";
	}
}
