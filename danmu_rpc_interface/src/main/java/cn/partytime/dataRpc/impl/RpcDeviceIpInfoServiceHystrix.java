package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcDeviceIpInfoService;
import cn.partytime.model.DeviceIpInfo;

import java.util.List;

public class RpcDeviceIpInfoServiceHystrix implements RpcDeviceIpInfoService {
    @Override
    public List<DeviceIpInfo> findByAddressId(String addressId) {
        return null;
    }
}
