package com.compression.model;

import java.io.Serializable;

public class BinaryTreeNODE implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6293322050169810169L;
	transient private static int serial = -2;
	public BinaryTreeNODE top;
	public BinaryTreeNODE left;
	public BinaryTreeNODE right;
	transient private int base = 0;
	transient private byte[] bin = new byte[this.base+1];
	private String hex;
	
	public void initNode(String hex, BinaryTreeNODE parent, int base){
		serial++;
		this.base = base;
		this.bin[this.base] = (byte) serial;
		this.hex = hex;
		this.top = parent;
		this.left = new BinaryTreeNODE();
		this.right = new BinaryTreeNODE();
	}
	
	public byte getBin() {return this.bin[this.base];}
	public String getHex() {return this.hex;}
	public void setHex(String hex) {this.hex = hex;}
	public String toString() {
		return "NODE {BIN : ["+this.bin[this.base]+"] ; HEX : ["+this.hex+"]}";
	}
}