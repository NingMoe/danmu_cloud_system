package cn.partytime.check.service;

import cn.partytime.check.config.DanmuChannelRepository;
import cn.partytime.check.dataService.*;
import cn.partytime.check.handlerThread.PreDanmuHandler;
import cn.partytime.check.handlerThread.TestDanmuHandler;
import cn.partytime.check.model.*;
import cn.partytime.check.model.command.*;
import cn.partytime.check.util.CommandTypeConst;
import cn.partytime.common.cachekey.*;
import cn.partytime.common.constants.PotocolComTypeConst;
import cn.partytime.common.constants.ProtocolConst;
import cn.partytime.common.util.DateUtils;
import cn.partytime.common.util.IntegerUtils;
import cn.partytime.common.util.ListUtils;
import cn.partytime.redis.service.RedisService;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by lENOVO on 2016/8/30.
 */

@Service
public class CommandHanderService {

    private static final Logger logger = LoggerFactory.getLogger(CommandHanderService.class);
    @Autowired
    private DanmuService danmuService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PartyService partyService;

    @Autowired
    private DanmuChannelRepository danmuChannelRepository;

    @Autowired
    private FunctionService functionService;


    @Autowired
    private CacheDataService cacheDataService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ManagerCachService managerCachService;

    @Autowired
    private TestDanmuHandler testDanmuHandler;

    @Autowired
    private PartyLogicService partyLogicService;

    @Autowired
    private PreDanmuHandler preDanmuHandler;

    @Autowired
    private DanmuLibraryPartyService danmuLibraryPartyService;

    @Autowired
    private DanmuLogService danmuLogService;


    @Autowired
    private CmdLogicService cmdLogicService;

    @Autowired
    private AdminUserService adminUserService;


    public void commandHandler(CommandModel commandModel, Channel channel) {
        //类型
        String type = commandModel.getType();
        //秘钥
        String key = commandModel.getKey();
        String partyId = commandModel.getPartyId();
        String addressId = commandModel.getAddressId();

        if (CommandTypeConst.PARTY_STATUS.equals(type)) {
            //设置电影开始
            partyStauts(type, partyId, addressId, commandModel.getPartyCtrl(), channel);
        } else if (CommandTypeConst.INIT.equals(type)) {
            //初始化
            initHandler(type, partyId, addressId, channel,key);
        } else if (CommandTypeConst.PRE_DANMU.equals(type)) {
            //预制弹幕处理
            preDanmuHandler(type, partyId, addressId, commandModel.getPreDanmu(), channel);
        } else if (CommandTypeConst.BLOCK_DANMU.equals(type)) {
            //屏蔽弹幕处理
            blockDanmuHandler(commandModel.getBlockDanmu(),channel);
        } else if (CommandTypeConst.NORMAL_DANMU.equals(type)) {
            //普通弹幕处理
            normalOrtestDanmuDanmuHandler(type, partyId, addressId, commandModel.getDanmu(),channel);
        } else if (CommandTypeConst.TEST_DANMU.equals(type)) {
            //测试弹幕处理
            normalOrtestDanmuDanmuHandler(type, partyId, addressId, commandModel.getDanmu(),channel);
        } else if (CommandTypeConst.SPECIAL_MOV.equals(type)) {
            //动画特效
            specialMovHandler(type, partyId, addressId, commandModel.getVideo(), channel);
        }else if (CommandTypeConst.DELAY_SECOND.equals(type)) {
            //是否开启延迟时间
            delayTimeHandler(partyId, commandModel.getDelayTime(), channel);
        } else if (CommandTypeConst.TEST_MODEL.equals(type)) {
            //是否开启测试模式
            testModelHander(partyId, addressId, commandModel.getTestDanmu(), channel);
        } else if (CommandTypeConst.FIND_CLIENTLIST.equals(type)) {
            //查找客户端数量
            findClientList(type, addressId, channel);
        } else if (CommandTypeConst.DANMU_DENSITY.equals(type)) {
            //设置弹幕密度
            setDanmuDensity(type, addressId, partyId, commandModel.getDensity(), channel);
        }
    }



    /**
     * 设置弹幕密度
     *
     * @param channel
     */
    public void setDanmuDensity(String type, String addressId, String partyId, DanmuDensityComModel danmuDensityComModel, Channel channel) {

        int danmuDensity = danmuDensityComModel.getDanmuDensity();

        //将弹幕密度存入缓存
        String key = FunctionControlCacheKey.FUNCITON_CONTROL_DANMU_DENSITY + partyId;
        redisService.set(key, danmuDensity);
        redisService.expire(key,60*60*24*7);

        //如果弹幕密度这是为0 不处理
        if (danmuDensity == 0) {
            return;
        }
        String msg = JSON.toJSONString(setObjectToBms(type, danmuDensity));
        logger.info("反馈给管理员的消息,{}", msg);
        pushCommandToPartyAdmin(partyId, type, msg);
    }


    /**
     * 获取客户端列表
     *
     * @param channel
     */
    public void findClientList(String type, String addressId, Channel channel) {
        logger.info("获取在线客户端");
        try {
            logger.info("获取地址{}客户端信息", addressId);
            List<DanmuClientModel> danmuClientModelList = cacheDataService.findDanmuClientList(addressId);
            logger.info("获取的客户端列表信息：{}", danmuClientModelList);
            if (ListUtils.checkListIsNotNull(danmuClientModelList)) {
                String msg = JSON.toJSONString(setObjectToBms(type, danmuClientModelList));
                logger.info("反馈给管理员的消息,{}", msg);
                sendMessageToBMS(channel, msg);
            }
        } catch (Exception e) {
            logger.error("获取在线客户端异常,{}", e.getMessage());
        }
    }

    /**
     * 测试模式的开启
     */
    private void testModelHander(String partyId, String addressId, TestDanmuComModel testDanmuComModel, Channel channel) {
        logger.info("测试模式操作");
        try {
            //测试模式的开启状
            boolean status = testDanmuComModel.getStatus();
            logger.info("活动:{},测试模式开启状态:{}", partyId, status);
            String key = FunctionControlCacheKey.FUNCITON_CONTROL_TESTMODEL + partyId;
            if (status) {
                redisService.set(key, status);
                logger.info("开启测试模式线程");
                restartTestThread(partyId, addressId, channel, true);
            } else {
                logger.info("关闭测试模式");
                redisService.set(key, status);
                redisService.expire(key, 0);
            }

            String msg = JSON.toJSONString(setObjectToBms(CommandTypeConst.TEST_MODEL, status));
            logger.info("反馈给管理员的消息:" + msg);
            pushCommandToPartyAdmin(partyId, CommandTypeConst.TEST_MODEL, msg);
        } catch (Exception e) {
            logger.info("测试模式操作异常,{}", e.getMessage());
        }

    }
    /**
     * 延迟时间的开启处理
     */
    private void delayTimeHandler(String partyId, DelayTimeComModel delayTimeComModel, Channel channel) {
        logger.info("延迟时间操作");
        try {
            boolean status = delayTimeComModel.getStatus();
            logger.info("活动:{},延迟时间操作状态:{}", partyId, status);
            String key = FunctionControlCacheKey.FUNCITON_CONTROL_DELAYSECOND + partyId;
            if (status) {
                redisService.incrKey(key, 1);
                redisService.expire(key,60*60*24*7);
            } else {
                Object object = redisService.get(key);
                if (object != null && IntegerUtils.objectConvertToInt(object) < 1) {
                    redisService.set(key, 0);
                    redisService.expire(key,60*60*24*7);
                } else {
                    redisService.decrKey(key, -1);
                    redisService.expire(key,60*60*24*7);
                }
            }
            String msg = JSON.toJSONString(setObjectToBms(CommandTypeConst.DELAY_SECOND, redisService.get(key)));
            logger.info("反馈给管理员的消息:" + msg);
            pushCommandToPartyAdmin(partyId, CommandTypeConst.DELAY_SECOND, msg);

        } catch (Exception e) {
            logger.info("延迟时间操作异常,{}", e.getMessage());
        }
    }







    /**
     * 动画特效处理
     */
    private void specialMovHandler(String type, String partyId, String addressId, VideoComModel videoComModel, Channel channel) {
        Map<String, Object> result = new HashMap<String, Object>();

        String id = videoComModel.getId();
        int status = videoComModel.getStatus();

        //清除动画特效缓存
        String key = FunctionControlCacheKey.FUNCITON_CONTROL_SPECIALMOV + partyId;
        Object object = redisService.get(key);
        if (object == null) {
            redisService.set(key, id);
            redisService.expire(key,60*60*24*7);

            //消息推送给管理端
            result.put("id", id);
            result.put("status", status);

            //广播命令给所有管理员
            pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, result)));



            Map<String,Object> dataMap = new HashMap<String,Object>();
            //消息推送给管理端
            dataMap.put("idd", id);
            dataMap.put("status", status);


            Map<String,Object>commandObject = new HashMap<String,Object>();
            commandObject.put("type","vedio");
            commandObject.put("data",dataMap);

            logger.info("发动给客户端的信息:" + JSON.toJSONString(commandObject));
            sendMessageToMq(addressId, commandObject);


        } else {
            String value = String.valueOf(object);
            if (value.equals(id)) {
                if (status == 1) {
                    redisService.expire(key, 0);
                } else {
                    redisService.set(key, id);
                    redisService.expire(key,60*60*24*7);
                }
                //消息推送给管理端
                result.put("id", id);
                result.put("status", status);
                //广播命令给所有管理员
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, result)));



                Map<String,Object> dataMap = new HashMap<String,Object>();
                //消息推送给管理端
                dataMap.put("idd", id);
                dataMap.put("status", status);


                Map<String,Object>commandObject = new HashMap<String,Object>();
                commandObject.put("type","vedio");
                commandObject.put("data",dataMap);

                logger.info("发动给客户端的信息:" + JSON.toJSONString(commandObject));
                sendMessageToMq(addressId, commandObject);


            } else {
                //开启新的动画特效
                redisService.set(key, id);
                redisService.expire(key,60*60*24*7);


                //消息推送给管理端
                result.put("id", id);
                result.put("status", status);
                //广播命令给所有管理员
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, result)));



                Map<String,Object> dataMap = new HashMap<String,Object>();
                //消息推送给管理端
                dataMap.put("idd", id);
                dataMap.put("status", status);

                Map<String,Object>commandObject = new HashMap<String,Object>();
                commandObject.put("type","vedio");
                commandObject.put("data",dataMap);

                logger.info("发动给客户端的信息:" + JSON.toJSONString(commandObject));
                sendMessageToMq(addressId, commandObject);
            }
        }
    }

    /**
     * 重启测试弹幕
     *
     * @param channel
     * @param newFlg
     */
    private void restartTestThread(String partyId, String addressId, Channel channel, boolean newFlg) {
        //int count = managerCachService.testDanmuCount(addressId, partyId);
        if (newFlg) {
            managerCachService.resetTestDanmuCount(addressId, partyId);
            testDanmuHandler.danmuListenHandler(addressId, partyId);
        }
    }

    /**
     * 普通弹幕处理
     */
    public void normalOrtestDanmuDanmuHandler(String type, String partyId, String addressId, DanmuComModel danmuComModel, Channel channel) {
        logger.info("向客户端广播弹幕");
        try {
            String color = danmuComModel.getColor();
            String msg = danmuComModel.getMessage();
            String openId = danmuComModel.getOpenId();
            logger.info("向客户端广播弹幕，活动:{}，地址:{},消息:{},颜色:{},openId:{}", partyId, addressId, msg, color, openId);

            //获取管理员通道
            AdminTaskModel adminTaskModel = danmuChannelRepository.get(channel);

            if(CommandTypeConst.NORMAL_DANMU.equals(type)){

                DanmuLog danmuLog = danmuLogService.findDanmuLogById(danmuComModel.getId());
                CmdTempAllData cmdTempAllData = cmdLogicService.findCmdTempAllDataByIdFromCache(danmuLog.getTemplateId());

                //更新弹幕状态
                AdminUser adminUser =  adminUserService.getAdminUser(adminTaskModel.getAuthKey());

                //更新日志信息
                danmuLog.setCheckUserId(adminUser.getId());
                danmuLog.setViewFlg(true);
                danmuLog.setUpdateTime(DateUtils.getCurrentDate());
                danmuLogService.save(danmuLog);

                logger.info("管理员:{},状态更新",adminUser.getId());
                String danmuId = danmuLog.getDanmuId();
                if(!StringUtils.isEmpty(danmuId)){
                    DanmuModel danmuModel = danmuService.findById(danmuId);
                    danmuModel.setCheckUserId(adminUser.getId());
                    danmuModel.setViewFlg(true);
                    danmuModel.setUpdateTime(DateUtils.getCurrentDate());
                    danmuService.save(danmuModel);
                }


                Map<String,Object> commandObject = new HashMap<String,Object>();
                commandObject.put("type",cmdTempAllData.getKey());

                commandObject.put("data",danmuLog.getContent());
                commandObject.put("isSendH5",cmdTempAllData.getIsSendH5());

                //ProtocolModel protocolModel = ProtocolUtil.setDanmuProtocolModel(cmdTemp.getKey(),danmuLog.getContent());
                pubDanmuToUserCachList(partyId, addressId, commandObject);
            }else{

                DanmuModel danmuLog = danmuService.findById(danmuComModel.getId());
                CmdTempAllData cmdTempAllData = cmdLogicService.findCmdTempAllDataByIdFromCache(danmuLog.getTemplateId());

                Map<String,Object> commandObject = new HashMap<String,Object>();
                commandObject.put("type",cmdTempAllData.getKey());

                commandObject.put("data",danmuLog.getContent());

                commandObject.put("isSendH5",cmdTempAllData.getIsSendH5());
                pubDanmuToUserCachList(partyId, addressId, commandObject);
            }
        } catch (Exception e) {
            logger.error("向客户端广播弹幕异常:" + e.getMessage());
        }
    }

    public void pubDanmuToUserCachList(String partyId, String addressId, Map<String,Object> commandMap) {
        try {
            logger.info("向活动场地广播弹幕");

            List<String> addressIdList = partyLogicService.findAddressIdListByPartyId(partyId);
            if(ListUtils.checkListIsNotNull(addressIdList)){
                for(String address:addressIdList){
                    sendMessageToMq(addressId, commandMap);
                }
            }
            /*List<PartyAddressRelation> partyAddressRelationList = partyAddressRelationService.findByPartyId(partyId);
            logger.info("获取当前活动下所有的地址关系:{}", JSON.toJSONString(partyAddressRelationList));

            if (ListUtils.checkListIsNotNull(partyAddressRelationList)) {
                //查询地址缓存
                for (PartyAddressRelation partyAddressRelation : partyAddressRelationList) {
                    sendMessageToMq(partyAddressRelation.getAddressId(), commandMap);
                }
            }*/
            //向屏幕队列广播弹幕
            /*if (ClientConst.CLIENT_TYPE_MOBILE.equals(protocolModel.getClientType())) {
                if (ListUtils.checkListIsNotNull(partyAddressRelationList)) {
                    //查询地址缓存
                    for (PartyAddressRelation partyAddressRelation : partyAddressRelationList) {
                        sendMessageToMq(partyAddressRelation.getAddressId(), protocolModel);
                    }
                }
            } else {
                sendMessageToMq(addressId, protocolModel);
            }*/
        } catch (Exception e) {
            logger.info("向活动下所有的屏幕广播弹幕异常:{}", e.getMessage());
        }
    }

    /**
     * 屏蔽弹幕处理
     */
    public void blockDanmuHandler(BlockDanmuComModel blockDanmuComModel,Channel channel) {
        logger.info("屏幕弹幕操作");
        try {

            AdminTaskModel adminTaskModel = danmuChannelRepository.get(channel);
            AdminUser adminUser =  adminUserService.getAdminUser(adminTaskModel.getAuthKey());
            DanmuLog danmuLog =danmuLogService.findDanmuLogById(blockDanmuComModel.getId());
            danmuLog.setCheckUserId(adminUser.getId());
            danmuLog.setBlocked(true);
            danmuLog.setViewFlg(true);
            danmuLog.setUpdateTime(DateUtils.getCurrentDate());
            danmuLogService.save(danmuLog);

            String danmuId = danmuLog.getDanmuId();
            if(!StringUtils.isEmpty(danmuId)){
                DanmuModel danmuModel = danmuService.findById(danmuId);
                danmuLog.setCheckUserId(adminUser.getId());
                danmuLog.setBlocked(true);
                danmuLog.setViewFlg(true);
                danmuLog.setUpdateTime(DateUtils.getCurrentDate());
                danmuService.save(danmuModel);
            }

        } catch (Exception e) {
            logger.info("屏幕弹幕操作异常:{}", e.getMessage());
        }

    }


    /**
     * 预制弹幕处理
     *
     * @return
     */
    private void preDanmuHandler(String type, String partyId, String addressId, PreDanmuComModel preDanmuComModel, Channel channel) {
        logger.info("预制弹幕处理操作");
        try {
            //活动编号
            Map<String, Object> result = new HashMap<String, Object>();
            boolean status = preDanmuComModel.getStatus();
            logger.info("预制弹幕处理操作,接收参数,活动:{},状态:{},类型:{}", partyId, status, type);
            String key = FunctionControlCacheKey.FUNCITON_CONTROL_PREDANMU + partyId;
            String preDanmuCacheKey = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId;
            if (status) {

                DanmuLibraryParty danmuLibraryParty = danmuLibraryPartyService.findByPartyId(partyId);
                if (danmuLibraryParty == null) {
                    result.put("message", "活动没有关联弹幕库");
                    sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms("error", result)));
                    return;
                }

                logger.info("开启预制弹幕");
                redisService.set(key, status);
                redisService.expire(key,60*60*24*7);



                //开启预制弹幕处理线程
                preDanmuHandler.danmuListenHandler(partyId);
            } else {
                //关闭预制弹幕
                redisService.expire(key, 0);
                //清除缓存中的预置弹幕
                redisService.expire(preDanmuCacheKey, 0);
            }

            result.put("type", type);
            result.put("status", status);
            //活动已将开始
            //sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms(type, result)));

            //命令广播给活动下的所有管理员
            pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, result)));

        } catch (Exception e) {
            logger.error("预制弹幕处理异常:{}", e.getMessage());
        }
    }

    /**
     * 电影开播
     *
     * @param type
     * @param partyId
     * @param addressId
     * @param partyComModel
     * @param channel
     */
    private void partyStauts(String type, String partyId, String addressId, PartyComModel partyComModel, Channel channel) {
        Map<String, Object> map = new HashMap<String, Object>();
        DanmuResult danmuResult = new DanmuResult();
        danmuResult.setType(type);
        Date now = DateUtils.getCurrentDate();
        Party party = partyService.findById(partyId);
        //活动状态
        int status = partyComModel.getStatus();
        map.put("status", status);
        //活动结束
        if (status == 3) {
            if (party.getStatus() == 3) {
                forceLogout(channel);
                return;
            }
            logger.info("活动:{},结束",party.getId());

            //告诉客户端当前的电影状态
            logger.info("通知客户端，当前活动已经结束");
            Map<String,Object> commandObject = new HashMap<String,Object>();
            commandObject.put("type", ProtocolConst.PROTOCOL_COMMAND);
            commandObject.put("data",getPartyCommandMap(party,status));
            logger.info("下发消息给客户端:" + JSON.toJSONString(commandObject));
            sendPartyStatusCommandToMq(addressId, commandObject);

            //清除活动缓存
            clearPartyCache(partyId, addressId);
            //设置活动的结束时间
            party.setEndTime(now);
            party.setUpdateTime(now);
            party.setStatus(status);
            partyService.updateParty(party);
            pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));

        } else if (status == 2) {
            //如果电影开始过
            if (party.getStatus() == 2) {
                map.put("status", party.getStatus());
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));
                return;
            }
            //判断当前场地是否有其他活动正在进行
            Party progressParty = partyLogicService.getPartyId(addressId);
            // 活动存在，并且与partyId 不一致，其他活动正在进行
            if (progressParty != null && !progressParty.getId().equals(partyId)) {
                //当前本场地有活动正在进行
                map.put("status", 4);
                map.put("partyName", progressParty.getName());
                sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms(type, map)));
                return;
            }

            //告诉客户端当前的电影状态
            logger.info("通知客户端，电影开始");
            Map<String,Object> commandObject = new HashMap<String,Object>();
            commandObject.put("type", ProtocolConst.PROTOCOL_COMMAND);
            commandObject.put("data",getPartyCommandMap(party,status));

            logger.info("下发消息给客户端:" + JSON.toJSONString(commandObject));
            sendPartyStatusCommandToMq(addressId, commandObject);

            //设置活动的结束时间
            party.setUpdateTime(now);
            party.setActivityStartTime(now);
            party.setStatus(status);
            partyService.updateParty(party);
            pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));

        } else if (status == 1) {

            //如果活动开始过，忽略本次请求
            if (party.getStatus() == 1) {
                //活动已将开始
                map.put("status", party.getStatus());
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));
                return;
            }

            //判断当前场地是否有其他活动正在进行
            Party progressParty = partyLogicService.getPartyId(addressId);
            // 活动存在，并且与partyId 不一致，其他活动正在进行
            if (progressParty != null && !progressParty.getId().equals(partyId)) {
                //当前本场地有活动正在进行
                map.put("status", 4);
                map.put("partyName", progressParty.getName());
                sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms(type, map)));
                return;
            }else if (progressParty != null && progressParty.getId().equals(partyId)) {
                //通知bms
                map.put("status", progressParty.getStatus());
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));
                return;
            }

            if (progressParty == null) {
                //告诉客户端当前的电影状态
                logger.info("通知客户端，活动开始");

                Map<String,Object> commandObject = new HashMap<String,Object>();
                commandObject.put("type", ProtocolConst.PROTOCOL_COMMAND);
                commandObject.put("data",getPartyCommandMap(party,status));
                logger.info("下发消息给客户端:" + JSON.toJSONString(commandObject));
                sendPartyStatusCommandToMq(addressId, commandObject);

                //设置活动的结束时间
                party.setStartTime(now);
                party.setUpdateTime(now);
                party.setStatus(status);
                pushCommandToPartyAdmin(partyId, type, JSON.toJSONString(setObjectToBms(type, map)));
                partyService.updateParty(party);
            }
        }
    }


    private void PartyStartLogic(){

    }

    public Map<String,Object> getPartyCommandMap(Party party,int status){
        Map<String,Object> dataObject = new HashMap<String,Object>();
        dataObject.put("type", PotocolComTypeConst.COMMANDTYPE_PARTY_STATUS);
        dataObject.put("partyId",party.getId());
        if(party.getStartTime()!=null){
            dataObject.put("partyTime",party.getStartTime().getTime());
        }
        if(party.getActivityStartTime()!=null){
            dataObject.put("movieTime",party.getActivityStartTime().getTime());
        }
        dataObject.put("status",status);
        return dataObject;
    }


    /**
     * 初始化处理
     *
     * @return
     */

    private void initHandler(String type, String partyId, String addressId, Channel channel,String key) {
        logger.info("初始化连接，活动:{}，地址:{}", partyId, addressId);

        //ConcurrentHashMap<Channel, AdminTaskModel> adminTaskModelConcurrentHashMap = danmuChannelRepository.findAdminTaskModelConcurrentHashMap();

        //Iterator entries = adminTaskModelConcurrentHashMap.entrySet().iterator();

        /*boolean isNotlogin = false;
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            AdminTaskModel adminTaskModel = (AdminTaskModel)entry.getValue();
            if(key.equals(adminTaskModel.getAuthKey())){
                isNotlogin = true;
                break;
            }
        }


        if(isNotlogin){
            result.put("message","审核界面已经打开过!");
            sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms("isRepeateLogin", result)));
        }*/

        Map<String, Object> result = new HashMap<String, Object>();

        AdminUser adminUser =  adminUserService.getAdminUser(key);

        Object object = redisService.get(AdminUserCacheKey.CHECK_AMDIN_CACHE_KEY+key);
        if(object!=null){
            result.put("message","审核界面已经打开过!");
            sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms("isRepeateLogin", result)));
            return;
        }else{
            redisService.set(AdminUserCacheKey.CHECK_AMDIN_CACHE_KEY+key,adminUser.getId());
            redisService.expire(AdminUserCacheKey.CHECK_AMDIN_CACHE_KEY+key,60*5);
        }






        //缓存管理员与通道的关系
        AdminTaskModel adminTaskModel = new AdminTaskModel();
        adminTaskModel.setPartyId(partyId);
        adminTaskModel.setAuthKey(key);
        adminTaskModel.setAdminId(channel.id().asLongText());
        adminTaskModel.setAddressId(addressId);
        adminTaskModel.setAdminName(adminUser.getUserName());
        danmuChannelRepository.set(channel, adminTaskModel);





        danmuChannelRepository.setManagerChannelIntoList(partyId, channel);
        //获取活动信息
        Party party = partyService.findById(partyId);
        //判断活动是否结束
        boolean partyIsOver = false;
        if (party.getStatus() > 2) {
            partyIsOver = false;
        }
        //设置活动的开始时间
        if (party.getActivityStartTime() != null) {
            //设置电影时间
            result.put("time", party.getActivityStartTime().getTime());
        }
        //设置活动状态
        result.put(CommandTypeConst.PARTY_STATUS, party.getStatus());
        //延迟时间
        result.put("delaySecond", functionService.getDelayTime(partyId));
        //预置弹幕是否开启
        result.put(CommandTypeConst.PRE_DANMU, functionService.getpreDanmuStatus(partyId));
        //测试模式是否开启
        result.put("testIsOpen", functionService.testModeIsOpen(partyId));
        //特效视频
        result.put("specialVideo", functionService.getSpecialMovStatus(partyId));
        //弹幕密度
        result.put("danmuDensity", functionService.getDanmuDensity(partyId));
        //活动名称
        result.put("partyName", party.getName());
        //初始化内容发送个管理界面
        sendMessageToBMS(channel, JSON.toJSONString(setObjectToBms(type, result)));
        //推送给所有管理员信息
        pushCommandToPartyAdmin(partyId, CommandTypeConst.ONLINE_AMDIN_COUNT, null);
    }




    /**
     * 发送消息到广播队列中
     */
    private void sendMessageToMq(String addressId,Map<String,Object> commandMap) {
        //弹幕下发到地址缓存队列中
        String key = DanmuCacheKey.PUB_DANMU_CACHE_LIST + addressId;
        String message = JSON.toJSONString(commandMap);
        logger.info("发送给服务器的客户端{}", message);
        redisService.setValueToList(key, message);
        redisService.expire(key, 60 * 60 * 1);

        //通知客户端
        redisTemplate.convertAndSend("addressId:danmu", addressId);
    }

    /**
     * 发送消息到广播队列中
     */
    private void sendPartyStatusCommandToMq(String addressId, Map<String,Object> map) {
        String key = CommandCacheKey.PUB_COMMAND_PARTYSTATUS_CACHE+addressId;
        String message = JSON.toJSONString(map);
        redisService.set(key, message);
        redisService.expire(key, 60 );
        //通知客户端
        redisTemplate.convertAndSend("party:command", addressId);

    }


    /**
     * 清除所有缓存
     *
     * @param partyId
     */
    private void clearPartyCache(String partyId, String addressId) {
        //延迟时间
        redisService.expire(FunctionControlCacheKey.FUNCITON_CONTROL_DELAYSECOND + partyId, 0);
        //测试模式是否开始
        redisService.expire(FunctionControlCacheKey.FUNCITON_CONTROL_TESTMODEL + partyId, 0);
        //预制弹幕状态
        redisService.expire(FunctionControlCacheKey.FUNCITON_CONTROL_PREDANMU + partyId, 0);
        //动画特效
        redisService.expire(FunctionControlCacheKey.FUNCITON_CONTROL_SPECIALMOV + partyId, 0);
        //弹幕密度
        redisService.expire(FunctionControlCacheKey.FUNCITON_CONTROL_DANMU_DENSITY + partyId, 0);
    }


    /**
     * 用户断开处理
     *
     * @param channel
     */
    public void forceLogout(Channel channel) {
        //移除弹幕池与通道的关系
        AdminTaskModel adminTaskModel = danmuChannelRepository.get(channel);

        //清除客户端缓存信息
        clearClientCacheData(adminTaskModel, channel);
        //关闭通道
        channel.close();
    }


    /**
     * 清除客户端缓存信息
     */
    private void clearClientCacheData(AdminTaskModel adminTaskModel, Channel channel) {
        if (adminTaskModel != null) {
            String partyId = adminTaskModel.getPartyId();
            managerCachService.subOnlineAdminCount(channel, partyId);
            //清除屏幕与地址关系
            danmuChannelRepository.remove(channel);

            redisService.expire(AdminUserCacheKey.CHECK_AMDIN_CACHE_KEY+adminTaskModel.getAuthKey(),0);

            danmuChannelRepository.removeManagerChannel(partyId, channel);

            pushCommandToPartyAdmin(partyId, CommandTypeConst.ONLINE_AMDIN_COUNT, null);
        }
    }

    /**
     * 返回消息给BMS
     *
     * @param channel
     * @param message
     */
    private void sendMessageToBMS(Channel channel, String message) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }



    public Map<String, Object> setObjectToBms(String type, Object object) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("type", type);
        objectMap.put("data", object);
        return objectMap;
    }



    public Map<String, Object> setObjectToBmsError(String type, Object object) {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("type", "error");
        objectMap.put("data", object);
        return objectMap;
    }

    /**
     * 在线管理员的数量推送给所有场地管理员
     * @param partyId
     * @param commandType
     * @param message
     */
    private void pushCommandToPartyAdmin(String partyId, String commandType, String message) {
        logger.info("获取当前活动下管理员数量");
        try {
            List<Channel> channelList = danmuChannelRepository.getManagerChannelList(partyId);
            List<String> managerNameList = new ArrayList<>();
            if (ListUtils.checkListIsNotNull(channelList)) {
                if (CommandTypeConst.ONLINE_AMDIN_COUNT.equals(commandType)) {
                    for (Channel channel : channelList) {
                        AdminTaskModel adminTaskModel = danmuChannelRepository.get(channel);
                        managerNameList.add(adminTaskModel.getAdminName());

                    }
                    message = JSON.toJSONString(setObjectToBms(commandType, managerNameList));
                }

                for (Channel channel : channelList) {
                    sendMessageToBMS(channel, message);
                }

                /*for (Channel channel : channelList) {
                    if (CommandTypeConst.ONLINE_AMDIN_COUNT.equals(commandType)) {
                        //在线管理员数量
                        message = JSON.toJSONString(setObjectToBms(commandType, channelList.size()));
                    }
                    logger.info("发送的消息:" + message);
                    sendMessageToBMS(channel, message);
                    logger.info("反馈给管理员的消息,{}", message);
                }*/
            }

        } catch (Exception e) {
            logger.error("获取当前活动下管理员数量异常:{}", e.getMessage());
        }
    }

}