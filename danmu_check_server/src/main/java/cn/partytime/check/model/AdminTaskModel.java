package cn.partytime.check.model;

import io.netty.channel.Channel;

/**
 * Created by lENOVO on 2016/10/11.
 */
public class AdminTaskModel {
    /*管理员编号*/
    private String adminId;

    private String adminName;

    /**
     *  秘钥
     */
    private String authKey;

    /**活动编号*/
    private String partyId;

    /**地址编号*/
    private String addressId;

    /**管理员类型*/
    private String type;

    /*任务数*/
    private int count;

    /*通道**/
    private Channel channel;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }


    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}
