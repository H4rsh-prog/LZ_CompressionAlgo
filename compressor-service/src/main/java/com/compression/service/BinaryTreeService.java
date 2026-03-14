package com.compression.service;

import java.util.Queue;


import com.compression.model.BinaryTreeNODE;


public class BinaryTreeService {
	private BinaryTreeNODE head;
	
	public BinaryTreeService(Queue<String> sortedHexes) {
		BinaryTreeNODE root = this.head = new BinaryTreeNODE(sortedHexes.poll());
		int leaf = 1, h = 0;
		boolean active_shift = true, shifted = false;
		while (!sortedHexes.isEmpty()) {
			if(root.equals(this.head) && active_shift) {
				active_shift = false;
				leaf--;
				shifted = true;
				root = root.left;
				h--;
			}
			if(h==leaf) {
				root = new BinaryTreeNODE(sortedHexes.poll());
				shifted = false;
			}
			if(shifted) {
				active_shift = false;
				root = root.left;
				h--;
			} else {
				if(active_shift) {
					if(root.equals(root.top.left)) {
						shifted = true;
						root = root.top.right;
					}
				} else {
					if(root.equals(root.top.right)) {
						active_shift = true;
						root = root.top;
						h++;
					} else {
						root = root.top.right;
					}
				}
			}
		}
	}
}
