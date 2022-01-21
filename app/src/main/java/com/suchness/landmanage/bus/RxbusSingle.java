package com.suchness.landmanage.bus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * @author: hejunfeng
 * @date: 2021/12/17 0017
 */
public enum RxbusSingle {
    INSTANCE;

    // 发射器
    private ObservableEmitter mEmitter;

    private Observable mObservable = Observable.create (emitter -> {
        mEmitter = emitter;
        emitter.onNext (new Object ());
    });

    public void post(Object obj){
        mEmitter.onNext (obj);
    }

    public <T> Observable<T> toObservable(Class<T> clazz){
        // 只处理clazz类型的数据
        return mObservable.ofType (clazz);
    }
}
