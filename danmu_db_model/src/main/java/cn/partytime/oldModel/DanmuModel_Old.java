package cn.partytime.oldModel;

import cn.partytime.baseModel.BaseModel;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by liuwei on 16/6/12.
 * 弹幕实体
 */
@Document(collection = "danmu_old")
public class DanmuModel_Old extends BaseModel{

    @Field("_id")
    private String id;
    private String color;
    private String msg;
    private Boolean isBlocked = false;
    //弹幕池
    private String danmuPoolId;

    private String checkUserId;

    /***弹幕来源 管理员:0,微信用户:1*/
    private int danmuSrc;

    /**弹幕类型 0:普通弹幕.1:语音弹幕*/
    private int type;

    /**
     * 开始时刻（当前时间-电影开始时间）
     */
    private int time;

    /**是否查看状态*/
    private boolean viewFlg=false;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public String getDanmuPoolId() {
        return danmuPoolId;
    }

    public void setDanmuPoolId(String danmuPoolId) {
        this.danmuPoolId = danmuPoolId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getDanmuSrc() {
        return danmuSrc;
    }

    public void setDanmuSrc(int danmuSrc) {
        this.danmuSrc = danmuSrc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isViewFlg() {
        return viewFlg;
    }

    public void setViewFlg(boolean viewFlg) {
        this.viewFlg = viewFlg;
    }

    public String getCheckUserId() {
        return checkUserId;
    }

    public void setCheckUserId(String checkUserId) {
        this.checkUserId = checkUserId;
    }
}
