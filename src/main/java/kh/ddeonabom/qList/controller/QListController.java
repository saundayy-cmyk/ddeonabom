package kh.ddeonabom.qList.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/qList")
public class QListController {
	
	@GetMapping("/list")
	public String qList() {
		return "views/qList/qBoard";
	}
	
}
