package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class DataBlockService {

	@Setter private int maxBytesUsed = 1;
	@Setter private boolean verbose = false;
	
	public HashMap<ByteBuffer, Integer> findRepetitiveBytes(byte[] byteArr) {
		HashMap<ByteBuffer, Integer> frequencyTable = new HashMap<>();
		HashMap<ByteBuffer, Integer> temp_frequencyTable = new HashMap<>();
		HashSet<Byte> potentialBlockStart = new HashSet<>();
		int byteArrSize = byteArr.length;
		int i;
		for(i=0;i<100000;i++) {
			if(verbose) System.out.print("FINDING REPETITION PROGRESS : ["+i+"/"+byteArrSize+"]\r");
			if(potentialBlockStart.contains(byteArr[i])) {
				ByteBuffer dataBlock = findByteBlock(byteArr, temp_frequencyTable, i);
				temp_frequencyTable.put(dataBlock, temp_frequencyTable.getOrDefault(dataBlock, 0)+1);
				i += dataBlock.limit()-1;
			} else {
				potentialBlockStart.add(byteArr[i]);
			}
		}
		if(verbose) System.out.print("COMMITING FIRST REPETITION BATCH... \r");
		for(Entry<ByteBuffer, Integer> e: temp_frequencyTable.entrySet()) {
			frequencyTable.put(e.getKey(), e.getValue());
		}
		temp_frequencyTable.clear();
		while(i<byteArrSize) {
			int k;
			for(k=i;k<i+100000 && k<byteArrSize;k++) {
				if(verbose) System.out.print("FINDING REPETITION PROGRESS : ["+k+"/"+byteArrSize+"]\r");
				if(potentialBlockStart.contains(byteArr[k])) {
					ByteBuffer dataBlock = findByteBlock(byteArr, temp_frequencyTable, k);
					temp_frequencyTable.put(dataBlock, Math.max(frequencyTable.getOrDefault(dataBlock, 0), temp_frequencyTable.getOrDefault(dataBlock, 0))+1);
					k += dataBlock.limit()-1;
				} else {
					potentialBlockStart.add(byteArr[k]);
				}
			}
			if(verbose) System.out.print("COMMITING REPETITION BATCH NO. ["+(int)k%100000+"] \r");
			for(Entry<ByteBuffer, Integer> e: temp_frequencyTable.entrySet()) {
				frequencyTable.put(e.getKey(), frequencyTable.getOrDefault(e.getKey(), 0)+e.getValue());
			}
			temp_frequencyTable.clear();
			i=k;
		}
		int preLength = frequencyTable.size();
		for(ByteBuffer invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ByteBuffer>() {
			@Override
			public boolean test(ByteBuffer t) {
				return frequencyTable.get(t)==1;
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		int postLength = frequencyTable.size();
		if(verbose) System.out.println("TABLE REDUCED BY ["+(preLength-postLength)+"] ENTRIES AFTER FILTERING NON REPEATING BLOCKS");
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
		CompressionService.setDictionaryLimit((int)Math.pow(255, this.maxBytesUsed));
		if(verbose) System.out.println("SETTING DELIMITER TO ["+this.maxBytesUsed+":"+CompressionService.dictionaryLimit+"] BYTES WITH THE FREQUENCY TABLE ENTRIES EXCEEDING ["+preLength+"]");
		for(ByteBuffer invalidKeys : frequencyTable.keySet().stream().filter(new Predicate<ByteBuffer>() {
			@Override
			public boolean test(ByteBuffer t) {
				return t.limit()<=(maxBytesUsed+1);
			}
		}).toList()) {
			frequencyTable.remove(invalidKeys);
		}
		postLength = frequencyTable.size();
		if(verbose) System.out.println("TABLE REDUCED BY ["+(preLength-postLength)+"] ENTRIES AFTER FILTERING BLOCKS LARGER THAN DICTIONARY LIMIT");
		return blockOptimization(frequencyTable);
	}
	public ByteBuffer findByteBlock(byte[] byteArr, HashMap<ByteBuffer, Integer> frequencyTable, int startIndx) {
		Set<ByteBuffer> repeatingBlocks = frequencyTable.keySet();
		byte[] start_byteArr = new byte[1];
		start_byteArr[0] = byteArr[0];
		ByteBuffer dataBlock = ByteBuffer.wrap(start_byteArr);
		int byteArrSize = byteArr.length;
		for(int i=startIndx+1;i<byteArrSize;i++) {
			if(repeatingBlocks.contains(dataBlock)) {
				frequencyTable.put(dataBlock, frequencyTable.getOrDefault(dataBlock, 0)+1);
				start_byteArr = dataBlock.array();
				dataBlock = ByteBuffer.allocate(i-startIndx+1);
				dataBlock.put(0, start_byteArr);
				dataBlock.position(1);
				continue;
			}
			break;
		}
		return dataBlock;
	}
	private HashMap<ByteBuffer, Integer> blockOptimization(HashMap<ByteBuffer, Integer> frequencyTable) {
		ArrayList<ByteBuffer> sortedBytes = new ArrayList<>(frequencyTable.keySet());
		sortedBytes.sort(new Comparator<ByteBuffer>() {
			@Override
			public int compare(ByteBuffer o1, ByteBuffer o2) {
				if(o1.limit()!=o2.limit()) return o1.limit()-o2.limit();
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
