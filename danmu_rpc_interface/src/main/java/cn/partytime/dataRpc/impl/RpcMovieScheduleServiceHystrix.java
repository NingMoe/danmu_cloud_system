package cn.partytime.dataRpc.impl;

import cn.partytime.dataRpc.RpcMovieScheduleService;
import cn.partytime.model.MovieSchedule;

import java.util.List;

public class RpcMovieScheduleServiceHystrix implements RpcMovieScheduleService {
    @Override
    public List<MovieSchedule> findByPartyIdAndAddressId(String partyId, String addressId) {
        return null;
    }

    @Override
    public MovieSchedule insertMovieSchedule(MovieSchedule movieSchedule) {
        return null;
    }

    @Override
    public MovieSchedule updateMovieSchedule(MovieSchedule movieSchedule) {
        return null;
    }
}
