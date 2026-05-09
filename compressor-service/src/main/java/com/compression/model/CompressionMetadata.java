package com.compression.model;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompressionMetadata implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 688194584119399061L;
	private ArrayList<ByteArrayWrapper> sortedBytes;
	private ArrayList<Integer> compressedIndices;
}
