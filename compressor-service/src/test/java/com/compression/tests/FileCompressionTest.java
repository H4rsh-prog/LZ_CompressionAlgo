package com.compression.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.compression.service.CompressionService;
import com.compression.service.DataBlockService;
import com.compression.service.FileHandlingService;


public class FileCompressionTest {
	protected File inputFile;
	protected File outputFile;
	protected DataBlockService dataBlockService;
	protected CompressionService compressionService;
	protected FileHandlingService fileHandlerService;
	
	@BeforeEach
	public void setUp() {
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\input");
		this.outputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\output");
		this.dataBlockService = new DataBlockService();
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
	public void testHexArrGen() throws IOException {
		new Thread(new GcRunner()).start();
		System.out.println(inputFile.getTotalSpace());
		ArrayList<Byte> byteArr = this.fileHandlerService.readFileByte(inputFile);
		System.out.println("HEX GENERATED");
		testFrequencyTable(byteArr);
	}
	public void testFrequencyTable(ArrayList<Byte> byteArr) throws IOException {
		HashMap<ArrayList<Byte>, Integer> frequencyTable = this.dataBlockService.findRepetitiveBytes(byteArr);
		System.out.println("FREQUENCT TABLE GENERATED");
		testCompressionWithoutBinaryTree(frequencyTable, byteArr);
	}
	public void testCompressionWithoutBinaryTree(HashMap<ArrayList<Byte>, Integer> frequencyTable, ArrayList<Byte> byteArr) throws IOException {
		this.compressionService.generateSortedBytesFromFrequency(frequencyTable);
		System.out.println("GENERATED SORTED HEX");
		ArrayList<Byte> compressedData = this.compressionService.startCompression(byteArr);
		System.out.println("DATA COMPRESSED");
		this.fileHandlerService.writeFileByte(compressedData, outputFile);
		System.out.println("DATA WRITTEN");
	}	
}
