package com.compression.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompressionMetadata {
	private ArrayList<ByteArrayWrapper> sortedBytes;
}
