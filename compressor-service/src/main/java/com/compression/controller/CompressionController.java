package com.compression.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compression.service.ControllerService;

@RestController
public class CompressionController {
	@Autowired ControllerService service;
	
	@PostMapping("/compress")
	public ArrayList<String> compressData(@RequestBody Object data) {
		return this.service.compressData(data);
	}
	@GetMapping("/decompress")
	public Object decompressData(@RequestParam("data") String data) {
		return this.service.decompressData(data);
	}
}
