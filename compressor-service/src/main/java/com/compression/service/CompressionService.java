package com.compression.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.compression.model.BinaryTreeNODE;

import lombok.Getter;
import lombok.Setter;


@Service
public class CompressionService {
	private HashMap<ArrayList<Byte>, Integer> byteMapping = new HashMap<>();
	@Getter @Setter private ArrayList<ArrayList<Byte>> sortedBytes = new ArrayList<>();
	@Setter private boolean verbose = false;
	@Setter static int dictionaryLimit = 254;
	private HashMap<Integer, ArrayList<Byte>> byteArrCache = new HashMap<>();
	
	public void generateSortedBytesFromFrequency(HashMap<ArrayList<Byte>, Integer> frequencyTable) {
		this.byteMapping = frequencyTable;
		this.sortedBytes.addAll(this.byteMapping.keySet());
		this.sortedBytes.sort(new Comparator<ArrayList<Byte>>() {
			@Override
			public int compare(ArrayList<Byte> o1, ArrayList<Byte> o2) {
				if(o1.size()!=o2.size()) return o1.size()-o2.size();
				return frequencyTable.get(o1).intValue()-frequencyTable.get(o2).intValue();
			}
		});
		this.sortedBytes = new ArrayList<>(this.sortedBytes.subList(0, dictionaryLimit));
	}
	public ArrayList<Byte> startCompression(ArrayList<Byte> byteArr) {
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.size());
		int sortedBytesSize = this.sortedBytes.size();
		for(int i=0;i<sortedBytesSize;i++) {
			System.out.println("COMPRESSING HEXES ["+i+"/"+sortedBytesSize+"]");
			findAndReplace(byteArr, sortedBytes.get(i), i);
		}
		if(verbose) System.out.println("STRING COMPRESSESD TO LENGTH : "+byteArr.size());
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - END");
		return byteArr;
	}
	private void findAndReplace(ArrayList<Byte> searchParam, ArrayList<Byte> query, int byteVal) {
		int searchParamSize = searchParam.size();
		int querySize = query.size();
		for(int i=0;i<searchParamSize;i++) {
			if(searchParam.get(i).equals(query.get(0))) {
				if(i+querySize-1>=searchParamSize) return;
				int j;
				for(j=1;j<querySize;j++) {
					if(searchParam.get(i+j).equals(query.get(j))) {
						continue;
					}
					break;
				}
				if(j==querySize) {
					for(j=0;j<querySize;j++) {
						searchParam.remove(i);
					}
					ArrayList<Byte> parsedBytes = intToByteArr(byteVal);
					int byteSize = parsedBytes.size();
					searchParam.add(i, (byte) 127);
					searchParam.addAll(i, parsedBytes);
					searchParam.add(i, (byte) 127);
					searchParamSize -= querySize;
					searchParamSize += (byteSize+2);
					i += (byteSize+1);
				}
			}
		}
	}
	private ArrayList<Byte> intToByteArr(int intVal) {
		if(this.byteArrCache.containsKey(intVal)) return this.byteArrCache.get(intVal);
		String byteString = Integer.toBinaryString(intVal | Integer.MAX_VALUE+1);
		ArrayList<Byte> byteArr = new ArrayList<>();
		char[] binArr = byteString.toCharArray();
		int byteVal = 0;
		for(int i=1;i<32;i++) {
			if(binArr[i]=='0') continue;
			int k=0;
			for(int j=31;j>=i;j--,k++) {
				if(k==8) {
					k = 0;
					byteArr.add(0, (byte) byteVal);;
					byteVal = 0;
				}
				if(binArr[j]=='1') {
					byteVal += (int) Math.pow(2, k);
				}
			}
			byteVal += (int) Math.pow(2, (k%8));
			byteArr.add(0, (byte) byteVal);;
		}
		this.byteArrCache.put(intVal, byteArr);
		return byteArr;
	}
	public ArrayList<Byte> startDecompression(ArrayList<Byte> byteArr) {
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.size());
		int byteArrSize = byteArr.size();
		for(int i=0;i<byteArrSize;i++) {
			if(byteArr.get(i) == (byte) 127) {
				int j = 1;
				while(byteArr.get(i+j) != (byte) 127) {
					j++;
				}
				ArrayList<Byte> compressedBytes = byteArr.subList(i+1, i+j);
				byteArr.remove(i);
				byteArr.addAll(i, sortedHexes.get(byteValue));
			}
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.size());
		if(verbose) System.err.println(hexArr);
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return hexArr;
	}
	public static int calcByteFromHex(String hex){
		Set<Character> hexChars = new HashSet<>(Set.of('a','b','c','d','e','f'));
		int byteValue = 0;
		for(int j=hex.length()-1,k=0;j>=0;j--,k++) {
			int bitValue = 0;
			char hexChar = hex.charAt(j);
			if(hexChars.contains(hexChar)) {
				switch (hexChar) {
				case 'a':
					bitValue = 10;
					break;
				case 'b':
					bitValue = 11;
					break;
				case 'c':
					bitValue = 12;
					break;
				case 'd':
					bitValue = 13;
					break;
				case 'e':
					bitValue = 14;
					break;
				default:
					bitValue = 15;
				}
			} else {
				bitValue = Integer.parseInt(String.valueOf(hexChar));
			}
			byteValue += (bitValue*(Math.pow(16, k)));
		}
		return byteValue;
	}
}
