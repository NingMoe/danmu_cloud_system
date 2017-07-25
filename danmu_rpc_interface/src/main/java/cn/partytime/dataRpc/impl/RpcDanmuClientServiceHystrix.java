package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcDanmuClientService;
import cn.partytime.model.DanmuClient;

import java.util.List;

public class RpcDanmuClientServiceHystrix implements RpcDanmuClientService {
    @Override
    public DanmuClient findByRegistCode(String registCode) {
        return null;
    }

    @Override
    public List<DanmuClient> findByAddressId(String addressId) {
        return null;
    }
}
