package cn.partytime.rpcService.dataRpc.impl;

import cn.partytime.model.PageResultDTO;
import cn.partytime.rpcService.dataRpc.ProjectorService;
import cn.partytime.model.Projector;
import cn.partytime.model.ProjectorAction;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 2017/7/11.
 */

@Component
public class ProjectorServiceHystrix implements ProjectorService {
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
    public PageResultDTO<ProjectorAction> findProjectorActionPage(String registorCode, int page, int size) {
        return null;
    }
}
