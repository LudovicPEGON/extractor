package com.everteam.extractor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.text.impl.OCRTextExtractor;
import com.google.common.io.Files;

/**
 * Service class handling the implementation of controller methods. Depending
 * upon the content type of file passed, an extractor implementation returns
 * TextData.
 * 
 * @author arpita
 *
 */
@Service
public class TextService {
    private static final Logger log = LoggerFactory.getLogger(TextService.class);

    @Autowired
    private TextExtractorFactory textFactory;

    @Autowired
    private OCRTextExtractor OCRTextExtractor;

    @Value("${extractor.OCR.dpi:200}")
    private int dpi;

    @Value("${extractor.OCR.rationMaxLimit:1000}")
    private double ratioMaxLimit;

    @Value("${extractor.OCR.isTesseractInstalled:true}")
    private boolean isTesseractInstalled;

    /**
     * Get text of a file from its URI.
     * 
     * @param uri
     * @return {@link TextData}
     * @throws MalformedURLException
     * @throws IOException
     */
    public TextData getText(String uri) throws MalformedURLException, IOException {
        //String path = URLDecoder.decode(uri, "UTF-8");
        if (log.isDebugEnabled())
            log.debug("Extracting text and metadata for uri " + uri);
        File file = new File(uri);
        String extension = FilenameUtils.getExtension(file.getName());
        File tempFile = File.createTempFile("tempFile", "." + extension);
        Files.copy(file, tempFile);
        return getTextFromFile(tempFile);
    }

    /**
     * Get text from a MultipartFile.
     * 
     * @param multipartFile
     * @return {@link TextData}
     * @throws IOException
     */
    public TextData getText(MultipartFile multipartFile) throws IOException {
        if (log.isDebugEnabled())
            log.debug("Extracting text and metadata for multipartFile " + multipartFile);
        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        File file = File.createTempFile("tempFile", "." + extension);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return getTextFromFile(file);
    }

    /**
     * Default method to get text from a File.
     * 
     * @param file
     * @return {@link TextData}
     * @throws InvalidPasswordException
     * @throws IOException
     */
    public TextData getTextFromFile(File file) throws InvalidPasswordException, IOException {
        InputStream inputStream = new FileInputStream(file);
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
        TextData returnedTextData = textFactory.getExtractor(contentType).getText(inputStream);
        inputStream.close();
        if ((isTesseractInstalled) && (file.getAbsolutePath().toUpperCase().endsWith(".PDF")) && returnedTextData.gettext().isEmpty()) {
            returnedTextData = getTextFromPdf(file, returnedTextData);
        }
        file.delete();
        return returnedTextData;
    }

    /**
     * Do OCR on the first page of a PDF file
     * Can be parameterized in the file application.yml to modify performances
     * 
     * @param file
     * @param returnedTextData
     * @return {@link TextData}
     * @throws IOException
     */
    public TextData getTextFromPdf(File file, TextData returnedTextData) throws IOException {
        double fileSize = file.length() / 1024; /* size of the file in kilobyte */
        double nbPages = (double) Integer.parseInt(returnedTextData.getMetadata().get("xmpTPg:NPages"));
        double ratio = fileSize / nbPages;
        if (ratio < ratioMaxLimit) {
            // get first page of the PDF
            PDDocument pdfInput = PDDocument.load(file);
            PDPage doc = pdfInput.getPage(0);
            PDDocument pdfOutput = new PDDocument();
            pdfOutput.addPage(doc);

            // save the first page in a temporary PDF file
            File tempPDFFile = File.createTempFile("tempPDFFile", "pdf");
            FileOutputStream outputStreamPDF = new FileOutputStream(tempPDFFile);
            pdfOutput.save(outputStreamPDF);
            pdfInput.close();
            pdfOutput.close();
            outputStreamPDF.flush();
            outputStreamPDF.close();
                
            // process OCR on the temporary JPG file
            InputStream inputStreamPDF = new FileInputStream(tempPDFFile);
            returnedTextData.settext(OCRTextExtractor.getText(inputStreamPDF).gettext().trim());
            inputStreamPDF.close();
            
            // delete temporary files
            tempPDFFile.delete();
            System.out.println();
        }
        return returnedTextData;
    }
}
