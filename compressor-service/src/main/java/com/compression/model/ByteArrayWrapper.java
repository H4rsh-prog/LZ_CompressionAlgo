package com.compression.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.Getter;

public class ByteArrayWrapper {
	@Getter private byte[] data;
	public ByteArrayWrapper(ByteBuffer buf) {
		updateArray(buf);
	}
	public ByteArrayWrapper(byte[] byteArr) {
		updateArray(byteArr);
	}
	public void updateArray(ByteBuffer buf) {
		data = new byte[buf.remaining()];
		buf.get(data);
		buf.rewind();
	}
	public void updateArray(byte[] byteArr) {
		data = byteArr;
	}
	
    @Override public int hashCode() { return Arrays.hashCode(data); }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ByteArrayWrapper other = (ByteArrayWrapper) obj;
        return Arrays.equals(data, other.data);
    }
}
