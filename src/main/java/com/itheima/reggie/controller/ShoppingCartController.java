package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId=BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者是套餐，是否在购物车中
        Long dishId=shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        if(dishId!=null){
            //添加到购物车的是菜品
           queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        else{
            //添加搭配购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前菜品或者是套餐是否在购物车，如果说不在就要加入，否则num++
        ShoppingCart cartServiceOne=shoppingCartService.getOne(queryWrapper);

        //如果已经存在，就在原来数量基础上加一
        if(cartServiceOne!=null){
            Integer nunmber=cartServiceOne.getNumber();
            cartServiceOne.setNumber(nunmber+1);
            //将新的ShoppingCart更新进去
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不存在，则添加到购物车，数量默认是一
            shoppingCart.setNumber(1);
            //设置入库时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //因为这里是新加入所以说查出来的肯定是空，直接将新构造的shoppingcaart赋值即可，方便统一返回
            cartServiceOne=shoppingCart;

        }



        return R.success(cartServiceOne);
    }

    @GetMapping("/list")
    //购物车列表展示
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //查询某用户的所有信息
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        //排序条件
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list=shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");


    }

    //购物车减少数量
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){


        log.info("购物车数量减1:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId=BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者是套餐，是否在购物车中
        Long dishId=shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        if(dishId!=null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        else{
            //添加搭配购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前菜品或者是套餐是否在购物车，如果说不在就要加入，否则num++
        ShoppingCart cartServiceOne=shoppingCartService.getOne(queryWrapper);

       //此时已经查询到需要减少的数据
        if(cartServiceOne.getNumber()>1){
            //此时只需要将数量减1即可
         Integer number=cartServiceOne.getNumber();
         number--;
         //将修改的数据保存
            cartServiceOne.setNumber(number);
            shoppingCartService.updateById(cartServiceOne);
        }
        else{
            //此时已经是此数据的最后一条，再减1就删除该菜品
            shoppingCartService.removeById(cartServiceOne);
        }



        return R.success(cartServiceOne);


    }
}
