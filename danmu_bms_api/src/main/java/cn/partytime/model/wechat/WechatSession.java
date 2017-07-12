package cn.partytime.model.wechat;

import cn.partytime.model.PartyLogicModel;
import cn.partytime.model.manager.DanmuAddress;

/**
 * Created by administrator on 2016/12/1.
 */
public class WechatSession {

    private String openId;

    private PartyLogicModel partyLogicModel;

    private DanmuAddress danmuAddress;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }


    public DanmuAddress getDanmuAddress() {
        return danmuAddress;
    }

    public void setDanmuAddress(DanmuAddress danmuAddress) {
        this.danmuAddress = danmuAddress;
    }

    public PartyLogicModel getPartyLogicModel() {
        return partyLogicModel;
    }

    public void setPartyLogicModel(PartyLogicModel partyLogicModel) {
        this.partyLogicModel = partyLogicModel;
    }
}