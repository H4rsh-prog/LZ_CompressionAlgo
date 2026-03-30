package com.compression.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class FileHandlingService {

	@Setter private boolean verbose = false;
	
	public ArrayList<Byte> readFileByte(File file) throws IOException {
		ArrayList<Byte> byteArr = new ArrayList<>();
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = fis.readAllBytes();
		fis.close();
		int byteLength = bytes.length;
		for(int i=0;i<byteLength;i++) {
			if(verbose) System.out.print("Bytes loaded ["+i+"/"+byteLength+"]\r");
			byteArr.add(bytes[i]);
		}
		return byteArr;
	}
	public void writeFileByte(ArrayList<Byte> byteArr, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		int byteArrSize = byteArr.size();
		byte[] bytes = new byte[byteArrSize];
		for(int i=0;i<byteArrSize;i++) {
			if(verbose) System.out.print("Bytes loaded ["+i+"/"+byteArrSize+"]\r");
			bytes[i] = byteArr.get(i);
		}
		fos.write(bytes);
		fos.close();
	}
}
