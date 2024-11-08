package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //员工登入
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的password进行md5加密处理
        String password=employee.getPassword();
        //对password进行md5加密,md5DigestAsHex的参数是byte[]，所以说要将string转换
        password=DigestUtils.md5DigestAsHex(password.getBytes());


        //根据用户名查询查数据库
        //构造查询条件
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<Employee>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());

        //2.再调用service的方法,mp提供的方法，用户名不能重复，所以说查询查来的只有一个
       Employee emp= employeeService.getOne(queryWrapper);


        //3.判断是否查询到，如果说没有查询到就返回错误
        if(emp==null){
            return R.error("登录失败");
        }

        //4.比对密码,如果不一致返回登录失败
        if(!emp.getPassword().equals(password)){
            return R.error("登入失败");
        }

        //5.查看员工的工作状态，如果为禁用，则返回员工已禁用的结果
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }

        //6.登入成功,将员工id存入ssesion并返回
        request.getSession().setAttribute("employee",emp.getId());
        //将员工数据返回
        return R.success(emp);
    }




    //页面退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session当中保存的当前员工的id
        request.getSession().removeAttribute(("employee"));
        return R.success("退出成功");
    }

    //员工增加
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){

        log.info("新增员工，员工信息:{}",employee);

        //给管理员设置默认密码123456,需要进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        //设置时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

//        //获取当前用户登录的id
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        //设置当前用户的操作人和更新人
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        //调用mybatis-plus的save方法进行保存
        employeeService.save(employee);



        return R.success("新增员工成功");

    }

    //分页查询

    @GetMapping("/page")
    public  R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //使用Page的原因是mybatisplus封装了Page属性，里面是返回的数据包装。
        Page pageInfo=new Page(page,pageSize);


        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加过滤条件,使用模糊查询是否存在含有name的字段的name,查询zhangsan，那么zhangsan1，zhangsan123都会出现
        queryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        //添加一个排序条件,根据更新时间
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询,mybatisplus会在service自动将数据封装在page里面
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    //员工修改（通用的无论是修改状态还是全部员工数据）
    //前端传递的是json就要加RequestBody
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

//        long id=Thread.currentThread().getId();
//        log.info("线程id为:{}",id);

//        employee.setUpdateTime(LocalDateTime.now());
//        //通过session获取到存储到前端当前用户的id
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);  //mybatisplus分装的方法
        return R.success("员工信息修改成功");
    }

    //根据id查询内容
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }


}
