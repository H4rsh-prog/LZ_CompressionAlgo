package com.compression.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

@Service
public class FileHandlingService {
	public ArrayList<Byte> readFileByte(File file) throws IOException {
		ArrayList<Byte> byteArr = new ArrayList<>();
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = fis.readAllBytes();
		fis.close();
		int byteLength = bytes.length;
		for(int i=0;i<byteLength;i++) {
			System.out.print("Bytes loaded ["+i+"/"+byteLength+"]\r");
			byteArr.add(bytes[i]);
		}
		return byteArr;
	}
	public void writeFileByte(byte[] byteArr, File file) throws IOException {
		//TO IMPLEMENT
	}
	public ArrayList<String> readFileHex(File file) throws IOException{
		ArrayList<String> hexArr = new ArrayList<>();
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArr = fis.readAllBytes();
		for(int i=0;i<byteArr.length;i++) {
			System.out.print("Bytes loaded ["+i+"/"+byteArr.length+"]\r");
			hexArr.add(Integer.toHexString(byteArr[i]));
		}
		return hexArr;
	}
	public void writeFileHex(ArrayList<String> hexArr, File file) throws IOException {
		int delimitCtn = 0;
		for(String hex: hexArr) {
			if(hex.startsWith(".")) {
				delimitCtn++;
			}
		}
		int delimited = 0;
		byte[] byteArr = new byte[hexArr.size()+delimitCtn];
		int hexArrSize = hexArr.size();
		for(int i=0;i<hexArrSize;i++) {
			String hexElement = hexArr.get(i);
			System.out.print("Bytes loaded ["+i+"/"+hexArrSize+"]\r");
			if(hexElement.startsWith(".")) {
				byteArr[i+delimited] = (byte) 127;
				delimited++;
				byteArr[i+delimited] = (byte) CompressionService.calcByteFromHex(hexElement.substring(1));
				
			} else {
				byteArr[i+delimited] = (byte) CompressionService.calcByteFromHex(hexElement);
			}
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(byteArr);
		fos.close();
	}
}
