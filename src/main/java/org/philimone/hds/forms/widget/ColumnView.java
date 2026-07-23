package org.philimone.hds.forms.widget;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.FormController;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.model.enums.ColumnValueStatus;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

import java.io.File;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ColumnView extends LinearLayout {

    protected ColumnGroupView columnGroupView;
    protected ColumnModel columnModel;
    protected Column column;
    protected TextView txtColumnRequired;
    protected ExternalMethodCallListener methodCallListener;

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view.getContext(), attrs);
        this.columnGroupView = view;
        this.columnModel = columnModel;
        this.column = columnModel.getColumn();
        this.methodCallListener = callListener;

        buildViews(resource);
    }

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, resource, null, columnModel, callListener);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == View.VISIBLE) {
            refreshLabels();
        }
    }

    public FormFragment getActivity() {
        return this.columnGroupView.getFormPanel();
    }

    public String getLabel() {
        return this.column.getLabel();
    }

    public String getName() {
        return this.column.getName();
    }

    public ColumnType getType() {
        return this.column.getType();
    }

    public abstract String getValue();

    public abstract String getValueAsXml();

    public abstract void setValue(String value);

    public abstract void refreshModelToUI();

    public abstract void refreshInteractionState();

    public abstract void refreshLabels();

    public Column getColumn() {
        return this.column;
    }

    public ColumnModel getColumnModel() {
        return columnModel;
    }

    protected String generateMediaFilename(String extension) {
        FormFragment hostFragment = getActivity();
        String instanceFileName = hostFragment.getFormInstanceFileName();

        // To avoid duplicates files inside repeat groups or other situations we will append a nanoTime
        long uniqueTimestamp = System.nanoTime();

        StringBuilder filename = new StringBuilder(instanceFileName);
        filename.append("_").append(column.getName());
        filename.append("_").append(uniqueTimestamp);

        filename.append(extension);
        return filename.toString();
    }

    protected Uri resolveUri(String value) {
        if (StringUtil.isBlank(value)) return null;

        Uri uri = Uri.parse(value);
        if (uri.getScheme() == null || "file".equals(uri.getScheme())) {
            String filePath = uri.getScheme() == null ? value : uri.getPath();
            if (filePath == null) return uri;

            File file = new File(filePath);
            if (!file.exists()) {
                FormFragment activity = getActivity();
                if (activity != null) {
                    String instancesDirPath = activity.getInstancesDirPath();
                    if (instancesDirPath != null) {
                        file = new File(instancesDirPath, filePath);
                    }
                }
            }

            if (file.exists()) {
                try {
                    String authority = getContext().getPackageName() + ".fileprovider";
                    uri = FileProvider.getUriForFile(getContext(), authority, file);
                } catch (Exception e) {
                    uri = Uri.fromFile(file);
                }
            }
        }
        return uri;
    }

    private void buildViews(@LayoutRes int resource) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resource, this);
    }

    protected void afterUserInput() {
        // Update model value with USER_INPUT status
        columnModel.setValue(getValue(), ColumnValueStatus.FROM_USER_INPUT);

        // Notify controller to re-evaluate form logic
        if (getActivity() != null && getActivity().getFormController() != null) {
            FormController controller = getActivity().getFormController();
            controller.onModelValueChanged(columnModel);

            columnGroupView.refreshChildViews();
        }
    }

    public boolean isDisplayable() {
        return columnModel.isDisplayable();
    }

    public DateUtil.SupportedCalendar getSupportedCalendar() {
        return (this.columnGroupView != null && this.columnGroupView.getFormPanel() != null) ? this.columnGroupView.getFormPanel().supportedCalendar : null;
    }

    protected void setTextHtml(TextView textView, String labelText) {
        if (labelText != null) {
            labelText = columnModel.translateVariables(labelText);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(labelText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(labelText));
            }

            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public String toString() {
        return "ColumnView{" + getName() + "}";
    }
}
