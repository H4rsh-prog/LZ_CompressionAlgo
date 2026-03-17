package com.compression.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecompressionRequest {
	private ArrayList<String> compressedData;
	private ArrayList<ArrayList<String>> freqSortedHexes;
}
