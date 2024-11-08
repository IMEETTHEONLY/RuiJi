package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DIshFlavorService;
import com.itheima.reggie.service.DIshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DIshServiceImpl extends ServiceImpl<DishMapper, Dish> implements DIshService {
    @Autowired
    private DIshFlavorService dIshFlavorService;
    //新增菜品，插入菜品同时需要增加口味数据，需要操作两张表:dish  dish_flavor
    @Override
    @Transactional  //开启事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish,因为继承了dish
        this.save(dishDto);

        //获取菜品的id，因为你在存储dish_flavor时也需要id，这个id是操作两张表的桥梁
        Long id=dishDto.getId();

        List<DishFlavor> flavors=dishDto.getFlavors();

        //为DishFlavor赋值dishID,通过stream的方式为每一个DishFlavor对象赋值dishid
        flavors=flavors.stream().map((item) ->{
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());  //这里的意思是重新转换为集合的形式


        //保存菜品口味到菜品口味dish_flavor
        dIshFlavorService.saveBatch(flavors);

    }

    @Override
    //根据id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish=this.getById(id);

        DishDto dishDto=new DishDto();
        //将菜品基本信息考入到new出来的dishdto.
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应的口味信息，从dish_flavor查询
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        //调用dishflavor的list方法
        List<DishFlavor> flavors=dIshFlavorService.list(queryWrapper);

        //给new出来的dishdto再加上flavors
        dishDto.setFlavors(flavors);


        return dishDto;
    }

    //修改菜品信息，同时更新口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据   --dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dIshFlavorService.remove(queryWrapper);

        //添加提交过来的口味数据     --dish_flavor表的insert操作
       List<DishFlavor> flavors= dishDto.getFlavors();

        //为DishFlavor赋值dishID,通过stream的方式为每一个DishFlavor对象赋值dishid
        flavors=flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());  //这里的意思是重新转换为集合的形式

       dIshFlavorService.saveBatch(flavors);

    }
}
