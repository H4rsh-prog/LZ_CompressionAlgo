package com.compression.tests;

public class GcRunner implements Runnable{

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(20000);
				System.gc();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}

}
