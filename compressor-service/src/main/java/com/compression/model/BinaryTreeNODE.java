package com.compression.model;

import java.io.Serializable;
import java.util.ArrayList;

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
	transient private int binInt = 0;
	private ArrayList<String> hex;
	
	public void initTree() {
		serial = -2;
	}
	public void initNode(ArrayList<String> hex, BinaryTreeNODE parent, int base){
		serial++;
		this.binInt = serial;
		this.base = base;
		this.bin[this.base] = (byte) serial;
		this.hex = hex;
		this.top = parent;
		this.left = new BinaryTreeNODE();
		this.right = new BinaryTreeNODE();
	}
	public BinaryTreeNODE findNODE(BinaryTreeNODE node, String binaryString) {		//WORKS AFTER HEIGHT -2
		if(node!=null && node.getBin()!=0) {
			String currentBin = node.getBinStr();
			if(currentBin.equals(binaryString)) return node;
			if(binaryString.substring(currentBin.length()).startsWith("1")) {
				findNODE(node.right, binaryString);
			} else {
				findNODE(node.left, binaryString);
			}
		}
		return null;
	}
	public byte getBin() {return this.bin[this.base];}
	public String getBinStr() {return Integer.toBinaryString(this.binInt);}
	public int getBinInt() {return this.binInt;}
	public ArrayList<String> getHex() {return this.hex;}
	public void setHex(ArrayList<String> hex) {this.hex = hex;}
	public String toString() {
		return "NODE {BIN : "+getBinStr()+" ; HEX : "+this.hex+"}";
	}
}