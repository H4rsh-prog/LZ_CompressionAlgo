package com.compression.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

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

	public ArrayList<ByteArrayWrapper> removeUnusedBytes(byte[] prmv_byteArr, ArrayList<ByteArrayWrapper> sortedBytes) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		while(byteArr.remaining()>0) {
			int indx = byteArr.position();
			if(verbose) System.out.println("COMPRESSING BYTES ["+indx+"/"+byteArr.remaining()+"]");
			for(int i=0;i<sortedBytes.size();i++) {
				byte[] query = sortedBytes.get(i).getData();
				int queryByteSize = Math.min(query.length, byteArr.remaining());
				byte[] searchField = new byte[queryByteSize];
				byteArr.get(indx, searchField);
				if(Arrays.equals(query, searchField)) {
					sortedBytes.get(i).setUsed(true);
					break;
				}
			}
			byteArr.position(indx+1);
		}
		List<ByteArrayWrapper> unusedBytes = sortedBytes.stream().filter(new Predicate<ByteArrayWrapper>() {
			@Override
			public boolean test(ByteArrayWrapper t) {
				return !t.isUsed();
			}
		}).toList();
		sortedBytes.removeAll(unusedBytes);
		return sortedBytes;
	}
	public byte[] startCompression(byte[] prmv_byteArr, ArrayList<ByteArrayWrapper> sortedBytes) {
//		sortedBytes = this.removeUnusedBytes(prmv_byteArr, sortedBytes);
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
//				if(verbose) System.out.println("qeuryByteSize = ["+queryByteSize+"] ; query.length = ["+query.length+"] ; byteArr.remaining() = ["+byteArr.remaining()+"] ; Indx = ["+indx+"]");
				byteArr.get(indx, searchField);
//				if(verbose) System.out.println(ByteArrayWrapper.toString(searchField)+"\n"+ByteArrayWrapper.toString(query));
				if(Arrays.equals(query, searchField)) {
					byteArr = compressBytes(byteArr.array(), indx, i, new StringBuffer("WORKING ON BYTE INDEX ["+indx+"] WITH QUERY "+ByteArrayWrapper.toString(query)+" "));
					this.indiceList.add(indx);
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
	private ByteBuffer compressBytes(byte[] originalBytes, int placeholderIndx, int dictionaryIndx, StringBuffer padding) {
		int querySize = this.sortedBytes.get(dictionaryIndx).getData().length;
		byte[] leftArr, rightArr, midArr;
		midArr = createCompressedbytes(intToByteArr(dictionaryIndx)).array();
		leftArr = Arrays.copyOfRange(originalBytes, 0, placeholderIndx);
		rightArr = Arrays.copyOfRange(originalBytes, placeholderIndx+querySize, originalBytes.length);
		ByteBuffer newBytes = ByteBuffer.allocate(originalBytes.length+(midArr.length-querySize));
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
		buffer.put(1, byteCode);
		buffer.rewind();
		return buffer;
	}
	public byte[] startDecompression(byte[] prmv_byteArr) {
		ByteBuffer byteArr = ByteBuffer.wrap(prmv_byteArr);
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION - START");
		if(verbose) System.out.println("STARTING WITH STRING LENGTH : "+byteArr.limit());
		byte[] midArr;
		System.out.println(this.indiceList);
		for(int i=this.indiceList.size()-1;i>=0;i--) {
			midArr = new byte[1];
			int placementIndx = this.indiceList.get(i);
			midArr[0] = byteArr.get(placementIndx);
			int byteLength = byteArrToInt(new ByteArrayWrapper(midArr));
			midArr = new byte[byteLength];
			byteArr.get(this.indiceList.get(i)+1, midArr);
			int dictionaryIndx = byteArrToInt(new ByteArrayWrapper(midArr));
			System.out.println("DECOMPRESSION ON BYTE INDEX ["+this.indiceList.get(i)+"] WITH QUERY "+ByteArrayWrapper.toString(midArr)+" AT DICT-INDEX ["+dictionaryIndx+"]");
			byteArr = decompressBytes(byteArr.array(), placementIndx, dictionaryIndx, new StringBuffer("DECOMPRESSION ON BYTE INDEX ["+this.indiceList.get(i)+"] WITH QUERY "+ByteArrayWrapper.toString(midArr)+" "));
		}
		if(verbose) System.out.println("STRING DECOMPRESSESD TO LENGTH : "+byteArr.limit());
		if(verbose) System.out.println("``````````````````````````DECOMPRESSION FUNCTION");
		return byteArr.array();
	}
	private ByteBuffer decompressBytes(byte[] compressedBytes, int placementIndx, int dictionaryIndx, StringBuffer padding) {
		byte[] leftArr, rightArr, midArr;
		midArr = new byte[1];
		midArr[0] = compressedBytes[placementIndx];
		int compressedByteLength = byteArrToInt(new ByteArrayWrapper(midArr));
		midArr = this.sortedBytes.get(dictionaryIndx).getData();
		leftArr = Arrays.copyOfRange(compressedBytes, 0, placementIndx);
		rightArr = Arrays.copyOfRange(compressedBytes, placementIndx+compressedByteLength+1, compressedBytes.length);
		ByteBuffer newBytes = ByteBuffer.allocate(compressedBytes.length+(midArr.length-(compressedByteLength+1)));	//LENGTH OF THE COMPRESSED BYTES + 1 FOR BYTE STORING THE LENGTH
		newBytes.put(0, leftArr);
		newBytes.put(placementIndx, midArr);
		newBytes.put(placementIndx+midArr.length, rightArr);
		if(verbose) System.out.println(padding.toString()+"CRUNCHED UP ["+(compressedByteLength+1)+"] BYTES INTO ["+midArr.length+"] BYTES ; UPDATED DIGEST SIZE = ["+newBytes.limit()+"] ; PREVIOUS BYTE STRING : "+ByteArrayWrapper.toString(Arrays.copyOfRange(compressedBytes, placementIndx, placementIndx+compressedByteLength+1))+" ; COMPRESSED BYTE STRING : "+ByteArrayWrapper.toString(midArr));
		return newBytes;
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
