package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcWechatService;
import cn.partytime.model.WechatUser;
import cn.partytime.model.WechatUserInfo;

public class RpcWechatServiceHystrix implements RpcWechatService {
    @Override
    public WechatUser findByOpenId(String openId) {
        return null;
    }

    @Override
    public WechatUserInfo findByWechatId(String wechatId) {
        return null;
    }
}
