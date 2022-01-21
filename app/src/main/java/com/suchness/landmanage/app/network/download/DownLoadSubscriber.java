package com.suchness.landmanage.app.network.download;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by goldze on 2017/5/11.
 */

public class DownLoadSubscriber<T> extends DisposableObserver<T> {
    private ProgressCallBack fileCallBack;

    private DownLoadMoreCallBack moreCallBack;

    public DownLoadSubscriber(ProgressCallBack fileCallBack) {
        this.fileCallBack = fileCallBack;
    }

    public DownLoadSubscriber(DownLoadMoreCallBack moreCallBack){
        this.moreCallBack = moreCallBack;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fileCallBack != null)
            fileCallBack.onStart();

    }

    @Override
    public void onComplete() {
        if (fileCallBack != null)
            fileCallBack.onCompleted();

        if (moreCallBack != null)
            moreCallBack.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        if (fileCallBack != null)
            fileCallBack.onError(e);

        if (moreCallBack != null)
            moreCallBack.onError(e);
    }

    @Override
    public void onNext(T t) {
        if (fileCallBack != null)
            fileCallBack.onSuccess(t);
    }
}