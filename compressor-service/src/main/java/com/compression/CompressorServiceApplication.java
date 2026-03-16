package com.compression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.compression.service.BinaryTreeService;
import com.compression.service.CompressionService;
import com.compression.service.ControllerService;
import com.compression.service.DataBlockService;

import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class CompressorServiceApplication {

	public static void main(String[] args) {
		ApplicationContext cx = SpringApplication.run(CompressorServiceApplication.class, args);
//		ObjectMapper mapper = new ObjectMapper();
//		byte[] byteArr = mapper.writeValueAsBytes(mapper.readValue("{\"name\":\"this_name_will_repeat_maybe\",\"property\":\"name_maybe\",\"other_property\":\"repeating?\",\"maybe_more_properties\":\"name\"}", Object.class));
//		cx.getBean(ControllerService.class).compressData(mapper.readValue("{\"name\":\"this_name_will_repeat_maybe\",\"property\":\"name_maybe\",\"other_property\":\"repeating?\",\"maybe_more_properties\":\"name\"}", Object.class));
//		DataBlockService dbService = cx.getBean(DataBlockService.class);
//		HashMap<String, Integer> frequencyTable = new HashMap<>();
//		String hexCode = "";
//		for(byte b : byteArr) {
//			hexCode += Integer.toHexString(b);
//		}
//		dbService.findRepetition(hexCode);
//		System.out.println(hexCode);
//		List<String> sortedKeys = new ArrayList<>();
//		for(String key : frequencyTable.keySet()) sortedKeys.add(key);
//		Collections.sort(sortedKeys, new Comparator<String>() {
//			@Override
//			public int compare(String o1, String o2) {
//				return frequencyTable.get(o1).intValue()-frequencyTable.get(o2).intValue();
//			}
//		});
//		for(String key : sortedKeys) System.out.println(key+" : "+frequencyTable.get(key));
//		PriorityQueue<String> hexQueue = new PriorityQueue<>((o1, o2) -> frequencyTable.get(o2)-frequencyTable.get(o1));
//		hexQueue.addAll(sortedKeys);
//		BinaryTreeService binTreeService = new BinaryTreeService();
//		binTreeService.initBinaryTree(hexQueue);
//		System.out.println("HUFFMAN TREE CONSTRUCTED");
//		CompressionService compressService = new CompressionService();
//		System.out.println("STARTING COMPRESSION");
//		compressService.initHexMapping(binTreeService.getHead());
//		System.out.println(hexCode);
//		String compressedHexCode = compressService.startCompression(hexCode);
//		System.err.println(compressedHexCode);
//		String decompressedHexCode = compressService.startDecompression(compressedHexCode);
//		System.err.println(decompressedHexCode);
//		System.out.println("DECOMPRESSION SUCCESSFUL = "+decompressedHexCode.equals(hexCode));
		ObjectMapper mapper = new ObjectMapper();
		BinaryTreeService binTreeService = new BinaryTreeService();
		DataBlockService dataBlockService = new DataBlockService();
		CompressionService compressionService = new CompressionService();
		dataBlockService.setVerbose(true);
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
		System.out.println(binTreeService.treeToString());
		compressionService.initHexMapping(binTreeService.getHead());
		System.out.println(hexArr);
		ArrayList<String> compressedData = compressionService.startCompression(hexArr);
		ArrayList<String> decompressedData = compressionService.startDecompression(compressedData);
		System.out.println(compressedData.equals(decompressedData));
		byte[] byteData = new byte[decompressedData.size()];
		for(int i=0;i<decompressedData.size();i++) {
			byteData[i] = (byte) CompressionService.calcByteFromHex(decompressedData.get(i));
		}
		String byteString = "";
		for(byte b: byteData) {
			byteString += (char)b;
		}
		System.out.println(byteString);
		System.out.println(mapper.readValue(byteString, Object.class));
	}
}
