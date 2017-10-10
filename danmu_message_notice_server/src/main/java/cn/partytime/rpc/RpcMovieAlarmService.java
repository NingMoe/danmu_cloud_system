package cn.partytime.rpc;

import cn.partytime.cache.alarm.AlarmCacheService;
import cn.partytime.common.constants.AlarmKeyConst;
import cn.partytime.common.constants.LogCodeConst;
import cn.partytime.dataRpc.RpcPartyService;
import cn.partytime.logicService.CommonDataService;
import cn.partytime.message.bean.MessageObject;
import cn.partytime.message.proxy.MessageHandlerService;
import cn.partytime.model.PartyModel;
import cn.partytime.service.DanmuAlarmService;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/rpcMovie")
@Slf4j
public class RpcMovieAlarmService {
    @Autowired
    private DanmuAlarmService danmuAlarmService;

    @Autowired
    private MessageHandlerService messageHandlerService;

    @Autowired
    private RpcPartyService rpcPartyService;

    @Autowired
    private CommonDataService commonDataService;

    @Autowired
    private AlarmCacheService alarmCacheService;


    @RequestMapping(value = "/movieStartError" ,method = RequestMethod.GET)
    public void movieStartError(@RequestParam String partyId,@RequestParam String addressId, @RequestParam long time) {
        log.info("==================================movieStartError");
        PartyModel party = rpcPartyService.getPartyByPartyId(partyId);
        Map<String,String> map = new HashMap<String,String>();
        if(time==0){
            log.info("当time为0的时候，电影没有正常开始");
            map = commonDataService.setMapByAddressId(AlarmKeyConst.ALARM_MOVIESTARTERROR,addressId,partyId);
            sendMessage(LogCodeConst.PartyLogCode.MOVIE_START_ERROR,map,AlarmKeyConst.ALARM_MOVIESTARTERROR);
        }
    }

    @RequestMapping(value = "/movieTime" ,method = RequestMethod.GET)
    public void movieTime(@RequestParam String partyId,@RequestParam String addressId, @RequestParam long time) {

        PartyModel party = rpcPartyService.getPartyByPartyId(partyId);

        MessageObject<Map<String,String>> mapMessageObject = null;
        Map<String,String> map = new HashMap<String,String>();
        if(party!=null && party.getMovieTime()!=0){
            long movieTime = party.getMovieTime();

            long timeMinute = time/1000/60;
            long movieMinute = party.getMovieTime()/1000/60;
            log.info("time:{},moviTime:{}",timeMinute,movieMinute);
            if(timeMinute < movieMinute){
                //触发事件过短
                map = commonDataService.setMapByAddressId(AlarmKeyConst.ALARM_KEY_MOVIESHORT,addressId,partyId);
                //mapMessageObject = new MessageObject<Map<String,String>>(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_SHORT,map);
                sendMessage(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_SHORT,map,AlarmKeyConst.ALARM_KEY_MOVIESHORT);
            }else if(timeMinute > movieMinute) {
                //触发时间过
                map = commonDataService.setMapByAddressId(AlarmKeyConst.ALARM_KEY_MOVIEOVERTIME,addressId,partyId);
                //mapMessageObject = new MessageObject<Map<String,String>>(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_SHORT,map);
                sendMessage(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_LONG,map,AlarmKeyConst.ALARM_KEY_MOVIEOVERTIME);
            }else{
                return;
            }
        }else {
            long minute = time / 1000 / 60;
            log.info("minute:{}", minute);
            if (minute < 90) {
                //触发事件过短
                map = commonDataService.setMapByAddressId(AlarmKeyConst.ALARM_KEY_MOVIESHORT, addressId, partyId);
                sendMessage(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_SHORT,map,AlarmKeyConst.ALARM_KEY_MOVIESHORT);
            } else if (minute > 180) {
                //触发时间过
                map = commonDataService.setMapByAddressId(AlarmKeyConst.ALARM_KEY_MOVIEOVERTIME, addressId, partyId);
                sendMessage(LogCodeConst.PartyLogCode.MOVIE_TIME_TOO_LONG,map,AlarmKeyConst.ALARM_KEY_MOVIEOVERTIME);

            }
        }
    }

    private void sendMessage(String type,Map<String,String> map,String typeName){
        if(map!=null){
            String addressId = map.get("addressId");
            String partyId = map.get("partyId");
            int cacheCount = alarmCacheService.findAlarmCount(addressId,typeName);
            if(cacheCount>0){
                log.info("{}:告警发出的次数超过上限",typeName);
                return;
            }
            alarmCacheService.addAlarmCount(0,addressId,typeName);
            MessageObject<Map<String,String>>  mapMessageObject = new MessageObject<Map<String,String>>(type,map);
            mapMessageObject.setValue(0);
            mapMessageObject.setThreshold(0);
            messageHandlerService.messageHandler(danmuAlarmService,mapMessageObject);
        }
    }
}
