package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController  {
    @Autowired
    private UserService userService;


    //发送手机短信验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
            //获取手机号
            String phone=user.getPhone();
            //判断phone是否为空
            if(StringUtils.hasText(phone)){

                //生成随机的4为验证码,调用工具类（黑马提供的）
             String code=ValidateCodeUtils.generateValidateCode(4).toString();
             //查看生成的验证码，知道信息验证流程就可以了
             log.info("code:{}",code);
                //调用阿里云提供的短信服务API完成发送短信,但是我没有签名（私人不能申请），就不调用了
                //SMSUtils.sendMessage();
                //保存验证码到session，校验用户提交的验证码，键值对，根据phone来存取
                session.setAttribute(phone,code);

                return R.success("手机验证码发送成功");
            }


        return R.error("手机验证码发送失败");
    }



    //移动端用户登录
    @PostMapping("/login")
                        //用于接收前端的json数据
    public R<User> login(@RequestBody Map map , HttpSession session){

        log.info(map.toString());
        //获取手机号
        String phone=map.get("phone").toString();

        //获取验证码
        String code=map.get("code").toString();
        //从session中获取保存的验证码
        Object codeInSession=session.getAttribute(phone);
        //验证码比对(页面提交的验证码和session的验证码对比)
        if(codeInSession!=null&&codeInSession.equals(code)){
            //如果能够对比成功，登录成功


            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
           User user= userService.getOne(queryWrapper);
            if(user==null){
                //判断当前手机号的用户是否为新用户，如果说是新用户就自动完成注册
                user=new User();
                user.setPhone(phone);
                //设置状态啊
                user.setStatus(1);
                userService.save(user);
            }
            //将当前用户的id存入session
            session.setAttribute("user",user.getId());
            return R.success(user);
        }



        return R.error("登录失败");
    }

}
