package com.compression.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class DataBlockService {
	@Setter private int maxBytesUsed = 1;
	@Setter private boolean verbose = false;
	@Setter private int optimizationMethod = 0;
	
	public HashMap<ArrayList<String>, Integer> findRepetition(ArrayList<String> hexArr){
		HashMap<ArrayList<String>, Integer> frequencyTable = new HashMap<>();
		HashSet<String> potentialBlockStart = new HashSet<>();
		if (verbose) System.out.println(hexArr);
		for(int i=0;i<hexArr.size();i++) {
			if(potentialBlockStart.contains(hexArr.get(i))) {
				ArrayList<String> dataBlock = findBlock(hexArr, frequencyTable, i);
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				i += dataBlock.size()-1;
			} else {
				potentialBlockStart.add(hexArr.get(i));
			}
		}
		if (verbose) System.out.println("before filtering non repeating blocks : "+frequencyTable);
		for(ArrayList<String> invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ArrayList<String>>() {
			@Override
			public boolean test(ArrayList<String> t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		if (verbose) System.out.println("before filtering blocks smaller than compression key : "+frequencyTable);
		for(ArrayList<String> invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ArrayList<String>>() {
			@Override
			public boolean test(ArrayList<String> t) {
				return t.size()<=(maxBytesUsed);
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		if (verbose) System.out.println("after filtering : "+frequencyTable);
		return frequencyTable;
	}
	public ArrayList<String> findBlock(ArrayList<String> hexArr, HashMap<ArrayList<String>, Integer> frequencyTable, int startIndx) {
		Set<ArrayList<String>> repeatingBlocks = frequencyTable.keySet();
		ArrayList<String> dataBlock = new ArrayList<>();
		dataBlock.add(hexArr.get(startIndx));
		for(int i=startIndx+1;i<hexArr.size();i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				dataBlock.add(hexArr.get(i));
				continue;
			}
			break;
		}
		return dataBlock;
	}
	public void removeDuplicatesBySize(HashMap<ArrayList<String>, Integer> frequencyTable) {
		ArrayList<ArrayList<String>> sortedKeys = new ArrayList<>();
		for(ArrayList<String> key : frequencyTable.keySet()) sortedKeys.add(key);
		Collections.sort(sortedKeys, new Comparator<ArrayList<String>>() {
			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				return o2.size()-o1.size();
			}
		});
		System.out.println(sortedKeys);
		for(int i=0;i<sortedKeys.size();i++) {
			for(int j=sortedKeys.size()-1;i<j;j--) {
				if(sortedKeys.get(i).contains(sortedKeys.get(j))) {
					frequencyTable.remove(sortedKeys.get(j));
					sortedKeys.remove(j);
				}
			}
		}
		System.out.println(sortedKeys);
	}
	public void removeDuplicatesByFrequency(HashMap<ArrayList<String>, Integer> frequencyTable) {
		ArrayList<ArrayList<String>> sortedKeys = new ArrayList<>();
		for(ArrayList<String> key : frequencyTable.keySet()) sortedKeys.add(key);
		Collections.sort(sortedKeys, new Comparator<ArrayList<String>>() {
			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				return frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue();
			}
		});
		System.out.println(sortedKeys);
		for(int i=0;i<sortedKeys.size()-1;i++) {
			for(int j=sortedKeys.size()-1;i<j;j--) {
				if(sortedKeys.get(i).contains(sortedKeys.get(j)) || sortedKeys.get(j).contains(sortedKeys.get(i))) {
					frequencyTable.remove(sortedKeys.get(j));
					sortedKeys.remove(j);
				}
			}
		}
		System.out.println(sortedKeys);
	}
//	public Map<String, Integer> findRepetition(String hexCode) {
//		HashMap<String, Integer> frequencyTable = new HashMap<>();
//		System.out.println(hexCode);
//		HashSet<Character> potentialBlockStart = new HashSet<>();
//		for(int i=0;i<hexCode.length();i++) {
//			if(potentialBlockStart.contains(hexCode.charAt(i))) {
//				String dataBlock = findBlock(hexCode, frequencyTable, i);
//				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
//				i += dataBlock.length()-1;
//			} else {
//				potentialBlockStart.add(hexCode.charAt(i));
//			}
//		}
//		System.out.println("BEFORE FILTERING NON REPEATING BLOCKS : "+frequencyTable);
//		// TO FILTER NON REPEATING BLOCKS
//		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
//			@Override
//			public boolean test(String t) {
//				return frequencyTable.get(t)==1;
//			}
//		}).toList()) {
//			frequencyTable.remove(invalidKeys);
//		}
//		System.out.println("BEFORE FILTERING BLOCKS SMALLER THAN COMPRESSION KEY : "+frequencyTable);
//		// TO FILTER BLOCKS SMALLER THAN COMPRESSION KEYS
//		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
//			@Override
//			public boolean test(String t) {
//				return t.length()<=(maxBytesUsed*2);	//BECAUSE WE ARE USING HEXCODE I.E. TWO CHARS FOR SINGLE BYTE
//			}
//		}).toList()) {
//			frequencyTable.remove(invalidKeys);
//		}
//		System.out.println("FINISHED FILTERING");
//		return frequencyTable;
//	}
//	private String findBlock(String hexCode, Map<String, Integer> frequencyTable, int startIndx) {
//		Set<String> repeatingBlocks = frequencyTable.keySet();
//		String dataBlock = String.valueOf(hexCode.charAt(startIndx));
//		for(int i=startIndx+1;i<hexCode.length();i++) {
//			if(repeatingBlocks.contains(dataBlock)) {
//				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
//				dataBlock += String.valueOf(hexCode.charAt(i));
//				continue;
//			}
//			break;
//		}
//		return dataBlock;
//	}
//	private void removeDuplicates(Map<String, Integer> frequencyTable) {
//		// TO PREFER BLOCK SIZE OR BLOCK REPETITION?
//		for(Entry<String, Integer> e : frequencyTable.entrySet()) {
//			System.out.println(e.getKey()+" : "+e.getValue());
//		}
//		System.out.println("Enter Duplicate Optimization Technique [1: RemovalByBlockSize ; 0: RemovalByBlockFrequency]");
//		int choice = new java.util.Scanner(System.in).nextInt();
//		if(choice==1) {
//			//BLOCK SIZE
//			removeDuplicatesBySize(frequencyTable);
//		} else if(choice==0) {
//			//BLOCK REPETIIION
//			removeDuplicatesByFrequency(frequencyTable);
//		}
//	}
//	public void removeDuplicatesBySize(Map<String, Integer> frequencyTable) {
//		List<String> sortedKeys = new ArrayList<>();
//		for(String key : frequencyTable.keySet()) sortedKeys.add(key);
//		Collections.sort(sortedKeys, new Comparator<String>() {
//			@Override
//			public int compare(String o1, String o2) {
//				return o2.length()-o1.length();
//			}
//		});
//		System.out.println(sortedKeys);
//		for(int i=0;i<sortedKeys.size();i++) {
//			for(int j=sortedKeys.size()-1;i<j;j--) {
//				if(sortedKeys.get(i).contains(sortedKeys.get(j))) {
//					frequencyTable.remove(sortedKeys.get(j));
//					sortedKeys.remove(j);
//				}
//			}
//		}
//		System.out.println(sortedKeys);
//	}
//	public void removeDuplicatesByFrequency(Map<String, Integer> frequencyTable) {
//		List<String> sortedKeys = new ArrayList<>();
//		for(String key : frequencyTable.keySet()) sortedKeys.add(key);
//		Collections.sort(sortedKeys, new Comparator<String>() {
//			@Override
//			public int compare(String o1, String o2) {
//				return frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue();
//			}
//		});
//		System.out.println(sortedKeys);
//		for(int i=0;i<sortedKeys.size()-1;i++) {
//			for(int j=sortedKeys.size()-1;i<j;j--) {
//				if(sortedKeys.get(i).contains(sortedKeys.get(j)) || sortedKeys.get(j).contains(sortedKeys.get(i))) {
//					frequencyTable.remove(sortedKeys.get(j));
//					sortedKeys.remove(j);
//				}
//			}
//		}
//		System.out.println(sortedKeys);
//	}
}
