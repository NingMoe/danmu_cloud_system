package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcAdminService;
import cn.partytime.model.AdminUser;
import org.springframework.stereotype.Component;

@Component
public class RpcAdminServiceHystrix implements RpcAdminService {
    @Override
    public Boolean checkAuthKey(String authKey) {
        return null;
    }

    @Override
    public AdminUser getAdminUser(String authKey) {
        return null;
    }
}
