package com.compression.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.service.BinaryTreeService;
import com.compression.service.CompressionService;
import com.compression.service.DataBlockService;
import com.compression.service.FileHandlingService;

public class FileCompressionTest {
	protected File inputFile;
	protected File outputFile;
	protected DataBlockService dataBlockService;
	protected BinaryTreeService binTreeService;
	protected CompressionService compressionService;
	protected FileHandlingService fileHandlerService;
	protected ArrayList<String> hexArr;
	protected HashMap<ArrayList<String>, Integer> frequencyTable;
	
	@BeforeEach
	public void setUp() {
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\input");
		this.outputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\output");
		this.dataBlockService = new DataBlockService();
		this.binTreeService = new BinaryTreeService();
		this.compressionService = new CompressionService();
		this.fileHandlerService = new FileHandlingService();
//		this.dataBlockService.setVerbose(true);
//		this.binTreeService.setVerbose(true);
//		this.compressionService.setVerbose(true);
	}
	@AfterEach
	public void tearDown() {
		this.inputFile = null;
		this.outputFile = null;
		this.dataBlockService = null;
		this.binTreeService = null;
		this.compressionService = null;
		this.fileHandlerService = null;
		System.gc();
	}
	@Test
	public void testHexArrGen() throws IOException {
		new Thread(new GcRunner()).start();
		System.out.println(inputFile.getTotalSpace());
		ArrayList<String> hexArr = this.fileHandlerService.readFileHex(inputFile);
		System.out.println("HEX GENERATED");
		testFrequencyTable(hexArr);
	}
	@Test
	public void testFrequencyTable(ArrayList<String> hexArr) {
		HashMap<ArrayList<String>, Integer> frequencyTable = this.dataBlockService.findRepetition(hexArr);
		System.out.println("FREQUENCT TABLE GENERATED");
		testBinTree(frequencyTable);
	}
	@Test
	public void testBinTree(HashMap<ArrayList<String>, Integer> frequencyTable) {
		PriorityQueue<ArrayList<String>> hexQueue = this.dataBlockService.generateQueueFromFrequency(frequencyTable);
		System.out.println("PRIORITY QUEUE GENERATED");
		this.binTreeService.initBinaryTree(hexQueue);
		System.out.println("BINARY TREE INIT");
		System.out.println(this.binTreeService.treeToString());
	}
//	@Test
	public void testFileCompression() throws IOException {
		System.out.println(inputFile.getTotalSpace());
		ArrayList<String> hexArr = this.fileHandlerService.readFileHex(inputFile);
		HashMap<ArrayList<String>, Integer> frequencyTable = this.dataBlockService.findRepetition(hexArr);
		System.out.println(frequencyTable);
		PriorityQueue<ArrayList<String>> hexQueue = this.dataBlockService.generateQueueFromFrequency(frequencyTable);
		frequencyTable = null;
		System.out.println(hexQueue);
		this.binTreeService.initBinaryTree(hexQueue);
		System.out.println("BIN TREE GENERATED");
		this.compressionService.initHexMapping(this.binTreeService.getHead());
		System.out.println("HEX MAPPING INITIALIZED");
		ArrayList<String> compressedData = this.compressionService.startCompression(hexArr);
		hexArr = null;
		System.out.println(compressedData);
		this.fileHandlerService.writeFileHex(compressedData, outputFile);
		System.out.println(outputFile.getTotalSpace());
	}
	
}
