package com.compression.tests;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.model.ByteArrayWrapper;
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
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\inpuet.png");
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
//	@Test
	public void testByteArrGen() throws IOException {
		this.driver.setVerbosity(true, true, true);
		byte[] originalData = this.file.readFileByte(inputFile);
		byte[] compressedData = this.driver.compressFile(inputFile, 0);
		this.file.writeFileByte(compressedData, outputFile);
		byte[] decompressedData = this.driver.decompressFile(outputFile);
		System.out.println("STATUS = "+Arrays.equals(originalData, decompressedData));
	}
//	@Test
	public void testCompressionNonIterative() throws IOException {
		byte[] originalData = this.file.readFileByte(inputFile);
		ArrayList<ByteArrayWrapper> sortedBytes = this.dictionary.createDictionary(originalData);
		byte[] compressedData = this.compress.startCompression(originalData, sortedBytes);
		this.file.writeFileByte(compressedData, outputFile);
		byte[] decompressedData = this.compress.startDecompression(compressedData);
		System.out.println("STATUS = "+Arrays.equals(originalData, decompressedData));	
	}
	@Test
	public void testCompressionSinglePhase() throws IOException {
		this.driver.compressFileSinglePhase(inputFile);
	}
}
