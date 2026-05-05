package com.compression.model;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.Getter;

public class ByteArrayWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7612020715874677345L;
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
    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[ ");
    	for(byte b : data) {
    		sb.append(b+" ");
    	}
    	sb.append("]");
    	return sb.toString();
    }
    public static String toString(byte[] data) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[ ");
    	for(byte b : data) {
    		sb.append(b+" ");
    	}
    	sb.append("]");
    	return sb.toString();
    }
    public static String toString(ByteBuffer data) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[ ");
    	for(byte b : data.array()) {
    		sb.append(b+" ");
    	}
    	sb.append("]");
    	return sb.toString();
    }
}
