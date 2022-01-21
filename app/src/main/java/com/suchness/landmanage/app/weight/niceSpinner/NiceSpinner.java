package com.suchness.landmanage.app.weight.niceSpinner;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;


import com.suchness.landmanage.R;

import java.util.Arrays;
import java.util.List;

/*
 * Copyright (C) 2015 Angelo Marchesin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class NiceSpinner extends AppCompatTextView {
    private static final int MAX_LEVEL = 10000;
    private static final int VERTICAL_OFFSET = 1;
    private static final String INSTANCE_STATE = "instance_state";
    private static final String SELECTED_INDEX = "selected_index";
    private static final String IS_POPUP_SHOWING = "is_popup_showing";
    private static final String IS_ARROW_HIDDEN = "is_arrow_hidden";
    private static final String ARROW_DRAWABLE_RES_ID = "arrow_drawable_res_id";
    private int selectedIndex;
    private Drawable arrowDrawable;
    private ListPopupWindow popupWindow;
    private NiceSpinnerBaseAdapter adapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private OnSpinnerItemSelectedListener onSpinnerItemSelectedListener;
    private boolean isArrowHidden;
    private int textColor;
    private int backgroundSelector;
    private int arrowDrawableTint;
    private int displayHeight;
    private int parentVerticalOffset;
    private int dropDownListPaddingBottom;
    @DrawableRes
    private int arrowDrawableResId;
    private SpinnerTextFormatter spinnerTextFormatter = new SimpleSpinnerTextFormatter();
    private SpinnerTextFormatter selectedTextFormatter = new SimpleSpinnerTextFormatter();
    private PopUpTextAlignment horizontalAlignment;
    @Nullable
    private ObjectAnimator arrowAnimator = null;

    public NiceSpinner(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public NiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public NiceSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instance_state", super.onSaveInstanceState());
        bundle.putInt("selected_index", this.selectedIndex);
        bundle.putBoolean("is_arrow_hidden", this.isArrowHidden);
        bundle.putInt("arrow_drawable_res_id", this.arrowDrawableResId);
        if (this.popupWindow != null) {
            bundle.putBoolean("is_popup_showing", this.popupWindow.isShowing());
        }

        return bundle;
    }

    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle)savedState;
            this.selectedIndex = bundle.getInt("selected_index");
            if (this.adapter != null) {
                this.setTextInternal(this.selectedTextFormatter.format(this.adapter.getItemInDataset(this.selectedIndex)).toString());
                this.adapter.setSelectedIndex(this.selectedIndex);
            }

            if (bundle.getBoolean("is_popup_showing") && this.popupWindow != null) {
                this.post(this::showDropDown);
            }

            this.isArrowHidden = bundle.getBoolean("is_arrow_hidden", false);
            this.arrowDrawableResId = bundle.getInt("arrow_drawable_res_id");
            savedState = bundle.getParcelable("instance_state");
        }

        super.onRestoreInstanceState(savedState);
    }

    private void init(Context context, AttributeSet attrs) {
        Resources resources = this.getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NiceSpinner);
        int defaultPadding = resources.getDimensionPixelSize(R.dimen.dp_12);
        this.setGravity(8388627);
        this.setPadding(resources.getDimensionPixelSize(R.dimen.dp_24), defaultPadding, defaultPadding, defaultPadding);
        this.setClickable(true);
        this.backgroundSelector = typedArray.getResourceId(R.styleable.NiceSpinner_backgroundSelector, R.drawable.selector);
        this.setBackgroundResource(this.backgroundSelector);
        this.textColor = typedArray.getColor(R.styleable.NiceSpinner_textTint, this.getDefaultTextColor(context));
        this.setTextColor(this.textColor);
        this.popupWindow = new ListPopupWindow(context);
        this.popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= NiceSpinner.this.selectedIndex && position < NiceSpinner.this.adapter.getCount()) {
                    ++position;
                }

                NiceSpinner.this.selectedIndex = position;
                if (NiceSpinner.this.onSpinnerItemSelectedListener != null) {
                    NiceSpinner.this.onSpinnerItemSelectedListener.onItemSelected(NiceSpinner.this, view, position, id);
                }

                if (NiceSpinner.this.onItemClickListener != null) {
                    NiceSpinner.this.onItemClickListener.onItemClick(parent, view, position, id);
                }

                if (NiceSpinner.this.onItemSelectedListener != null) {
                    NiceSpinner.this.onItemSelectedListener.onItemSelected(parent, view, position, id);
                }

                NiceSpinner.this.adapter.setSelectedIndex(position);
                NiceSpinner.this.setTextInternal(NiceSpinner.this.adapter.getItemInDataset(position));
                NiceSpinner.this.dismissDropDown();
            }
        });
        this.popupWindow.setModal(true);
        this.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                if (!NiceSpinner.this.isArrowHidden) {
                    NiceSpinner.this.animateArrow(false);
                }

            }
        });
        this.isArrowHidden = typedArray.getBoolean(R.styleable.NiceSpinner_hideArrow, false);
        this.arrowDrawableTint = typedArray.getColor(R.styleable.NiceSpinner_arrowTint, getResources().getColor(R.color.black));
        this.arrowDrawableResId = typedArray.getResourceId(R.styleable.NiceSpinner_arrowDrawable, R.drawable.arrow);
        this.dropDownListPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_dropDownListPaddingBottom, 0);
        this.horizontalAlignment = PopUpTextAlignment.fromId(typedArray.getInt(R.styleable.NiceSpinner_popupTextAlignment, PopUpTextAlignment.CENTER.ordinal()));
        CharSequence[] entries = typedArray.getTextArray(R.styleable.NiceSpinner_entries);
        if (entries != null) {
            this.attachDataSource(Arrays.asList(entries));
        }

        typedArray.recycle();
        this.measureDisplayHeight();
    }

    private void measureDisplayHeight() {
        this.displayHeight = this.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    private int getParentVerticalOffset() {
        if (this.parentVerticalOffset > 0) {
            return this.parentVerticalOffset;
        } else {
            int[] locationOnScreen = new int[2];
            this.getLocationOnScreen(locationOnScreen);
            return this.parentVerticalOffset = locationOnScreen[1];
        }
    }

    protected void onDetachedFromWindow() {
        if (this.arrowAnimator != null) {
            this.arrowAnimator.cancel();
        }

        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT <= 19) {
            this.onVisibilityChanged(this, this.getVisibility());
        }

    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.arrowDrawable = this.initArrowDrawable(this.arrowDrawableTint);
        this.setArrowDrawableOrHide(this.arrowDrawable);
    }

    private Drawable initArrowDrawable(int drawableTint) {
        if (this.arrowDrawableResId == 0) {
            return null;
        } else {
            Drawable drawable = ContextCompat.getDrawable(this.getContext(), this.arrowDrawableResId);
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable).mutate();
                if (drawableTint != 2147483647 && drawableTint != 0) {
                    DrawableCompat.setTint(drawable, drawableTint);
                }
            }

            return drawable;
        }
    }

    private void setArrowDrawableOrHide(Drawable drawable) {
        if (!this.isArrowHidden && drawable != null) {
            this.setCompoundDrawablesWithIntrinsicBounds((Drawable)null, (Drawable)null, drawable, (Drawable)null);
        } else {
            this.setCompoundDrawablesWithIntrinsicBounds((Drawable)null, (Drawable)null, (Drawable)null, (Drawable)null);
        }

    }

    private int getDefaultTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(16842806, typedValue, true);
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{16842806});
        int defaultTextColor = typedArray.getColor(0, -16777216);
        typedArray.recycle();
        return defaultTextColor;
    }

    public Object getItemAtPosition(int position) {
        return this.adapter.getItemInDataset(position);
    }

    public Object getSelectedItem() {
        return this.adapter.getItemInDataset(this.selectedIndex);
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setArrowDrawable(@DrawableRes @ColorRes int drawableId) {
        this.arrowDrawableResId = drawableId;
        this.arrowDrawable = this.initArrowDrawable(R.drawable.arrow);
        this.setArrowDrawableOrHide(this.arrowDrawable);
    }

    public void setArrowDrawable(Drawable drawable) {
        this.arrowDrawable = drawable;
        this.setArrowDrawableOrHide(this.arrowDrawable);
    }

    private void setTextInternal(Object item) {
        if (this.selectedTextFormatter != null) {
            this.setText(this.selectedTextFormatter.format(item));
        } else {
            this.setText(item.toString());
        }
    }

    public void setSelectedIndex(int position) {
        if (this.adapter != null) {
            if (position < 0 || position > this.adapter.getCount()) {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }

            this.adapter.setSelectedIndex(position);
            this.selectedIndex = position;
            Spannable s = this.selectedTextFormatter.format(this.adapter.getItemInDataset(position));
            this.setText(s.toString());
        }

    }

    /** @deprecated */
    @Deprecated
    public void addOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /** @deprecated */
    @Deprecated
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public <T> void attachDataSource(@NonNull List<T> list) {
        this.adapter = new NiceSpinnerAdapter(this.getContext(), list, this.textColor, this.backgroundSelector, this.spinnerTextFormatter, this.horizontalAlignment);
        this.setAdapterInternal(this.adapter);
    }

    public void setAdapter(ListAdapter adapter) {
        this.adapter = new NiceSpinnerAdapterWrapper(this.getContext(), adapter, this.textColor, this.backgroundSelector, this.spinnerTextFormatter, this.horizontalAlignment);
        this.setAdapterInternal(this.adapter);
    }

    public PopUpTextAlignment getPopUpTextAlignment() {
        return this.horizontalAlignment;
    }

    private <T> void setAdapterInternal(NiceSpinnerBaseAdapter<T> adapter) {
        if (adapter.getCount() > 0) {
            this.selectedIndex = 0;
            this.popupWindow.setAdapter(adapter);
            this.setTextInternal(adapter.getItemInDataset(this.selectedIndex));
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.isEnabled() && event.getAction() == 1) {
            if (!this.popupWindow.isShowing()) {
                this.showDropDown();
            } else {
                this.dismissDropDown();
            }
        }

        return super.onTouchEvent(event);
    }

    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : 10000;
        int end = shouldRotateUp ? 10000 : 0;
        this.arrowAnimator = ObjectAnimator.ofInt(this.arrowDrawable, "level", new int[]{start, end});
        this.arrowAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        this.arrowAnimator.start();
    }

    public void dismissDropDown() {
        if (!this.isArrowHidden) {
            this.animateArrow(false);
        }

        this.popupWindow.dismiss();
    }

    public void showDropDown() {
        if (!this.isArrowHidden) {
            this.animateArrow(true);
        }

        this.popupWindow.setAnchorView(this);
        this.popupWindow.show();
        ListView listView = this.popupWindow.getListView();
        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalFadingEdgeEnabled(false);
            listView.setHorizontalFadingEdgeEnabled(false);
        }

    }

    private int getPopUpHeight() {
        return Math.max(this.verticalSpaceBelow(), this.verticalSpaceAbove());
    }

    private int verticalSpaceAbove() {
        return this.getParentVerticalOffset();
    }

    private int verticalSpaceBelow() {
        return this.displayHeight - this.getParentVerticalOffset() - this.getMeasuredHeight();
    }

    public void setTintColor(@ColorRes int resId) {
        if (this.arrowDrawable != null && !this.isArrowHidden) {
            DrawableCompat.setTint(this.arrowDrawable, ContextCompat.getColor(this.getContext(), resId));
        }

    }

    public void setArrowTintColor(int resolvedColor) {
        if (this.arrowDrawable != null && !this.isArrowHidden) {
            DrawableCompat.setTint(this.arrowDrawable, resolvedColor);
        }

    }

    public void hideArrow() {
        this.isArrowHidden = true;
        this.setArrowDrawableOrHide(this.arrowDrawable);
    }

    public void showArrow() {
        this.isArrowHidden = false;
        this.setArrowDrawableOrHide(this.arrowDrawable);
    }

    public boolean isArrowHidden() {
        return this.isArrowHidden;
    }

    public void setDropDownListPaddingBottom(int paddingBottom) {
        this.dropDownListPaddingBottom = paddingBottom;
    }

    public int getDropDownListPaddingBottom() {
        return this.dropDownListPaddingBottom;
    }

    public void setSpinnerTextFormatter(SpinnerTextFormatter spinnerTextFormatter) {
        this.spinnerTextFormatter = spinnerTextFormatter;
    }

    public void setSelectedTextFormatter(SpinnerTextFormatter textFormatter) {
        this.selectedTextFormatter = textFormatter;
    }

    public void performItemClick(int position, boolean showDropdown) {
        if (showDropdown) {
            this.showDropDown();
        }

        this.setSelectedIndex(position);
    }

    public void performItemClick(View view, int position, int id) {
        this.showDropDown();
        ListView listView = this.popupWindow.getListView();
        if (listView != null) {
            listView.performItemClick(view, position, (long)id);
        }

    }

    public OnSpinnerItemSelectedListener getOnSpinnerItemSelectedListener() {
        return this.onSpinnerItemSelectedListener;
    }

    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener onSpinnerItemSelectedListener) {
        this.onSpinnerItemSelectedListener = onSpinnerItemSelectedListener;
    }
}
