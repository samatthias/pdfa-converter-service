package com.mmosimann.pdfaconverterservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.Resource;

@SpringBootApplication
public class PdfaConverterServiceApplication implements CommandLineRunner{
	
	private static final Logger logger = LoggerFactory.getLogger(PdfFileControler.class);
	
	@Resource
	PdfFileService pdfFileService;

	public static void main(String[] args) {
		SpringApplication.run(PdfaConverterServiceApplication.class, args);
	}
	
	@Override
	public void run(String... arg) throws Exception {
		logger.info("Init upload directory");
		pdfFileService.init();
	}

}
