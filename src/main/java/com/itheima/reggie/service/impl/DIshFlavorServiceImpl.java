package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DIshFlavorMapper;
import com.itheima.reggie.service.DIshFlavorService;
import com.itheima.reggie.service.DIshService;
import org.springframework.stereotype.Service;

@Service
public class DIshFlavorServiceImpl extends ServiceImpl<DIshFlavorMapper, DishFlavor> implements DIshFlavorService {
}
