package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;


@Service
public class CompressionService {
	private HashMap<ByteBuffer, Integer> byteMapping = new HashMap<>();
	@Getter @Setter private ArrayList<ByteBuffer> sortedBytes = new ArrayList<>();
	@Setter private boolean verbose = false;
	@Setter static int dictionaryLimit = 254;
	@Getter private HashMap<Integer, ByteBuffer> cache_intToByteArr = new HashMap<>();
	@Getter private HashMap<ByteBuffer, Integer> cache_byteArrToInt = new HashMap<>();
	
	public void generateSortedBytesFromFrequency(HashMap<ByteBuffer, Integer> frequencyTable) {
		this.byteMapping = frequencyTable;
		this.sortedBytes.addAll(this.byteMapping.keySet());
		this.sortedBytes.sort(new Comparator<ByteBuffer>() {
			@Override
			public int compare(ByteBuffer o1, ByteBuffer o2) {
				if(o1.limit()!=o2.limit()) return o1.limit()-o2.limit();
				return frequencyTable.get(o1).intValue()-frequencyTable.get(o2).intValue();
			}
		});
		this.sortedBytes = new ArrayList<>(this.sortedBytes.subList(0, dictionaryLimit));
	}
	public ByteBuffer startCompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int sortedBytesSize = this.sortedBytes.size();
		for(int i=0;i<sortedBytesSize;i++) {
			System.out.println("COMPRESSING HEXES ["+i+"/"+sortedBytesSize+"]");
			findAndReplace(byteArr, sortedBytes.get(i), i);
		}
		if(verbose) System.out.println("STRING COMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - END");
		return byteArr;
	}
	private void findAndReplace(ByteBuffer searchParam, ByteBuffer query, int byteVal) {
		int searchParamSize = searchParam.limit();
		int querySize = query.limit();
		byte[] leftArr;
		byte[] rightArr;
		byte[] midArr;
		ByteBuffer midBuffer;
		for(int i=0;i<searchParamSize;i++) {
			if(searchParam.get(i)==query.get(0)) {
				if(i+querySize-1>=searchParamSize) return;
				int j;
				for(j=1;j<querySize;j++) {
					if(searchParam.get(i+j)==query.get(j)) {
						continue;
					}
					break;
				}
				if(j==querySize) {
					ByteBuffer parsedBytes = intToByteArr(byteVal);
					int byteSize = parsedBytes.limit();
					leftArr = searchParam.slice(0, i+1).array();
					rightArr = searchParam.slice(i+querySize, searchParam.limit()).array();
					midBuffer = ByteBuffer.allocate(byteSize+2);
					midBuffer.put(0, (byte) 127);
					midBuffer.put(1, parsedBytes.array());
					midBuffer.put(byteSize+1, (byte) 127);
					midArr = midBuffer.array();
					searchParam = ByteBuffer.allocate(leftArr.length+midArr.length+rightArr.length);
					searchParam.put(0, leftArr);
					searchParam.put(leftArr.length, midArr);
					searchParam.put(leftArr.length+midArr.length, rightArr);
					searchParam.position(1);
					
					searchParamSize += (byteSize-querySize+2);
					i += (byteSize+1);
				}
			}
		}
	}
	public ByteBuffer startDecompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int byteArrSize = byteArr.limit();
		for(int i=0;i<byteArrSize;i++) {
			if(byteArr.get(i) == (byte) 127) {
				int j = 1;
				while(byteArr.get(i+j) != (byte) 127) {
					j++;
				}
				ByteBuffer compressedBytes = byteArr.slice(i+1, i+j);
				for(int k=i;k<i+j+1;k++) {
					byteArr.remove(k);
				}
				byteArrSize -= (j+1);
				int byteIndx = byteArrToInt(compressedBytes);
				ArrayList<Byte> uncompressedBytes = sortedBytes.get(byteIndx);
				int uncompressedBytesSize = uncompressedBytes.size();
				byteArr.addAll(i, uncompressedBytes);
				byteArrSize += uncompressedBytesSize;
				i += uncompressedBytesSize-1;
			}
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.size());
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return byteArr;
	}
	private ByteBuffer intToByteArr(int intVal) {
		if(this.cache_intToByteArr.containsKey(intVal)) return this.cache_intToByteArr.get(intVal);
		String byteString = Integer.toBinaryString(intVal | Integer.MAX_VALUE+1);
		ByteBuffer byteArr = ByteBuffer.allocate(0);
		byte[] tempArr;
		char[] binArr = byteString.toCharArray();
		int byteVal = 0;
		for(int i=1;i<32;i++) {
			if(binArr[i]=='0') continue;
			int k=0;
			for(int j=31;j>=i;j--,k++) {
				if(k==8) {
					k = 0;
					tempArr = byteArr.array();
					byteArr = ByteBuffer.allocate(byteArr.limit()+1);
					byteArr.put(0, (byte)byteVal);
					byteArr.put(1, tempArr);
					byteArr.position(1);
					byteVal = 0;
				}
				if(binArr[j]=='1') {
					byteVal += (int) Math.pow(2, k);
				}
			}
			byteVal += (int) Math.pow(2, (k%8));
			tempArr = byteArr.array();
			byteArr = ByteBuffer.allocate(byteArr.limit()+1);
			byteArr.put(0, (byte)byteVal);
			byteArr.put(1, tempArr);
			byteArr.position(1);
		}
		this.cache_intToByteArr.put(intVal, byteArr);
		return byteArr;
	}
	private int byteArrToInt(ByteBuffer byteArr) {
		if(this.cache_byteArrToInt.containsKey(byteArr)) return this.cache_byteArrToInt.get(byteArr);
		int intVal = 0;
		String byteString = "";
		for(Byte b : byteArr) {
			byteString = Integer.toBinaryString(b | 256).substring(1) + byteString;
		}
		char[] binArr = byteString.toCharArray();
		for(int i=binArr.length-1,k=0;i>-1;i--,k++) {
			if(binArr[i]=='1') {
				intVal += (int) Math.pow(2, k);
			}
		}
		this.cache_byteArrToInt.put(byteArr, intVal);
		return intVal;
	}
	private void genReverseCacheFromInt(HashMap<Integer, ArrayList<Byte>> cache_intToByteArr) {
		this.cache_intToByteArr = cache_intToByteArr;
		this.cache_byteArrToInt.clear();
		for(Entry<Integer, ArrayList<Byte>> e : cache_intToByteArr.entrySet()) {
			this.cache_byteArrToInt.put(e.getValue(), e.getKey());
		}
	}
	private void genReverseCachefromByte(HashMap<ArrayList<Byte>, Integer> cache_byteArrToInt) {
		this.cache_byteArrToInt = cache_byteArrToInt;
		this.cache_intToByteArr.clear();
		for(Entry<ArrayList<Byte>, Integer> e : cache_byteArrToInt.entrySet()) {
			this.cache_intToByteArr.put(e.getValue(), e.getKey());
		}
	}
}
