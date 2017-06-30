package cn.partytime.service;

import cn.partytime.common.cachekey.CollectorServerCacheKey;
import cn.partytime.logic.collector.DanmuCollectorInfo;
import cn.partytime.model.ResultInfo;
import cn.partytime.model.ServerInfo;
import cn.partytime.model.wechat.WechatUser;
import cn.partytime.redis.service.RedisService;
import cn.partytime.repository.user.WechatUserRepository;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lENOVO on 2016/9/23.
 */

@Service
public class MobileLoginService {

    @Autowired
    private WechatUserRepository wechatUserRepository;

    @Autowired
    private RedisService redisService;

    public WechatUser findWechatUser(String openId){
        return  wechatUserRepository.findByOpenId(openId);
    }

    public ResultInfo findCollectorInfo(String openId) {

        ResultInfo resultInfo = new ResultInfo();
        try {
            WechatUser wechatUser = wechatUserRepository.findByOpenId(openId);;
            if (wechatUser == null) {
                resultInfo.setCode(404);
                resultInfo.setMessage("无效的openId");
                return resultInfo;
            }
            String key = CollectorServerCacheKey.COLLECTOR_SERVERLIST_CACHE_KEY;
            Set<String> collectorServerStr = redisService.getSortSetByRnage(key, 0, 1, true);
            DanmuCollectorInfo danmuCollectorInfo = null;
            if (collectorServerStr != null && !collectorServerStr.isEmpty()) {
                for (String str : collectorServerStr) {
                    danmuCollectorInfo = JSON.parseObject(str, DanmuCollectorInfo.class);
                    break;
                }
                if (danmuCollectorInfo != null) {
                    resultInfo.setCode(200);
                    resultInfo.setMessage("OK!");
                    ServerInfo serverInfo = new ServerInfo();
                    serverInfo.setIp(danmuCollectorInfo.getIp());
                    serverInfo.setPort(danmuCollectorInfo.getPort());
                    resultInfo.setServerInfo(serverInfo);
                    return resultInfo;
                }
            }else{
                resultInfo.setCode(400);
                resultInfo.setMessage("没有可用的弹幕服务器!");
                return resultInfo;
            }
        } catch (Exception e) {
            resultInfo.setCode(500);
            resultInfo.setMessage("server error");
            return resultInfo;
        }
        return null;
    }

}
