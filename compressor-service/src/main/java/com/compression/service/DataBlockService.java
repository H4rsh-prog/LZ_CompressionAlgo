package com.compression.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
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
			System.out.print("FINDING REPETITION PROGRESS : ["+i+"/"+hexArr.size()+"]\r");
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
	private ArrayList<String> findBlock(ArrayList<String> hexArr, HashMap<ArrayList<String>, Integer> frequencyTable, int startIndx) {
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
	public PriorityQueue<ArrayList<String>> generateQueueFromFrequency(HashMap<ArrayList<String>, Integer> frequencyTable){
		PriorityQueue<ArrayList<String>> hexQueue = new PriorityQueue<ArrayList<String>>(
				(o1, o2) -> frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue()
			);
		hexQueue.addAll(frequencyTable.keySet());
		return hexQueue;
	}
	private void RudimentarySizeOptimization(HashMap<ArrayList<String>, Integer> frequencyTable) {
		ArrayList<ArrayList<String>> sortedHexes = new ArrayList<>(frequencyTable.keySet());
		sortedHexes.sort(new Comparator<ArrayList<String>>() {
			@Override
			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
				return frequencyTable.get(o1).intValue()-frequencyTable.get(o2).intValue();
			}
		});
		for(int i=0;i<sortedHexes.size();i++) {
			if(i<255) continue;
			frequencyTable.remove(sortedHexes.get(i));
		}
	}
	// LEGACY OPTIMIZATION TECHNIQUE
//	public void removeDuplicatesBySize(HashMap<ArrayList<String>, Integer> frequencyTable) {
//		ArrayList<ArrayList<String>> sortedKeys = new ArrayList<>();
//		for(ArrayList<String> key : frequencyTable.keySet()) sortedKeys.add(key);
//		Collections.sort(sortedKeys, new Comparator<ArrayList<String>>() {
//			@Override
//			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
//				return o2.size()-o1.size();
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
//	public void removeDuplicatesByFrequency(HashMap<ArrayList<String>, Integer> frequencyTable) {
//		ArrayList<ArrayList<String>> sortedKeys = new ArrayList<>();
//		for(ArrayList<String> key : frequencyTable.keySet()) sortedKeys.add(key);
//		Collections.sort(sortedKeys, new Comparator<ArrayList<String>>() {
//			@Override
//			public int compare(ArrayList<String> o1, ArrayList<String> o2) {
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
