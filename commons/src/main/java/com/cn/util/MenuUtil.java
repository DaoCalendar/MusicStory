package com.cn.util;

import com.cn.pojo.TreeVO;
import com.cn.entity.Permission;

import java.util.*;

/**
 * 菜单工具类
 * @author ngcly
 * @date 2018-01-02 17:54
 */
public class MenuUtil {
    private MenuUtil(){}

    static List<Permission> resultList;

    /**
     * 根据条件将菜单转成Tree对象
     */
    public static Set<TreeVO> makeTreeList(List<Permission> originMenus){
        Set<TreeVO> trees = new LinkedHashSet<>();
        TreeVO tree1;
        for(Permission permission:originMenus){
            if(Permission.RESOURCE_MENU.equals(permission.getResourceType())){
                tree1 = new TreeVO(permission.getId(),permission.getName(),permission.getParentId(),permission.getUrl(),false,permission.getIcon());
                trees.add(tree1);
            }
        }
        return eachTree(trees);
    }

    /**
     * 判断菜单是否被勾选
     * @param originMenus 原菜单
     * @param roleMenus   角色对应菜单
     */
    public static Set<TreeVO> makeTreeList(List<Permission> originMenus, List<Permission> roleMenus){
        Set<TreeVO> trees = new HashSet<>();
        TreeVO tree1;
        boolean contained;
        for(Permission sysPermission:originMenus){
            contained=false;
            for(Permission rolePermission:roleMenus){
                if(sysPermission.getId().equals(rolePermission.getId())){
                    contained=true;
                    break;
                }
            }
            if(contained){
                tree1 = new TreeVO(sysPermission.getId(),sysPermission.getName(),sysPermission.getParentId(),sysPermission.getUrl(),true,sysPermission.getIcon());
            }else {
                tree1 = new TreeVO(sysPermission.getId(),sysPermission.getName(),sysPermission.getParentId(),sysPermission.getUrl(),false,sysPermission.getIcon());
            }
            trees.add(tree1);
        }
        return eachTree(trees);
    }

    /**
     * 将已转成Tree对象的list进行转换成树状
     */
    public static Set<TreeVO> eachTree(Set<TreeVO> trees){
        Set<TreeVO> rootTrees = new HashSet<>();
        for (TreeVO tree : trees) {
            if(tree.getParentId() == 0){
                rootTrees.add(tree);
            }
            for (TreeVO t : trees) {
                if(t.getParentId().equals(tree.getId())){
                    if(tree.getChildren() == null){
                        List<TreeVO> myChildrenList = new ArrayList<>();
                        myChildrenList.add(t);
                        tree.setChildren(myChildrenList);
                    }else{
                        tree.getChildren().add(t);
                    }
                }
            }
        }
        return rootTrees;
    }

    /**
     * 将菜单根据树状排序
     */
    public static List<Permission> treeOrderList(List<Permission> sysPermissions){
        resultList = new ArrayList<>();
        sortList(sysPermissions,0);
        return resultList;
    }

    public static void sortList(List<Permission> list,long id){
        for(Permission permission:list){
            if(permission.getParentId() == id){
                resultList.add(permission);
                sortList(list,permission.getId());
            }
        }
    }
}
