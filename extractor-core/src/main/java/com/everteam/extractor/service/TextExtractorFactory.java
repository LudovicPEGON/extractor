package com.everteam.extractor.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.everteam.extractor.text.ITextExtractor;
import com.everteam.extractor.text.impl.DwgTextExtractor;
import com.everteam.extractor.text.impl.TikaTextExtractor;

/**
 * A factory class for Text extractors. Returns an extractor depending upon the
 * content type of input file. TikaTextExtractor is default type.
 * 
 * @author arpita
 *
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "extractor.text")
public class TextExtractorFactory {
    private static final Logger log = LoggerFactory.getLogger(TextExtractorFactory.class);

    enum TextExtractors {
        tika
    };

    Map<String, String> contentTypes;

    public Map<String, String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(Map<String, String> contentTypes) {
        this.contentTypes = contentTypes;
    }
    
    /**
     * Return a specific Extractor depending on a contentType.
     * 
     * @param contentType
     * @return ITextExtractor
     */
    public ITextExtractor getExtractor(String contentType) {
        if (log.isDebugEnabled())
            log.debug("Get the extractor for content type " + contentType);
        if (contentType.equalsIgnoreCase("image/vnd.dwg")) {
            return new DwgTextExtractor();
        }
        else if (contentTypes != null && contentTypes.keySet() != null) {
            for (String type : contentTypes.keySet()) {
                if (type.equals(contentType)) {
                    String extractortype = contentTypes.get(type);
                    if (extractortype.equals(TextExtractors.tika.name())) {
                        return new TikaTextExtractor();
                    }
                }
            }
        }
        return new TikaTextExtractor();
    }
}