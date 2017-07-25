package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcDanmuAddressService;
import cn.partytime.model.DanmuAddress;

import java.util.List;

public class RpcDanmuAddressServiceHystrix implements RpcDanmuAddressService {
    @Override
    public DanmuAddress findAddressByLonLat(Double longitude, Double latitude) {
        return null;
    }

    @Override
    public DanmuAddress findById(String id) {
        return null;
    }

    @Override
    public List<DanmuAddress> findByType(Integer type) {
        return null;
    }
}
