package cn.partytime.rpcService.alarmRpc;

import cn.partytime.common.util.ServerConst;
import cn.partytime.rpcService.alarmRpc.impl.DanmuAlarmServiceHystrix;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by dm on 2017/7/20.
 */

@FeignClient(value = ServerConst.SERVER_NAME_DANMUMESSAGENOTICE,fallback = DanmuAlarmServiceHystrix.class)
public interface DanmuAlarmService {

    @RequestMapping(value = "/rpcDanmu/danmuAlarm" ,method = RequestMethod.GET)
    public void danmuAlarm(@RequestParam(value = "type") String type, @RequestParam(value = "code") String code);
}