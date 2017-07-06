package cn.partytime.check.listener;

import cn.partytime.check.config.DanmuChannelRepository;
import cn.partytime.check.handlerThread.RealTimeDanmuHandler;
import cn.partytime.common.util.ListUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("realTimeDanmuMessageListener")
public class RealTimeDanmuMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDanmuMessageListener.class);

    @Autowired
    private RealTimeDanmuHandler realTimeDanmuHandler;

    @Autowired
    private DanmuChannelRepository danmuChannelRepository;


    @Override
    public void onMessage(Message message, byte[] bytes) {
        if (message != null) {
            String partyId = JSON.parseObject(String.valueOf(message), String.class).replace("'", "");
            logger.info("处理活动{}弹幕",partyId);
            List<Channel> channelList = danmuChannelRepository.getManagerChannelList(partyId);
            if(!ListUtils.checkListIsNotNull(channelList)){
                logger.info("本服务器不处理此活动的弹幕!");
            }
            realTimeDanmuHandler.danmuListenHandler(partyId);
        }

    }
}