package cn.partytime.dataRpc;

import cn.partytime.dataRpc.impl.RpcDanmuLibraryPartyServiceHystrix;
import cn.partytime.model.DanmuLibraryParty;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by dm on 2017/7/10.
 */

@FeignClient(value = "${dataRpcServer}",fallback = RpcDanmuLibraryPartyServiceHystrix.class)
public interface RpcDanmuLibraryPartyService {


    @RequestMapping(value = "/rpcDanmuLibraryParty/findByPartyId" ,method = RequestMethod.GET)
    public DanmuLibraryParty findByPartyId(@RequestParam(value = "partyId") String partyId);

}
