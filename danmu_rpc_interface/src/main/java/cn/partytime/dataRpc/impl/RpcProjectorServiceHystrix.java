package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcProjectorService;
import cn.partytime.model.PageResultModel;
import cn.partytime.model.Projector;
import cn.partytime.model.ProjectorAction;

public class RpcProjectorServiceHystrix implements RpcProjectorService {
    @Override
    public Projector findByRegisterCode(String registorCode) {
        return null;
    }

    @Override
    public void saveProjectAction(ProjectorAction projectorAction) {

    }

    @Override
    public void saveProjector(Projector projector) {

    }

    @Override
    public PageResultModel<ProjectorAction> findProjectorActionPage(String registorCode, int page, int size) {
        return null;
    }
}
