package cn.partytime.collector.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by administrator on 2017/4/13.
 */

@Data
public class WechatUserInfo {

    private String id;

    /**
     * 微信id
     */
    private String wechatId;

    /**
     * 首次关注时的地理位置
     */
    private Double registLongitude;

    private Double registLatitude;

    /**
     * 首次关注的时间
     */
    private Date registDate;

    /**
     * 最近一次打开公众账号的地理位置信息
     */
    private Double lastLongitude;

    private Double lastLatitude;

    /**
     * 最近一次打开公众账号的时间
     */
    private Date lastGetLocationDate;


}
