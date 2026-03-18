package com.compression.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

@Service
public class FileHandlingService {
	public ArrayList<String> readFileHex(File file) throws IOException{
		ArrayList<String> hexArr = new ArrayList<>();
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArr = fis.readAllBytes();
		for(int i=0;i<byteArr.length;i++) {
			System.out.print("\r Bytes loaded ["+i+"/"+byteArr.length+"]");
			hexArr.add(Integer.toHexString(byteArr[i]));
		}
		return hexArr;
	}
	public void writeFileHex(ArrayList<String> hexArr, File file) throws FileNotFoundException {
		System.out.println(hexArr);
		System.out.println(hexArr.size());
		int delimitCtn = 0;
		for(String hex: hexArr) {
			if(hex.startsWith(".")) {
				delimitCtn++;
			}
		}
		System.out.println(delimitCtn);
		int delimited = 0;
		byte[] byteArr = new byte[hexArr.size()+delimitCtn];
		for(int i=0;i<hexArr.size();i++) {
			if(hexArr.get(i).startsWith(".")) {
				byteArr[i+delimited] = (byte) 127;
				delimited++;
				byteArr[i+delimited] = (byte) CompressionService.calcByteFromHex(hexArr.get(i).substring(1));
				
			} else {
				byteArr[i+delimited] = (byte) CompressionService.calcByteFromHex(hexArr.get(i));
			}
		}
		for(byte b : byteArr) {
			System.out.print(b+" ");
		}
		System.out.println();
		System.out.println(byteArr.length);
		
	}
}
