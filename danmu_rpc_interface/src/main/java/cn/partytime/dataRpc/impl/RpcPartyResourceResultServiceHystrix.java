package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcPartyResourceResultService;
import cn.partytime.model.PartyResourceResult;

import java.util.List;

public class RpcPartyResourceResultServiceHystrix implements RpcPartyResourceResultService {
    @Override
    public List<PartyResourceResult> findLatelyParty() {
        return null;
    }

    @Override
    public List<PartyResourceResult> findLatelyPartyByAddressIdAndType(String addressId, Integer type) {
        return null;
    }
}
