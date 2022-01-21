package com.suchness.landmanage.app.utils;

import android.content.Context;

import com.suchness.landmanage.R;

import cn.pedant.SweetAlert.ProgressHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author: hejunfeng
 * @date: 2021/11/30 0030
 */
public class SweetAlertDialogUtils {
    private SweetAlertDialog progressDialog = null;

    public static interface DialogCallBack{
        void conform();
        void cancel();
    }

    public static SweetAlertDialog showSweetProgressDialog(Context context,String content){
        SweetAlertDialog dialog = new SweetAlertDialog(context,SweetAlertDialog.PROGRESS_TYPE);
        ProgressHelper helper = dialog.getProgressHelper();
        helper.setBarColor(R.color.error_stroke_color);
        dialog.setTitleText(content);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    public static SweetAlertDialog showCunstomDialog(Context context){
        final SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Loading");
        pDialog.show();
        pDialog.setCancelable(false);
        pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.blue_btn_bg_color));
        pDialog.setTitleText("Success!")
                .setConfirmText("OK")
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        return pDialog;
    }

    public static SweetAlertDialog showBasicDialog(Context context,String title, String content, final DialogCallBack callBack){
        final SweetAlertDialog dialog = new SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText(title);
        dialog.setContentText(content);
        dialog.setCancelText("no");
        dialog.setConfirmText("ok");
        dialog.showCancelButton(true);
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                callBack.cancel();
                dialog.dismissWithAnimation();
            }
        });
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                callBack.conform();
                dialog.dismissWithAnimation();
            }
        }).show();
        return dialog;
    }
}
