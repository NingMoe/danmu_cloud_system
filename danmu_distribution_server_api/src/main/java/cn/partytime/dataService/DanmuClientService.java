package cn.partytime.dataService;

import cn.partytime.common.util.ServerConst;
import cn.partytime.dataService.impl.DanmuClientServiceHystrix;
import cn.partytime.model.DanmuClient;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by dm on 2017/7/4.
 */

@FeignClient(value = ServerConst.SERVER_NAME_DATASERVER,fallback = DanmuClientServiceHystrix.class)
public interface DanmuClientService {

    @RequestMapping(value = "/danmuClient/findByRegistCode" ,method = RequestMethod.GET)
    public DanmuClient findByRegistCode(@RequestParam(value = "registCode") String registCode);

}
