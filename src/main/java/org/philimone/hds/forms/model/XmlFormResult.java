package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlFormResult {

    private HForm form;
    private String xmlResult;

    public XmlFormResult(HForm form, List<ColumnValue> collectedValues) {
        this.form = form;
        this.xmlResult = generateXml(collectedValues);
    }

    private String generateXml(List<ColumnValue> collectedValues) {

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = doc.createElement(form.getFormId());
            doc.appendChild(rootElement);

            collectedValues.forEach( columnValue -> {
                //members
                createElement(doc, rootElement, columnValue);
            });

            // write the content into xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            StreamResult xmlResult = new StreamResult(writer);

            transformer.transform(new DOMSource(doc), xmlResult);

            return writer.toString();

        } catch (TransformerException | ParserConfigurationException exception) {
            exception.printStackTrace();
        }

        return null;

    }

    private Element createElement(Document doc, Element rootElement, ColumnValue columnValue) {

        String textData = null;

        if (columnValue.getColumnType() == ColumnType.GPS) {
            return createGpsElement(doc, rootElement, columnValue);
        } else {
            textData = columnValue.getValue();
        }

        Element element = doc.createElement(columnValue.getColumnName());
        if (textData != null){
            element.appendChild(doc.createTextNode(textData));
        }
        rootElement.appendChild(element); //add to root

        return element;
    }

    private Element createGpsElement(Document doc, Element rootElement, ColumnValue columnValue) {
        Map<String, Double> gpsValues = columnValue.getGpsValues();

        gpsValues.forEach( (column, value) -> {
            Element element = doc.createElement(column);
            element.appendChild(doc.createTextNode(value==null ? "" : value.toString()));
            rootElement.appendChild(element);
        });

        return null;
    }

    public HForm getForm() {
        return form;
    }

    public void setForm(HForm form) {
        this.form = form;
    }

    public String getXmlResult() {
        return xmlResult;
    }
}
