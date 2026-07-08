package com.compression.service;

import java.nio.ByteBuffer;

import com.compression.model.ByteArrayWrapper;

public class VarIntegerBytes {
	public static byte[] encode(int value)  {
		System.out.print(value + " => ");
		byte[] buffer = new byte[5];
		int indx = 0;
		while((value & 0xFFFFFF80) != 0) {
			buffer[indx++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		buffer[indx++] = (byte) (value & 0x7F);
		byte[] result = new byte[indx];
		System.arraycopy(buffer, 0, result, 0, indx);
		System.out.println(ByteArrayWrapper.toStringBin(result));
		return result;
	}
	public static int decode(byte[] bytes) {
		int value = 0;
		int shift = 0;
		byte b;
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		while(buffer.hasRemaining()) {
			b = buffer.get();
			value |= (b & 0x7F) << shift;
			if((b & 0x80) == 0) return value;
			shift += 7;
		}
		return 0;
	}
}
