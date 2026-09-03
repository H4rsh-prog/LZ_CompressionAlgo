package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import com.compression.model.ByteArrayWrapper;

import lombok.Setter;

@Service
public class DictionaryService {

	@Setter private int maxBytesUsed = 1;
	@Setter private boolean verbose = false;
	@Setter static int dictionaryLimit = 254;
	@Setter private int additionalMarkerBytes = 1;
	
	//LINEAR APPROACH
	public ArrayList<ByteArrayWrapper> createDictionaryLinear(byte[] byteArr) {
		HashMap<ByteArrayWrapper, Integer> frequencyTable = new HashMap<>();
		HashMap<ByteArrayWrapper, Integer> temp_frequencyTable = new HashMap<>();
		HashSet<Byte> potentialBlockStart = new HashSet<>();
		int byteArrSize = byteArr.length;
		int i;
		for(i=0;i<100000 && i<byteArrSize;i++) {
			if(verbose) System.out.print("FINDING REPETITION PROGRESS : ["+i+"/"+byteArrSize+"]\r");
			if(potentialBlockStart.contains(byteArr[i])) {
				ByteArrayWrapper dataBlock = findByteBlock(byteArr, temp_frequencyTable, i);
				temp_frequencyTable.put(dataBlock, temp_frequencyTable.getOrDefault(dataBlock, 0)+1);
				i += dataBlock.getData().length-1;
			} else {
				potentialBlockStart.add(byteArr[i]);
			}
		}
		if(verbose) System.out.print("COMMITING FIRST REPETITION BATCH... \r");
		for(Entry<ByteArrayWrapper, Integer> e: temp_frequencyTable.entrySet()) {
			frequencyTable.put(e.getKey(), e.getValue());
		}
		temp_frequencyTable.clear();
		while(i<byteArrSize) {
			int k;
			for(k=i;k<i+100000 && k<byteArrSize;k++) {
				if(verbose) System.out.print("FINDING REPETITION PROGRESS : ["+k+"/"+byteArrSize+"]\r");
				if(potentialBlockStart.contains(byteArr[k])) {
					ByteArrayWrapper dataBlock = findByteBlock(byteArr, temp_frequencyTable, k);
					temp_frequencyTable.put(dataBlock, Math.max(frequencyTable.getOrDefault(dataBlock, 0), temp_frequencyTable.getOrDefault(dataBlock, 0))+1);
					k += dataBlock.getData().length-1;
				} else {
					potentialBlockStart.add(byteArr[k]);
				}
			}
			if(verbose) System.out.print("COMMITING REPETITION BATCH NO. ["+(int)k%100000+"] \r");
			for(Entry<ByteArrayWrapper, Integer> e: temp_frequencyTable.entrySet()) {
				frequencyTable.put(e.getKey(), frequencyTable.getOrDefault(e.getKey(), 0)+e.getValue());
			}
			temp_frequencyTable.clear();
			i=k;
		}
		return sanitizeFrequencies(frequencyTable);
	}
	private ByteArrayWrapper findByteBlock(byte[] byteArr, HashMap<ByteArrayWrapper, Integer> frequencyTable, int startIndx) {
		Set<ByteArrayWrapper> repeatingBlocks = frequencyTable.keySet();
		byte[] start_byteArr = new byte[1];
		start_byteArr[0] = byteArr[startIndx];
		ByteArrayWrapper dataBlock = new ByteArrayWrapper(ByteBuffer.wrap(start_byteArr));
		int byteArrSize = byteArr.length;
		for(int i=startIndx+1;i<byteArrSize;i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				start_byteArr = dataBlock.getData();
				ByteBuffer newBuffer = ByteBuffer.allocate(i-startIndx+1);
				newBuffer.put(0, start_byteArr);
				newBuffer.put(i-startIndx, byteArr[i]);
				newBuffer.rewind();
				dataBlock.updateArray(newBuffer);
				continue;
			}
			break;
		}
		return dataBlock;
	}
	//LINEAR APPROACH
	
	//DAC APPROACH
	public ArrayList<ByteArrayWrapper> createDictionaryDAC(byte[] byteArr) {
		HashMap<ByteArrayWrapper, Integer> frequencyTable = new HashMap<>();
		divideAndFindRepetition(byteArr, frequencyTable, 0);
		return sanitizeFrequencies(frequencyTable);
	}
	public void divideAndFindRepetition(byte[] byteArr, HashMap<ByteArrayWrapper, Integer> currentFrequency, int depth) {
		if(byteArr.length<=1) return;
		if(verbose) System.out.println("CURRENT DEPTH : " + depth);
		ByteArrayWrapper wrapper = new ByteArrayWrapper(byteArr);
		int len = byteArr.length;
		int mid = len/2;
		currentFrequency.put(wrapper, currentFrequency.getOrDefault(wrapper, 0)+1);
		divideAndFindRepetition(Arrays.copyOfRange(byteArr, 0, mid), currentFrequency, depth+1);
		divideAndFindRepetition(Arrays.copyOfRange(byteArr, mid, len), currentFrequency, depth+1);
	}
	//DAC APPROACH
	
	public ArrayList<ByteArrayWrapper> sanitizeFrequencies(HashMap<ByteArrayWrapper, Integer> frequencyTable){
		int preLength = frequencyTable.size();
		// FILTERING NON REPEATING PATTERNS
		for(ByteArrayWrapper invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ByteArrayWrapper>() {
			@Override
			public boolean test(ByteArrayWrapper t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		int postLength = frequencyTable.size();
		if(verbose) System.out.println("TABLE REDUCED BY ["+(preLength-postLength)+"] ENTRIES AFTER FILTERING NON REPEATING BLOCKS NOW WITH REMAINING ENTRIES : "+frequencyTable.size());
		
		// CALCULATING MAXIMUM BYTES USED
		preLength = postLength;
		if(preLength>255) {
			if(preLength>Math.pow(2, 16)) {
				if(preLength>Math.pow(2, 24)) {
					setMaxBytesUsed(4);
				} else {
					setMaxBytesUsed(3);
				}
			} else {
				setMaxBytesUsed(2);
			}
		} else {
			setMaxBytesUsed(1);
		}
		setDictionaryLimit((int)Math.pow(255, this.maxBytesUsed));
		if(verbose) System.out.println("SETTING DELIMITER TO ["+this.maxBytesUsed+":"+dictionaryLimit+"] BYTES WITH THE FREQUENCY TABLE ENTRIES EXCEEDING ["+preLength+"]");
		
		// FILTERING PATTERNS SMALLER THAN DICTIONARY LIMIT
		for(ByteArrayWrapper invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ByteArrayWrapper>() {
			@Override
			public boolean test(ByteArrayWrapper t) {
				return t.getData().length<=(maxBytesUsed+additionalMarkerBytes);
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		postLength = frequencyTable.size();
		if(verbose) System.out.println("TABLE REDUCED BY ["+(preLength-postLength)+"] ENTRIES AFTER FILTERING BLOCKS SMALLER THAN DICTIONARY LIMIT NOW REMAINING ENTRIES : "+frequencyTable.size());
		
		// FILTERING OVERLAPPING PATTERNS
		ArrayList<ByteArrayWrapper> lengthPrioritizedList = generateSortedBytesFromFrequency(frequencyTable, PRIORITY.FREQUENCY);
		Set<ByteArrayWrapper> invalidKeys = new HashSet<ByteArrayWrapper>();
		int len = lengthPrioritizedList.size();
		for(int i=0;i<len;i++) {
			byte[] superset = lengthPrioritizedList.get(i).getData();
			int supersetLen = superset.length;
			for(int j=0;j<len;j++) {
				byte[] subset = lengthPrioritizedList.get(j).getData();
				int subsetLen = subset.length;
				// CHECKING IF SUBSET
				for(int k=0;k+subsetLen<supersetLen;k++) {
					if(Arrays.equals(subset, Arrays.copyOfRange(superset, k, k+subsetLen))) {
						invalidKeys.add(lengthPrioritizedList.get(j));
						break;
					}
				}
			}
		}
		for(ByteArrayWrapper key : invalidKeys) {
			lengthPrioritizedList.remove(key);
		}
		return new ArrayList<>(lengthPrioritizedList.subList(0, Math.min(dictionaryLimit, lengthPrioritizedList.size())));
	}
	
	enum PRIORITY {
		FREQUENCY,
		LENGTH
	}
	private ArrayList<ByteArrayWrapper> generateSortedBytesFromFrequency(HashMap<ByteArrayWrapper, Integer> frequencyTable, PRIORITY priority) {
		ArrayList<ByteArrayWrapper> sortedBytes = new ArrayList<>();
		sortedBytes.addAll(frequencyTable.keySet());
		switch(priority) {
		case LENGTH:
			sortedBytes.sort(new Comparator<ByteArrayWrapper>() {
				@Override
				public int compare(ByteArrayWrapper o1, ByteArrayWrapper o2) {
					if(o2.getData().length!=o1.getData().length) return o2.getData().length-o1.getData().length;
					return frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue();
				}
			});
			break;
		case FREQUENCY:
			sortedBytes.sort(new Comparator<ByteArrayWrapper>() {
				@Override
				public int compare(ByteArrayWrapper o1, ByteArrayWrapper o2) {
					return frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue();
				}
			});
		}
		return sortedBytes;
	}
}
