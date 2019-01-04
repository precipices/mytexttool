package com.wk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {
	/**
	 * 返回网站首页 
	 */
	@RequestMapping(value= {"/","/index","/main"})
	public String index() {
		return "main";
	}
}
