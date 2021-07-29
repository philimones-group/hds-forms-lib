package org.philimone.hds.forms.widget.dialog;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.ColumnGroupView;

import androidx.annotation.StringRes;

public class ToastX extends Toast {

    private Context mContext;
    private TextView txtMessage;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public ToastX(Context context) {
        super(context);
        this.mContext = context;
        initialize();
    }

    private void initialize() {
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastView = inflater.inflate(R.layout.custom_toast, null);
        setView(toastView);


        txtMessage = toastView.findViewById(R.id.message);

    }

    public void setMessage(String message){
        this.txtMessage.setText(message);
    }

    public void setMessage(@StringRes int messageId){
        this.txtMessage.setText(messageId);
    }

    public void setGravityCenter(View viewParent) {

        int[] coords = new int[2];
        viewParent.getLocationOnScreen(coords);

        int vw = viewParent.getWidth();
        int vh = viewParent.getHeight();
        int tw = getView().getWidth();
        int th = getView().getHeight();
        int offsetx = (vw - tw)/2;
        int offsety = (vh - th)/2;

        offsetx = 0; //+realpos
        offsety += coords[1]; //+realpos

        setGravity(Gravity.TOP | Gravity.CENTER, offsetx, offsety);

    }
}
