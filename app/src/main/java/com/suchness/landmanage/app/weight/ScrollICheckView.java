package com.suchness.landmanage.app.weight;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import androidx.annotation.Nullable;

import com.suchness.landmanage.R;


/**
 * @Author hejunfeng
 * @Date 13:39 2021/3/29 0029
 * @Description com.analysis.wisdomtraffic.view
 **/
public class ScrollICheckView extends LinearLayout {
    private LinearLayout share = null;
    private LinearLayout delete = null;
    private Scroller scroller;

    public ScrollICheckView(Context context) {
        super(context);
    }

    public ScrollICheckView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.scroller_view,this);
        share = findViewById(R.id.share);
        delete = findViewById(R.id.delete);
        scroller = new Scroller(context);
    }

    public ScrollICheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void ScrollTo(int x,int y){
        scroller.startScroll(getScrollX(),getScrollY(),x,y,1500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(),scroller.getCurrY());
            postInvalidate();
        }
    }

    public void setShareClickListener(ScrollOnClickListener listener){
        share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.click();
            }
        });
    }

    public void setDeleteClickListener(ScrollOnClickListener listener){
        delete.setOnClickListener(v ->{
            listener.click();
        });
    }

    public interface ScrollOnClickListener{
        public void click();
    }
}
