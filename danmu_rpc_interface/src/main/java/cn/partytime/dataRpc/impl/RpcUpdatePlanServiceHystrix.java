package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcUpdatePlanService;
import cn.partytime.model.UpdatePlan;

import java.util.List;

public class RpcUpdatePlanServiceHystrix implements RpcUpdatePlanService {
    @Override
    public List<UpdatePlan> findByAddressId(String addressId) {
        return null;
    }

    @Override
    public void update(String id, Integer status, String machineNum) {

    }
}
