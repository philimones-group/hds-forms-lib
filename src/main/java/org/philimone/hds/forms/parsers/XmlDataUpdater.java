package org.philimone.hds.forms.parsers;

import android.util.Log;

import org.philimone.hds.forms.model.HForm;
import mz.betainteractive.utilities.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlDataUpdater {
    private String xmlSavedFormPath;
    private HForm form;

    public XmlDataUpdater(HForm form, String xmlSavedFormPath) {
        this.form = form;
        this.xmlSavedFormPath = xmlSavedFormPath;
    }

    public void updateValues(Map<String, String> contentMap) {
        try {

            Set<String> columnsSet = contentMap.keySet();
            File xmlFile = new File(xmlSavedFormPath);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            // Change the content of node
            NodeList nodeList = doc.getElementsByTagName(this.form.getFormId());

            NodeList childList = nodeList.item(0).getChildNodes();

            for (int i = 0; i < childList.getLength(); i++) {

                Node node = childList.item(i);

                if (node.getNodeType()==Node.ELEMENT_NODE && columnsSet.contains(node.getNodeName())){

                    Element elementNode = (Element) node;

                    String value = contentMap.get(node.getNodeName());

                    value = StringUtil.isBlank(value) ? "" : value;

                    elementNode.setTextContent(value);
                }

            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(xmlFile);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

        } catch (Exception e) {
            Log.d("error", ""+e.getMessage());

            e.printStackTrace();
        }
    }
}
