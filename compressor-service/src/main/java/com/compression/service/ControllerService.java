package com.compression.service;

import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.compression.dto.CompressionKeysRepository;
import com.compression.model.CompressionKeysEntity;

import tools.jackson.databind.ObjectMapper;

@Service
public class ControllerService {
	@Autowired ObjectMapper mapper;
	@Autowired DataBlockService dataBlockService;
	@Autowired BinaryTreeService binTreeService;
	@Autowired CompressionService compressService;
	@Autowired CompressionKeysRepository repo;
	
	public String compressData(Object data) {
		String hexCode = "";
		for(byte b : this.mapper.writeValueAsBytes(data)) {
			hexCode += Integer.toHexString(b);
		}
		Map<String, Integer> frequencyTable = this.dataBlockService.findRepetition(hexCode);
		PriorityQueue<String> hexQueue = new PriorityQueue<String>((o1, o2) -> frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue());
		hexQueue.addAll(frequencyTable.keySet());
		this.binTreeService.initBinaryTree(hexQueue);
		this.compressService.initHexMapping(this.binTreeService.getHead());
		String compressedData = this.compressService.startCompression(hexCode);
		this.repo.save(new CompressionKeysEntity(compressedData.hashCode(), this.compressService.getSortedHexes()));
		return compressedData;
	}
	public Object decompressData(String data) {
		Optional<CompressionKeysEntity> entity = this.repo.findById(data.hashCode());
		if(entity.isEmpty()) {
			return new Object() {
				public String status = "failed";
				public String reason = "compression entity for given data not found in repository";
			};
		}
		this.compressService.setSortedHexes(entity.get().getSortedHexKeys());
		return this.mapper.readValue(this.compressService.startDecompression(data), Object.class);
	}
}
