package cn.partytime.check.dataService;

import cn.partytime.check.dataService.impl.PartyLogicServiceHystrix;
import cn.partytime.check.model.Party;
import cn.partytime.common.util.ServerConst;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dm on 2017/7/6.
 */

@FeignClient(value = ServerConst.SERVER_NAME_DATASERVER,fallback = PartyLogicServiceHystrix.class)
public interface PartyLogicService {

    @RequestMapping(value = "/partyLogic/findAddressIdListByPartyId" ,method = RequestMethod.GET)
    public List<String> findAddressIdListByPartyId(@RequestParam(value = "partyId") String partyId);


    @RequestMapping(value = "/getPartyId" ,method = RequestMethod.GET)
    public Party getPartyId(@RequestParam(value = "addressId") String addressId);
}