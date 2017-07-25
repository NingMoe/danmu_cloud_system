package cn.partytime.dataRpc;

import cn.partytime.dataRpc.impl.RpcDanmuAddressServiceHystrix;
import cn.partytime.model.DanmuAddress;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dm on 2017/7/10.
 */

@FeignClient(value = "${dataRpcServer}",fallback = RpcDanmuAddressServiceHystrix.class)
public interface RpcDanmuAddressService {

    @RequestMapping(value = "/rpcDanmuAddress/findAddressByLonLat" ,method = RequestMethod.GET)
    public DanmuAddress findAddressByLonLat(@RequestParam(value = "longitude") Double longitude, @RequestParam(value = "latitude") Double latitude);

    @RequestMapping(value = "/rpcDanmuAddress/findById" ,method = RequestMethod.GET)
    public DanmuAddress findById(@RequestParam(value = "id") String id);

    @RequestMapping(value = "/rpcDanmuAddress/findAddressListByType" ,method = RequestMethod.GET)
    public List<DanmuAddress> findByType(@RequestParam(value = "type") Integer type);

}
