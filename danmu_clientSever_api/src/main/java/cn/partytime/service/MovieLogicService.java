package cn.partytime.service;

import cn.partytime.common.util.DateUtils;
import cn.partytime.common.util.ListUtils;
import cn.partytime.model.*;
import cn.partytime.rpcService.alarmRpc.ProjectorAlarmService;
import cn.partytime.rpcService.dataRpc.DanmuClientService;
import cn.partytime.rpcService.dataRpc.MovieService;
import cn.partytime.rpcService.dataRpc.PartyService;
import cn.partytime.rpcService.dataRpc.ProjectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by dm on 2017/7/19.
 */

@Service
public class MovieLogicService {


    private static final Logger logger = LoggerFactory.getLogger(MovieLogicService.class);

    @Autowired
    private PartyService partyService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ProjectorService projectorService;


    @Autowired
    private DanmuClientService danmuClientService;

    @Autowired
    private ProjectorAlarmService projectorAlarmService;



    public RestResultModel partyStart(String registCode,String command,long clientTime) {
        Party party = partyService.findByMovieAliasOnLine(command);
        logger.info("弹幕开始请求：指令编号：{},registCode:{}", command, registCode);
        RestResultModel restResultModel = new RestResultModel();
        DanmuClient danmuClient = danmuClientService.findByRegistCode(registCode);
        restResultModel = checkClientExist(danmuClient,registCode);
        if(restResultModel!=null){
            return restResultModel;
        }
        restResultModel = checkPartyIsOk(party);
        if(restResultModel!=null){
            return restResultModel;
        }
        String addressId = danmuClient.getAddressId();
        restResultModel  = movieService.partyStart(party.getId(),addressId,clientTime);
        firstDanmuStartCommandHandler(registCode);

        return restResultModel;
    }

    public void firstDanmuStartCommandHandler(String registCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DanmuClient danmuClient =  danmuClientService.findByRegistCode(registCode);
                String addressId = danmuClient.getAddressId();
                List<DanmuClient> danmuClientList = danmuClientService.findByAddressId(addressId);
                if(ListUtils.checkListIsNotNull(danmuClientList)){
                    for(DanmuClient tempDanmuClient:danmuClientList){
                        PageResultModel<ProjectorAction> projectorActions =  projectorService.findProjectorActionPage(registCode,0,1);
                        List<ProjectorAction> projectorActionList =projectorActions.getRows();
                        if(ListUtils.checkListIsNotNull(projectorActionList)){
                            for(ProjectorAction projectorAction :projectorActionList){
                                if(projectorAction.getEndTime()!=null){
                                    projectorAlarmService.projectorOpen(registCode);
                                }
                                if(projectorAction.getCreateTime().before(DateUtils.getCurrentDate())){
                                    projectorAlarmService.projectorOpen(registCode);
                                }
                            }
                        }
                    }
                }


            }
        }).start();
    }



    public RestResultModel moviceStart(String partyId,String registCode,long clientTime) {
        logger.info("电影开始请求：活动编号：{},registCode:{}", partyId, registCode);
        RestResultModel restResultModel = new RestResultModel();
        DanmuClient danmuClient = danmuClientService.findByRegistCode(registCode);
        restResultModel = checkClientExist(danmuClient,registCode);
        if(restResultModel!=null){
            return restResultModel;
        }
        Party party = partyService.getPartyByPartyId(partyId);
        restResultModel = checkPartyIsOk(party);
        if(restResultModel!=null){
            return restResultModel;
        }
        String addressId = danmuClient.getAddressId();
        return movieService.movieStart(party.getId(),addressId,clientTime);
    }

    /**
     * 电影结束
     * @param registCode
     * @return
     */
    @RequestMapping(value = "/moviceStop" ,method = RequestMethod.GET)
    public RestResultModel moviceStop(String partyId, String registCode,long clientTime) {
        logger.info("电影结束请求：活动编号：{},registCode:{}", partyId, registCode);
        RestResultModel restResultModel = new RestResultModel();

        DanmuClient danmuClient = danmuClientService.findByRegistCode(registCode);
        restResultModel = checkClientExist(danmuClient,registCode);
        if(restResultModel!=null){
            return restResultModel;
        }
        Party party = partyService.getPartyByPartyId(partyId);
        restResultModel = checkPartyIsOk(party);
        if(restResultModel!=null){
            return restResultModel;
        }
        restResultModel = new RestResultModel();
        String addressId = danmuClient.getAddressId();
        return movieService.movieStop(party.getId(),addressId,clientTime);
    }





    private RestResultModel checkClientExist(DanmuClient danmuClient,String registCode){
        if (danmuClient == null) {
            RestResultModel restResultModel = new RestResultModel();
            logger.info("注册码:{}错误", registCode);
            restResultModel.setResult(404);
            restResultModel.setResult_msg("客户端不存在!");
            return restResultModel;
        }
        return null;
    }

    private RestResultModel checkPartyIsOk(Party party){
        RestResultModel restResultModel = new RestResultModel();
        if (party == null) {
            logger.info("电影不存在");
            restResultModel.setResult(404);
            restResultModel.setResult_msg("活动不存在");
            return restResultModel;
        }
        if (party.getType() == 0) {
            logger.info("不是电影场");
            restResultModel.setResult(405);
            restResultModel.setResult_msg("此活动不是电影场");
            return restResultModel;
        }

        if (party.getStatus() == 4) {
            logger.info("电影已经下线");
            restResultModel.setResult(406);
            restResultModel.setResult_msg("活动已经下线");
            return restResultModel;
        }

        return null;
    }



}
