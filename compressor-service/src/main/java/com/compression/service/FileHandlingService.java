package com.compression.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class FileHandlingService {

	@Setter private boolean verbose = false;
	
	public byte[] readFileByte(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = fis.readAllBytes();
		fis.close();
		return bytes;
	}
	public void writeFileByte(ByteBuffer byteArr, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(byteArr.array());
		fos.close();
	}
}
