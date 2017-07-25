package cn.partytime.message.rpc;

import cn.partytime.common.constants.LogCodeConst;
import cn.partytime.dataRpc.RpcDanmuClientService;
import cn.partytime.message.bean.MessageObject;
import cn.partytime.message.messageHandlerService.BulbLifeAlarmService;
import cn.partytime.message.proxy.MessageHandlerService;
import cn.partytime.model.DanmuClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dm on 2017/7/19.
 */


@RestController
@RequestMapping("/rpcBulb")
public class RpcBulbAlarmService {

    private static final Logger logger = LoggerFactory.getLogger(RpcBulbAlarmService.class);
    @Autowired
    private BulbLifeAlarmService bulbLifeAlarmService;

    @Autowired
    private RpcDanmuClientService rpcDanmuClientService;

    @Autowired
    private MessageHandlerService messageHandlerService;



    @RequestMapping(value = "/blubLife" ,method = RequestMethod.GET)
    public void partyStart(@RequestParam String registerCode) {
        DanmuClient danmuClient = rpcDanmuClientService.findByRegistCode(registerCode);

        Map<String,Object> map = new HashMap<>();
        MessageObject<Map<String,Object>> mapMessageObject = new MessageObject<Map<String,Object>>(LogCodeConst.CLientLogCode.BULB_LIFE_TIME,map);
        messageHandlerService.messageHandler(bulbLifeAlarmService,mapMessageObject);
    }
}
