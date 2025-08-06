package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;
import mz.betainteractive.utilities.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
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
    private String formUuid;
    private String filename;
    private String collectedDate;

    public XmlFormResult(HForm form, Collection<ColumnValue> collectedValues, String instancesDirPath) {
        this.form = form;
        this.xmlResult = generateXml(collectedValues);
        this.filename = generateFilename(instancesDirPath);
    }

    private String generateXml(Collection<ColumnValue> collectedValues) {

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = doc.createElement(form.getFormId());
            doc.appendChild(rootElement);

            collectedValues.forEach( columnValue -> {

                if (columnValue instanceof RepeatColumnValue) {
                    //handle it
                    RepeatColumnValue repeatColumnValue = (RepeatColumnValue) columnValue;
                    createRepeatElement(doc, rootElement, repeatColumnValue);

                } else {
                    createElement(doc, rootElement, columnValue);

                    if (columnValue.getColumnType()==ColumnType.INSTANCE_UUID) {
                        this.formUuid = columnValue.getValue();
                    }

                    if (columnValue.getColumnType()==ColumnType.TIMESTAMP && columnValue.getColumnName().equals("collectedDate")) {
                        this.collectedDate = columnValue.getValue();
                    }
                }


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

    private String generateFilename(String basePath) {
        //form-id + form-uuid + date
        return basePath + form.getFormId() + "-" + formUuid + "-" + formatUnderscoreDate(collectedDate) + ".xml";
    }

    private String formatUnderscoreDate(String collectedDate) {
        //yyyy-MM-dd_HH_mm_ss
        collectedDate = collectedDate.replaceAll(" ", "_");
        collectedDate = collectedDate.replaceAll(":", "_");

        return collectedDate;
    }

    private Element createRepeatElement(Document doc, Element rootElement, RepeatColumnValue repeatColumnValue) {
        Element groupElement = doc.createElement(repeatColumnValue.getGroupName());

        int repeatCount = repeatColumnValue.getCount();

        for (int i = 0; i < repeatCount; i++) {
            Element objElement = doc.createElement(repeatColumnValue.getNodeName()); //<rawSome></rawSome>

            Collection<ColumnValue> columnValues = repeatColumnValue.getColumnValues(i);

            for (ColumnValue columnValue : columnValues) {
                if (columnValue.getColumnType()==ColumnType.GPS){
                    List<Element> elements = createGpsElement(doc, columnValue);
                    elements.forEach(element -> {
                        objElement.appendChild(element);
                    });
                } else {
                    Element element = createElement(doc, columnValue);
                    objElement.appendChild(element);
                }

            }

            groupElement.appendChild(objElement);
        }

        rootElement.appendChild(groupElement);

        return groupElement;
    }

    private Element createElement(Document doc, ColumnValue columnValue) {

        String textData = columnValue.getValue();

        Element element = doc.createElement(columnValue.getColumnName());
        if (textData != null){
            element.appendChild(doc.createTextNode(textData));
        }

        return element;
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

    private List<Element> createGpsElement(Document doc, ColumnValue columnValue) {
        List<Element> elements = new ArrayList<>();

        Map<String, Double> gpsValues = columnValue.getGpsValues();

        gpsValues.forEach( (column, value) -> {
            Element element = doc.createElement(column);
            element.appendChild(doc.createTextNode(value==null ? "" : value.toString()));
            elements.add(element);
        });

        return elements;
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

    public String getFormUuid() {
        return formUuid;
    }

    public String getFilename() {
        return filename;
    }

    public String getXmlResult() {
        return xmlResult;
    }
}
