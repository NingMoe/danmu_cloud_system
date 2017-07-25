package cn.partytime.dataRpc;

import cn.partytime.dataRpc.impl.RpcPartyServiceHystrix;
import cn.partytime.model.Party;
import cn.partytime.model.PartyLogicModel;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by dm on 2017/7/10.
 */
@FeignClient(value = "${dataRpcServer}",fallback = RpcPartyServiceHystrix.class)
public interface RpcPartyService {

    @RequestMapping(value = "/rpcParty/findPartyAddressId" ,method = RequestMethod.GET)
    public PartyLogicModel findPartyAddressId(@RequestParam(value = "addressId") String addressId);

    @RequestMapping(value = "/rpcParty/findByMovieAliasOnLine" ,method = RequestMethod.GET)
    public Party findByMovieAliasOnLine(@RequestParam(value = "command") String command);

    @RequestMapping(value = "/rpcParty/getPartyByPartyId" ,method = RequestMethod.GET)
    public Party getPartyByPartyId(@RequestParam(value = "partyId") String partyId);

    @RequestMapping(value = "/rpcParty/findTemporaryParty" ,method = RequestMethod.GET)
    public PartyLogicModel findTemporaryParty(@RequestParam(value = "addressId") String addressId);

    @RequestMapping(value = "/rpcParty/saveParty" ,method = RequestMethod.POST)
    public Party saveParty(Party party);

    @RequestMapping(value = "/rpcParty/findAddressIdListByPartyId" ,method = RequestMethod.GET)
    public List<String> findAddressIdListByPartyId(@RequestParam(value = "partyId") String partyId);



    @RequestMapping(value = "/rpcParty/getPartyByAddressId" ,method = RequestMethod.GET)
    public Party getPartyId(@RequestParam(value = "addressId") String addressId);


    @RequestMapping(value = "/rpcParty/findPartyByLonLat" ,method = RequestMethod.GET)
    public PartyLogicModel findPartyByLonLat(@RequestParam(value = "longitude") Double longitude, @RequestParam(value = "latitude") Double latitude);



    @RequestMapping(value = "/rpcParty/checkPartyIsOver" ,method = RequestMethod.POST)
    public boolean checkPartyIsOver(Party party);

    @RequestMapping(value = "/rpcParty/deleteParty" ,method = RequestMethod.GET)
    public void deleteParty(@RequestParam(value = "partyId") String partyId);




    @RequestMapping(value = "/rpcParty/getPartyDmDensity" ,method = RequestMethod.GET)
    public int getPartyDmDensity(@RequestParam(value = "addressId")  String addressId, @RequestParam(value = "partyId") String partyId);

}
