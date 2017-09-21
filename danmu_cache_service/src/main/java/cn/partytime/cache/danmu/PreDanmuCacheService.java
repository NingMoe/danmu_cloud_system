package cn.partytime.cache.danmu;


import cn.partytime.common.cachekey.danmu.PreDanmuCacheKey;
import cn.partytime.common.constants.CommonConst;
import cn.partytime.common.util.DateUtils;
import cn.partytime.common.util.LongUtils;
import cn.partytime.common.util.NumberUtils;
import cn.partytime.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PreDanmuCacheService {

    @Autowired
    private RedisService redisService;




    public long findPreDanmAlarmDelayTime(String partyId,String addressId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_DELAY_ALARM_DELAY_TIME+partyId+ CommonConst.COLON+addressId;
        return LongUtils.objectConvertToLong(redisService.get(key));
    }

    public void setPreDanmAlarmDelayTime(String partyId,String addressId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_DELAY_ALARM_DELAY_TIME+partyId+ CommonConst.COLON+addressId;
        redisService.set(key, DateUtils.getCurrentDate().getTime());
        redisService.expire(key,60*5);
    }

    public void removePreDanmAlarmDelayTime(String partyId,String addressId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_DELAY_ALARM_DELAY_TIME+partyId+ CommonConst.COLON+addressId;
        redisService.expire(key,0);
    }

    public Object findPreDanmu(String partyId,String addressId,String libraryId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId+ CommonConst.COLON+addressId+CommonConst.COLON+libraryId;
        Object object =  redisService.popFromList(key);
        log.info("object:{}",object);
        return object;
    }


    public void removePreDanmuFromCache(String partyId,String addressId,String libraryId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId+ CommonConst.COLON+addressId+CommonConst.COLON+libraryId;
        redisService.expire(key,0);
    }

    /**
     * 将预置弹幕添加到缓存中
     * @param partyId
     * @param libraryId
     * @param danmu
     */
    public void addPreDanmuIntoCacheUnderParty(String partyId,String addressId, String libraryId, String danmu){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId+ CommonConst.COLON+addressId+CommonConst.COLON+libraryId;
        redisService.setValueToList(key, danmu);
    }

    public long getPreDanmuListSize(String partyId,String addressId, String libraryId){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId+ CommonConst.COLON+addressId+CommonConst.COLON+libraryId;
        return redisService.getListLength(key);
    }

    /**
     * 设置预置弹幕时间
     * @param partyId
     * @param libraryId
     * @param time
     */
    public void setPreDanmuIntoCacheUnderPartyTime(String partyId,String addressId,String libraryId, long time){
        String key = PreDanmuCacheKey.PARTY_PREDANMU_CACHE_LIST + partyId+ CommonConst.COLON+addressId+CommonConst.COLON+libraryId;
        redisService.expire(key,time);
    }
}
