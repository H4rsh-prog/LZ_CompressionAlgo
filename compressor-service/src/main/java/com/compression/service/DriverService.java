package com.compression.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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
	private boolean storePrimitives = true;
	
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
			if(storePrimitives) {
				this.fileHandler.writeObjectByte(generatePrimitiveByteArray(this.compressionHistory.get(0).getSortedBytes()), new File(fileDirectory+"_dir\\"+fileName+".compressed.dictionary.metadata"));
				this.fileHandler.writeObjectByte(generatePrimitiveIntegerArray(this.compressionHistory.get(0).getCompressedIndices()), new File(fileDirectory+"_dir\\"+fileName+".compressed.indices.metadata"));
			} else {
				this.fileHandler.writeObjectByte(this.compressionHistory, new File(fileDirectory+"_dir\\"+fileName+".compressed.metadata"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteCode;
	}
	private byte[][] generatePrimitiveByteArray(ArrayList<ByteArrayWrapper> list) {
		Object[] objArr = list.toArray();
		byte[][] byteArr = new byte[objArr.length][];
		for (int i=0;i<objArr.length;i++) {
			ByteArrayWrapper wrapper = (ByteArrayWrapper) objArr[i];
			byteArr[i] = wrapper.getData();
		}
		return byteArr;
	}
	private int[] generatePrimitiveIntegerArray(ArrayList<Integer> list) {
		Object[] objArr = list.toArray();
		int[] indiceArr = new int[objArr.length];
		for (int i=0;i<objArr.length;i++) {
			Integer wrapper = (Integer) objArr[i];
			indiceArr[i] = wrapper.intValue();
		}
		return indiceArr;
	}
	public byte[] decompressFileSinglePhase(File file) throws IOException, ClassNotFoundException {
		String fileName = file.getName();
		if(!fileName.endsWith(".compressed")) {
			System.err.println("unrecognized file naming scheme file type mismatch may occur");
		}
		String fileDirectory = file.getAbsolutePath();
		byte[] byteCode = this.fileHandler.readFileByte(file);
		this.compressionHistory.clear();
		if(storePrimitives) {
			ArrayList<ByteArrayWrapper> byteList = generateWrapperByteList(this.fileHandler.readObjectByte(new File(fileDirectory+".dictionary.metadata"), byte[][].class));
			ArrayList<Integer> indiceList = generateWrapperIntegerList(this.fileHandler.readObjectByte(new File(fileDirectory+".indices.metadata"), int[].class));
			this.compressionHistory.add(new CompressionMetadata(byteList, indiceList));
		} else {
			this.compressionHistory = this.fileHandler.readObjectByte(new File(fileDirectory+".metadata"), ArrayList.class);
		}
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
	private ArrayList<ByteArrayWrapper> generateWrapperByteList(byte[][] array) {
		ArrayList<ByteArrayWrapper> list = new ArrayList<ByteArrayWrapper>();
		for (int i=0;i<array.length;i++) {
			list.add(new ByteArrayWrapper(array[i]));
		}
		return list;
	}
	private ArrayList<Integer> generateWrapperIntegerList(int[] array) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i=0;i<array.length;i++) {
			list.add(Integer.valueOf(array[i]));
		}
		return list;
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
		int itr=1;
		while(!(compressionRate<threshold)) {
			System.out.println("compression itr = "+itr);
			currentBytes = this.compressionHandler.startCompression(currentBytes, this.dictionaryHandler.createDictionary(currentBytes));
			compressionRate = 100-(((double)currentBytes.length)/byteSize)*100;
			System.out.println("ITR ["+itr+"] : reductedBytes = ["+(byteSize - currentBytes.length)+"] ; byteSize = [old = ["+byteSize+"] ; new = ["+(currentBytes.length)+"]] ; compressionRate = ["+compressionRate+"]");
			byteSize = currentBytes.length;
			this.compressionHistory.add(new CompressionMetadata(new ArrayList<>(this.compressionHandler.getSortedBytes()), new ArrayList<>(this.compressionHandler.getIndiceList())));
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
			new File(fileDirectory+"_dir").mkdir();
			File compressedFile = new File(fileDirectory+"_dir\\"+fileName+".compressed");
			File metadataFile = new File(fileDirectory+"_dir\\"+fileName+".compressed.metadata");
			if(compressedFile.exists() || metadataFile.exists()) {
				compressedFile.delete();
				metadataFile.delete();
			}
			compressedFile.createNewFile();
			metadataFile.createNewFile();
			this.fileHandler.writeFileByte(currentBytes, new File(fileDirectory+"_dir\\"+fileName+".compressed"));
			this.fileHandler.writeObjectByte(this.compressionHistory, new File(fileDirectory+"_dir\\"+fileName+".compressed.metadata"));
			
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
		this.compressionHistory = this.fileHandler.readObjectByte(new File(fileDirectory+".metadata"), ArrayList.class);
		if(this.compressionHistory == null) {
			System.err.println("metadata not found");
		} else {
			for(int i=this.compressionHistory.size()-1;i>=0;i--) {
				System.out.println("decompression itr = "+(i+1));
				System.out.println("size sorted "+this.compressionHistory.get(i).getSortedBytes().size());
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
