package com.suchness.landmanage.bus;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/**
 * @author: hejunfeng
 * @date: 2021/12/17 0017
 */
public enum  Rxbus {
    INSTANCE;

    private Subject mSubject = ReplaySubject.create ();

    public void post(Object obj){
        mSubject.onNext (obj);
    }

    public <T> Observable<T> toObservable(Class<T> clazz){
        // 只处理clazz类型的数据
        return mSubject.ofType (clazz);
    }
}
