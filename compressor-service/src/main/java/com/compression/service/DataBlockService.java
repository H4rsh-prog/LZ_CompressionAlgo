package com.compression.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataBlockService {

	public void findBlocks(String hexCode, Map<String, Integer> frequencyTable) {
		HashSet<Character> potentialBlockStart = new HashSet<>();
		for(int i=0;i<hexCode.length();i++) {
			if(potentialBlockStart.contains(hexCode.charAt(i))) {
				findRepetition(hexCode, frequencyTable, i);
			} else {
				potentialBlockStart.add(hexCode.charAt(i));
			}
		}
	}
	public void findRepetition(String hexCode, Map<String, Integer> frequencyTable, int startIndx) {
		Set<String> repeatingBlocks = frequencyTable.keySet();
		String dataBlock = String.valueOf(hexCode.charAt(startIndx));
		for(int i=startIndx+1;i<hexCode.length();i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				dataBlock += String.valueOf(hexCode.charAt(i));
				continue;
			}
		}
	}
}
