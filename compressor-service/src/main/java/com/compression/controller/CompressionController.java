package com.compression.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.compression.service.ControllerService;

@RestController
public class CompressionController {
	@Autowired ControllerService service;
	
	//TO REIMPLEMENT
}
