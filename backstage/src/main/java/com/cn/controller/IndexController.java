package com.cn.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import com.cn.ManagerService;
import com.cn.RoleService;
import com.cn.entity.Manager;
import com.cn.entity.Permission;
import com.cn.entity.Role;
import com.cn.pojo.ManagerDetail;
import com.cn.pojo.ValidateCode;
import com.cn.util.MenuUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.security.Principal;
import java.util.*;

/**
 * 后台首页控制器
 *
 * @author chen
 * @date 2018-01-02 17:26
 */
@Controller
@AllArgsConstructor
public class IndexController {
    private final ManagerService managerService;
    private final RoleService roleService;

    /**
     * 若加载了data rest包 则默认根目录为 所有接口link
     * 这里必须得配置个 / 不然所有静态文件全部不起作用
     * 只有security才这样 shiro正常 原因未知
     */
    @RequestMapping("/")
    public String index(Principal principal, Model model) {
        Authentication authentication = (Authentication) principal;
        ManagerDetail managerDetail = (ManagerDetail) authentication.getPrincipal();
        Set<Role> roleList = managerDetail.getRoleList();
        boolean init = false;
        List<Permission> menuList = new ArrayList<>();
        if (!"admin".equals(principal.getName())) {
            roleList.forEach(role -> menuList.addAll(role.getPermissions()));
        } else {
            //管理员拥有最高权限
            List<Role> roles = roleService.getAllRole();
            List<Permission> permissions = roleService.getPermissionList();
            // 得到当前的认证信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            //  生成当前的所有授权
            Collection<GrantedAuthority> updatedAuthorities = new HashSet<>(auth.getAuthorities());
            // 添加 授权
            roles.forEach(role -> updatedAuthorities.add(new SimpleGrantedAuthority(role.getRoleCode())));
            permissions.forEach(permission -> updatedAuthorities.add(new SimpleGrantedAuthority(permission.getPurview())));
            // 生成新的认证信息
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
            // 重置认证信息
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            menuList.addAll(permissions);
        }
        if(managerDetail.getState()==Manager.STATE_INITIALIZE){
            init = true;
            Manager manager = managerService.getManagerById(managerDetail.getId());
            manager.setState(Manager.STATE_NORMAL);
            managerService.updateManager(manager);
        }
        model.addAttribute("init",init);
        model.addAttribute("manager",managerService.getManagerById(managerDetail.getId()));
        model.addAttribute("menuList", MenuUtil.makeTreeList(menuList));
        return "index";
    }

    /**
     * 登录页面
     *
     * @return login.html
     */
    @RequestMapping("/login")
    public String login(@RequestParam(required = false)String error,Model model) {
        model.addAttribute("error",error);
        return "login";
    }

    /**
     * kaptcha 验证码
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/kaptcha")
    public void defaultKaptcha(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        ICaptcha captcha = CaptchaUtil.createGifCaptcha(116, 36, 4);
        ValidateCode validateCode = new ValidateCode(captcha.getCode(),60);
        //生产验证码字符串并保存到session中
        request.getSession().setAttribute("validateCode", validateCode);
        //图形验证码写出，可以写出到文件，也可以写出到流
        try (OutputStream out = response.getOutputStream()){
            captcha.write(out);
        }
    }

    /**
     * 主页
     */
    @RequestMapping("/home")
    public String home(){
        return "home";
    }

    /**
     * 云中遨游
     */
    @RequestMapping("/clouds")
    public String clouds(){
        return "other/clouds";
    }

}
