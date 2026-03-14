package com.compression.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

@Service
public class DataBlockService {
	private int maxBytesUsed = 1;
	
	public void findRepetition(String hexCode, Map<String, Integer> frequencyTable) {
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
		// TO FILTER NON REPEATING BLOCKS
		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		// TO FILTER BLOCKS SMALLER THAN COMPRESSION KEYS
		for(String invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return t.length()<=maxBytesUsed;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
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
}
