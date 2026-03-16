package com.compression.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.compression.model.BinaryTreeNODE;

@Service
public class CompressionService {
	private HashMap<String, Byte> hexMapping = new HashMap<>();
	private ArrayList<String> sortedHexes = new ArrayList<>();
	
	
	public ArrayList<String> getSortedHexes() {
		return sortedHexes;
	}
	public void setSortedHexes(ArrayList<String> sortedHexes) {
		this.sortedHexes = sortedHexes;
	}
	
	public void initHexMapping(BinaryTreeNODE entryNode){
		this.hexMapping.put(entryNode.getHex(), (byte) 0);
		generateHexMappingRecursively(entryNode.left);
		generateHexMappingRecursively(entryNode.right);
		this.sortedHexes.addAll(this.hexMapping.keySet());
		this.sortedHexes.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return hexMapping.get(o1).intValue()-hexMapping.get(o2).intValue();
			}
		});
		System.out.println("SORTED HEX : "+sortedHexes);
	}
	private void generateHexMappingRecursively(BinaryTreeNODE node) {
		if(node != null && node.getBin() != 0) {
			this.hexMapping.put(node.getHex(), node.getBin());
			generateHexMappingRecursively(node.left);
			generateHexMappingRecursively(node.right);
		}
	}
	public String startCompression(String hexString) {
		System.out.println("STARTING WITH STRING LENGTH : "+hexString.length());
		for(String hex : this.sortedHexes) {
			hexString = hexString.replaceAll(hex, "."+Integer.toHexString(hexMapping.get(hex)));
		}
		System.out.println("STRING COMPRESSESD TO LENGTH : "+hexString.length());
		return hexString;
	}
	public String startDecompression(String hexString) {
		System.out.println("STARTING WITH COMPRESSED STRING LENGTH : "+hexString.length());
		for(int i=0;i<this.sortedHexes.size();i++) {
			hexString = hexString.replaceAll("[.]"+Integer.toHexString(i), this.sortedHexes.get(i));
		}
		System.out.println("STRING DECOMPRESSESD TO LENGTH : "+hexString.length());
		return hexString;
	}
}
