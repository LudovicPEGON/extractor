package com.everteam.extractor.text;

import java.io.InputStream;

import com.everteam.extractor.domain.TextData;

/**
 * Interface Text Extraction.
 * 
 * @author jg.try
 *
 */
public interface ITextExtractor {
    
    /**
     * @param inputStream
     * @return TextData 
     * Returns an object of TextData (a combination of
     *         extracted text and metadata).
     */
    public TextData getText(InputStream inputStream);
}
