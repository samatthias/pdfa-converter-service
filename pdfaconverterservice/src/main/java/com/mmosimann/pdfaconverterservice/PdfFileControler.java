package com.mmosimann.pdfaconverterservice;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

/* 
 * Resources: 
 * https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
 * https://www.bezkoder.com/spring-boot-file-upload/
 * 
 * curl -v -F "file=@\"Barcode_Test_Page.pdf\"" http://localhost:8081/convertpdf
 * 
 */

@RestController
public class PdfFileControler {

	private static final Logger logger = LoggerFactory.getLogger(PdfFileControler.class);

	@Autowired
	private PdfFileService pdfFileService;
	
	@GetMapping("/health")
	public String health() {
		return "{ok, alive}";
	}
	    
	@PostMapping("/convertpdf")
	public ResponseEntity<Resource> convertPDF(@RequestParam("file") MultipartFile file,  HttpServletRequest request) {
		
		String orgininalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		String tmpFilePath = pdfFileService.storePdfFile(file);
		
		String convertedFilePath = null;
		try {
			convertedFilePath = pdfFileService.convertPdfFile(tmpFilePath, orgininalFileName );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	    
	    
	    // Load file as Resource
        Resource resource = pdfFileService.loadFileAsResource(convertedFilePath);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
	    
	    
	    

	    return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        //return ResponseEntity.ok().build(); 
	    }

}
