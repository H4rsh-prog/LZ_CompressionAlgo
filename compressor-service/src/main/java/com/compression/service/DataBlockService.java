package com.compression.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

@Service
public class DataBlockService {

	private int maxBytesUsed = 1;
	
	public void findRepetition(String hexCode, Map<String, Integer> frequencyTable) {
		System.out.println(hexCode);
		HashSet<Character> potentialBlockStart = new HashSet<>();
		for(int i=0;i<hexCode.length();i++) {
			if(potentialBlockStart.contains(hexCode.charAt(i))) {
				String dataBlock = findBlock(hexCode, frequencyTable, i);
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				i += dataBlock.length()-1;
			} else {
				potentialBlockStart.add(hexCode.charAt(i));
			}
		}
		System.out.println("BEFORE FILTERING NON REPEATING BLOCKS : "+frequencyTable);
		// TO FILTER NON REPEATING BLOCKS
		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		System.out.println("BEFORE FILTERING BLOCKS SMALLER THAN COMPRESSION KEY : "+frequencyTable);
		// TO FILTER BLOCKS SMALLER THAN COMPRESSION KEYS
		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return t.length()<=(maxBytesUsed*2);	//BECAUSE WE ARE USING HEXCODE I.E. TWO CHARS FOR SINGLE BYTE
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		System.out.println("FINISHED FILTERING");
		removeDuplicates(frequencyTable);
	}
	public String findBlock(String hexCode, Map<String, Integer> frequencyTable, int startIndx) {
		Set<String> repeatingBlocks = frequencyTable.keySet();
		String dataBlock = String.valueOf(hexCode.charAt(startIndx));
		for(int i=startIndx+1;i<hexCode.length();i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				dataBlock += String.valueOf(hexCode.charAt(i));
				continue;
			}
			break;
		}
		return dataBlock;
	}
	public void removeDuplicates(Map<String, Integer> frequencyTable) {
		// TO PREFER BLOCK SIZE OR BLOCK REPETITION?
		for(Entry<String, Integer> e : frequencyTable.entrySet()) {
			System.out.println(e.getKey()+" : "+e.getValue());
		}
		System.out.println("Enter Duplicate Optimization Technique [1: RemovalByBlockSize ; 0: RemovalByBlockFrequency]");
		int choice = new java.util.Scanner(System.in).nextInt();
		if(choice==1) {
			//BLOCK SIZE
			removeDuplicatesBySize(frequencyTable);
		} else if(choice==0) {
			//BLOCK REPETIIION
			removeDuplicatesByFrequency(frequencyTable);
		}
	}
	public void removeDuplicatesBySize(Map<String, Integer> frequencyTable) {
		List<String> sortedKeys = new ArrayList<>();
		for(String key : frequencyTable.keySet()) sortedKeys.add(key);
		Collections.sort(sortedKeys, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.length()-o1.length();
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
	public void removeDuplicatesByFrequency(Map<String, Integer> frequencyTable) {
		List<String> sortedKeys = new ArrayList<>();
		for(String key : frequencyTable.keySet()) sortedKeys.add(key);
		Collections.sort(sortedKeys, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
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
}
