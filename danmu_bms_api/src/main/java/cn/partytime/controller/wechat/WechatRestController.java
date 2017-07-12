package cn.partytime.controller.wechat;

import cn.partytime.common.util.ComponentKeyConst;
import cn.partytime.model.PartyLogicModel;
import cn.partytime.model.manager.H5Template;
import cn.partytime.model.wechat.WechatUser;
import cn.partytime.model.wechat.WeixinMessage;
import cn.partytime.service.*;
import cn.partytime.service.wechat.WechatUserInfoService;
import cn.partytime.service.wechat.WechatUserService;
import cn.partytime.util.MessageUtil;
import cn.partytime.util.WechatSignUtil;
import cn.partytime.util.WeixinUtil;
import cn.partytime.wechat.entity.PayNotifyXmlEntity;
import cn.partytime.wechat.entity.ReceiveXmlEntity;
import cn.partytime.wechat.message.TextMessage;
import cn.partytime.wechat.message.Voice;
import cn.partytime.wechat.message.VoiceMessage;
import cn.partytime.wechat.payService.WechatPayService;
import cn.partytime.wechat.pojo.AccessToken;
import cn.partytime.wechat.pojo.UserInfo;
import cn.partytime.wechat.process.FormatXmlProcess;
import cn.partytime.wechat.process.ReceiveXmlProcess;
import cn.partytime.wechat.words.Words;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 微信xml消息处理流程逻辑类
 */
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
public class WechatRestController {
    private static final Logger logger = LoggerFactory.getLogger(WechatRestController.class);
    @Autowired
    private BmsWechatUserService bmsWechatUserService;

    @Autowired
    private BmsPartyService bmsPartyService;

    @Autowired
    private WeixinMessageService weixinMessageService;

    @Autowired
    private BmsDanmuService bmsDanmuService;

    @Autowired
    private WechatUserService wechatUserService;

    @Autowired
    private DanmuAddressService danmuAddressService;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private WechatRewardService wechatRewardService;

    @Autowired
    private WechatUserInfoService wechatUserInfoService;

    @Autowired
    private H5TemplateService h5TemplateService;



    @Autowired
    private BmsColorService bmsColorService;

    /**
     * 微信公众平台验证用
     *
     * @param id
     */
    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    public String valid(Long id, HttpServletRequest request, HttpServletResponse response) {
        //WXPlatformDto wxPlatformDto =wxPlatformService.getByPK(id);
        log.info("valid get message-----GET");

        // 微信加密签名
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");

        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            token = "gh78ruf84jgh57sj88fj12dc";
        }

        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (WechatSignUtil.checkSignature(signature, timestamp, nonce, token)) {
            return echostr;
        }
        return null;
    }

    /**
     * 解析处理xml、获取智能回复结果（通过图灵机器人api接口）
     *
     * @return 最终的解析结果（xml格式数据）
     */
    @RequestMapping(value = "/valid", method = RequestMethod.POST)
    public String processWechatMag(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("valid get message-----POST");

        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        /** 解析xml数据 */
        ReceiveXmlEntity xmlEntity = new ReceiveXmlProcess().getMsgEntity(request);
        log.info("valid post:" + xmlEntity.toString());
        //按照touserName 得到公众帐号信息
        String wechatId = xmlEntity.getToUserName();
        String openId = xmlEntity.getFromUserName();
        UserInfo userInfo = WeixinUtil.getUserInfo(bmsWechatUserService.getAccessToken().getToken(), openId);
        WechatUser wechatUser = wechatUserService.updateUserInfo(userInfo.toWechatUser());


        String result = "";
        //消息类型为event
        if (MessageUtil.REQ_MESSAGE_TYPE_EVENT.equals(xmlEntity.getMsgType())) {
            logger.info("event------->"+xmlEntity.getEvent());
            //当用户同意允许公众账号获取地理位置时，每次打开微信公众账号，都会收到此消息
            if (MessageUtil.REQ_MESSAGE_TYPE_LOCATION.equals(xmlEntity.getEvent())) {
                log.info("REQ_MESSAGE_TYPE_LOCATION" + xmlEntity.getContent());
                bmsWechatUserService.saveUserLocation(wechatUser,xmlEntity);
                //关注微信
            } else if (MessageUtil.EVENT_TYPE_SUBSCRIBE.equals(xmlEntity.getEvent())) {
                log.info("EVENT_TYPE_SUBSCRIBE" + xmlEntity.getContent());
                bmsWechatUserService.subscribe(userInfo);
                TextMessage text = new TextMessage();
                text.setToUserName(xmlEntity.getFromUserName());
                text.setFromUserName(xmlEntity.getToUserName());
                text.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
                text.setCreateTime(new Date().getTime());
                text.setFuncFlag(0);
                text.setContent(Words.WELCOME);
                result = FormatXmlProcess.textMessageToXml(text);
                //取消关注
            } else if (MessageUtil.EVENT_TYPE_UNSUBSCRIBE.equals(xmlEntity.getEvent())) {
                log.info("EVENT_TYPE_UNSUBSCRIBE" + xmlEntity.getContent());
                wechatUserService.unsubscribe(openId);
                //CLICK事件推送
            } else if (MessageUtil.EVENT_TYPE_CLICK.equals(xmlEntity.getEvent())) {

                String eventKey = xmlEntity.getEventKey();
                if (!StringUtils.isEmpty(eventKey)) {
                    if (eventKey.equals("VOICE_DAN_MU")) {

                       result = bmsWechatUserService.sendVoiceDanmu(xmlEntity);
                    } else if (eventKey.equals("MONEY")) {

                    } else if (eventKey.equals("CONTECT_US")) {

                    }

                }
                log.info("EVENT_TYPE_CLICK" + eventKey);
                return result;

                //view事件推送
            } else if (MessageUtil.EVENT_TYPE_VIEW.equals(xmlEntity.getEvent())) {
                String eventKey = xmlEntity.getEventKey();
                //处理view事件推送,主要记录用户点击事件
                String url = xmlEntity.getUrl();
                log.info("EVENT_TYPE_VIEW" + eventKey);
            }
        } else if (MessageUtil.REQ_MESSAGE_TYPE_TEXT.equals(xmlEntity.getMsgType())) {
            String content = xmlEntity.getContent();
            if (!StringUtils.isEmpty(content)) {
                log.info("RESP_MESSAGE_TYPE_TEXT" + content);

                List<WeixinMessage> weixinMessageList = weixinMessageService.findAll();
                if( null != weixinMessageList && weixinMessageList.size() > 0){
                    for(WeixinMessage weixinMessage : weixinMessageList){
                        for(String word : weixinMessage.getWordList()){
                            if(content.indexOf(word) != -1){
                                if( null == weixinMessage.getMediaId()){
                                    TextMessage text = new TextMessage();
                                    text.setToUserName(xmlEntity.getFromUserName());
                                    text.setFromUserName(xmlEntity.getToUserName());
                                    text.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
                                    text.setCreateTime(new Date().getTime());
                                    text.setFuncFlag(0);
                                    text.setContent(weixinMessage.getMessage());
                                    result = FormatXmlProcess.textMessageToXml(text);
                                }else{
                                    VoiceMessage voiceMessage = new VoiceMessage();
                                    voiceMessage.setToUserName(xmlEntity.getFromUserName());
                                    voiceMessage.setFromUserName(xmlEntity.getToUserName());
                                    voiceMessage.setMsgType(MessageUtil.REQ_MESSAGE_TYPE_VOICE);
                                    voiceMessage.setCreateTime(new Date().getTime());
                                    voiceMessage.setFuncFlag(0);
                                    Voice voice = new Voice();
                                    voice.setMediaId(weixinMessage.getMediaId());
                                    voiceMessage.setVoice(voice);
                                    result = FormatXmlProcess.voiceMessageToXml(voiceMessage);
                                    log.info(result);
                                }
                            }
                        }
                    }

                }
            }

        } else if (MessageUtil.REQ_MESSAGE_TYPE_VOICE.equals(xmlEntity.getMsgType())) {
            String recognition = xmlEntity.getRecognition();

            TextMessage text = new TextMessage();
            text.setToUserName(xmlEntity.getFromUserName());
            text.setFromUserName(xmlEntity.getToUserName());
            text.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
            text.setCreateTime(new Date().getTime());
            text.setFuncFlag(0);
            if (!StringUtils.isEmpty(recognition)) {
                log.info("REQ_MESSAGE_TYPE_VOICE" + recognition);
                if (recognition.indexOf("!") != -1) {

                }
                text.setContent("弹幕：“" + recognition + "”发送");
                result = FormatXmlProcess.textMessageToXml(text);

                //发送语音弹幕
                PartyLogicModel partyLogicModel = bmsPartyService.findCurrentParty(openId);
                if(partyLogicModel!=null){

                    String partyId = partyLogicModel.getPartyId();
                    String addressId = partyLogicModel.getAddressId();

                    Map<String,String> map = new HashMap<String,String>();
                    map.put("message",recognition);
                    map.put("color",bmsColorService.getRandomColor());
                    bmsDanmuService.sendDanmuByWechat(ComponentKeyConst.P_DANMU,map,openId,partyId,addressId,1,1);
                    //bmsDanmuService.saveDanmu(recognition,null,openId,1,1);
                }

            }


        } else if(MessageUtil.SHAKE_AROUND_USER_SHAKE.equals(xmlEntity.getMsgType())){
            
        }
        return result;

    }


    @RequestMapping(value = "/testAccesstoken", method = RequestMethod.GET)
    public String testAccesstoken(Long id, HttpServletRequest request, HttpServletResponse response) {
        AccessToken accessToken = bmsWechatUserService.getAccessToken();
        return accessToken.getToken();
    }


    @RequestMapping(value = "/createMenu", method = RequestMethod.GET)
    public String createMenu(Model model, HttpServletRequest request) {
        bmsWechatUserService.createMenu();
        return "success";
    }

    @RequestMapping(value = "/delMenu", method = RequestMethod.GET)
    public String delMenu(Model model, HttpServletRequest request) {
        bmsWechatUserService.delMenu();
        return "success";
    }

    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public Map pay(String nonceStr,String timestamp ,String openId,String attach, HttpServletRequest request,String h5TempId){
        String body="弹幕电影-打赏1圆";
        String detail="";
        Integer total_fee = 100;
        if(!StringUtils.isEmpty(h5TempId)){
            H5Template h5Template = h5TemplateService.findById(h5TempId);
            if( null != h5Template){
                if(!StringUtils.isEmpty(h5Template.getPayTitle())){
                    body = h5Template.getPayTitle();
                }
                if( null != h5Template.getPayMoney()){
                    total_fee = h5Template.getPayMoney();
                }
            }

        }

        String clientIp = request.getHeader("x-forwarded-for");
        if(StringUtils.isEmpty(clientIp)){
            clientIp = request.getRemoteAddr();
        }
        if(!StringUtils.isEmpty(clientIp) && clientIp.indexOf(",")!=-1){
            clientIp = clientIp.substring(0,clientIp.indexOf(","));
        }
        Map<String,String> map = wechatPayService.createUnifiedorder(nonceStr,timestamp,openId,body,detail,attach,total_fee,clientIp);
        return map;
    }

    @RequestMapping(value = "/payNotify", method = RequestMethod.POST)
    public String payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PayNotifyXmlEntity entity = new ReceiveXmlProcess().getWeChatPayEntity(request);
        log.info(entity.toString());
        PartyLogicModel partyLogicModel = bmsPartyService.findCurrentParty(entity.getOpenid());
        String h5Id = "";
        String addressId = null;
        String partyId = null;
        String dmCmdId = "";
        Map<String, String> map = new HashMap<String, String>();
        WechatUser wechatUser = bmsWechatUserService.findByOpenId(entity.getOpenid());
        if (null != wechatUser) {
            String attach = entity.getAttach();
            if (!StringUtils.isEmpty(attach)) {
                if (attach.indexOf(",") != -1) {
                    String[] attachs = attach.split(",");
                    for (int i = 0; i < attachs.length; i++) {
                        if (attachs[i].indexOf("dmId") != -1) {
                            dmCmdId = attachs[i].split(":")[1];
                        } else if (attachs[i].indexOf("h5Id") != -1) {
                            h5Id = attachs[i].split(":")[1];
                            log.info("h5Id:" + h5Id);
                        } else {
                            String[] dms = attachs[i].split(":");
                            if (dms[1].equals("noname")) {
                                map.put(dms[0], "匿名用户");
                            } else if (dms[1].equals("name")) {
                                map.put(dms[0], wechatUser.getNick());
                            }
                        }
                    }
                } else {
                    dmCmdId = attach;
                }
            }
        }
        if( null != partyLogicModel) {
            addressId = partyLogicModel.getAddressId();
            partyId = partyLogicModel.getPartyId();
            bmsDanmuService.sendDanmuByWechat(dmCmdId, map, entity.getOpenid(), partyId, addressId, 1, 0);
            //bmsDanmuService.sendMessageToScreenClient(addressId, MessageConst.MESSAGE_FROM_ClIENT, null, DanmuTypeEnmu.getEnName(DanmuConst.MONEY_DANMU), null, null, null, name);
        }
        H5Template h5Template = h5TemplateService.findById(h5Id);
        wechatRewardService.save(partyId,addressId,entity.getOpenid(),entity.getTime_end(),h5Template.getPayMoney(),h5Id);
        return "success";
    }

    @RequestMapping(value = "/sanae", method = RequestMethod.GET)
    public List<List<String>> sanae(){
        List<List<String>> listStr = new ArrayList<>();
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        List<String> l3 = new ArrayList<>();

        listStr.add(l1);
        listStr.add(l2);
        listStr.add(l3);

        return listStr;
    }

    /**
    public static void main(String[] args){
        String attach = "dmId:592525d30cf26a8f85eeac21,h5Id:592689fd0cf24d415c21be6a,name:name";
        String dmCmdId = "";
        String h5Id = "";
        if (attach.indexOf(",") != -1) {
            String[] attachs = attach.split(",");
            for (int i = 0; i < attachs.length; i++) {
                if (attachs[i].indexOf("dmId")!=-1) {
                    dmCmdId = attachs[i].split(":")[1];
                    System.out.print(dmCmdId);
                } else if (attachs[i].indexOf("h5Id")!=-1) {
                    h5Id = attachs[i].split(":")[1];
                    System.out.print(h5Id);
                } else {
                    String[] dms = attachs[i].split(":");
                    if (dms[1].equals("noname")) {
                        //map.put(dms[0], "匿名用户");
                    } else if (dms[1].equals("name")) {
                        //map.put(dms[0], wechatUser.getNick());
                    }
                }
            }
            dmCmdId = attachs[0];
        } else {
            dmCmdId = attach;
        }
    }
    **/
}