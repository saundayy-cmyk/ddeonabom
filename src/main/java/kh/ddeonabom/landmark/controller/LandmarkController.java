package kh.ddeonabom.landmark.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kh.ddeonabom.landmark.model.service.LandmarkService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/landmark")
public class LandmarkController {
	private final LandmarkService lService;
	
	@GetMapping("list")
	public String selectList() {
		return "views/landmark/list.html";
	}
	
	
	
	
}
