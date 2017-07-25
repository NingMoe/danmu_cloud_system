package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcPartyService;
import cn.partytime.model.Party;
import cn.partytime.model.PartyLogicModel;

import java.util.List;

public class RpcPartyServiceHystrix implements RpcPartyService {
    @Override
    public PartyLogicModel findPartyAddressId(String addressId) {
        return null;
    }

    @Override
    public Party findByMovieAliasOnLine(String command) {
        return null;
    }

    @Override
    public Party getPartyByPartyId(String partyId) {
        return null;
    }

    @Override
    public PartyLogicModel findTemporaryParty(String addressId) {
        return null;
    }

    @Override
    public Party saveParty(Party party) {
        return null;
    }

    @Override
    public List<String> findAddressIdListByPartyId(String partyId) {
        return null;
    }

    @Override
    public Party getPartyId(String addressId) {
        return null;
    }

    @Override
    public PartyLogicModel findPartyByLonLat(Double longitude, Double latitude) {
        return null;
    }

    @Override
    public boolean checkPartyIsOver(Party party) {
        return false;
    }

    @Override
    public void deleteParty(String partyId) {

    }

    @Override
    public int getPartyDmDensity(String addressId, String partyId) {
        return 0;
    }
}
