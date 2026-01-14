package com.ciyocloud.storage.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.storage.cloud.OssStorageFactory;
import com.ciyocloud.storage.entity.SysFileEntity;
import com.ciyocloud.storage.service.SysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 * 系统文件控制器
 *
 * @author ciyo
 */
@Slf4j
@RestController
@RequestMapping("/storage/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService sysFileService;

    @PostMapping("/upload")
    public Result<SysFileEntity> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.failed("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = FileNameUtil.extName(originalFilename);
        if (StrUtil.isBlank(fileExt)) {
            fileExt = "";
        }

        // 生成存储文件名 (UUID)
        String fileName = IdUtil.simpleUUID() + (StrUtil.isNotEmpty(fileExt) ? "." + fileExt : "");

        // 构建存储路径: yyyy/MM/dd/uuid.ext
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
        String filePath = datePath + "/" + fileName;

        // 上传文件
        String fileUrl = OssStorageFactory.getStorageService().upload(file.getInputStream(), filePath);

        // 保存文件信息
        SysFileEntity sysFile = new SysFileEntity();
        sysFile.setOriginName(originalFilename);
        sysFile.setFileName(fileName);
        sysFile.setFileExt(fileExt);
        sysFile.setContentType(file.getContentType());
        sysFile.setFileSize(file.getSize());
        sysFile.setFilePath(filePath);
        sysFile.setFileUrl(fileUrl);
        // 默认临时文件，业务调用方负责更新为非临时
        sysFile.setIsTemp(1);
        sysFile.setStatus(1); // 正常
        // 默认过期时间，例如 1 天后过期，如果业务没确认则会被清理（需配合定时任务，这里先设置一个默认值或不设置）
        // sysFile.setExpireTime(LocalDateTime.now().plusDays(1));

        sysFileService.saveFile(sysFile);

        return Result.success(sysFile);
    }
}
