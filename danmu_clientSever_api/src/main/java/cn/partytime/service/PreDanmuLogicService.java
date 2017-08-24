package cn.partytime.service;

import cn.partytime.common.cachekey.PreDanmuCacheKey;
import cn.partytime.common.util.ListUtils;
import cn.partytime.dataRpc.RpcCmdService;
import cn.partytime.dataRpc.RpcPreDanmuService;
import cn.partytime.model.CmdTempAllData;
import cn.partytime.model.CmdTempComponentData;
import cn.partytime.model.PreDanmuModel;
import cn.partytime.redis.service.RedisService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dm on 2017/7/12.
 */

@Service
@Slf4j
public class PreDanmuLogicService {


    @Autowired
    private RpcCmdService rpcCmdService;

    @Autowired
    private RpcPreDanmuService rpcPreDanmuService;

    @Autowired
    private RedisService redisService;

    public void danmuListenHandler(String partyId) {
        log.info("加载活动{}的预置弹幕",partyId);
        String preDanmuCacheKey = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId;

        String libraryId = rpcPreDanmuService.findDanmuLibraryIdByParty(partyId);
        if(StringUtils.isEmpty(libraryId)){
            log.info("未找到弹幕库");
            return;
        }
        long count = rpcPreDanmuService.findPreDanmuCountByLibraryId(libraryId);
        long index = 0;
        int pageSize = 500;
        if (count % pageSize > 0) {
            index = count / pageSize + 1;
        } else {
            index = count / pageSize;
        }
        List<PreDanmuModel> preDanmuModelList = new ArrayList<PreDanmuModel>();
        for(int i=0; i<index; i++){
            List<PreDanmuModel> preDanmuModelTempList = rpcPreDanmuService.findPreDanmuByLibraryId(libraryId,i,pageSize);
            preDanmuModelList.addAll(preDanmuModelTempList);
        }

        if (ListUtils.checkListIsNotNull(preDanmuModelList)) {
            log.info("获取预置弹幕的数量:{}",preDanmuModelList.size());
            for(PreDanmuModel preDanmuModel:preDanmuModelList){
                Map<String,Object> preDanmuMap = new HashMap<String,Object>();
                String templateId = preDanmuModel.getTemplateId();
                Map<String,Object> contentMap = preDanmuModel.getContent();
                CmdTempAllData cmdTempAllData = rpcCmdService.findCmdTempAllDataByIdFromCache(templateId);
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
        }else{
            log.info("获取预置弹幕的数量:{}",0);
        }





        /*List<PreDanmuModel> preDanmuModelList = rpcPreDanmuService.findByPartyId(partyId);
        if (ListUtils.checkListIsNotNull(preDanmuModelList)) {
            log.info("获取预置弹幕的数量:{}",preDanmuModelList.size());
            for(PreDanmuModel preDanmuModel:preDanmuModelList){
                Map<String,Object> preDanmuMap = new HashMap<String,Object>();
                String templateId = preDanmuModel.getTemplateId();
                Map<String,Object> contentMap = preDanmuModel.getContent();
                CmdTempAllData cmdTempAllData = rpcCmdService.findCmdTempAllDataByIdFromCache(templateId);
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
        }else{
            log.info("获取预置弹幕的数量:{}",0);
        }*/
        //预制弹幕缓存时间
        redisService.expire(preDanmuCacheKey, 60 * 60 * 24);
    }
}
