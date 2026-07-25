package org.philimone.hds.forms.utilities;

import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;

import org.philimone.hds.forms.model.HForm;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

public class XmlDataUpdater {
    private String xmlSavedFormPath;
    private HForm form;

    public XmlDataUpdater(HForm form, String xmlSavedFormPath) {
        this.form = form;
        this.xmlSavedFormPath = xmlSavedFormPath;
    }

    public void updateValues(Map<String, String> contentMap) {
        if (contentMap == null || contentMap.isEmpty()) return;

        File originalFile = new File(xmlSavedFormPath);
        if (!originalFile.exists()) return;

        AtomicFile atomicFile = new AtomicFile(originalFile);
        FileOutputStream fos = null;

        try {
            fos = atomicFile.startWrite();
            
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument("UTF-8", null);

            try (FileInputStream fis = atomicFile.openRead()) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(fis, null);

                int eventType = parser.getEventType();
                String currentFormId = form.getFormId();
                boolean insideRoot = false;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            String namespace = parser.getNamespace();
                            String prefix = parser.getPrefix();
                            String tagName = parser.getName();

                            if (prefix != null) {
                                serializer.setPrefix(prefix, namespace);
                            }
                            serializer.startTag(namespace, tagName);

                            if (tagName.equals(currentFormId)) {
                                insideRoot = true;
                            }

                            // Copy attributes
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                serializer.attribute(parser.getAttributeNamespace(i), 
                                                     parser.getAttributeName(i), 
                                                     parser.getAttributeValue(i));
                            }

                            // Update value if needed
                            if (insideRoot && contentMap.containsKey(tagName)) {
                                String newValue = contentMap.get(tagName);
                                serializer.text(newValue == null ? "" : newValue);
                                
                                // Robust skip: move to the corresponding END_TAG of this element
                                skipSubtree(parser);
                                serializer.endTag(parser.getNamespace(), parser.getName());

                                if (tagName.equals(currentFormId)) {
                                    insideRoot = false;
                                }
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            String endTagName = parser.getName();
                            serializer.endTag(parser.getNamespace(), endTagName);
                            if (endTagName.equals(currentFormId)) {
                                insideRoot = false;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            serializer.text(parser.getText());
                            break;

                        case XmlPullParser.CDSECT:
                            serializer.cdsect(parser.getText());
                            break;

                        case XmlPullParser.ENTITY_REF:
                            serializer.entityRef(parser.getName());
                            break;

                        case XmlPullParser.IGNORABLE_WHITESPACE:
                            serializer.ignorableWhitespace(parser.getText());
                            break;

                        case XmlPullParser.PROCESSING_INSTRUCTION:
                            serializer.processingInstruction(parser.getText());
                            break;

                        case XmlPullParser.COMMENT:
                            serializer.comment(parser.getText());
                            break;

                        case XmlPullParser.DOCDECL:
                            serializer.docdecl(parser.getText());
                            break;
                    }
                    eventType = parser.next();
                }
            }

            serializer.endDocument();
            serializer.flush();
            
            atomicFile.finishWrite(fos);

        } catch (Exception e) {
            Log.e("XmlDataUpdater", "Error updating XML: " + e.getMessage(), e);
            if (fos != null) {
                atomicFile.failWrite(fos);
            }
        }
    }

    /**
     * Skips all content within the current element, including nested tags, 
     * until the matching END_TAG is reached.
     */
    private void skipSubtree(XmlPullParser parser) throws Exception {
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
