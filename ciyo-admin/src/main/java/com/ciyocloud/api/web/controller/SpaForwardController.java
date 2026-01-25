//package com.ciyocloud.api.web.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//public class SpaForwardController {
//
//    @Value("${platform.api-prefix:/api}")
//    private String apiPrefix;
//
//    /**
//     * 所有非 API、非静态资源请求，兜底到 index.html
//     */
//    @RequestMapping("/**")
//    public String forward(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//
//        // 1️⃣ 排除 API 请求
//        if (uri.startsWith(apiPrefix)) {
//            return null; // 交给后端 Controller 或返回 404
//        }
//
//        // 2️⃣ 排除静态资源
//        if (uri.contains(".")) {
//            return null; // 资源请求交给 ResourceHandler
//        }
//
//        // 3️⃣ 兜底 SPA
//        return "forward:/index.html";
//    }
//}
