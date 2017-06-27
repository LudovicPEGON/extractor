package com.everteam.extractor.text.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.text.ITextExtractor;

/**
 * A default implementation of ITextExtractor. Utilize AutoDetectParser to parse
 * the InputStream, an object of BodyContentHandler contains the extracted text
 * and metaData holds the metadata information of file.
 * 
 * The configuration xml file is used to disable the OCR.
 * 
 * @author arpita, jg.try
 *
 */
@Component
public class TikaTextExtractor implements ITextExtractor {
    private static final Logger log = LoggerFactory.getLogger(TikaTextExtractor.class);
    private TikaConfig tikaConfig =null;

    /**
     *Extract {@link TextData} from an InputStream in a file.
     * 
     * @param inputStream
     * @return {@link TextData}
     */
    @Override
    public TextData getText(InputStream inputStream) {
        try {
            URL confUrl = this.getClass().getResource("/tika-config.xml");
            tikaConfig = new TikaConfig(confUrl);
        }
        catch (TikaException | IOException | SAXException e1) {
            log.error("An error has occured while configuring Tika."
                    + e1.getMessage());
            throw new RuntimeException(e1.getMessage(), e1);
        }
        AutoDetectParser parser = new AutoDetectParser(tikaConfig);
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        TextData textData = new TextData();
        
        try {
            parser.parse(inputStream, handler, metadata);
            Map<String, String> textMetadata = new HashMap<>();
            String[] names = metadata.names();
            for (String key : names) {
                textMetadata.put(key, metadata.get(key));
            }
            String text = handler.toString().trim();
            textData.settext(text);
            textData.setMetadata(textMetadata);
        } catch (Exception e) {
            log.error("An error has occured while parsing the file."
                    + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return textData;
    }

}
