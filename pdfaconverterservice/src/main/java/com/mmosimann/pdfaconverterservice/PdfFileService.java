package com.mmosimann.pdfaconverterservice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfFileService {
	
	
	@Autowired
	private Environment env;
	
	@Value("${file.upload-dir}")
	private String uploadDirectory;
	
	static Logger logger = LoggerFactory.getLogger(PdfFileService.class);
	
	
	public void init() {
		logger.info("Upload directory set to: " + env.getProperty("file.upload-dir"));
	  try {
		 if (Files.exists(Paths.get(uploadDirectory))) {
			 FileSystemUtils.deleteRecursively(Paths.get(uploadDirectory));
		 }
		logger.info("Directory created sucessfully");
		Files.createDirectories(Paths.get(uploadDirectory));
	
	     
	  } catch (IOException e) {
	     throw new RuntimeException("Could not initialize folder for upload!");
	  }
	}

	public String storePdfFile(MultipartFile file) {
		
		UUID uuid = UUID.randomUUID();
		Path targetLocation = null;
		
		// Normalize file name
       
        
        try {
            // Check if the file's name contains invalid characters
//            if(fileName.contains("..")) {
//                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
//            }

            // Copy file to the target location (Replacing existing file with the same name)
        	
            //Path targetLocation = this.fileStorageLocation.resolve(fileName);
            targetLocation = Paths.get(uploadDirectory,uuid.toString(),"tmp.pdf");
            Files.createDirectories(Paths.get(uploadDirectory,uuid.toString()));
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
           // throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        	logger.error(ex.toString());
        }
		
		// TODO Auto-generated method stub
		return targetLocation.toString();
	}

	public Resource loadFileAsResource(String convertedFilePath) {
		 try {
		      Resource resource = new UrlResource("file:////" + convertedFilePath);

		      if (resource.exists() || resource.isReadable()) {
		        return resource;
		      } else {
		        throw new RuntimeException("Could not read the file!");
		      }
		 } catch (MalformedURLException e) {
		      throw new RuntimeException("Error: " + e.getMessage());
		 }
		 
	}

	public String convertPdfFile(String tmpFilePath, String orgininalFileName) throws IOException {
		File inputFile = new File(tmpFilePath);
		PDDocument doc = PDDocument.load(inputFile);
		
		PDFont font = PDType0Font.load(doc, this.loadFont());
		
        if (!font.isEmbedded())
        {
        	throw new IllegalStateException("PDF/A compliance requires that all fonts used for"
        			+ " text rendering in rendering modes other than rendering mode 3 are embedded.");
        }
        
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();
        
        
        try
        {
            DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
            dc.setTitle("asdf");
            //dc.add
            //dc.addCreator("My APPLICATION Creator");
            //dc.addDate(cal);
            //dc.addCreator("My APPLICATION Creator");
            
            PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
            id.setPart(1);
            id.setConformance("A");
            
            XmpSerializer serializer = new XmpSerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.serialize(xmp, baos, true);

            PDMetadata metadata = new PDMetadata(doc);
            metadata.importXMPMetadata(baos.toByteArray());
            doc.getDocumentCatalog().setMetadata(metadata);
        }
        catch(BadFieldValueException | TransformerException e)
        {
            // won't happen here, as the provided value is valid
            throw new IllegalArgumentException(e);
        }
        
        PDDocumentCatalog catalogue = doc.getDocumentCatalog();
        PDMarkInfo  mark = new PDMarkInfo(); // new PDMarkInfo(page.getCOSObject()); 
        PDStructureTreeRoot treeRoot = new PDStructureTreeRoot(); 
        catalogue.setMarkInfo(mark);
        catalogue.setStructureTreeRoot(treeRoot);           
        catalogue.getMarkInfo().setMarked(true);
        
        PDDocumentInformation pdi = new PDDocumentInformation();
        //pdi.setCreationDate(cal);
        //pdi.setModificationDate(cal);            
        //pdi.setAuthor("My APPLICATION Author");
        //pdi.setProducer("My APPLICATION Producer");;
        //pdi.setCreator("My APPLICATION Creator");
        pdi.setTitle("asdf");
        //pdi.setSubject("PDF to PDF/A{2,3}-{A,U,B}");       
        doc.setDocumentInformation(pdi);
        
        
        //File colerProfileFile = this.loadRGBProfile();	
        //InputStream colorProfile = new FileInputStream(colerProfileFile);
        PDOutputIntent intent = new PDOutputIntent(doc, this.loadRGBProfile());
        intent.setInfo("sRGB IEC61966-2.1");
        intent.setOutputCondition("sRGB IEC61966-2.1");
        intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
        intent.setRegistryName("http://www.color.org");
        doc.getDocumentCatalog().addOutputIntent(intent);
        


        
        Path outputFilePath = Paths.get(Paths.get(tmpFilePath).getParent().toString(), "pdf-1a_" + orgininalFileName);

        doc.save(outputFilePath.toString());
        doc.close();

		return outputFilePath.toString();
	}
	
	private InputStream loadRGBProfile() {
		return this.loadFile("sRGB Profile.icc");
	}
	
	private InputStream loadFont() {
		return this.loadFile("arial.ttf");
	
	}
	
	private InputStream loadFile(String filename) {
		
		//Resource resource = new ClassPathResource(filename);
		InputStream is = null;
		try {
			is = new ClassPathResource(filename).getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return is;
	}

}
