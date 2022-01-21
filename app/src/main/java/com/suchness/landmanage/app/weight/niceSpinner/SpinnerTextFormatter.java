package com.suchness.landmanage.app.weight.niceSpinner;

import android.text.Spannable;

public interface SpinnerTextFormatter<T> {
    Spannable format(T text);
}
