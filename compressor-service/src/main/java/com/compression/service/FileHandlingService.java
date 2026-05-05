package com.compression.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	public void writeFileByte(byte[] byteArr, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(byteArr);
		fos.close();
	}
	public <T> T readObjectByte(File file, Class<T> clazz) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Object o = ois.readObject();
		ois.close();
		return clazz.isInstance(o)?(clazz.cast(o)):null;
	}
	public void writeObjectByte(Object o, File file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(o);
		oos.close();
	}
	
}
