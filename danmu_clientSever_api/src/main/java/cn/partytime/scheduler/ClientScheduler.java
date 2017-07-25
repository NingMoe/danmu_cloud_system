package cn.partytime.scheduler;


import cn.partytime.alarmRpc.RpcMovieAlarmService;
import cn.partytime.alarmRpc.RpcProjectorAlarmService;
import cn.partytime.common.util.DateUtils;
import cn.partytime.common.util.ListUtils;
import cn.partytime.dataRpc.*;
import cn.partytime.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
@RefreshScope
public class ClientScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ClientScheduler.class);

    @Autowired
    private RpcProjectorAlarmService rpcProjectorAlarmService;

    @Autowired
    private RpcProjectorService rpcProjectorService;

    @Autowired
    private RpcDanmuClientService rpcDanmuClientService;

    @Autowired
    private RpcDanmuAddressService rpcDanmuAddressService;

    @Autowired
    private RpcPartyService partyService;

    @Autowired
    private RpcMovieScheduleService rpcMovieScheduleService;

    @Autowired
    private RpcMovieAlarmService rpcMovieAlarmService;

    @Scheduled(cron = "0 0/5 * * * ?")
    private void moviePlayTimeListener(){
        logger.info("movie time is too long listener");
        List<DanmuAddress> danmuAddressList = rpcDanmuAddressService.findByType(0);
        if(ListUtils.checkListIsNotNull(danmuAddressList)){
            for(DanmuAddress danmuAddress:danmuAddressList){
                String addressId = danmuAddress.getId();
                PartyLogicModel partyLogicModel  = partyService.findPartyAddressId(addressId);
                if(partyLogicModel!=null){
                    String partyId = partyLogicModel.getPartyId();
                    List<MovieSchedule>  movieScheduleList =  rpcMovieScheduleService.findByPartyIdAndAddressId(partyId,addressId);
                    if(ListUtils.checkListIsNotNull(movieScheduleList)){
                        MovieSchedule movieSchedule = movieScheduleList.get(0);
                        if(movieSchedule.getEndTime()==null){
                            Date currentDate = DateUtils.getCurrentDate();
                            Date danmuStartTime = movieSchedule.getStartTime();
                            Date movieStartTime = movieSchedule.getMoviceStartTime();
                            if(movieStartTime!=null){
                                long time = currentDate.getTime() - movieStartTime.getTime();
                                rpcMovieAlarmService.movieTime(partyId,addressId,time);
                            }
                        }
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 5 2 * * ?")
    private void projectorCloseCommandListener(){

        List<DanmuAddress> danmuAddressList = rpcDanmuAddressService.findByType(0);
        if(ListUtils.checkListIsNotNull(danmuAddressList)){
            for(DanmuAddress danmuAddress:danmuAddressList){
                List<DanmuClient>  danmuClientList = rpcDanmuClientService.findByAddressId(danmuAddress.getId());
                if(ListUtils.checkListIsNotNull(danmuClientList)){
                    for(DanmuClient danmuClient:danmuClientList){
                        String regsitrorCode = danmuClient.getRegistCode();
                        PageResultModel<ProjectorAction> projectorActions =  rpcProjectorService.findProjectorActionPage(regsitrorCode,0,1);
                        List<ProjectorAction> projectorActionList = projectorActions.getRows();
                        if(projectorActionList!=null){
                            ProjectorAction projectorAction = projectorActionList.get(0);
                            if(projectorAction.getEndTime()==null){
                                rpcProjectorAlarmService.projectorClose(regsitrorCode);
                            }
                        }
                    }
                }
            }
        }

    }

}
