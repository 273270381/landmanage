package com.suchness.landmanage.data.been;

/**
 * @Author hejunfeng
 * @Date 11:45 2021/3/15 0015
 * @Description com.analysis.wisdomtraffic.been
 **/
public class TosMessage {
    public int Type;
    public Object object;

    public TosMessage(int type, Object object) {
        Type = type;
        this.object = object;
    }

    public static TosMessage getInstance(int type, Object o){
        return new TosMessage(type,o);
    }

    public int getType() {
        return Type;
    }

    public Object getObject() {
        return object;
    }

    public void setType(int type) {
        Type = type;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
