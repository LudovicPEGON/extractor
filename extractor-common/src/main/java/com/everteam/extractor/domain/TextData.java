package com.everteam.extractor.domain;

import java.util.Map;


/**
 * Text data from a file.
 * 
 * TextData = Text (String) + Metadata (Map<String, String>)
 * 
 * @author jg.try
 *
 */
public class TextData {

    public String text;
    public Map<String, String> metadata;

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String gettext() {
        return text;
    }

    public void settext(String text) {
        this.text = text;
    }

}
