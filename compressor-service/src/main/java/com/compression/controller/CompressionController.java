package com.compression.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.compression.model.DecompressionRequest;
import com.compression.service.ControllerService;

@RestController
public class CompressionController {
	@Autowired ControllerService service;
	
	@PostMapping("/compress")
	public ArrayList<String> compressData(@RequestBody Object data) {
		return this.service.compressData(data, true);
	}
	@PostMapping("/decompress")
	public Object decompressData(@RequestBody ArrayList<String> data) {
		return this.service.decompressData(data, true);
	}
	@PostMapping("/compressWithEntity")
	public Object compressDataWithEntity(@RequestBody Object data) {
		return this.service.compressDataWithEntity(data);
	}
	@PostMapping("/decompressWithEntity")
	public Object decompressDataWithEntity(@RequestBody DecompressionRequest data) {
		return this.service.decompressDataWithEntity(data);
	}
}
