package com.compression.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.compression.service.FileHandlingService;


public class FileHandlerTest {
	protected ArrayList<String> hexArr;
	protected FileHandlingService service;
	
	@BeforeEach
	public void setUp() {
		this.hexArr = new ArrayList<>(List.of("7b","22","6e","61","6d","65",".5","22","74","68","69","73",
                "5f","6e","61","6d",".0","77","69","6c","6c","5f","72","65",
                ".2","61","74","5f","6d",".1","62",".6","2c","22","70","72",
                "6f",".2","72","74","79",".5","22","6e","61","6d",".0","6d",
                ".1","62",".6","2c","22","6f","74","68",".3","5f","70","72",
                "6f",".2","72","74","79",".5","22","72","65",".2","61","74",
                "69","6e","67","3f","22","2c","22","6d",".1","62",".0","6d",
                "6f","72",".0","70","72","6f",".2","72","74","69","65","73",
                ".5","22","6e","61","6d",".6","7d"
            ));
		this.service = new FileHandlingService();
	}
	@AfterEach
	public void tearDown() {
		this.hexArr = null;
		this.service = null;
	}
	@Test
	public void testFileWriting() throws FileNotFoundException {
		File f = new File("./target/testObjects/hex_file_write_test");
		this.service.writeFileHex(hexArr, f);
	}

}
