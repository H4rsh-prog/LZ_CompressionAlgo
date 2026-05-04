package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.compression.model.ByteArrayWrapper;

import lombok.Getter;
import lombok.Setter;


@Service
public class CompressionService {
	final byte MARKER = (byte) 0x7F;
	final byte ESCAPE = (byte) 0x7E;

	@Getter @Setter private ArrayList<ByteArrayWrapper> sortedBytes = new ArrayList<>();
	@Setter private boolean verbose = false;
	@Getter private HashMap<Integer, ByteArrayWrapper> cache_intToByteArr = new HashMap<>();
	@Getter private HashMap<ByteArrayWrapper, Integer> cache_byteArrToInt = new HashMap<>();
	
	public byte[] startCompression(byte[] prmv_byteArr, ArrayList<ByteArrayWrapper> sortedBytes) {
		this.sortedBytes=sortedBytes;
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
		return byteArr.array();
	}
	private ByteBuffer findAndReplace(ByteBuffer searchParam, byte[] query, int byteVal, StringBuffer padding) {
		int searchParamSize = searchParam.limit();
		int querySize = query.length;
		byte[] leftArr;
		byte[] rightArr;
		ByteArrayWrapper parsedBytes = escapeMarkerBytes(intToByteArr(byteVal));
		int byteSize = parsedBytes.getData().length;
		ByteBuffer midBuffer = ByteBuffer.allocate(byteSize+4);
		midBuffer.put(0, MARKER);
		midBuffer.put(1, ESCAPE);
		midBuffer.put(2, parsedBytes.getData());
		midBuffer.put(byteSize+2, ESCAPE);
		midBuffer.put(byteSize+3, MARKER);
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
					searchParam = ByteBuffer.allocate(searchParamSize+((byteSize+4)-querySize));	// 3 PADDING BYTES [2: MARKER ; 2:ESCAPE]
					searchParam.put(0, leftArr);
					searchParam.put(i, midArr);
					searchParam.put(i+midArr.length, rightArr);
					searchParam.rewind();
					searchParamSize = searchParam.limit();
					i += (byteSize+3);	// 3 PADDING BYTES [2: MARKER ; 2:ESCAPE]
					if(verbose) System.out.println(padding.toString()+"CRUNCHED UP ["+querySize+"] BYTES INTO ["+midArr.length+"] BYTES AT INDEX ["+i+"] ; UPDATED DIGEST SIZE = ["+searchParam.limit()+"] ; PREVIOUS BYTE STRING : "+ByteArrayWrapper.toString(old_bytes)+" ; COMPRESSED BYTE STRING : "+ByteArrayWrapper.toString(midArr));					
				}
			}
		}
		return searchParam;
	}
	public byte[] startDecompression(byte[] prmv_byteArr) {
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
				while(!(byteArr.get(i+j) == ESCAPE && byteArr.get(i+j+1) == MARKER)) {
					j++;
				}
				ByteArrayWrapper compressedBytes = new ByteArrayWrapper(Arrays.copyOfRange(byteArr.array() ,i+2, i+j)); // i+2 PADDING FOR ONE MARKER + ONE ESCAPE
				rightArr = Arrays.copyOfRange(byteArr.array(), i+j+2, byteArrSize);	//i+j+2 PADDING FOR ONE END MARKER AND ONE ESCAPE
				leftArr = Arrays.copyOfRange(byteArr.array(), 0, i);
				compressedBytes = unescapeMarkerBytes(compressedBytes);
				int parsedInt = byteArrToInt(compressedBytes);
				if(parsedInt>=sortedBytes.size() || parsedInt<0) {continue;}
				midArr = this.sortedBytes.get(parsedInt).getData();
				byteArr = ByteBuffer.allocate(leftArr.length+midArr.length+rightArr.length);
				byteArr.put(0, leftArr);
				byteArr.put(i, midArr);
				byteArr.put(i+midArr.length, rightArr);
				byteArr.rewind();
				byteArrSize = byteArr.limit();
				if(verbose) System.out.println("DECOMPRESSED BYTES [ 127 126 "+compressedBytes.toString()+" 127 ] INTO "+ByteArrayWrapper.toString(midArr)+" AT INDEX ["+i+"] ; DIGEST SIZE EXPANDED [ "+(compressedBytes.getData().length+4)+" -> "+midArr.length+" ] ;  UPDATED DIGEST SIZE = ["+byteArr.limit()+"]");
				i += (midArr.length-(compressedBytes.getData().length+3));	// 2 PADDING BYTES [1: ENDMARKER ; 2:ESCAPE] BECAUSE i SITTING ON STARTMARKER
			}
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return byteArr.array();
	}
	
	//ESCAPING INDEX BYTES (i.e. MARKER AND ESCAPE) SO THAT THE FUNCTIONS DONT MISREAD INDICES AS BYTECODE
	public ByteArrayWrapper escapeMarkerBytes(ByteArrayWrapper rawBytes) {
		byte[] rawByteArr = rawBytes.getData();
		ByteBuffer tempBuffer = ByteBuffer.allocate(rawByteArr.length*2);
		for(byte b : rawByteArr) {
			if(b == MARKER || b == ESCAPE) {
				tempBuffer.put(ESCAPE);
				tempBuffer.put((b==MARKER)?(byte)0x01:(byte)0x00);
			} else {
				tempBuffer.put(b);
			}
		}
		rawByteArr = new byte[tempBuffer.position()];
		tempBuffer.rewind();
		tempBuffer.get(rawByteArr);
		rawBytes.updateArray(rawByteArr);
		return rawBytes;
	}
	//UNESCAPING ESCAPED INDICES AT DECOMPRESSION TO READ THEM
	public ByteArrayWrapper unescapeMarkerBytes(ByteArrayWrapper rawBytes) {
		byte[] rawByteArr = rawBytes.getData();
		ByteBuffer tempBuffer = ByteBuffer.allocate(rawByteArr.length);
		for(int i =0;i<rawByteArr.length;i++) {
			if(rawByteArr[i] == ESCAPE) {
				i++;
				try {
					tempBuffer.put((rawByteArr[i]==(byte)0x01)?MARKER:ESCAPE);
				} catch(Exception e) {
					byte[] temp = new byte[10];
					tempBuffer.get(i-5, temp, 0, 4);
					System.err.println("exception at indx "+i+" surrounding bytes = "+(new ByteArrayWrapper(temp)));
					System.err.println("original bytes = "+new ByteArrayWrapper(rawByteArr));
					e.printStackTrace();
				}
			} else {
				tempBuffer.put(rawByteArr[i]);
			}
		}
		rawByteArr = new byte[tempBuffer.position()];
		tempBuffer.rewind();
		tempBuffer.get(rawByteArr);
		rawBytes.updateArray(rawByteArr);
		return rawBytes;
	}
	
	private int byteArrToInt(ByteArrayWrapper byteArr) {
		if(this.cache_byteArrToInt.containsKey(byteArr)) return this.cache_byteArrToInt.get(byteArr);
		int intVal = 0;
		byte[] bytes = byteArr.getData();
		for(byte b: bytes) {
			intVal = (intVal<<8) | (b&0xFF);
			/*
			 * (intVal<<8) left-shifts the integer bytes 8 bits so that 00000000 00001001 essentially becomes 00001001 00000000
			 * meanwhile (b&0xFF) ensures only the last 8 bits (0xFF) are used such that 11100010 00001001 becomes 00000000 00001001
			 * lastly the bitwise-OR (|) operator appends the last 8 bits extracted from "b" to the 8 bit left-shifted space in intVal
			 * 		i.e 00001001 00000000 (OR) 00000000 00001001 = 00001001 00001001
			 * */
		}
		this.cache_byteArrToInt.put(byteArr, intVal);
		return intVal;
	}
	private ByteArrayWrapper intToByteArr(int intVal) {
		if(this.cache_intToByteArr.containsKey(intVal)) return this.cache_intToByteArr.get(intVal);
		if(intVal==0) {
			ByteArrayWrapper result = new ByteArrayWrapper(new byte[] {0x0});
			this.cache_intToByteArr.put(intVal, result);
			return result;
		}
		// calculating byte length
		String byteString = Integer.toBinaryString(intVal | Integer.MAX_VALUE+1);
		int len = byteString.length();
		char[] binCharArr = byteString.toCharArray();
		for(int i=1;i<len;i++) {
			if(binCharArr[i]=='0') continue;
			byteString = byteString.substring(i);
			break;
		}
		len = ceilDiv(byteString.length(),8);
		//populating bytes
		byte[] result = new byte[len];
		for(int i=len-1;i>=0;i--) {
			result[i] = (byte) (intVal & 0xFF);
			intVal>>=8;
			/*
			 * (intVal & 0xFF) extracts the last 8 bits of the integer and assigns them to i'th index of result
			 * meanwhile intVal>>=8 right shifts those 8 bits such that they are not present for the next iteration
			 * i.e intVal = 01011001 00001001
			 * 01011001 00001001 & 00000000 11111111 = 00000000 00001001
			 * intVal = 01011001 00001001 >> 8 = 00000000 01011001
			 * */
		}
		ByteArrayWrapper wrappedResult = new ByteArrayWrapper(result);
		this.cache_intToByteArr.put(intVal, wrappedResult);
		return wrappedResult;
	}
	// FOR SOME REASON MATH.CEILDIV IS THROWING AN UNRESOLVED EXCEEPTION AS IT COULD NOT FIND IT
	int ceilDiv(int x, int y) {
        final int q = x / y;
        if ((x ^ y) >= 0 && (q * y != x)) {
            return q + 1;
        }
        return q;
    }
}
