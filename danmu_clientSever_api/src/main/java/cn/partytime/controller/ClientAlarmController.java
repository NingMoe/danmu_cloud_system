package cn.partytime.controller;

import cn.partytime.model.RestResultModel;
import cn.partytime.rpcService.alarmRpc.DanmuAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dm on 2017/7/18.
 */

@RestController
@RequestMapping(value = "/v1/api/javaClient")
public class ClientAlarmController {

    @Autowired
    private DanmuAlarmService danmuAlarmService;

    @RequestMapping(value = "/alarm/{code}/{type}", method = RequestMethod.GET)
    public RestResultModel danmuNotice(@PathVariable("code") String code,@PathVariable("type") String type){
        RestResultModel restResultModel = new RestResultModel();
        danmuAlarmService.danmuAlarm(type,code);
        restResultModel.setResult(200);
        return restResultModel;
    }

}