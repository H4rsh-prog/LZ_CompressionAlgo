package com.compression.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.compression.dto.CompressionKeysRepository;
import com.compression.model.CompressionKeysEntity;
import com.compression.model.DecompressionRequest;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import tools.jackson.databind.ObjectMapper;

@Service
public class ControllerService {
	@Autowired ObjectMapper mapper;
	@Autowired DataBlockService dataBlockService;
	@Autowired BinaryTreeService binTreeService;
	@Autowired CompressionService compressService;
	@Autowired CompressionKeysRepository repo;
	
	@Setter private boolean verbose = true;
	
	public ArrayList<String> compressData(Object data, boolean saveEntity) {
		byte[] byteArr = this.mapper.writeValueAsBytes(data);
		ArrayList<String> hexArr = new ArrayList<>();
		for(byte b : byteArr) {
			hexArr.add(Integer.toHexString(b));
		}
		HashMap<ArrayList<String>, Integer> frequencyTable = this.dataBlockService.findRepetition(hexArr);
		PriorityQueue<ArrayList<String>> hexQueue = new PriorityQueue<ArrayList<String>>(
					(o1, o2) -> frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue()
				);
		hexQueue.addAll(frequencyTable.keySet());
		this.binTreeService.initBinaryTree(hexQueue);
		if(verbose) System.out.println(this.binTreeService.treeToString());
		this.compressService.initHexMapping(binTreeService.getHead());
		ArrayList<String> compressedData = compressService.startCompression(hexArr);
		if(saveEntity) this.repo.save(new CompressionKeysEntity(compressedData.hashCode(), compressService.getSortedHexes()));
		if(verbose) System.out.println(compressService.getSortedHexes());
		return compressedData;
	}
	public Object decompressData(ArrayList<String> hexArr, boolean savedEntity) {
		if(savedEntity) {
			Optional<CompressionKeysEntity> entity = this.repo.findById(hexArr.hashCode());
			if(entity.isEmpty()) {
				return new Object() {
					public String status = "failed";
					public String reason = "compression entity for given data not found in repository";
				};
			}
			this.compressService.setSortedHexes(entity.get().getSortedHexKeys());
			if(verbose) System.out.println(compressService.getSortedHexes());
		}
		ArrayList<String> decompressedHexArr = this.compressService.startDecompression(hexArr);
		byte[] byteData = new byte[decompressedHexArr.size()];
		for(int i=0;i<decompressedHexArr.size();i++) {
			byteData[i] = (byte) CompressionService.calcByteFromHex(decompressedHexArr.get(i));
		}
		String jsonString = "";
		for(byte b : byteData) {
			jsonString += (char) b;
		}
		System.out.println(jsonString);
		return this.mapper.readValue(byteData, Object.class);
	}
	public Object compressDataWithEntity(Object data) {
		return new Object() {
			public ArrayList<String> compressedData = compressData(data, false);
			public ArrayList<ArrayList<String>> freqSortedHexes = compressService.getSortedHexes();
			public int beforeCompressionSizeInBytes = mapper.writeValueAsBytes(data).length;
			public int afterCompressionSizeInBytes = compressedData.size();
		};
	}
	public Object decompressDataWithEntity(DecompressionRequest data) {
		System.out.println("CURRENT : "+this.compressService.getSortedHexes());
		System.out.println("NEW : "+data.getFreqSortedHexes());
		this.compressService.setSortedHexes(data.getFreqSortedHexes());
		return decompressData(data.getCompressedData(), false);
	}
	@PostConstruct
	private void initVerbose(){
		this.dataBlockService.setVerbose(verbose);
		this.binTreeService.setVerbose(verbose);
		this.compressService.setVerbose(verbose);
	}
}
