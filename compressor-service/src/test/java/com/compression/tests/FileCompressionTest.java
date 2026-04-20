package com.compression.tests;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.model.ByteArrayWrapper;
import com.compression.service.CompressionService;
import com.compression.service.DictionaryService;
import com.compression.service.FileHandlingService;


public class FileCompressionTest {
	protected File inputFile;
	protected File outputFile;
	protected DictionaryService dataBlockService;
	protected CompressionService compressionService;
	protected FileHandlingService fileHandlerService;
	
	@BeforeEach
	public void setUp() {
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\input");
		this.outputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\output");
		this.dataBlockService = new DictionaryService();
		this.compressionService = new CompressionService();
		this.fileHandlerService = new FileHandlingService();
		
		this.dataBlockService.setVerbose(true);
		this.fileHandlerService.setVerbose(true);
		this.compressionService.setVerbose(true);
	}
	@AfterEach
	public void tearDown() {
		this.inputFile = null;
		this.outputFile = null;
		this.dataBlockService = null;
		this.compressionService = null;
		this.fileHandlerService = null;
		System.gc();
	}
	@Test
	public void testByteArrGen() throws IOException {
		new Thread(new GcRunner()).start();
		System.out.println(inputFile.getTotalSpace());
		byte[] byteArr = this.fileHandlerService.readFileByte(inputFile);
		System.out.println("BYTES GENERATED");
		testDictionary(byteArr);
	}
	public void testDictionary(byte[] byteArr) throws IOException {
		HashMap<ByteArrayWrapper, Integer> frequencyTable = this.dataBlockService.findRepetitiveBytes(byteArr);
		System.out.println("FREQUENCT TABLE GENERATED");
		testCompressionWithoutBinaryTree(frequencyTable, byteArr);
	}
	public void testCompressionWithoutBinaryTree(HashMap<ByteArrayWrapper, Integer> frequencyTable, byte[] byteArr) throws IOException {
		this.compressionService.generateSortedBytesFromFrequency(frequencyTable);
		System.out.println("GENERATED SORTED HEX");
		ByteBuffer compressedData = this.compressionService.startCompression(byteArr);
		System.out.println("DATA COMPRESSED");
		this.fileHandlerService.writeFileByte(compressedData, outputFile);
		System.out.println("DATA WRITTEN");
		decompressData(compressedData.array(), byteArr);
	}
	public void decompressData(byte[] compressedData, byte[] byteArr) {
		System.out.println("DECOMPRESSING");
		ByteBuffer decompressedData =this.compressionService.startDecompression(compressedData);
		System.out.println("DECOMPRESSED");
		ByteArrayWrapper decompressedBytes = new ByteArrayWrapper(decompressedData);
		System.out.println("SUCCESFUL : "+Arrays.equals(byteArr, decompressedBytes.getData()));
	}
}
