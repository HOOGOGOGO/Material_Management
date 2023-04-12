package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.BaseContext;
import com.rblue.common.R;
import com.rblue.entity.Category;
import com.rblue.entity.Manager;
import com.rblue.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user")
public class ManagerController {

    @Autowired
    private ManagerService service;

    /**
     * 员工登录
     *
     * @param request
     * @param manager
     * @return
     */

    @PostMapping("/login")
    public R<Manager> login(HttpServletRequest request, @RequestBody Manager manager) {
        String username = manager.getUsername();
        String password = manager.getPassword();
        String job = manager.getJob();

        //2、根据username查询是否有此用户
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<Manager>();
        queryWrapper.eq(Manager::getUsername, username);//相等的条件
        queryWrapper.eq(Manager::getJob, job);//相等的条件
        Manager man = service.getOne(queryWrapper);
        //3、是否被禁用
        if (man == null) {//没查到
            return R.error("登陆失败，用户不存在");
        }
        //4、查到了，比对密码
        if (!man.getPassword().equals(password)) {
            return R.error("登陆失败，密码错误");
        }
        //5、是否被禁用
        if (man.getStatus() == 0) {//被禁用了
            return R.error("登陆失败，账号已禁用");
        }
        //6、登陆成功
        request.getSession().setAttribute("manager", man);
        return R.success(man);
    }

    /**
     * 查询所有用户信息
     *
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> SelectAll(String name, int pageSize, int page) {
        //创建page构造器
        Page<Manager> managerPage = new Page<>(page, pageSize);
        //创建条件构造器
        LambdaQueryWrapper<Manager> lwq = new LambdaQueryWrapper<>();
        lwq.eq(Manager::getIsDeleted,0);
        //当name不为空时执行
        lwq.like(name != null, Manager::getName, name);
        //添加排序，按照职务排序
        lwq.orderByAsc(Manager::getJob);
        service.page(managerPage, lwq);

        log.info("查询结果为：" + managerPage);
        return R.success(managerPage);
    }


    /**
     * 添加用户
     *
     * @param manager
     * @return
     */
    @PostMapping
    public R<String> addManager(@RequestBody Manager manager) {
        if (manager != null) {

            service.save(manager);
        }
        log.info(String.valueOf(manager));
        return R.success("添加成功");
    }

    /**
     * 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Manager> selectById(@PathVariable Long id) {
        log.info("当前id:" + id);
        //条件构造器
        LambdaQueryWrapper<Manager> lqw = new LambdaQueryWrapper<>();
        //添加条件
        lqw.eq(Manager::getId, id);
        lqw.orderByAsc(Manager::getCreateTime);
        Manager manager = service.getOne(lqw);
        log.info("当前查找用户信息为：" + manager);
        return R.success(manager);
    }
    /**
     * 修改
     *
     * @param manager
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Manager manager) {
        if(manager!=null){
            //更新用户
            service.updateById(manager);
        }
        return R.success("修改成功");
    }
    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        //遍历ids单个修改is——deleted为1
        for (Long id:ids){
            if(id!=null){
                Manager manager=new Manager();
                manager.setId(id);
                manager.setIsDeleted(1);
                service.updateById(manager);
            }
        }
        log.info(String.valueOf(ids));
        return R.success("删除成功");
    }
    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        log.info("正在退出中......");
        //将session中的内容删除
        request.getSession().removeAttribute("manager");
        return R.success("退出成功");
    }

    /**
     * 获取仓库管理员列表
     * @return
     */
    @GetMapping("/list")
    public R<List<Manager>> getManager(){
        //创建构造器
        LambdaQueryWrapper<Manager> lqw=new LambdaQueryWrapper<>();
        //获取job为仓库管理员的
        lqw.eq(Manager::getJob,1);
        //查询
       List<Manager> lists=service.list(lqw);
        return R.success(lists);
    }
}
