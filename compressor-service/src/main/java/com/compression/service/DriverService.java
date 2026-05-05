package com.compression.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
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
	public byte[] compressFileSinglePhase(File file) throws IOException {
		String fileName = file.getName();
		String fileDirectory = file.getAbsolutePath();
		byte[] byteCode = this.fileHandler.readFileByte(file);
		ArrayList<ByteArrayWrapper> sortedBytes = this.dictionaryHandler.createDictionary(byteCode);
		byteCode = this.compressionHandler.startCompression(byteCode, sortedBytes);
		try {
			this.fileHandler.writeFileByte(byteCode, new File(fileDirectory+"\\"+fileName+".compressed"));
			this.fileHandler.writeObjectByte(sortedBytes, new File(fileDirectory+"\\"+fileName+".compressed.metadata"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteCode;
	}
	public byte[] decompressFileSinglePhase(File file) throws IOException, ClassNotFoundException {
		String fileName = file.getName();
		if(!fileName.endsWith(".compressed")) {
			System.err.println("unrecognized file naming scheme file type mismatch may occur");
		}
		String fileDirectory = file.getAbsolutePath();
		byte[] byteCode = this.fileHandler.readFileByte(file);
		ArrayList<ByteArrayWrapper> sortedBytes = null;
		sortedBytes = this.fileHandler.readObjectByte(new File(fileDirectory+".metadata"), sortedBytes.getClass());
		if(sortedBytes == null) {
			System.err.println("dictionary not found");
		} else {
			this.compressionHandler.setSortedBytes(sortedBytes);
			byteCode = this.compressionHandler.startDecompression(byteCode);
			try {
				this.fileHandler.writeFileByte(byteCode, new File((fileName.endsWith(".compressed")?fileDirectory.substring(0,fileDirectory.length()-11):fileDirectory.concat(".decompressed"))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return byteCode;
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
			currentBytes = this.compressionHandler.escapeMarkerBytes(new ByteArrayWrapper(currentBytes)).getData();
			sortedBytes = this.dictionaryHandler.createDictionary(currentBytes);
			try {
				currentBytes = this.compressionHandler.startCompression(currentBytes, sortedBytes);
			} finally {
				FileOutputStream fos =new FileOutputStream(new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\compressed_phase_"+itr));
				fos.write(currentBytes);
			}
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
			try {
				currentBytes = this.compressionHandler.startDecompression(currentBytes);
			} finally {
				FileOutputStream fos =new FileOutputStream(new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\decompressed_phase_"+i));
				fos.write(currentBytes);
			}
			currentBytes = this.compressionHandler.unescapeMarkerBytes(new ByteArrayWrapper(currentBytes)).getData();
		}
		System.out.println("DECOMPRESSED FILE BACK TO "+currentBytes.length+" BYTES");
		return currentBytes;
	}
}
