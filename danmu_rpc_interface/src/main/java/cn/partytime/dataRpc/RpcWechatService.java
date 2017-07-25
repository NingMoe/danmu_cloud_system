package cn.partytime.dataRpc;

import cn.partytime.dataRpc.impl.RpcWechatServiceHystrix;
import cn.partytime.model.WechatUser;
import cn.partytime.model.WechatUserInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by dm on 2017/7/10.
 */
@FeignClient(value = "${dataRpcServer}",fallback = RpcWechatServiceHystrix.class)
public interface RpcWechatService {

    @RequestMapping(value = "/rpcWechat/findByOpenId" ,method = RequestMethod.GET)
    public WechatUser findByOpenId(@RequestParam(value = "openId") String openId);

    @RequestMapping(value = "/rpcWechat/findByWechatId" ,method = RequestMethod.GET)
    public WechatUserInfo findByWechatId(@RequestParam(value = "wechatId") String wechatId);

}
