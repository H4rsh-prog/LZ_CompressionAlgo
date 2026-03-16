package com.compression.service;

import java.util.Queue;


import com.compression.model.BinaryTreeNODE;


public class BinaryTreeService {
	private BinaryTreeNODE head;
	private Queue<String> sortedHexes;
	
	public BinaryTreeService(Queue<String> sortedHexes) {
		this.sortedHexes = sortedHexes;
		BinaryTreeNODE root;
		BinaryTreeNODE TERMINATOR = new BinaryTreeNODE();
		TERMINATOR.initNode("TERMINATOR", null, 0);
		TERMINATOR.left.initNode(sortedHexes.poll(), TERMINATOR, 0);
		root = this.head = TERMINATOR.left;
		insertNode(root, TERMINATOR, 0, 0, true, false);
		StringBuilder sb = new StringBuilder();
		traversePreOrder(sb, "", "├──", this.head);
		System.out.println(sb.toString());
	}
	public void traversePreOrder(StringBuilder sb, String padding, String pointer, BinaryTreeNODE node) {	//STOLEN METHOD FOR PRINTING TREE
	    if (node != null) {
	        sb.append(padding);
	        sb.append(pointer);
	        sb.append(Integer.toBinaryString(node.getBin()));
	        sb.append("\n");

	        StringBuilder paddingBuilder = new StringBuilder(padding);
	        paddingBuilder.append("│  ");

	        String paddingForBoth = paddingBuilder.toString();
	        String pointerForRight = "└──";
	        String pointerForLeft = (node.right!=null && node.right.getBin()!=0) ? "├──" : "└──";

	        if(node.left.getBin()!=0)traversePreOrder(sb, paddingForBoth, pointerForLeft, node.left);
	        if(node.right.getBin()!=0)traversePreOrder(sb, paddingForBoth, pointerForRight, node.right);
	    }
	}
	public void insertNode(BinaryTreeNODE root, BinaryTreeNODE parent, int h, int leaf, boolean active_shift, boolean shifted) {
		if(sortedHexes.size()<1){System.out.println("NO ELEMENTS REMAINING CLOSING OP");return;}
		System.out.println("``````````````INSERTION ITERATION`\nLEAF="+leaf+"; HEIGHT="+h+"; ACTIVE_SHIFT="+active_shift+"; SHIFTED="+shifted+"; ELEMENTS_REMAINING="+this.sortedHexes.size());
		System.out.println("PREVIOUS NODE WAS :"+parent);
		System.out.println("CURRENT NODE :"+root);
		if(root.equals(this.head) && active_shift) {
			System.out.println("INCREMENTING LEAF GOING DOWN");
			insertNode(root.left, root, h-1, leaf-1, false, true);
			if(sortedHexes.size()<1){System.out.println("NO ELEMENTS REMAINING CLOSING OP");return;}
		}
		if(h == leaf) {
			root.initNode(sortedHexes.poll(), parent, 0);
			shifted = false;
			System.out.println("````````````INSERTING AT LEAF");
			System.out.println("PREVIOUS NODE WAS :"+parent);
			System.out.println("CURRENT NODE :"+root);
			System.out.println("````````````ÌNSERTED AT LEAF");
		}
		if(shifted) {
			System.out.println("SHIFTING NODES GOING DOWN");
			insertNode(root.left, root, h-1, leaf, false, shifted);
		} else {
			if(active_shift) {
				System.out.println("SHIFT ACTIVE");
				if(parent.left.equals(root)) {
					System.out.println("FOUND LEFT ON SHIFT ACTIVE GOING RIGHT");
					insertNode(parent.right, parent, h, leaf, active_shift, true);
				} else {
					System.out.println("DIDNT FIND LEFT ON SHIFT ACTIVE STILL GOING UP");
					insertNode(parent, parent.top, h+1, leaf, true, shifted);
				}
			} else {
				System.out.println("PARENT LEFT : "+parent.left+" ; PARENT RIGHT : "+parent.right+" ; CURRENT : "+root);
				if(parent.left.equals(root)) {
					System.out.println("NODE WAS NORMAL LEFT GOING RIGHT");
					insertNode(parent.right, parent, h, leaf, active_shift, shifted);
				} else {
					System.out.println("NODE WAS NORMAL RIGHT ACTIVATING SHIFT AND GOING UP");
					insertNode(parent, parent.top, h+1, leaf, true, shifted);
				}
			}
		}
	}
	public BinaryTreeNODE getHead() {
		return head;
	}
}
