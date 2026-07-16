package org.philimone.hds.forms.utilities;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.HForm;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormMediaDeletionUtil {

    public static void deleteMediaFiles(HForm form, File xmlFile) {
        if (form == null || !form.hasMediaColumns() || xmlFile == null || !xmlFile.exists()) {
            return;
        }

        Set<String> mediaColumns = getAllMediaColumnNames(form);
        if (mediaColumns.isEmpty()) return;

        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fis, null);

            File instancesDir = xmlFile.getParentFile();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (mediaColumns.contains(tagName)) {
                        String filename = parser.nextText();
                        deleteMediaFile(instancesDir, filename);
                    }
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e("FormMediaDeletionUtil", "Error deleting media files using PullParser", e);
        }
    }

    private static Set<String> getAllMediaColumnNames(HForm form) {
        Set<String> names = new HashSet<>();
        collectMediaColumns(form.getColumns(), names);
        return names;
    }

    private static void collectMediaColumns(List<ColumnGroup> groups, Set<String> names) {
        for (ColumnGroup group : groups) {
            if (group instanceof ColumnRepeatGroup) {
                collectMediaColumns(((ColumnRepeatGroup) group).getColumnsGroups(), names);
            } else {
                for (Column column : group.getColumns()) {
                    if (column.isMediaColumn()) {
                        names.add(column.getName());
                    }
                }
            }
        }
    }

    private static void deleteMediaFile(File instancesDir, String filename) {
        if (filename == null || filename.trim().isEmpty()) return;

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
