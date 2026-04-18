package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.compression.model.ByteArrayWrapper;

import lombok.Getter;
import lombok.Setter;


@Service
public class CompressionService {
	final byte MARKER = (byte) 0x7F;
	final byte ESCAPE = (byte) 0x7E;

	private HashMap<ByteArrayWrapper, Integer> byteMapping = new HashMap<>();
	@Getter @Setter private ArrayList<ByteArrayWrapper> sortedBytes = new ArrayList<>();
	@Setter private boolean verbose = false;
	@Setter static int dictionaryLimit = 254;
	@Getter private HashMap<Integer, ByteArrayWrapper> cache_intToByteArr = new HashMap<>();
	@Getter private HashMap<ByteArrayWrapper, Integer> cache_byteArrToInt = new HashMap<>();
	
	public void generateSortedBytesFromFrequency(HashMap<ByteArrayWrapper, Integer> frequencyTable) {
		this.byteMapping = frequencyTable;
		this.sortedBytes.addAll(this.byteMapping.keySet());
		this.sortedBytes.sort(new Comparator<ByteArrayWrapper>() {
			@Override
			public int compare(ByteArrayWrapper o1, ByteArrayWrapper o2) {
				if(o2.getData().length!=o1.getData().length) return o2.getData().length-o1.getData().length;
				return frequencyTable.get(o2).intValue()-frequencyTable.get(o1).intValue();
			}
		});
		this.sortedBytes = new ArrayList<>(this.sortedBytes.subList(0, Math.min(dictionaryLimit, this.sortedBytes.size())));	//LIMIT ENTRIES
	}
	public ByteBuffer startCompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int sortedBytesSize = this.sortedBytes.size();
		for(int i=0;i<sortedBytesSize;i++) {
			if(verbose) System.out.println("COMPRESSING BYTES ["+i+"/"+sortedBytesSize+"]");
			byteArr = findAndReplace(byteArr, sortedBytes.get(i).getData(), i, new StringBuffer("WORKING ON BYTE SEQUENCE ["+i+"/"+sortedBytesSize+"] :- "));
		}
		if(verbose) System.out.println("STRING COMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - END");
		return byteArr;
	}
	private ByteBuffer findAndReplace(ByteBuffer searchParam, byte[] query, int byteVal, StringBuffer padding) {
		int searchParamSize = searchParam.limit();
		int querySize = query.length;
		byte[] leftArr;
		byte[] rightArr;
		ByteArrayWrapper parsedBytes = intToByteArr(byteVal);
		int byteSize = parsedBytes.getData().length;
		ByteBuffer midBuffer = ByteBuffer.allocate(byteSize+3);
		midBuffer.put(0, MARKER);
		midBuffer.put(1, ESCAPE);
		midBuffer.put(2, parsedBytes.getData());
		midBuffer.put(byteSize+2, MARKER);
		midBuffer.rewind();
		byte[] midArr = midBuffer.array();
		for(int i=0;i<searchParamSize;i++) {
			if(searchParam.get(i)==query[0]) {
				if(i+querySize-1>=searchParamSize) return searchParam;
				int j;
				for(j=1;j<querySize;j++) {
					if(searchParam.get(i+j)==query[j]) {
						continue;
					}
					break;
				}
				if(j==querySize) {
					leftArr = Arrays.copyOfRange(searchParam.array(), 0, i);
					rightArr = Arrays.copyOfRange(searchParam.array(),i+querySize, searchParamSize);
					byte[] old_bytes = Arrays.copyOfRange(searchParam.array(), i, i+querySize);
					searchParam = ByteBuffer.allocate(searchParamSize+((byteSize+3)-querySize));	// 3 PADDING BYTES [2: MARKER ; 1:ESCAPE]
					searchParam.put(0, leftArr);
					searchParam.put(i, midArr);
					searchParam.put(i+midArr.length, rightArr);
					searchParam.rewind();
					searchParamSize = searchParam.limit();
					i += (byteSize+3);	// 3 PADDING BYTES [2: MARKER ; 1:ESCAPE]
					if(verbose) System.out.println(padding.toString()+"CRUNCHED UP ["+querySize+"] BYTES INTO ["+midArr.length+"] BYTES AT INDEX ["+i+"] ; UPDATED DIGEST SIZE = ["+searchParam.limit()+"] ; PREVIOUS BYTE STRING : "+ByteArrayWrapper.toString(old_bytes)+" ; COMPRESSED BYTE STRING : "+ByteArrayWrapper.toString(midArr));					
				}
			}
		}
		return searchParam;
	}
	private ByteArrayWrapper intToByteArr(int intVal) {
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
		ByteArrayWrapper wrappedArr = new ByteArrayWrapper(byteArr);
		this.cache_intToByteArr.put(intVal, wrappedArr);
		return wrappedArr;
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
		int byteArrSize = prmv_byteArr.length;
		byte[] leftArr;
		byte[] rightArr;
		byte[] midArr;
		for(int i=0;i<byteArrSize-2;i++) {
			if(byteArr.get(i) == MARKER && byteArr.get(i+1) == ESCAPE) {
				int j = 2;	// PADDING FOR ONE MARKER + ONE ESCAPE
				while(byteArr.get(i+j) != MARKER) {
					j++;
				}
				ByteArrayWrapper compressedBytes = new ByteArrayWrapper(Arrays.copyOfRange(byteArr.array() ,i+2, i+j)); // i+2 PADDING FOR ONE MARKER + ONE ESCAPE
				rightArr = Arrays.copyOfRange(byteArr.array(), i+j+1, byteArrSize);	//i+j+1 PADDING FOR ONE END MARKER
				leftArr = Arrays.copyOfRange(byteArr.array(), 0, i);
				int parsedInt = byteArrToInt(compressedBytes);
				if(parsedInt>=sortedBytes.size() || parsedInt<0) {continue;}
				
				//BREAK AND CHECK

//				System.out.println("traversing "+compressedBytes.toString());
//				for(j=i-2;j<i+(compressedBytes.getData().length+3)+2;j++) {
//					System.out.println(byteArr.get(j));
//				}
				
				
				midArr = this.sortedBytes.get(parsedInt).getData();
				byteArr = ByteBuffer.allocate(leftArr.length+midArr.length+rightArr.length);
				byteArr.put(0, leftArr);
				byteArr.put(i, midArr);
				byteArr.put(i+midArr.length, rightArr);
				byteArr.rewind();
				byteArrSize = byteArr.limit();
				

				//BREAK AND CHECK
				
//				System.out.println("decompressed to "+ByteArrayWrapper.toString(midArr));
//				for(j=i-2;j<i+(midArr.length)+2;j++) {
//					System.out.println(byteArr.get(j));
//				}
				
				
				if(verbose) System.out.println("DECOMPRESSED BYTES [ 127 126 "+compressedBytes.toString()+" 127 ] INTO "+ByteArrayWrapper.toString(midArr)+" AT INDEX ["+i+"] ; DIGEST SIZE EXPANDED [ "+(compressedBytes.getData().length+3)+" -> "+midArr.length+" ] ;  UPDATED DIGEST SIZE = ["+byteArr.limit()+"]");
				i += (midArr.length-(compressedBytes.getData().length+2));	// 2 PADDING BYTES [1: ENDMARKER ; 1:ESCAPE] BECAUSE i SITTING ON STARTMARKER
			}
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return byteArr;
	}
	private int byteArrToInt(ByteArrayWrapper byteArr) {
		if(this.cache_byteArrToInt.containsKey(byteArr)) return this.cache_byteArrToInt.get(byteArr);
		int intVal = 0;
		String byteString = "";
		for(byte b : byteArr.getData()) {
			byteString = Integer.toBinaryString(b | 256).substring(1) + byteString;
		}
		if(byteString=="") return -1;
		char[] binArr = byteString.toCharArray();
		for(int i=binArr.length-1,k=0;i>-1;i--,k++) {
			if(binArr[i]=='1') {
				intVal += (int) Math.pow(2, k);
			}
		}
		if(verbose) System.out.println(byteString+" -> "+intVal);
		this.cache_byteArrToInt.put(byteArr, intVal);
		return intVal;
	}
}
