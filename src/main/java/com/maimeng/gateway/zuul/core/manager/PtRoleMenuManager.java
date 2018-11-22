package com.maimeng.gateway.zuul.core.manager;

import com.maimeng.gateway.zuul.config.cache.RoleMenuCache;
import com.maimeng.gateway.zuul.core.model.PtMenu;
import com.maimeng.gateway.zuul.core.model.PtRole;
import com.maimeng.gateway.zuul.core.model.PtRoleMenu;
import com.maimeng.gateway.zuul.core.repository.PtMenuRepository;
import com.maimeng.gateway.zuul.core.repository.PtMenuRoleRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuweifeng wrote on 2018/1/11.
 */
@Component
public class PtRoleMenuManager {
    @Resource
    private PtMenuRoleRepository ptMenuRoleRepository;
    @Resource
    private RoleMenuCache roleMenuCache;
    @Resource
    private PtMenuRepository ptMenuRepository;


    public List<PtMenu> findByRoleId(Long roleId) {
        //读缓存
        List<PtMenu> menuList = roleMenuCache.findMenuByRoleId(roleId);
        if (menuList != null) {
            return menuList;
        }
        List<PtRoleMenu> menuRoles = ptMenuRoleRepository.findByRoleId(roleId);
        menuList = menuRoles.stream().map(ptRoleMenu -> ptMenuRepository.getOne(ptRoleMenu.getMenuId())).collect
                (Collectors.toList());
        roleMenuCache.saveMenusByRoleId(roleId, menuList);
        return menuList;
    }

    public List<PtMenu> findAllMenuByRoles(List<PtRole> roles) {
        List<PtMenu> menus = new ArrayList<>();
        for (PtRole role : roles) {
            menus.addAll(findByRoleId(role.getId()));
        }
        return menus;
    }
}
