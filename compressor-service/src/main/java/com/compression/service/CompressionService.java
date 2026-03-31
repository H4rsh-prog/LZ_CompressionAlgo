package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
		this.sortedBytes = new ArrayList<>(this.sortedBytes.subList(0, Math.min(dictionaryLimit, this.sortedBytes.size())));
	}
	public ByteBuffer startCompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int sortedBytesSize = this.sortedBytes.size();
		for(int i=0;i<sortedBytesSize;i++) {
			if(verbose) System.out.println("COMPRESSING BYTES ["+i+"/"+sortedBytesSize+"]");
//			System.out.println("query = "+printBufferByte(sortedBytes.get(i))+" ; freq = "+byteMapping.get(sortedBytes.get(i)));
			byteArr = findAndReplace(byteArr, sortedBytes.get(i), i, new StringBuffer("WORKING ON BYTE SEQUENCE ["+i+"/"+sortedBytesSize+"] :- "));
		}
		if(verbose) System.out.println("STRING COMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - END");
		return byteArr;
	}
	private ByteBuffer findAndReplace(ByteBuffer searchParam, ByteBuffer query, int byteVal, StringBuffer padding) {
		int searchParamSize = searchParam.limit();
		int querySize = query.limit();
		byte[] leftArr;
		byte[] rightArr;
		ByteBuffer parsedBytes = intToByteArr(byteVal);
		int byteSize = parsedBytes.limit();
		ByteBuffer midBuffer = ByteBuffer.allocate(byteSize+2);
		midBuffer.put(0, (byte) 127);
		midBuffer.put(1, parsedBytes.array());
		midBuffer.put(byteSize+1, (byte) 127);
		byte[] midArr = midBuffer.array();
		for(int i=0;i<searchParamSize;i++) {
			if(searchParam.get(i)==query.get(0)) {
				if(i+querySize-1>=searchParamSize) return searchParam;
				int j;
				for(j=1;j<querySize;j++) {
//					System.out.println("finding "+query.get(j)+", next element is "+searchParam.get(i+j)+" at i = "+i+j);
					if(searchParam.get(i+j)==query.get(j)) {
						continue;
					}
					break;
				}
				if(j==querySize) {
					leftArr = Arrays.copyOfRange(searchParam.array(), 0, i);
					rightArr = Arrays.copyOfRange(searchParam.array(),i+querySize, searchParam.limit());
					searchParam = ByteBuffer.allocate(leftArr.length+midArr.length+rightArr.length);
					searchParam.put(0, leftArr);
					searchParam.put(leftArr.length, midArr);
					searchParam.put(leftArr.length+midArr.length, rightArr);
					searchParam.position(0);
					searchParamSize += (byteSize-querySize+2);
					i += (byteSize+1);
					if(verbose) System.out.println(padding.toString()+"CRUNCHED UP ["+querySize+"] BYTES INTO ["+midArr.length+"] BYTES AT INDEX ["+i+"] ; UPDATED DIGEST SIZE = ["+searchParam.limit()+"]");
				}
			}
		}
		return searchParam;
	}
	private ByteBuffer intToByteArr(int intVal) {
		if(this.cache_intToByteArr.containsKey(intVal)) return this.cache_intToByteArr.get(intVal);
		String byteString = Integer.toBinaryString(intVal | Integer.MAX_VALUE+1);
		ByteBuffer byteArr = ByteBuffer.allocate(0);
		byte[] tempArr;
		char[] binArr = byteString.toCharArray();
		int byteVal = 0;
		boolean opening = false;
		for(int i=1;i<32 && !opening;i++) {
			if(binArr[i]=='0') continue;
			opening = true;
			int k=0;
			for(int j=31;j>=i;j--,k++) {
				if(k==8) {
					k=0;
					tempArr = byteArr.array();
					byteArr = ByteBuffer.allocate(byteArr.limit()+1);
					byteArr.put(0, (byte)byteVal);
					byteArr.put(1, tempArr);
					byteArr.position(0);
					byteVal = 0;
				}
				if(binArr[j]=='1') {
					byteVal += (int) Math.pow(2, k);
				}
			}
			tempArr = byteArr.array();
			byteArr = ByteBuffer.allocate(byteArr.limit()+1);
			byteArr.put(0, (byte)byteVal);
			byteArr.put(1, tempArr);
			byteArr.position(0);
		}
		if(byteArr.limit()==0) {
			byteArr = ByteBuffer.allocate(1);
			byteArr.put(0,(byte)0x0);
		}
		this.cache_intToByteArr.put(intVal, byteArr);
		return byteArr;
	}
	public String printBuffer(ByteBuffer buff) {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		for(byte b : buff.array()) {
			sb.append(Integer.toBinaryString(b | 0x100).substring(1)+" ");
		}
		sb.append("]");
		return sb.toString();
	}
	public String printBufferByte(ByteBuffer buff) {
		StringBuffer sb = new StringBuffer();
		sb.append("[ ");
		for(byte b : buff.array()) {
			sb.append(b+" ");
		}
		sb.append("]");
		return sb.toString();
	}
	public ByteBuffer startDecompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int byteArrSize = byteArr.limit();
		byte[] leftArr;
		byte[] rightArr;
		byte[] midArr;
		for(int i=0;i<byteArrSize;i++) {
			if(byteArr.get(i) == (byte) 127) {
				int j = 1;
				while(byteArr.get(i+j) != (byte) 127) {
					j++;
				}
				ByteBuffer compressedBytes = ByteBuffer.wrap(Arrays.copyOfRange(byteArr.array() ,i+1, i+j));
				rightArr = Arrays.copyOfRange(byteArr.array(), i+j+1, byteArr.limit());
				leftArr = Arrays.copyOfRange(byteArr.array(), 0, i);
				midArr = this.sortedBytes.get(byteArrToInt(compressedBytes)).array();
				byteArr = ByteBuffer.allocate(leftArr.length+midArr.length+rightArr.length);
				byteArr.put(0, leftArr);
				byteArr.put(leftArr.length, midArr);
				byteArr.put(leftArr.length+midArr.length, rightArr);
				byteArr.position(0);
				byteArrSize += midArr.length-(j+1);
				i += midArr.length-1;
			}
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return byteArr;
	}
	private int byteArrToInt(ByteBuffer byteArr) {
		System.out.println(printBuffer(byteArr));
		if(this.cache_byteArrToInt.containsKey(byteArr)) return this.cache_byteArrToInt.get(byteArr);
		int intVal = 0;
		String byteString = "";
		for(byte b : byteArr.array()) {
			byteString = Integer.toBinaryString(b | 256).substring(1) + byteString;
		}
		System.out.println(byteString);
		char[] binArr = byteString.toCharArray();
		for(int i=binArr.length-1,k=0;i>-1;i--,k++) {
			if(binArr[i]=='1') {
				intVal += (int) Math.pow(2, k);
			}
		}
		System.out.println(intVal);
		this.cache_byteArrToInt.put(byteArr, intVal);
		return intVal;
	}
//	private void genReverseCacheFromInt(HashMap<Integer, ArrayList<Byte>> cache_intToByteArr) {
//		this.cache_intToByteArr = cache_intToByteArr;
//		this.cache_byteArrToInt.clear();
//		for(Entry<Integer, ArrayList<Byte>> e : cache_intToByteArr.entrySet()) {
//			this.cache_byteArrToInt.put(e.getValue(), e.getKey());
//		}
//	}
//	private void genReverseCachefromByte(HashMap<ArrayList<Byte>, Integer> cache_byteArrToInt) {
//		this.cache_byteArrToInt = cache_byteArrToInt;
//		this.cache_intToByteArr.clear();
//		for(Entry<ArrayList<Byte>, Integer> e : cache_byteArrToInt.entrySet()) {
//			this.cache_intToByteArr.put(e.getValue(), e.getKey());
//		}
//	}
}
