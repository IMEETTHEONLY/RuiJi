package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DIshFlavorService;
import com.itheima.reggie.service.DIshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//菜品管理
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DIshService dIshService;

    @Autowired
    private DIshFlavorService dIshFlavorService;

    @Autowired
    private CategoryService categoryService;


    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增的的数据:{}",dishDto.toString());


        dIshService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    //菜品分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器
        Page<Dish> pageinfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();

        //添加过滤条件  即sql语句中的name=name    Dish::getName这种中是数据库里面的字段
        queryWrapper.like(name!=null,Dish::getName,name);

        //添加排序条件,根据跟新时间降序排
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dIshService.page(pageinfo,queryWrapper);


//       将除了Dish以外的所有数据封装到DishDto，因为它有统计数量哪些
        BeanUtils.copyProperties(pageinfo,dishDtoPage,"records");  //前面是源数据,忽略recoard因为这个recorad需要字节构造含有categoryname
        //获取查询出来的dish列表
        List<Dish> records=pageinfo.getRecords();

        //构造一个dishito完成所有数据封装(包含categoryName的封装)
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //将每一条遍历的dish数据赋值给dishdto
            BeanUtils.copyProperties(item,dishDto);

            Long categoryid=item.getCategoryId();  //得到分类id
            //查询分类对象,调用分类表查询分类名字
            Category category=categoryService.getById(categoryid);
           if(category!=null){
               //得到分类的名称
               String categoryName=category.getName();

               //将categoryName加入dishDto
               dishDto.setCategoryName(categoryName);
           }

            return dishDto;
        }).collect(Collectors.toList());


        //分装dishDto的records
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //根据id查询dish和对应的口味信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        //调用自己写的方法，查询两张表
        DishDto dishDto=dIshService.getByIdWithFlavor(id);


        return R.success(dishDto);
    }


    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("新增的的数据:{}",dishDto.toString());


        dIshService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

//
//    //根据条件查询所有菜品
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        //查询categoryid为前端传递的，并且状态为启售即为1的菜品信息
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId()).eq(Dish::getStatus,1);
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list=dIshService.list(queryWrapper);
//        return R.success(list);
//    }





    //根据条件查询所有菜品,升级版可以返回口味信息
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //查询categoryid为前端传递的，并且状态为启售即为1的菜品信息
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId()).eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list=dIshService.list(queryWrapper);




        //构造一个dishito完成所有数据封装(包含categoryName的封装)
        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //将每一条遍历的dish数据赋值给dishdto
            BeanUtils.copyProperties(item,dishDto);

            Long categoryid=item.getCategoryId();  //得到分类id
            //查询分类对象,调用分类表查询分类名字
            Category category=categoryService.getById(categoryid);
            if(category!=null){
                //得到分类的名称
                String categoryName=category.getName();

                //将categoryName加入dishDto
                dishDto.setCategoryName(categoryName);
            }



            //追加口味信息
            //获取菜品id
            Long dishId=item.getId();

                LambdaQueryWrapper<DishFlavor>  lambdaQueryWrapper=new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

            List<DishFlavor> dishFlavorList=dIshFlavorService.list(lambdaQueryWrapper);
            //将每个菜品的口味信息加上，item基础信息
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());




        return R.success(dishDtoList);
    }

    //修改状态信息
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("修改的status值:{},ids:{}",status,ids);
        ids.stream().map((item)->{
            //从原数据库中读取需要修改的数据
           Dish dish=dIshService.getById(item);
           //设置新的status
           dish.setStatus(status);
           dIshService.updateById(dish);
            return dish;
        }).collect(Collectors.toList());
        return R.success("账号状态更改成功");
    }


    //批量删除
    @DeleteMapping()
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("批量删除");
        dIshService.removeByIds(ids);

        return R.success("删除菜品成功");
    }

}


