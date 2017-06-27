package com.everteam.extractor.text.impl;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.cad.fileformats.cad.CadImage;
import com.aspose.cad.fileformats.cad.cadconsts.CadEntityTypeName;
import com.aspose.cad.fileformats.cad.cadobjects.CadAttDef;
import com.aspose.cad.fileformats.cad.cadobjects.CadAttrib;
import com.aspose.cad.fileformats.cad.cadobjects.CadBaseEntity;
import com.aspose.cad.fileformats.cad.cadobjects.CadBlockEntity;
import com.aspose.cad.fileformats.cad.cadobjects.CadInsertObject;
import com.aspose.cad.fileformats.cad.cadobjects.CadMText;
import com.aspose.cad.fileformats.cad.cadobjects.CadText;
import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.text.ITextExtractor;

/**
 * Extract text from .dwg file using the library Aspose.CAD.
 * 
 * For more information see : https://www.aspose.com/community/forums/
 * 
 * @author jg.try
 *
 */
public class DwgTextExtractor implements ITextExtractor {
    private static final Logger log = LoggerFactory.getLogger(DwgTextExtractor.class);
    
    
    /**
     *Extract {@link TextData} from an InputStream in a .dwg file.
     * 
     * Loop through the elements of the CadImage (main element of a .dwg file) to find text data.
     * 
     * @param inputStream
     * @return {@link TextData}
     */
    @Override
    public TextData getText(InputStream inputStream) {
        TextData textData = new TextData();
        StringBuilder text=new StringBuilder();
        CadImage cadImage = (CadImage) CadImage.load(inputStream);
        
        try {
            // Search for text in the entity
            for (CadBaseEntity entity : cadImage.getEntities()) {
                IterateCADNodeEntities(entity, text);
            }

            // Search for text in the block section
            for (CadBlockEntity blockEntity : cadImage.getBlockEntities().getValues()) {
                for (CadBaseEntity entity : blockEntity.getEntities()) {
                    IterateCADNodeEntities(entity, text);
                }
            }
            textData.settext(text.toString());
            return textData;
        }
        catch (IOException e) {
            log.error("An error has occured while parsing the file."
                    + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // Recursive function to iterate nodes inside nodes entities
    private static void IterateCADNodeEntities(CadBaseEntity obj, StringBuilder text) throws IOException {
        switch (obj.getTypeName()) {
            case CadEntityTypeName.TEXT:
                CadText childObjectText = (CadText) obj;
                text.append(childObjectText.getDefaultValue());
                break;

            case CadEntityTypeName.MTEXT:
                CadMText childObjectMText = (CadMText) obj;
                text.append(childObjectMText.getText());
                break;

            case CadEntityTypeName.INSERT:
                CadInsertObject childInsertObject = (CadInsertObject) obj;

                for (CadBaseEntity tempobj : childInsertObject.getChildObjects()) {
                    IterateCADNodes(tempobj, text);
                }
                break;

            case CadEntityTypeName.ATTDEF:
                CadAttDef attDef = (CadAttDef) obj;
                text.append(attDef.getDefaultString());
                break;

            case CadEntityTypeName.ATTRIB:
                CadAttrib attAttrib = (CadAttrib) obj;
                text.append(attAttrib.getDefaultText());
                break;
        }
    }
    // Recursive function to iterate nodes inside nodes 
    public static void IterateCADNodes(CadBaseEntity obj, StringBuilder text) throws IOException {
        if (obj.getClass() == CadText.class) {
            CadText childObj = (CadText) obj;

            if (childObj.getChildObjects().size() != 0) {
                for (CadBaseEntity tempobj : childObj.getChildObjects()) {
                    IterateCADNodes(tempobj, text);
                }
            }
            else {
                text.append(childObj.getDefaultValue());
            }
        }
        else if (obj.getClass() == CadMText.class) {
            CadMText childObj = (CadMText) obj;

            if (childObj.getChildObjects().size() != 0) {
                for (CadBaseEntity tempobj : childObj.getChildObjects()) {
                    IterateCADNodes(tempobj, text);
                }
            }
            else {
                text.append(childObj.getText());
            }
        }
        else if (obj.getClass() == CadInsertObject.class) {
            CadInsertObject childObj = (CadInsertObject) obj;
            if (childObj.getChildObjects().size() != 0) {
                for (CadBaseEntity tempobj : childObj.getChildObjects()) {
                    IterateCADNodes(tempobj, text);
                }
            }
            else {
                if (childObj.getTypeName() == CadEntityTypeName.ATTDEF) {
                    text.append(((CadAttDef) ((CadBaseEntity) childObj)).getDefaultString());
                }
                else if (childObj.getTypeName() == CadEntityTypeName.ATTRIB) {
                    text.append(((CadAttrib) ((CadBaseEntity) childObj)).getDefaultText());
                }
            }
        }
    }
}
