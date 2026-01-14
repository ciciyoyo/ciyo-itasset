package com.ciyocloud.generator.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.generator.entity.GenTableColumnEntity;
import com.ciyocloud.generator.entity.GenTableEntity;
import com.ciyocloud.generator.service.GenTableService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@ConditionalOnProperty(name = "platform.gen.enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/gen")
public class GenController {

    private final GenTableService genTableService;

    /**
     * 查询代码生成列表
     */
    @GetMapping("/list")
    public Result<Page<GenTableEntity>> genList(GenTableEntity genTable, Page pageQuery) {
        return Result.success(genTableService.pageGenTableList(genTable, pageQuery));
    }

    /**
     * 修改代码生成业务
     *
     * @param tableId 表ID
     */
    @GetMapping(value = "/{tableId}")
    public Result<Map<String, Object>> getInfo(@PathVariable Long tableId) {
        GenTableEntity table = genTableService.selectGenTableById(tableId);
        List<GenTableEntity> tables = genTableService.selectGenTableAll();
        List<GenTableColumnEntity> list = genTableService.getGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return Result.success(map);
    }

    /**
     * 查询数据库列表
     */
    @GetMapping("/db/list")
    public Result<Page> dataList(GenTableEntity genTable, Page pageQuery) {
        return Result.success(genTableService.pageDbTableList(genTable, pageQuery));
    }

    /**
     * 查询数据表字段列表
     *
     * @param tableId 表ID
     */
    @GetMapping(value = "/column/{tableId}")
    public Result<Page> columnList(Long tableId) {
        List<GenTableColumnEntity> list = genTableService.getGenTableColumnListByTableId(tableId);
        Page<GenTableColumnEntity> page = new Page<>();
        page.setRecords(list);
        page.setTotal(list.size());
        return Result.success(page);
    }

    /**
     * 导入表结构（保存）
     *
     * @param tables 表名串
     */
    @PostMapping("/importTable")
    public Result<Void> importTableSave(String tables) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTableEntity> tableList = genTableService.selectDbTableListByNames(tableNames);
        genTableService.importGenTable(tableList);
        return Result.success();
    }

    /**
     * 修改保存代码生成业务
     */
    @PutMapping
    public Result<Void> editSave(@Validated @RequestBody GenTableEntity genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return Result.success();
    }

    /**
     * 删除代码生成
     *
     * @param tableIds 表ID串
     */
    @DeleteMapping("/{tableIds}")
    public Result<Void> remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return Result.success();
    }

    /**
     * 预览代码
     *
     * @param tableId 表ID
     */
    @GetMapping("/preview/{tableId}")
    public Result<Map<String, String>> preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return Result.success(dataMap);
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableName 表名
     */
    @GetMapping("/download/{tableName}")
    public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableName 表名
     */
    @GetMapping("/genCode/{tableName}")
    public Result<Void> genCode(@PathVariable("tableName") String tableName) {
        genTableService.generatorCode(tableName);
        return Result.success();
    }

    /**
     * 同步数据库
     *
     * @param tableName 表名
     */
    @GetMapping("/synchDb/{tableName}")
    public Result<Void> synchDb(@PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return Result.success();
    }

    /**
     * 批量生成代码
     *
     * @param tables 表名串
     */
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = Convert.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"ciyo.zip\"");
        response.addHeader("Content-Length", String.valueOf(data.length));
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }
}
