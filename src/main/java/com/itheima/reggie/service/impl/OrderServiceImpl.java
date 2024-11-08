package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements  OrderService{

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    //用户下单
    @Override
    @Transactional   //操控多张表
    public void submit(Orders orders) {
        //1.获得当前用户的id
        Long userID= BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userID);
       List<ShoppingCart> shoppingCarts= shoppingCartService.list(queryWrapper);

        if(shoppingCarts==null||shoppingCarts.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }
        //2.查询用户的信息
       User user= userService.getById(userID);

        //3.查询地址信息
        //获取从前端传递过来的地址id
        Long addressBookId=orders.getAddressBookId();
        AddressBook addressBook=addressBookService.getById(addressBookId);

        if(addressBook==null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //4.向订单表插入数据，插入一条数据（一个单子一条数据）



        //订单号
        Long orderId= IdWorker.getId();
        //累加变量进行操作
        AtomicInteger amount=new AtomicInteger(0);
        //计算总金额
       List<OrderDetail> orderDetails= shoppingCarts.stream().map((item)->{
           OrderDetail orderDetail=new OrderDetail();
           //将item里面的数据封装到orderDetail里面去
           orderDetail.setOrderId(orderId);
           orderDetail.setNumber(item.getNumber());  //数量
           orderDetail.setDishFlavor(item.getDishFlavor());
           orderDetail.setDishId(item.getDishId());
           orderDetail.setSetmealId(item.getSetmealId());
           orderDetail.setName(item.getName());
           orderDetail.setImage(item.getImage());
           orderDetail.setAmount(item.getAmount()); //单份金额
           //调用new出来的amount的方法
           amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userID);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());  //收货人
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);

        //5.向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //6.清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}
