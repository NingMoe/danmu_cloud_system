package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcParamService;
import cn.partytime.model.ParamValueJson;

import java.util.List;

public class RpcParamServiceHystrix implements RpcParamService {
    @Override
    public List<ParamValueJson> findByRegistCode(String code) {
        return null;
    }
}
