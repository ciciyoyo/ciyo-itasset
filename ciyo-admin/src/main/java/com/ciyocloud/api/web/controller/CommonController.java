package com.ciyocloud.api.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import com.ciyocloud.common.util.AsyncProcessUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.storage.cloud.OssStorageFactory;
import com.ciyocloud.storage.util.MimeTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 通用接口
 *
 * @author : smalljop
 * @description : 通用
 * @create :  2021/06/08 15:32
 **/
@RestController
@RequestMapping("/common/")
@RequiredArgsConstructor
public class CommonController {


    /**
     * 获取异步处理进度
     *
     * @param key 唯一标识
     */
    @GetMapping("/process")
    public Result getProcess(@RequestParam String key) {
        return Result.success(AsyncProcessUtils.getProcess(key));
    }


    /**
     * 上传
     *
     * @param file 文件
     * @return url
     */
    @PostMapping("/upload")
    public Result avatar(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "fileType", required = false, defaultValue = "DEFAULT") String fileType) throws IOException {
        if (!file.isEmpty()) {
            OssStorageFactory.checkAllowedExtension(file, MimeTypeUtils.MimeTypeEnum.valueOf(fileType).getExtensions());
            String path = IdUtil.simpleUUID() +
                    CharUtil.DOT +
                    FileUtil.extName(file.getOriginalFilename());
            String url = OssStorageFactory.getStorageService().upload(file.getInputStream(), path);
            return Result.success(url);
        }
        return Result.failed("{sys.upload.error}");
    }


}
