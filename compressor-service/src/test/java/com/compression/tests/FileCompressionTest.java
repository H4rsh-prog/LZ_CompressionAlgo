package com.compression.tests;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.service.CompressionService;
import com.compression.service.DictionaryService;
import com.compression.service.DriverService;
import com.compression.service.FileHandlingService;


public class FileCompressionTest {
	protected File inputFile;
	protected File outputFile;
	DriverService driver;
	FileHandlingService file;
	DictionaryService dictionary;
	CompressionService compress;
	
	@BeforeEach
	public void setUp() {
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\input");
		this.outputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\output");
		this.driver = new DriverService();
		this.file = new FileHandlingService();
		this.compress = new CompressionService();
		this.dictionary = new DictionaryService();
	}
	@AfterEach
	public void tearDown() {
		this.inputFile = null;
		this.outputFile = null;
	}
	@Test
	public void testCompressionSinglePhase() throws IOException, ClassNotFoundException {
//		this.driver.setVerbosity(true, true, true);
		this.driver.setVerbosity(false, false, false);
		byte[] originalBytes = this.file.readFileByte(inputFile);
		byte[] bytecode = this.driver.compressFileSinglePhase(inputFile);
		File compressedFile = new File(inputFile.getAbsolutePath()+"\\"+inputFile.getName()+".compressed");
		if(!compressedFile.exists()) {
			System.err.println("compressed file not found");
			return;
		}
		bytecode = this.driver.decompressFileSinglePhase(compressedFile);
		System.out.println("STATUS = "+Arrays.equals(originalBytes, bytecode));
	}
//	@Test
	public void testCompressionIterativePhase() throws IOException, ClassNotFoundException {
		this.driver.setVerbosity(true, true, true);
//		this.driver.setVerbosity(false, false, false);
		byte[] originalBytes = this.file.readFileByte(inputFile);
		byte[] bytecode = this.driver.compressFile(inputFile, 5);
		File compressedFile = new File(inputFile.getAbsolutePath()+"\\"+inputFile.getName()+".compressed");
		if(!compressedFile.exists()) {
			System.err.println("compressed file not found");
			return;
		}
		bytecode = this.driver.decompressFile(compressedFile);
		System.out.println("STATUS = "+Arrays.equals(originalBytes, bytecode));
	}
}
