package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
* 检查用户是否完成登录*/
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*") //配置访问路径
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;  //强制转换成httpservlet类型的request
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        //1.获取本次请求的url
        String requestURL=request.getRequestURI();
        log.info("拦截到请求:{}",requestURL);

        //2.判断本次请求是否需要处理
        //定义不需要请求的路径
        String[] urls=new String[]{
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/common/**",
           "/user/sendMsg",  //移动端发送短信
           "/user/login"   //移动端登录
        };

        //调用方法匹配路径,判断是否需要处理
        boolean check=check(urls,requestURL);

        //3.如果说不需要处理，直接放行
        if(check){
            log.info("拦截到请求:{}不需要处理",requestURL);
            //放行
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.需要处理：判断登录状态，如果已登录，直接放行   --判断employee
            if(request.getSession().getAttribute("employee")!=null){
                //request.getSession().getAttribute("employee")的值就为id
                log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("employee"));

                //向自己写的basecontext类中，加入线程的公共值
                Long empId=(Long) request.getSession().getAttribute("employee");
                BaseContext.setCurrentId(empId);

//                long id=Thread.currentThread().getId();
//                log.info("线程id为:{}",id);
                //放行
                filterChain.doFilter(request,response);
                return;
            }

        //4-2.需要处理：判断登录状态，如果已登录，直接放行  --判断user
        if(request.getSession().getAttribute("user")!=null){
            //request.getSession().getAttribute("employee")的值就为id
            log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("user"));

            //向自己写的basecontext类中，加入线程的公共值(类似于静态变量)
            Long userId=(Long) request.getSession().getAttribute("user");
            //用于绑定当前用户的id，存储起来，后面使用就可以通过该方法取出
            BaseContext.setCurrentId(userId);

//                long id=Thread.currentThread().getId();
//                log.info("线程id为:{}",id);
            //放行
            filterChain.doFilter(request,response);
            return;
        }



        //5.如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        //因为前端需要的是json数据不能直接用对象，所以说要返回json
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));  //返回结果，前端在request.js里面获取
        return;


    }

    //路径匹配，检查本次请求是否需要放行
    public boolean check(String[] urls,String requestURL){
        for (String url : urls) {
            boolean match=PATH_MATCHER.match(url,requestURL);
                    if(match){
                        return true;
                    }
        }
        return false;
    }
}
