package com.suchness.landmanage.data.been;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: hejunfeng
 * @date: 2021/12/24 0024
 */
public class AlarmMsg implements Parcelable {
    private String imgPath;
    private String alarmType;
    private String startTime;


    protected AlarmMsg(Parcel in) {
        imgPath = in.readString();
        alarmType = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        channel = in.readString();
        area = in.readString();
        devDesc = in.readString();
        serialNumber = in.readString();
        gplLocation = in.readString();
        averageSpeed = in.readString();
    }

    public static final Creator<AlarmMsg> CREATOR = new Creator<AlarmMsg>() {
        @Override
        public AlarmMsg createFromParcel(Parcel in) {
            return new AlarmMsg(in);
        }

        @Override
        public AlarmMsg[] newArray(int size) {
            return new AlarmMsg[size];
        }
    };

    public void setAverageSpeed(String  averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    private String endTime;

    public String  getAverageSpeed() {
        return averageSpeed;
    }

    private String channel;
    private String area;
    private String devDesc;
    private String serialNumber;
    private String gplLocation;
    private String averageSpeed;

    public AlarmMsg() {
    }

    public AlarmMsg(String imgPath, String averageSpeed,String alarmType, String startTime, String endTime, String channel, String area, String devDesc, String serialNumber, String gplLocation) {
        this.imgPath = imgPath;
        this.alarmType = alarmType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.channel = channel;
        this.area = area;
        this.devDesc = devDesc;
        this.serialNumber = serialNumber;
        this.gplLocation = gplLocation;
        this.averageSpeed = averageSpeed;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setDevDesc(String devDesc) {
        this.devDesc = devDesc;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setGplLocation(String gplLocation) {
        this.gplLocation = gplLocation;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getChannel() {
        return channel;
    }

    public String getArea() {
        return area;
    }

    public String getDevDesc() {
        return devDesc;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getGplLocation() {
        return gplLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imgPath);
        dest.writeString(alarmType);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(channel);
        dest.writeString(area);
        dest.writeString(devDesc);
        dest.writeString(serialNumber);
        dest.writeString(gplLocation);
    }
}
