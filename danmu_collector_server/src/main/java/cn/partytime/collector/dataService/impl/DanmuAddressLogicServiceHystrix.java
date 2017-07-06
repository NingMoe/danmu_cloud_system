package cn.partytime.collector.dataService.impl;

import cn.partytime.collector.dataService.DanmuAddressLogicService;
import cn.partytime.collector.model.DanmuAddress;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 2017/7/5.
 */

@Component
public class DanmuAddressLogicServiceHystrix implements DanmuAddressLogicService {

    @Override
    public DanmuAddress findAddressByLonLat(Double longitude, Double latitude) {
        return null;
    }
}
