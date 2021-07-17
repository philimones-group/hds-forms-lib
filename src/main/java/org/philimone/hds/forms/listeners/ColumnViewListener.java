package org.philimone.hds.forms.listeners;

import org.philimone.hds.forms.widget.ColumnView;

public interface ColumnViewListener {
    void onColumnValueChanged(ColumnView columnView, boolean isBlank);
}
