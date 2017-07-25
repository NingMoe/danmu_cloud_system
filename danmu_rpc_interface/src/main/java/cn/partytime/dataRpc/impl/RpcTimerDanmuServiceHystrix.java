package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcTimerDanmuService;
import cn.partytime.model.TimerDanmuFileLogicModel;

import java.util.List;

public class RpcTimerDanmuServiceHystrix implements RpcTimerDanmuService {
    @Override
    public List<TimerDanmuFileLogicModel> findTimerDanmuFileList(String addressId) {
        return null;
    }
}
