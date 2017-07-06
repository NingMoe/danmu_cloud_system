package cn.partytime.check.netty;

import cn.partytime.danmu.distribution.service.CommandHanderService;
import cn.partytime.danmu.distribution.service.UserSessionService;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by lENOVO on 2016/12/6.
 */

@Component
@Qualifier("webSocketServerHandler")
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(CommandHanderService.class);

    private WebSocketServerHandshaker handshaker;

    @Autowired
    private UserSessionService userSessionService;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) request);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

        String url = req.uri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        if (parameters.size() == 0) {
            logger.info("参数不可缺省");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }
        //获取key
        String key = parameters.get("key").get(0);
        if(StringUtils.isEmpty(key)){
            logger.info("唯一标识key不能为空");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);



            //判断session是否存在，如果不存在就下线
            if (userSessionService.checkAuthKey(key)) {
                userSessionService.addSessionTime(key);

                //存储session
                /*String socketSessionKey = AdminUserCacheKey.ADMIN_USER_SESSION_SOCKET_CACHE_KEY+key;
                Object object = redisService.get(socketSessionKey);
                if(object!=null){
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
                    return;
                }else{
                    redisService.set(socketSessionKey,1);
                    redisService.expire(socketSessionKey,60*60*3);
                }*/

            } else {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
                return;
            }

        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + req.uri();
        return "ws://" + location;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
