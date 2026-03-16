package com.compression.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.compression.model.BinaryTreeNODE;


@Service
public class CompressionService {
	private HashMap<ArrayList<String>, Integer> hexMapping = new HashMap<>();
	private ArrayList<ArrayList<String>> sortedHexes = new ArrayList<>();
	
	
	public ArrayList<ArrayList<String>> getSortedHexes() {
		return sortedHexes;
	}
	public void setSortedHexes(ArrayList<ArrayList<String>> sortedHexes) {
		this.sortedHexes = sortedHexes;
	}
	
	public void initHexMapping(BinaryTreeNODE entryNode){
		this.hexMapping.put(entryNode.getHex(), 0);
		generateHexMappingRecursively(entryNode.left);
		generateHexMappingRecursively(entryNode.right);
		this.sortedHexes.addAll(this.hexMapping.keySet());
		this.sortedHexes.sort(new Comparator<ArrayList<String>>() {
			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				return hexMapping.get(o1).intValue()-hexMapping.get(o2).intValue();
			}
		});
		System.out.println("SORTED HEX : "+sortedHexes);
	}
	private void generateHexMappingRecursively(BinaryTreeNODE node) {
		if(node != null && node.getBin() != 0) {
			this.hexMapping.put(node.getHex(), node.getBinInt());
			generateHexMappingRecursively(node.left);
			generateHexMappingRecursively(node.right);
		}
	}
	public ArrayList<String> startCompression(ArrayList<String> hexArr) {
		System.out.println("``````````````````````````COMPRESSION FUNCTION");
		System.out.println("INPUT HEX STRING = "+hexArr);
		System.out.println("STARTING WITH STRING LENGTH : "+hexArr.size());
		ArrayList<String> backupHexArr = new ArrayList<>(hexArr);
		for(ArrayList<String> hex : this.sortedHexes) {
			findAndReplace(hexArr, hex);
		}
		System.out.println("STRING COMPRESSESD TO LENGTH : "+hexArr.size());
		System.err.println(hexArr);
		System.out.println("``````````````````````````COMPRESSION FUNCTION");
		return hexArr;
	}
	private void findAndReplace(ArrayList<String> searchParam, ArrayList<String> query) {
		for(int i=0;i<searchParam.size();i++) {
			if(searchParam.get(i).equals(query.get(0))) {
				int j;
				for(j=1;j<query.size();j++) {
					if(searchParam.get(i+j).equals(query.get(j))) {
						continue;
					}
					break;
				}
				if(j==query.size()) {
					for(j=0;j<query.size();j++) {
						searchParam.remove(i);
					}
					searchParam.add(i, "."+Integer.toHexString(hexMapping.get(query)));
				}
			}
		}
	}
	public ArrayList<String> startDecompression(ArrayList<String> hexArr) {
		System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		System.out.println("COMPRESSED DATA = "+hexArr);
		System.out.println("STARTING WITH STRING LENGTH : "+hexArr.size());
		for(int i=0;i<hexArr.size();i++) {
			if(hexArr.get(i).startsWith(".")) {
				String hex = hexArr.get(i).substring(1);
				int byteValue = calcByteFromHex(hex);
				hexArr.remove(i);
				hexArr.addAll(i, sortedHexes.get(byteValue));
			}
		}
		System.out.println("STRING DECOMPRESSESD TO LENGTH : "+hexArr.size());
		System.err.println(hexArr);
		System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return hexArr;
	}
	public static int calcByteFromHex(String hex){
		Set<Character> hexChar = new HashSet<>(Set.of('a','b','c','d','e','f'));
		int byteValue = 0;
		for(int j=hex.length()-1,k=0;j>=0;j--,k++) {
			int bitValue = 0;
			if(hexChar.contains(hex.charAt(j))) {
				switch (hex.charAt(j)) {
				case 'a':
					bitValue = 10;
					break;
				case 'b':
					bitValue = 11;
					break;
				case 'c':
					bitValue = 12;
					break;
				case 'd':
					bitValue = 13;
					break;
				case 'e':
					bitValue = 14;
					break;
				default:
					bitValue = 15;
				}
			} else {
				bitValue = Integer.parseInt(String.valueOf(hex.charAt(j)));
			}
			byteValue += (bitValue*(Math.pow(16, k)));
		}
		return byteValue;
	}
//	public String startCompression(String hexString) {
//		System.out.println("STARTING WITH STRING LENGTH : "+hexString.length());
//		for(ArrayList<String> hex : this.sortedHexes) {
//			hexString = hexString.replaceAll(hex, "."+Integer.toHexString(hexMapping.get(hex)));
//		}
//		System.out.println("STRING COMPRESSESD TO LENGTH : "+hexString.length());
//		return hexString;
//	}
//	public String startDecompression(String hexString) {
//		System.out.println("STARTING WITH COMPRESSED STRING LENGTH : "+hexString.length());
//		for(int i=0;i<this.sortedHexes.size();i++) {
//			hexString = hexString.replaceAll("[.]"+Integer.toHexString(i), this.sortedHexes.get(i));
//		}
//		System.out.println("STRING DECOMPRESSESD TO LENGTH : "+hexString.length());
//		return hexString;
//	}
}
