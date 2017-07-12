package cn.partytime.dataService.impl;

import cn.partytime.dataService.AdTimerService;
import cn.partytime.model.AdTimerResource;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 2017/7/11.
 */

@Component
public class AdTimerServiceHystrix implements AdTimerService {
    @Override
    public AdTimerResource findTimerDanmuFileList(String addressId) {
        return null;
    }
}