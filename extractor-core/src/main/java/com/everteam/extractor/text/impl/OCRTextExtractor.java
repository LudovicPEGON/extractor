package com.everteam.extractor.text.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.text.ITextExtractor;

/**
 * Perform OCR to extract text of a PDF file in its inline images.
 * 
 * @author jg.try
 *
 */
@Component
public class OCRTextExtractor implements ITextExtractor {
    private static final Logger log = LoggerFactory.getLogger(TikaTextExtractor.class);

    @Value("${extractor.OCR.languageConfig:}")
    private String languageConfig;

    /**
     * Extract {@link TextData} from an InputStream with OCR operation.
     * 
     * @param inputStream
     * @return {@link TextData}
     */
    @Override
    public TextData getText(InputStream inputStream) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        TextData textData = new TextData();

        TesseractOCRConfig TessOCRconfig = new TesseractOCRConfig();
        if (languageConfig == null) {
            languageConfig = "";
        }
        TessOCRconfig.setLanguage(languageConfig);
        ParseContext parseContext = new ParseContext();
        parseContext.set(TesseractOCRConfig.class, TessOCRconfig);

        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(true);
        parseContext.set(PDFParserConfig.class, pdfConfig);
        parseContext.set(Parser.class, parser); /*Need to add this line to make sure recursive parsing happens! */

        try {
            parser.parse(inputStream, handler, metadata, parseContext);
            Map<String, String> textMetadata = new HashMap<>();
            String[] names = metadata.names();
            for (String key : names) {
                textMetadata.put(key, metadata.get(key));
            }
            String text = handler.toString().trim();
            textData.settext(text);
            textData.setMetadata(textMetadata);
        } catch (Exception e) {
            log.error("An error has occured while parsing the file." + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return textData;
    }
}
