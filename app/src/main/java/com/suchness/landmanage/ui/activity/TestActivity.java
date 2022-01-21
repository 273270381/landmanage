package com.suchness.landmanage.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.suchness.landmanage.R;
import org.jetbrains.annotations.Nullable;
import butterknife.ButterKnife;

/**
 * @author: hejunfeng
 * @date: 2021/12/21 0021
 */
public class TestActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(@androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }


}
