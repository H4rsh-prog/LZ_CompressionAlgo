package com.compression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.compression.service.BinaryTreeService;
import com.compression.service.CompressionService;
import com.compression.service.DataBlockService;

import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class CompressorServiceApplication {

	public static void main(String[] args) {
//		ApplicationContext cx = SpringApplication.run(CompressorServiceApplication.class, args);ObjectMapper mapper = new ObjectMapper();
		/*BinaryTreeService binTreeService = new BinaryTreeService();
		DataBlockService dataBlockService = new DataBlockService();
		CompressionService compressionService = new CompressionService();
		dataBlockService.setVerbose(true);
		binTreeService.setVerbose(true);
		compressionService.setVerbose(true);
		Object data = mapper.readValue("{\"name\":\"this_name_will_repeat_maybe\",\"property\":\"name_maybe\",\"other_property\":\"repeating?\",\"maybe_more_properties\":\"name\"}", Object.class);
		byte[] byteArr = mapper.writeValueAsBytes(data);
		ArrayList<String> hexArr = new ArrayList<>();
		for(byte b : byteArr) {
			hexArr.add(Integer.toHexString(b));
		}
		HashMap<ArrayList<String>, Integer> frequencyTable = dataBlockService.findRepetition(hexArr);
		PriorityQueue<ArrayList<String>> hexQueue = new PriorityQueue<ArrayList<String>>(
					(o1, o2) -> frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue()
				);
		hexQueue.addAll(frequencyTable.keySet());
		binTreeService.initBinaryTree(hexQueue);
		compressionService.initHexMapping(binTreeService.getHead());
		System.out.println(hexArr);
		ArrayList<String> compressedData = compressionService.startCompression(hexArr);
		ArrayList<String> decompressedData = compressionService.startDecompression(compressedData);
		System.out.println("LOSSLESS = "+compressedData.equals(decompressedData));
		byte[] byteData = new byte[decompressedData.size()];
		for(int i=0;i<decompressedData.size();i++) {
			byteData[i] = (byte) CompressionService.calcByteFromHex(decompressedData.get(i));
		}
		String byteString = "";
		for(byte b: byteData) {
			byteString += (char)b;
		}
		System.out.println(byteString);*/
		
		
	}
}
