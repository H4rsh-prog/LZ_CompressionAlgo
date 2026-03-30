package com.compression.service;

import java.util.ArrayList;
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
	
	public HashMap<ArrayList<Byte>, Integer> findRepetitiveBytes(ArrayList<Byte> byteArr) {
		HashMap<ArrayList<Byte>, Integer> frequencyTable = new HashMap<>();
		HashSet<Byte> potentialBlockStart = new HashSet<>();
		int byteArrSize = byteArr.size();
		for(int i=0;i<byteArrSize;i++) {
			if(verbose) System.out.print("FINDING REPETITION PROGRESS : ["+i+"/"+byteArrSize+"]\r");
			if(potentialBlockStart.contains(byteArr.get(i))) {
				ArrayList<Byte> dataBlock = findByteBlock(byteArr, frequencyTable, i);
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				i += dataBlock.size()-1;
			} else {
				potentialBlockStart.add(byteArr.get(i));
			}
		}
		for(ArrayList<Byte> invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ArrayList<Byte>>() {
			@Override
			public boolean test(ArrayList<Byte> t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		for(ArrayList<Byte> invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ArrayList<Byte>>() {
			@Override
			public boolean test(ArrayList<Byte> t) {
				return t.size()<=(maxBytesUsed+1);
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		return blockOptimization(frequencyTable);
	}
	public ArrayList<Byte> findByteBlock(ArrayList<Byte> byteArr, HashMap<ArrayList<Byte>, Integer> frequencyTable, int startIndx) {
		Set<ArrayList<Byte>> repeatingBlocks = frequencyTable.keySet();
		ArrayList<Byte> dataBlock = new ArrayList<>();
		dataBlock.add(byteArr.get(startIndx));
		int byteArrSize = byteArr.size();
		for(int i=startIndx+1;i<byteArrSize;i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				dataBlock.add(byteArr.get(i));
				continue;
			}
			break;
		}
		return dataBlock;
	}
	private HashMap<ArrayList<Byte>, Integer> blockOptimization(HashMap<ArrayList<Byte>, Integer> frequencyTable) {
		ArrayList<ArrayList<Byte>> sortedBytes = new ArrayList<>(frequencyTable.keySet());
		sortedBytes.sort(new Comparator<ArrayList<Byte>>() {
			@Override
			public int compare(ArrayList<Byte> o1, ArrayList<Byte> o2) {
				if(o1.size()!=o2.size()) return o1.size()-o2.size();
				return frequencyTable.get(o1).intValue()-frequencyTable.get(o2).intValue();
			}
		});
		int byteCount = sortedBytes.size();
		CompressionService.dictionaryLimit = 254;
		for(int i=0;i<byteCount;i++) {
			if(i>255) continue;
			frequencyTable.remove(sortedBytes.get(i));
		}
		return frequencyTable;
	}	
}
