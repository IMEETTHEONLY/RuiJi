package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DIshService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DIshService dIshService;
    @Autowired
    private SetmealService setmealService;

    //根据id删除分类，删除前判断当前分类是否关联菜品，如果关联了删除失败
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品，如果说已经关联，抛出一个业务异常,就是在菜品表里面去查是否存在当前分类id
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1=dIshService.count(dishLambdaQueryWrapper);
        if(count1>0){
            //已经关联了菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询当前分类是否关联了套餐，如果说已经关联，抛出一个业务异常
      LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
       int count2= setmealService.count(setmealLambdaQueryWrapper);

        if(count2>0){
            //已经关联了套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除分类,调用ServiceImpl的方法删除
        removeById(id);
    }
}
