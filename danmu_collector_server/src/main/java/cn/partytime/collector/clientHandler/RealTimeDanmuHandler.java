package cn.partytime.collector.clientHandler;

import cn.partytime.collector.config.DanmuChannelRepository;
import cn.partytime.collector.service.ClientCacheService;
import cn.partytime.collector.service.DanmuSendService;
import cn.partytime.common.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lENOVO on 2016/10/9.
 */

@Component
public class RealTimeDanmuHandler {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeDanmuHandler.class);

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private DanmuSendService danmuSendService;

    public void danmuListenHandler(String addressId) {
        logger.info("实时弹幕监听线程启动");
        try {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    danmuSendService.sendDanmu(addressId);
                }
            });
        }catch (Exception e) {
            logger.error("实时弹幕监听线程异常:{}", e.getMessage());
        }
    }

}
