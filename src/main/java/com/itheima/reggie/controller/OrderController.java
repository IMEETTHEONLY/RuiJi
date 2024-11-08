package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    //用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);
        //调用自定义的方法
        orderService.submit(orders);

        return R.success("下单成功");
    }

    //分页查询订单信息
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("分页查询");
            //分页构造器
        Page<Orders> pageInfo=new Page<>(page,pageSize);

            //条件构造器，添加排序条件
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(Orders::getOrderTime);
        //调用page方法查询
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
}
