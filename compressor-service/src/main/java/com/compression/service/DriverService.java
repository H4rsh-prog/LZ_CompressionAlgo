package com.compression.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		byteCode = this.compressionHandler.startCompression(byteCode, this.dictionaryHandler.createDictionary(byteCode));
		this.compressionHistory.clear();
		this.compressionHistory.add(new CompressionMetadata(this.compressionHandler.getSortedBytes(), this.compressionHandler.getIndiceList()));
		System.out.println("SINGLE PHASE COMPRESSION FINISHED");
		try {
			new File(fileDirectory+"_dir").mkdir();
			File compressedFile = new File(fileDirectory+"_dir\\"+fileName+".compressed");
			File metadataFile = new File(fileDirectory+"_dir\\"+fileName+".compressed.metadata");
			if(compressedFile.exists() || metadataFile.exists()) {
				compressedFile.delete();
				metadataFile.delete();
			}
			compressedFile.createNewFile();
			metadataFile.createNewFile();
			this.fileHandler.writeFileByte(byteCode, new File(fileDirectory+"_dir\\"+fileName+".compressed"));
			this.fileHandler.writeObjectByte(this.compressionHistory, new File(fileDirectory+"_dir\\"+fileName+".compressed.metadata"));
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
		this.compressionHistory = this.fileHandler.readObjectByte(new File(fileDirectory+".metadata"), ArrayList.class);
		if(this.compressionHistory == null) {
			System.err.println("dictionary not found");
		} else {
			this.compressionHandler.setSortedBytes(this.compressionHistory.get(0).getSortedBytes());
			this.compressionHandler.setIndiceList(this.compressionHistory.get(0).getCompressedIndices());
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
		this.compressionHistory.clear();
		String fileName = file.getName();
		String fileDirectory = file.getAbsolutePath();
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
			this.compressionHistory.add(new CompressionMetadata(this.compressionHandler.getSortedBytes(), this.compressionHandler.getIndiceList()));
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
		try {
			this.fileHandler.writeFileByte(currentBytes, new File(fileDirectory+"\\"+fileName+".compressed"));
			this.fileHandler.writeObjectByte(this.compressionHistory, new File(fileDirectory+"\\"+fileName+".compressed.metadata"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentBytes;
	}
	public byte[] decompressFile(File file) throws IOException, ClassNotFoundException {
		String fileName = file.getName();
		if(!fileName.endsWith(".compressed")) {
			System.err.println("unrecognized file naming scheme file type mismatch may occur");
		}
		String fileDirectory = file.getAbsolutePath();
		byte[] byteCode = this.fileHandler.readFileByte(file);
		this.compressionHistory = this.fileHandler.readObjectByte(new File(fileDirectory+".metadata"), this.compressionHistory.getClass());
		if(this.compressionHistory == null) {
			System.err.println("metadata not found");
		} else {
			for(int i=this.compressionHistory.size()-1;i>=0;i--) {
				System.out.println("decompression itr = "+(i+1));
				this.compressionHandler.setSortedBytes(this.compressionHistory.get(i).getSortedBytes());
				this.compressionHandler.setIndiceList(this.compressionHistory.get(i).getCompressedIndices());
				byteCode = this.compressionHandler.startDecompression(byteCode);		
			}
			this.fileHandler.writeFileByte(byteCode, new File((fileName.endsWith(".compressed")?fileDirectory.substring(0,fileDirectory.length()-11):fileDirectory.concat(".decompressed"))));
			System.out.println("DECOMPRESSED FILE BACK TO "+byteCode.length+" BYTES");
		}
		return byteCode;
	}
}
