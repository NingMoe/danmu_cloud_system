package cn.partytime.dataRpc;

import cn.partytime.dataRpc.impl.RpcDanmuClientServiceHystrix;
import cn.partytime.model.DanmuClient;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dm on 2017/7/10.
 */


@FeignClient(value = "${dataRpcServer}",fallback = RpcDanmuClientServiceHystrix.class)
public interface RpcDanmuClientService {



    @RequestMapping(value = "/rpcDanmuClient/findByRegistCode" ,method = RequestMethod.GET)
    public DanmuClient findByRegistCode(@RequestParam(value = "registCode") String registCode);


    @RequestMapping(value = "/rpcDanmuClient/findByAddressId" ,method = RequestMethod.GET)
    public List<DanmuClient> findByAddressId(@RequestParam(value = "addressId") String addressId);
}
