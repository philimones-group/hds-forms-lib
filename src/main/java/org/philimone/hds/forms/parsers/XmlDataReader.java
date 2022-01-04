package org.philimone.hds.forms.parsers;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlDataReader {

    public static Map<String,String> getXmlMappedData(String xmlFileName) {
        Map<String, String> map = new LinkedHashMap<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            FileInputStream fis = new FileInputStream(xmlFileName);

            parser.setInput(fis, null);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = null;

                //Log.d("beforestart1", parser.getName()+", event="+eventType);

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        eventType = parser.next();
                        name = parser.getName(); //any form id tag

                        parser.nextTag(); //jump to first tag after form id
                        Map<String, String> tempMap = readNodes(name, parser);
                        map.putAll(tempMap);
                        break;
                }

                eventType = parser.next();
            }

            fis.close();

        }  catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private static Map<String,String> readNodes(String formId, XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, String> map = new LinkedHashMap<>();

        while (notEndOfXmlDoc(formId, parser)) {

            //<first>value</first> or <first />
            //is at: <first> or <first />
            String name = parser.getName();

            if (!isEmptyTag(name, parser)) { //not <first />
                parser.next();               //goto: value
                String value = parser.getText();
                parser.nextTag();            //goto: </first>

                map.put(name, value);
            } else {
                parser.nextTag();
            }

            parser.nextTag();
        }

        return map;
    }

    private static boolean notEndOfXmlDoc(String element, XmlPullParser parser) throws XmlPullParserException {
        return !(element.equals(parser.getName()) && parser.getEventType() == XmlPullParser.END_TAG);
    }

    private static boolean isEmptyTag(String element, XmlPullParser parser) throws XmlPullParserException {
        return (element.equals(parser.getName()) && parser.isEmptyElementTag());
    }
}
