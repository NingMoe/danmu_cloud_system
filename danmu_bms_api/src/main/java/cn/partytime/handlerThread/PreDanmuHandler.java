package cn.partytime.handlerThread;

import cn.partytime.common.cachekey.PreDanmuCacheKey;
import cn.partytime.common.util.ListUtils;
import cn.partytime.rpcService.CmdLogicService;
import cn.partytime.model.CmdTempAllData;
import cn.partytime.model.CmdTempComponentData;
import cn.partytime.model.danmu.PreDanmuModel;
import cn.partytime.redis.service.RedisService;
import cn.partytime.service.PreDanmuService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lENOVO on 2016/11/29.
 */

@Component
public class PreDanmuHandler {

    private static final Logger logger = LoggerFactory.getLogger(PreDanmuHandler.class);

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Autowired
    private PreDanmuService preDanmuService;


    @Autowired
    private RedisService redisService;


    @Autowired
    private CmdLogicService cmdLogicService;

    public void danmuListenHandler(String partyId) {
        logger.info("装载活动:{}预制弹幕",partyId);
        try {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    String preDanmuCacheKey = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId;



                    List<PreDanmuModel> preDanmuModelList = preDanmuService.findByPartyId(partyId);
                    if (ListUtils.checkListIsNotNull(preDanmuModelList)) {

                        for(PreDanmuModel preDanmuModel:preDanmuModelList){
                            Map<String,Object> preDanmuMap = new HashMap<String,Object>();
                            String templateId = preDanmuModel.getTemplateId();
                            Map<String,Object> contentMap = preDanmuModel.getContent();
                            CmdTempAllData cmdTempAllData = cmdLogicService.findCmdTempAllDataByIdFromCache(templateId);
                            List<CmdTempComponentData> cmdTempComponentDataList = cmdTempAllData.getCmdTempComponentDataList();
                            if(ListUtils.checkListIsNotNull(cmdTempComponentDataList)){
                                for(CmdTempComponentData cmdTempComponentData:cmdTempComponentDataList){
                                    String key =cmdTempComponentData.getKey();
                                    if(!contentMap.containsKey(key)){
                                        int type = cmdTempComponentData.getType();
                                        if(type==3){
                                            List<Object> list = new ArrayList<Object>();
                                            list.add(cmdTempComponentData.getDefaultValue());

                                            contentMap.put(key,list);
                                        }else{
                                            contentMap.put(key,cmdTempComponentData.getDefaultValue());
                                        }
                                    }
                                }
                                preDanmuMap.put("data",contentMap);
                            }
                            preDanmuMap.put("type",cmdTempAllData.getKey());
                            redisService.setValueToList(preDanmuCacheKey, JSON.toJSONString(preDanmuMap));
                            //preDanmuModelList.forEach(preDanmuModel -> redisService.setValueToList(preDanmuCacheKey, JSON.toJSONString(preDanmuModel)));
                        }
                    }

                    //预制弹幕缓存时间
                    redisService.expire(preDanmuCacheKey, 60 * 60 * 24);


                }
            });
        } catch (Exception e) {
            logger.error("预制弹幕装载线程启动异常:{}", e.getMessage());
        }
    }
}
