package com.compression.model;

public class BinaryTreeNODE {
	private static int serial = -1;
	public BinaryTreeNODE top;
	public BinaryTreeNODE left;
	public BinaryTreeNODE right;
	private byte[] bin = new byte[10];
	private String hex;
	private int base = 0;
	
	public BinaryTreeNODE(String hex) {
		serial++;
		this.hex = hex;
		this.bin[0] = (byte) serial;
	}
	public BinaryTreeNODE(String hex, int base) {
		serial++;
		this.hex = hex;
		this.base = base;
		this.bin[base] = (byte) serial;
	}
	public byte getBin() {return this.bin[this.base];}
	public String getHex() {return this.hex;}
}
