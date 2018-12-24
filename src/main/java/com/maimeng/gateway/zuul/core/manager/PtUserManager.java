package com.maimeng.gateway.zuul.core.manager;

import com.maimeng.gateway.zuul.core.model.PtUser;
import com.maimeng.gateway.zuul.core.repository.PtUserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wuweifeng wrote on 2018/10/30.
 */
@Service
public class PtUserManager {
    @Resource
    private PtUserRepository ptUserRepository;

    /**
     * 根据account查询User
     *
     * @param account
     *         账号名
     * @return 用户
     */
    public PtUser findByAccount(String account) {
        return ptUserRepository.findByAccount(account);
    }

    public PtUser find(Long id) {
        return ptUserRepository.getOne(id);
    }
}
