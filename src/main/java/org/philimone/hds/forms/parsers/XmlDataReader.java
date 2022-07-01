package org.philimone.hds.forms.parsers;

import android.util.Log;

import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.RepeatObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlDataReader {

    public static Map<String,Object> getXmlMappedData(String xmlFilename, HForm form) {
        Map<String, Object> map = new LinkedHashMap<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(xmlFilename));

            Node node = doc.getElementsByTagName(form.getFormId()).item(0);

            if (node == null) {
                return map;
            }

            readMainNodes(node, map, form);

            Log.d("processXml", "finished!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return map;
    }

    private static void readMainNodes(Node node, Map<String,Object> map, HForm form) {
        NodeList nodes = node.getChildNodes();

        //Log.d("executing-readmain",""+node.getNodeName());

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            //Log.d("odk-xml-param", ""+n.getNodeName()+", "+n.getTextContent());

            if (n.getNodeType() == Node.ELEMENT_NODE) {

                if (n.hasChildNodes() && form.isRepeatColumnName(n.getNodeName())) {
                    //process repeat groups
                    String repeatNodeName = n.getNodeName();
                    NodeList repeatChilds = n.getChildNodes();

                    RepeatObject newRepObjList = new RepeatObject();
                    for (int ri = 0; ri < repeatChilds.getLength(); ri++) {
                        Node nodeRepObj = repeatChilds.item(ri);
                        NodeList childElements = nodeRepObj.getChildNodes();

                        //the repeat object doesnt care about the node name only its childs

                        Map<String, String> obj = newRepObjList.createNewObject();
                        for (int index=0; index < childElements.getLength(); index++) {
                            Node elementNode = childElements.item(index);
                            String elementName = elementNode.getNodeName();
                            String elementValue = elementNode.getTextContent();

                            obj.put(elementName, elementValue==null ? "" : elementValue);
                        }
                    }

                    map.put(repeatNodeName, newRepObjList);

                } else {
                    String name = n.getNodeName();
                    String value = n.getTextContent();

                    map.put(name, value==null ? "" : value);
                }

            }
        }
        //Log.d("finished", sbuilder.toString());
    }

}
