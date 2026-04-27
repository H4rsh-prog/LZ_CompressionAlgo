package com.compression.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;


import org.springframework.stereotype.Service;

import com.compression.model.ByteArrayWrapper;
import com.compression.model.CompressionMetadata;

import lombok.Getter;
import lombok.Setter;

@Service
public class DriverService {
	private FileHandlingService fileHandler = new FileHandlingService();
	private DictionaryService dictionaryHandler = new DictionaryService();
	private CompressionService compressionHandler = new CompressionService();
	@Getter @Setter private ArrayList<CompressionMetadata> compressionHistory = new ArrayList<>();
	
	public void setVerbosity(boolean file, boolean dictionary, boolean compression) {
		this.fileHandler.setVerbose(file);
		this.dictionaryHandler.setVerbose(dictionary);
		this.compressionHandler.setVerbose(compression);
	}
	
	public byte[] compressFile(File file, double threshold) throws IOException {
		if(threshold<5) threshold = 5;
		byte[] currentBytes = fileHandler.readFileByte(file);
		double compressionRate = 100;
		int originalBytes = currentBytes.length;
		int byteSize = originalBytes;
		ArrayList<ByteArrayWrapper> sortedBytes;
		int itr=1;
		while(!(compressionRate<threshold)) {
			System.out.println("compression itr = "+itr);
			sortedBytes = this.dictionaryHandler.createDictionary(currentBytes);
			currentBytes = this.compressionHandler.startCompression(currentBytes, sortedBytes);
			compressionRate = 100-(((double)currentBytes.length)/byteSize)*100;
			System.out.println("ITR ["+itr+"] : reductedBytes = ["+(byteSize - currentBytes.length)+"] ; byteSize = [old = ["+byteSize+"] ; new = ["+(currentBytes.length)+"]] ; compressionRate = ["+compressionRate+"]");
			byteSize = currentBytes.length;
			this.compressionHistory.add(new CompressionMetadata(sortedBytes));
			itr++;
			if(itr>10) break;
		}
		System.out.println("COMPRESSION STOPPED AFTER "+itr+" ITERATIONS WITH THE COMPRESSION RATE ON THE LAST ITERATION ["+compressionRate+"%] AND TOTAL COMPRESSION OF ["+(100-((((double)byteSize)/originalBytes)*100))+"%]");
		System.out.println(this.compressionHistory.stream().map(new Function<CompressionMetadata, String>() {

			@Override
			public String apply(CompressionMetadata t) {
				return "+ ["+t.getSortedBytes().size()+"] ELEMENTS";
			}
		}).toList());
		return currentBytes;
	}
	public byte[] decompressFile(File file) throws IOException {
		byte[] currentBytes = this.fileHandler.readFileByte(file);
		ArrayList<ByteArrayWrapper> sortedBytes;
		for(int i=this.compressionHistory.size()-1;i>=0;i--) {
			System.out.println("decompression itr = "+(i+1));
			sortedBytes = this.compressionHistory.get(i).getSortedBytes();
			this.compressionHandler.setSortedBytes(sortedBytes);
			currentBytes = this.compressionHandler.startDecompression(currentBytes);
		}
		System.out.println("DECOMPRESSED FILE BACK TO "+currentBytes.length+" BYTES");
		return currentBytes;
	}
}
