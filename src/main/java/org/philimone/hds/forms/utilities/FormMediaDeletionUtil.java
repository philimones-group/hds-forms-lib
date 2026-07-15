package org.philimone.hds.forms.utilities;

import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;
import android.util.Log;
import android.net.Uri;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FormMediaDeletionUtil {

    public static void deleteMediaFiles(HForm form, File xmlFile) {
        if (form == null || xmlFile == null || !xmlFile.exists()) {
            return;
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            File instancesDir = xmlFile.getParentFile();

            // Find the root element (Form ID)
            NodeList rootList = doc.getElementsByTagName(form.getFormId());
            if (rootList.getLength() > 0) {
                Element rootElement = (Element) rootList.item(0);
                processElement(instancesDir, rootElement, form.getColumns());
            }

        } catch (Exception e) {
            Log.e("FormMediaDeletionUtil", "Error deleting media files", e);
        }
    }

    private static void processElement(File instancesDir, Element element, List<ColumnGroup> columnGroups) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String nodeName = childElement.getNodeName();

                // Check if this node corresponds to a column or a repeat group
                for (ColumnGroup group : columnGroups) {
                    if (group instanceof ColumnRepeatGroup) {
                        ColumnRepeatGroup rg = (ColumnRepeatGroup) group;
                        if (rg.getGroupName().equals(nodeName)) {
                            // It's a repeat group, process its instances
                            processRepeatGroup(instancesDir, childElement, rg);
                        }
                    } else {
                        // It's a regular group, check its columns
                        Column column = group.getColumn(nodeName);
                        if (column != null && isMediaColumn(column)) {
                            deleteMediaFile(instancesDir, childElement.getTextContent());
                        }
                    }
                }
            }
        }
    }

    private static void processRepeatGroup(File instancesDir, Element groupElement, ColumnRepeatGroup rg) {
        NodeList instances = groupElement.getElementsByTagName(rg.getNodeName());

        for (int i = 0; i < instances.getLength(); i++) {
            Node instance = instances.item(i);
            if (instance.getNodeType() == Node.ELEMENT_NODE) {
                processElement(instancesDir, (Element) instance, rg.getColumnsGroups());
            }
        }
    }

    private static boolean isMediaColumn(Column column) {
        ColumnType type = column.getType();
        return type == ColumnType.IMAGE || type == ColumnType.AUDIO || type == ColumnType.VIDEO;
    }

    private static void deleteMediaFile(File instancesDir, String filename) {
        if (filename == null || filename.isEmpty()) return;

        File mediaFile = null;

        if (filename.startsWith("file://")) {
            try {
                String path = Uri.parse(filename).getPath();
                if (path != null) {
                    mediaFile = new File(path);
                }
            } catch (Exception e) {
                Log.e("FormMediaDeletionUtil", "Error parsing media file URI", e);
            }
        } else if (filename.startsWith("/")) {
            mediaFile = new File(filename);
        } else {
            mediaFile = new File(instancesDir, filename);
        }

        if (mediaFile != null && mediaFile.exists()) {
            boolean deleted = mediaFile.delete();
            if (!deleted) {
                Log.w("FormMediaDeletionUtil", "Could not delete media file: " + mediaFile.getAbsolutePath());
            }
        }
    }
}
