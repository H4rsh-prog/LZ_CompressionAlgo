package com.compression.tests;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	public void testField() throws IOException {
		byte[] byteArr = new byte[10];
		for(int i=0;i<10;i++) {
			byteArr[i] = (byte) 0x1;
		}
		ByteBuffer buff =ByteBuffer.wrap(byteArr);
		byte[] byteArr2 = new byte[10];
		for(int i=0;i<10;i++) {
			byteArr2[i] = (byte) 0x2;
		}
		ByteBuffer buff2 = buff.slice(1,3);
		for(byte b : buff.array()) {
			System.out.print(b+" ");
		}System.out.println();
		for(byte b : buff2.array()) {
			System.out.print(b+" ");
		}System.out.println();
		buff = ByteBuffer.allocate(11);
		System.out.println(buff);
		for(byte b : buff.array()) {
			System.out.print(b+" ");
		}System.out.println();
		buff.put(1, byteArr);
		System.out.println(buff);
		for(byte b : buff.array()) {
			System.out.print(b+" ");
		}System.out.println();
		byteArr = buff.array();
		System.out.println(byteArr.length);
	}
//	@Test
	public void testHexArrGen() throws IOException {
		new Thread(new GcRunner()).start();
		System.out.println(inputFile.getTotalSpace());
		byte[] byteArr = this.fileHandlerService.readFileByte(inputFile);
		System.out.println("HEX GENERATED");
		testFrequencyTable(byteArr);
	}
	public void testFrequencyTable(byte[] byteArr) throws IOException {
		HashMap<ByteBuffer, Integer> frequencyTable = this.dataBlockService.findRepetitiveBytes(ByteBuffer.wrap(byteArr).slice(0,10000).array());
		System.out.println("FREQUENCT TABLE GENERATED");
		testCompressionWithoutBinaryTree(frequencyTable, byteArr);
	}
	public void testCompressionWithoutBinaryTree(HashMap<ByteBuffer, Integer> frequencyTable, byte[] byteArr) throws IOException {
		this.compressionService.generateSortedBytesFromFrequency(frequencyTable);
		System.out.println("GENERATED SORTED HEX");
		ByteBuffer compressedData = this.compressionService.startCompression(byteArr);
		System.out.println("DATA COMPRESSED");
		this.fileHandlerService.writeFileByte(compressedData, outputFile);
		System.out.println("DATA WRITTEN");
	}	
}
