package kh.ddeonabom.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleController {
	@GetMapping("schedule/list")
	public String scheduleList() {
		return "views/schedule/my-list";
	}
	
	@GetMapping("schedule/new")
	public String newSchedule() {
		return "views/schedule/write";
	}
}
