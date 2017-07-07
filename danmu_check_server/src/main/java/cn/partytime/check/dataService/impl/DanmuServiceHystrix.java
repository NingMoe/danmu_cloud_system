package cn.partytime.check.dataService.impl;

import cn.partytime.check.dataService.DanmuService;
import cn.partytime.check.model.DanmuModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dm on 2017/7/6.
 */

@Component
public class DanmuServiceHystrix implements DanmuService {
    @Override
    public DanmuModel findById(String id) {
        return null;
    }

    @Override
    public DanmuModel save(DanmuModel danmuModel) {
        return null;
    }

    @Override
    public List<DanmuModel> findDanmuByIsBlocked(int page, int size, boolean isBlocked) {
        return null;
    }
}
