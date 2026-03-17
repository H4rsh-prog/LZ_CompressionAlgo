package com.compression.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.compression.model.BinaryTreeNODE;

import lombok.Getter;
import lombok.Setter;

@Service
public class BinaryTreeService {
	@Getter private BinaryTreeNODE head;
	private Queue<ArrayList<String>> sortedHexes;
	@Setter private boolean verbose = false;
	
	public void initBinaryTree(Queue<ArrayList<String>> sortedHexes) {
		this.sortedHexes = sortedHexes;
		BinaryTreeNODE root;
		BinaryTreeNODE TERMINATOR = new BinaryTreeNODE();
		TERMINATOR.initTree();
		TERMINATOR.initNode(new ArrayList(List.of("TERMINATOR")), null, 0);
		TERMINATOR.left.initNode(sortedHexes.poll(), TERMINATOR, 0);
		root = this.head = TERMINATOR.left;
		insertNode(root, TERMINATOR, 0, 0, true, false);
		if(verbose) System.out.println(treeToString());
	}
	public String treeToString() {
		StringBuilder sb = new StringBuilder();
		traversePreOrder(sb, "", "├──", this.head);
		return sb.toString();
	}
	private void traversePreOrder(StringBuilder sb, String padding, String pointer, BinaryTreeNODE node) {	//STOLEN METHOD FOR PRINTING TREE
	    if (node != null) {
	        sb.append(padding);
	        sb.append(pointer);
	        sb.append(node);
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
	private void insertNode(BinaryTreeNODE root, BinaryTreeNODE parent, int h, int leaf, boolean active_shift, boolean shifted) {
		if(sortedHexes.size()<1){if(verbose) System.out.println("NO ELEMENTS REMAINING CLOSING OP");return;}
		if(verbose) System.out.println("``````````````INSERTION ITERATION`\nLEAF="+leaf+"; HEIGHT="+h+"; ACTIVE_SHIFT="+active_shift+"; SHIFTED="+shifted+"; ELEMENTS_REMAINING="+this.sortedHexes.size());
		if(verbose) System.out.println("PREVIOUS NODE WAS :"+parent);
		if(verbose) System.out.println("CURRENT NODE :"+root);
		if(root.equals(this.head) && active_shift) {
			if(verbose) System.out.println("INCREMENTING LEAF GOING DOWN");
			insertNode(root.left, root, h-1, leaf-1, false, true);
			if(sortedHexes.size()<1){if(verbose) System.out.println("NO ELEMENTS REMAINING CLOSING OP");return;}
		}
		if(h == leaf) {
			root.initNode(sortedHexes.poll(), parent, 0);
			shifted = false;
			if(verbose) System.out.println("````````````INSERTING AT LEAF");
			if(verbose) System.out.println("PREVIOUS NODE WAS :"+parent);
			if(verbose) System.out.println("CURRENT NODE :"+root);
			if(verbose) System.out.println("````````````ÌNSERTED AT LEAF");
		}
		if(shifted) {
			if(verbose) System.out.println("SHIFTING NODES GOING DOWN");
			insertNode(root.left, root, h-1, leaf, false, shifted);
		} else {
			if(active_shift) {
				if(verbose) System.out.println("SHIFT ACTIVE");
				if(parent.left.equals(root)) {
					if(verbose) System.out.println("FOUND LEFT ON SHIFT ACTIVE GOING RIGHT");
					insertNode(parent.right, parent, h, leaf, active_shift, true);
				} else {
					if(verbose) System.out.println("DIDNT FIND LEFT ON SHIFT ACTIVE STILL GOING UP");
					insertNode(parent, parent.top, h+1, leaf, true, shifted);
				}
			} else {
				if(verbose) System.out.println("PARENT LEFT : "+parent.left+" ; PARENT RIGHT : "+parent.right+" ; CURRENT : "+root);
				if(parent.left.equals(root)) {
					if(verbose) System.out.println("NODE WAS NORMAL LEFT GOING RIGHT");
					insertNode(parent.right, parent, h, leaf, active_shift, shifted);
				} else {
					if(verbose) System.out.println("NODE WAS NORMAL RIGHT ACTIVATING SHIFT AND GOING UP");
					insertNode(parent, parent.top, h+1, leaf, true, shifted);
				}
			}
		}
	}
}
