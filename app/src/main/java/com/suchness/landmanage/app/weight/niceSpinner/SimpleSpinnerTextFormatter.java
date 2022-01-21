package com.suchness.landmanage.app.weight.niceSpinner;

import android.text.Spannable;
import android.text.SpannableString;



public class SimpleSpinnerTextFormatter implements SpinnerTextFormatter {


    @Override
    public Spannable format(Object text) {
        return new SpannableString(text.toString());
    }
}
