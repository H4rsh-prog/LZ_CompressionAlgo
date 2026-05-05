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
	@Getter @Setter private ArrayList<Integer> indiceList = new ArrayList<>();
	@Setter private boolean verbose = false;
	@Getter private HashMap<Integer, ByteArrayWrapper> cache_intToByteArr = new HashMap<>();
	@Getter private HashMap<ByteArrayWrapper, Integer> cache_byteArrToInt = new HashMap<>();
	
	public byte[] startCompression(byte[] prmv_byteArr, ArrayList<ByteArrayWrapper> sortedBytes) {
		this.sortedBytes.clear();
		this.indiceList.clear();
		this.sortedBytes=sortedBytes;
		int dictionarySize = sortedBytes.size();
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		while(byteArr.remaining()>0) {
			int indx = byteArr.position();
			if(verbose) System.out.println("COMPRESSING BYTES ["+indx+"/"+byteArr.remaining()+"]");
			for(int i=0;i<dictionarySize;i++) {
				byte[] query = sortedBytes.get(i).getData();
				int queryByteSize = Math.min(query.length, byteArr.remaining());
				byte[] searchField = new byte[queryByteSize];
				byteArr.get(searchField, indx, queryByteSize);
				if(Arrays.equals(query, searchField)) {
					byteArr = replaceBytes(byteArr.array(), indx, i, new StringBuffer("[WORKING ON BYTE INDEX ["+indx+"] WITH QUERY ["+ByteArrayWrapper.toString(query)+"] ]"));
					indx += intToByteArr(i).getData().length;
					break;
				}
			}
			byteArr.position(indx+1);
		}
		if(verbose) System.out.println("STRING COMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````COMPRESSION FUNCTION - END");
		return byteArr.array();
	}
	private ByteBuffer replaceBytes(byte[] originalBytes, int placeholderIndx, int dictionaryIndx, StringBuffer padding) {
		int querySize = this.sortedBytes.get(dictionaryIndx).getData().length;
		byte[] leftArr, rightArr, midArr;
		midArr = createCompressedbytes(intToByteArr(dictionaryIndx)).array();
		leftArr = Arrays.copyOfRange(originalBytes, 0, placeholderIndx);
		rightArr = Arrays.copyOfRange(originalBytes, dictionaryIndx+querySize, originalBytes.length);
		ByteBuffer newBytes = ByteBuffer.allocate(originalBytes.length-(querySize-midArr.length));
		newBytes.put(0, leftArr);
		newBytes.put(placeholderIndx, midArr);
		newBytes.put(placeholderIndx+midArr.length, rightArr);
		if(verbose) System.out.println(padding.toString()+"CRUNCHED UP ["+querySize+"] BYTES INTO ["+midArr.length+"] BYTES ; UPDATED DIGEST SIZE = ["+newBytes.limit()+"] ; PREVIOUS BYTE STRING : "+ByteArrayWrapper.toString(Arrays.copyOfRange(originalBytes, placeholderIndx, placeholderIndx+querySize))+" ; COMPRESSED BYTE STRING : "+ByteArrayWrapper.toString(midArr));
		return newBytes;
	}
	private ByteBuffer createCompressedbytes(ByteArrayWrapper parsedBytes) {
		byte[] byteCode = parsedBytes.getData();
		int byteSize = byteCode.length;
		if(byteSize>254) throw new RuntimeException("Byte Length Overflow");
		ByteBuffer buffer = ByteBuffer.allocate(byteSize+1);
		buffer.put(0, intToByteArr(byteSize).getData()[0]);
		buffer.put(1, parsedBytes.getData());
		buffer.rewind();
		return buffer;
	}
	public byte[] startDecompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		int byteArrSize = prmv_byteArr.length;
		byte[] leftArr;
		byte[] rightArr;
		byte[] midArr;
		ArrayList<Integer> reversedIndices = this.indiceList.reversed();
		for(Integer i : reversedIndices) {
			midArr = new byte[1];
			midArr[0] = byteArr.get(i);
			int byteLength = byteArrToInt(new ByteArrayWrapper(midArr));
			midArr = new byte[byteLength];
			byteArr.get(i+1, midArr);
			int dictionaryIndx = byteArrToInt(new ByteArrayWrapper(midArr));
			byteArr = replaceBytes(byteArr.array(), i, dictionaryIndx, new StringBuffer("[DECOMPRESSION ON BYTE INDEX ["+i+"] WITH QUERY ["+ByteArrayWrapper.toString(midArr)+"] ]"));
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
