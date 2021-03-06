package cn.partytime.config;

import cn.partytime.common.constants.ClientConst;
import cn.partytime.model.DanmuClientModel;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 保存所有的频道
 * Created by user on 16/6/24.
 */
@Component
public class DanmuChannelRepository {

    private static final Logger logger = LoggerFactory.getLogger(DanmuChannelRepository.class);

    //通道与客户端关系
    private ConcurrentHashMap<Channel, DanmuClientModel> channelConcurrentHashMap = new ConcurrentHashMap<Channel, DanmuClientModel>();

    public  ConcurrentHashMap<Channel, DanmuClientModel>  findConcurrentHashMap(){
        return channelConcurrentHashMap;
    }

    public void set(Channel channel, DanmuClientModel danmuClientModel) {
        logger.info("新的通道连接");
        channelConcurrentHashMap.put(channel, danmuClientModel);
    }

    public DanmuClientModel get(Channel channel) {
        return  channelConcurrentHashMap.get(channel);
    }

    public void remove(Channel channel) {
        logger.info("移除客户端");
        channelConcurrentHashMap.remove(channel);
    }

    public int size() {
        logger.info("获取客户端数量");
        return channelConcurrentHashMap.size();
    }

    public int findDanmuClientCount(int type,String addressId){

        int count =0;
        for (ConcurrentHashMap.Entry<Channel, DanmuClientModel> entry : channelConcurrentHashMap.entrySet()) {
            DanmuClientModel danmuClientModel = entry.getValue();
            if (addressId.equals(danmuClientModel.getAddressId()) && danmuClientModel.getClientType()==type) {
                count = count+1;
            }
        }
        return count;
    }


}
