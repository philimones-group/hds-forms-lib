package org.philimone.hds.forms.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class ColumnGroupView extends LinearLayout {

    private FormFragment formPanel;
    private ColumnGroupModel groupModel;
    private ExternalMethodCallListener methodCallListener;

    private TextView txtColumnGroupName;
    private TextView txtRepeatGroupRequired;
    private TextView txtRepeatGroupName;
    private TextView txtRepeatGroupIndex;
    private LinearLayout formRepeatGroupLayout;
    private LinearLayout formColumnGroupLayout;
    private RelativeLayout formToastLayout;
    private TextView formToastMessage;
    private List<ColumnView> columnViews = new ArrayList<>();

    public ColumnGroupView(Context context) {
        super(context);
        init();
    }

    public ColumnGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.column_group_view, this);

        this.txtColumnGroupName = findViewById(R.id.txtColumnGroupName);
        this.txtRepeatGroupRequired = findViewById(R.id.txtRepeatGroupRequired);
        this.txtRepeatGroupName = findViewById(R.id.txtRepeatGroupName);
        this.txtRepeatGroupIndex = findViewById(R.id.txtRepeatGroupIndex);
        this.formRepeatGroupLayout = findViewById(R.id.formRepeatGroupLayout);
        this.formColumnGroupLayout = findViewById(R.id.formColumnGroupLayout);
        this.formToastLayout = findViewById(R.id.formToastLayout);
        this.formToastMessage = findViewById(R.id.formToastMessage);
    }

    public void bind(FormFragment formPanel, ColumnGroupModel groupModel, ExternalMethodCallListener methodCallListener) {
        this.formPanel = formPanel;
        this.groupModel = groupModel;
        this.methodCallListener = methodCallListener;

        this.columnViews.clear();
        this.formColumnGroupLayout.removeAllViews();

        updateLabels();

        this.formToastMessage.setText("");

        if (groupModel.isRepeatItem()) {
            this.formRepeatGroupLayout.setVisibility(VISIBLE);
            setTextHtml(this.txtRepeatGroupName, groupModel.getRepeatGroup().getLabel());
            this.txtRepeatGroupIndex.setText(getContext().getString(R.string.repeat_group_instance_index_lbl, (groupModel.getRepeatIndex() + 1) + "", groupModel.getRepeatSize().toString()));
            this.txtColumnGroupName.setText("");
        } else {
            this.formRepeatGroupLayout.setVisibility(GONE);
        }

        this.txtColumnGroupName.setVisibility(txtColumnGroupName.getText().length() == 0 ? GONE : VISIBLE);

        for (ColumnModel columnModel : groupModel.getColumnModels()) {
            ColumnView view = createColumnView(columnModel);

            if (view != null) {
                formColumnGroupLayout.addView(view);
                columnViews.add(view);
                view.setVisibility(columnModel.getColumn().isHidden() ? GONE : VISIBLE);
            }
        }

        refreshChildViews();
    }

    private ColumnView createColumnView(ColumnModel columnModel) {
        ColumnType type = columnModel.getType();
        boolean readOnly = columnModel.isReadOnly();

        // In a more advanced implementation, we would use a ViewPool here
        if (type == ColumnType.INTEGER || type == ColumnType.DECIMAL || type == ColumnType.STRING) {
            return readOnly ? new ColumnTextView(this, columnModel, methodCallListener) : new ColumnTextboxView(this, columnModel, methodCallListener);
        }
        if (type == ColumnType.DATE) return new ColumnDateView(this, columnModel, methodCallListener);
        if (type == ColumnType.DATETIME) return new ColumnDateTimeView(this, columnModel, methodCallListener);
        if (type == ColumnType.SELECT) return new ColumnSelectView(this, columnModel, methodCallListener);
        if (type == ColumnType.MULTI_SELECT) return new ColumnMultiSelectView(this, columnModel, methodCallListener);
        if (type == ColumnType.GPS) return new ColumnGpsView(this, columnModel, methodCallListener);
        if (type == ColumnType.NOTE) return new ColumnNoteView(this, columnModel, methodCallListener);
        if (type == ColumnType.TIME) return new ColumnTimeView(this, columnModel, methodCallListener);
        if (type == ColumnType.BARCODE) return new ColumnBarcodeView(this, columnModel, methodCallListener);
        if (type == ColumnType.IMAGE) return new ColumnImageView(this, columnModel, methodCallListener);
        if (type == ColumnType.VIDEO) return new ColumnVideoView(this, columnModel, methodCallListener);
        if (type == ColumnType.AUDIO) return new ColumnAudioView(this, columnModel, methodCallListener);

        // System types usually rendered as TextView
        if (type == ColumnType.COLLECTED_BY || type == ColumnType.INSTANCE_UUID || type == ColumnType.DEVICE_ID ||
            type == ColumnType.START_TIMESTAMP || type == ColumnType.END_TIMESTAMP || type == ColumnType.EXECUTION_STATUS || type == ColumnType.TIMESTAMP) {
            return new ColumnTextView(this, columnModel, methodCallListener);
        }

        return null;
    }

    private void updateLabels() {
        if (groupModel.isRepeatItem()) {
            setTextHtml(this.txtRepeatGroupName, groupModel.getRepeatGroup().getLabel());
            this.txtRepeatGroupIndex.setText(getContext().getString(R.string.repeat_group_instance_index_lbl, (groupModel.getRepeatIndex() + 1) + "", groupModel.getRepeatSize().toString()));
            this.txtColumnGroupName.setText("");
        } else {
            setTextHtml(this.txtColumnGroupName, groupModel.getColumnGroup().getLabel() != null ? groupModel.getColumnGroup().getLabel() : "");
        }
    }

    public void showToastMessage(@StringRes int messageResId, ColumnModel model) {
        showToastMessage(getContext().getString(messageResId), model);
    }

    public void showToastMessage(String message, ColumnModel model) {
        setTextHtml(this.formToastMessage, message, model);

        this.formToastLayout.setAlpha(1f);
        this.formToastLayout.setVisibility(VISIBLE);
        this.formToastLayout.animate().alpha(1f).setDuration(200).setListener(null);

        new Handler().postDelayed(() -> {
            formToastLayout.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formToastLayout.setVisibility(GONE);
                }
            });
        }, 1500);
    }

    protected void setTextHtml(TextView textView, String labelText) {
        ColumnModel model = groupModel.getPrecedingColumnModel();
        setTextHtml(textView, labelText, model);
    }

    protected void setTextHtml(TextView textView, String labelText, ColumnModel model) {
        if (labelText != null) {
            if (model != null) {
                labelText = model.translateVariables(labelText);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(labelText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(labelText));
            }
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public List<ColumnView> getColumnViews() {
        return this.columnViews;
    }

    public FormFragment getFormPanel() {
        return formPanel;
    }

    public ColumnGroupModel getGroupModel() {
        return groupModel;
    }

    public void refreshChildViews() {
        updateLabels();
        for (ColumnView cv : columnViews) {
            cv.refreshLabels();
            cv.refreshInteractionState();
            cv.refreshModelToUI();
            cv.setVisibility(cv.getColumnModel().isDisplayable() && !cv.getColumn().isHidden() ? VISIBLE : GONE);
        }
    }
}
