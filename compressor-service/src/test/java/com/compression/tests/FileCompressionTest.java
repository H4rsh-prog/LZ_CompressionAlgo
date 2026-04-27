package com.compression.tests;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.service.DriverService;
import com.compression.service.FileHandlingService;


public class FileCompressionTest {
	protected File inputFile;
	protected File outputFile;
	DriverService driver;
	FileHandlingService file;
	
	@BeforeEach
	public void setUp() {
		this.inputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\input");
		this.outputFile = new File("C:\\Users\\User\\Documents\\workspace-spring-tools-for-eclipse-4.31.0.RELEASE\\Huffman_Compression\\compressor-service\\target\\testObjects\\output");
		this.driver = new DriverService();
		this.file = new FileHandlingService();
	}
	@AfterEach
	public void tearDown() {
		this.inputFile = null;
		this.outputFile = null;
	}
	@Test
	public void testByteArrGen() throws IOException {
		this.driver.setVerbosity(false, false, false);
		byte[] originalData = this.file.readFileByte(inputFile);
		byte[] compressedData = this.driver.compressFile(inputFile, 0);
		this.file.writeFileByte(compressedData, outputFile);
		byte[] decompressedData = this.driver.decompressFile(outputFile);
		System.out.println("STATUS = "+Arrays.equals(originalData, decompressedData));
	}
}
