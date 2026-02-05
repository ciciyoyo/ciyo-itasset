# Excel 导入导出实现指南

> 本文档提供完整的 Excel 导入导出实现方案，包括实体配置、后端实现、前端集成。可直接提供给 AI 辅助开发。

## 快速开始

系统使用 EasyExcel + 自定义注解实现 Excel 导入导出，支持：
- ✅ 自动生成带示例的导入模板
- ✅ 枚举类型自动转换
- ✅ SSE 实时进度推送
- ✅ 灵活的字段控制

## 一、核心注解

### @ExcelTemplate（类级别）
定义模板元信息
```java
@ExcelTemplate(code = "offering", sheetName = "服务导入模板")
public class OfferingEntity extends SysBaseEntity { }
```

### @ExcelProperty（字段级别）
标记 Excel 列，EasyExcel 原生注解
```java
@ExcelProperty(value = "服务名称")
private String name;

// 枚举字段需要转换器
@ExcelProperty(value = "服务状态", converter = DictEnumConvert.class)
private OfferingStatus status;
```

### @ExcelPropertyType（字段级别）
控制导入导出行为
```java
// EXPORT: 仅导出（ID、创建时间等）
@ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
private Long id;

// ALL: 导入导出都支持（默认）
@ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
private String name;
```

### @ExcelSample（字段级别）
提供示例数据
```java
// 方式1：直接指定
@ExcelSample("笔记本电脑-001")
private String name;

// 方式2：使用 Provider（推荐用于枚举）
@ExcelSample(provider = EnumSampleProvider.class)
private OfferingStatus status;
```

### @ExcelIgnoreUnannotated（类级别）
忽略未标注的字段，EasyExcel 原生注解
```java
@ExcelIgnoreUnannotated
public class OfferingEntity extends SysBaseEntity { }
```

## 二、实体类配置

### 1. Entity 配置示例

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("itam_offering")
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "offering", sheetName = "服务导入模板")
public class OfferingEntity extends SysBaseEntity {

    /** 主键 ID - 仅导出 */
    @TableId(value = "id")
    @ExcelProperty(value = "服务ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long id;

    /** 服务名称 - 导入导出 */
    @NotBlank(message = "服务名称不能为空")
    @ExcelProperty(value = "服务名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("云服务器维护")
    private String name;

    /** 供应商 ID - 仅导出 */
    @ExcelProperty(value = "供应商ID")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.EXPORT)
    private Long supplierId;

    /** 服务状态 - 枚举字段 */
    @ExcelProperty(value = "服务状态", converter = DictEnumConvert.class)
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample(provider = EnumSampleProvider.class)  // 自动获取所有枚举值
    private OfferingStatus offeringStatus;

    /** 购买日期 */
    @ExcelProperty(value = "购买日期")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("2026-01-01")
    private LocalDateTime startDate;

    /** 价格 */
    @ExcelProperty(value = "费用")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("10000.00")
    private BigDecimal cost;
}
```

### 2. VO 类配置（可选，用于关联对象）

```java
@Data
@EqualsAndHashCode(callSuper = true)
@ExcelIgnoreUnannotated
@ExcelTemplate(code = "offering", sheetName = "服务导入模板")
public class OfferingVO extends OfferingEntity {

    /** 供应商名称 - 用于导入时通过名称查找供应商 */
    @ExcelProperty(value = "供应商名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("阿里云")
    private String supplierName;

    /** 关联目标名称 */
    @ExcelProperty(value = "关联目标名称")
    @ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
    @ExcelSample("服务器-001")
    private String targetName;
}
```

### 3. 枚举类配置

枚举需实现 `IDictEnum` 接口：

```java
@Getter
@AllArgsConstructor
public enum OfferingStatus implements IDictEnum<String> {
    NORMAL("normal", "正常"),
    EXCEPTION("exception", "异常"),
    MAINTENANCE("maintenance", "维护中"),
    STOPPED("stopped", "已停止");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    @Override
    public String getValue() {
        return this.code;
    }
}
```

**说明：** 使用 `@ExcelSample(provider = EnumSampleProvider.class)` 时，示例值会自动显示为：`正常|异常|维护中|已停止`

## 三、后端实现

### 1. Service 接口

```java
public interface OfferingService extends BaseService<OfferingEntity> {
    
    /**
     * 导入服务数据
     *
     * @param inputStream      文件输入流
     * @param originalFilename 原始文件名
     * @param progressKey      前端传递的进度监听key
     * @param userId           用户 id
     */
    void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId);
}
```

### 2. Service 实现

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferingServiceImpl extends BaseServiceImpl<OfferingMapper, OfferingEntity> 
        implements OfferingService {

    @Override
    public void importData(InputStream inputStream, String originalFilename, String progressKey, Long userId) {
        try {
            log.info("开始导入服务数据，progressKey: {}, userId: {}, 文件名: {}", progressKey, userId, originalFilename);

            // 初始化进度
            SseAsyncProcessUtils.setTips("正在解析Excel文件...");

            // 创建支持SSE进度推送的导入监听器
            OfferingImportListener listener = new OfferingImportListener(progressKey, userId);
            
            // 更新进度：开始导入
            SseAsyncProcessUtils.setProcess(0, "开始导入服务数据...");
            
            //执行导入
            ExcelUtils.importExcel(inputStream, OfferingVO.class, listener);
        } catch (Exception e) {
            log.error("服务数据导入失败，progressKey: {}, 错误: {}", progressKey, e.getMessage(), e);
            SseAsyncProcessUtils.setError("导入失败: " + e.getMessage());
            throw e;
        }
    }
}
```

### 3. 导入监听器

```java
@Slf4j
public class OfferingImportListener extends SseProgressExcelListener<OfferingVO> {

    private final OfferingService offeringService;
    private final SuppliersService suppliersService;
    private final Long operatorId;

    public OfferingImportListener(String progressKey, Long userId) {
        super(progressKey, userId, false, 50, 1000);
        this.offeringService = SpringContextUtils.getBean(OfferingService.class);
        this.suppliersService = SpringContextUtils.getBean(SuppliersService.class);
        this.operatorId = userId;
    }

    @Override
    public void processData(OfferingVO offering, AnalysisContext context) {
        int currentRow = context.readRowHolder().getRowIndex() + 1;

        // 1. 验证必填字段
        if (StrUtil.isBlank(offering.getName())) {
            throw new BusinessException(String.format("第%d行 服务名称不能为空", currentRow));
        }
        if (StrUtil.isBlank(offering.getSupplierName())) {
            throw new BusinessException(String.format("第%d行 供应商名称不能为空", currentRow));
        }

        // 2. 处理关联对象：根据供应商名称查找供应商ID
        SuppliersEntity supplier = suppliersService.getOneSafe(
                new LambdaQueryWrapper<SuppliersEntity>()
                        .eq(SuppliersEntity::getName, offering.getSupplierName())
        );
        if (supplier == null) {
            throw new BusinessException(String.format("第%d行 供应商 '%s' 不存在", currentRow, offering.getSupplierName()));
        }
        offering.setSupplierId(supplier.getId());

        // 3. 设置默认值
        if (offering.getOfferingStatus() == null) {
            offering.setOfferingStatus(OfferingStatus.NORMAL);
        }

        // 4. 检查是否存在（根据业务逻辑决定新增或更新）
        OfferingEntity existing = offeringService.getOneSafe(
                new LambdaQueryWrapper<OfferingEntity>()
                        .eq(OfferingEntity::getName, offering.getName())
                        .eq(OfferingEntity::getSupplierId, offering.getSupplierId())
        );

        // 5. VO 转 Entity
        OfferingEntity entity = new OfferingEntity();
        BeanUtils.copyProperties(offering, entity);

        if (existing == null) {
            // 新增
            entity.setCreateBy(operatorId);
            entity.setDeleted(0);
            offeringService.save(entity);
        } else {
            // 根据导入数据是否有ID来决定是更新还是跳过
            if (offering.getId() != null && offering.getId().equals(existing.getId())) {
                // 更新
                entity.setUpdateBy(operatorId);
                entity.setCreateBy(existing.getCreateBy());
                entity.setCreateTime(existing.getCreateTime());
                entity.setDeleted(existing.getDeleted());
                offeringService.updateById(entity);
            } else {
                throw new BusinessException(String.format("第%d行 供应商 '%s' 下服务 '%s' 已存在",
                    currentRow, offering.getSupplierName(), offering.getName()));
            }
        }
    }
}
```

**监听器说明：**
- 继承 `SseProgressExcelListener` 自动支持 SSE 进度推送
- 构造函数参数：`progressKey`, `userId`, `isAutoCommit`, `batchSize`, `threshold`
- `processData` 方法处理每一行数据，自动批量提交

### 4. Controller 接口

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/itam/offering")
public class OfferingController {

    private final OfferingService offeringService;

    /**
     * SSE进度导入服务列表
     *
     * @param file        导入文件
     * @param progressKey 前端传递的进度监听key
     */
    @SaCheckPermission("itam:offering:import")
    @Log(title = "服务", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public Result<String> importData(MultipartFile file, @RequestParam String progressKey) throws Exception {
        Long userId = SecurityUtils.getUserId();
        
        // 异步导入
        ThreadUtil.execute(() -> {
            // 异步导入会删除文件，这里要转换到新的流
            try (var inputStream = new ByteArrayInputStream(file.getBytes())) {
                offeringService.importData(inputStream, file.getOriginalFilename(), progressKey, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        return Result.success();
    }

    /**
     * 导出服务列表
     */
    @SaCheckPermission("itam:offering:export")
    @Log(title = "服务", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(OfferingPageReq req) {
        List<OfferingVO> list = offeringService.queryListVo(req);
        ExcelUtils.exportExcel(list, "服务数据", OfferingVO.class);
    }
}
```

## 四、前端实现

### 1. 导入按钮集成

```vue
<template>
  <div class="page">
    <ElCard>
      <ArtTableHeader>
        <template #left>
          <ElSpace wrap>
            <!-- 导入按钮 -->
            <ExcelImport
              url="/itam/offering/importData"
              title="服务数据导入"
              templateCode="offering"
              v-hasPermi="['itam:offering:import']"
              @success="refreshData"
            />
            
            <!-- 导出按钮 -->
            <el-button 
              v-hasPermi="['itam:offering:export']" 
              icon="ele-Download" 
              @click="handleExport"
            >
              导出
            </el-button>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable :data="data" :columns="columns" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { exportOffering } from '@/api/itam/offering'
import { download } from '@/utils/business'
import ExcelImport from '@/components/business/excel-import/index.vue'

const data = ref([])

const refreshData = () => {
  // 刷新表格数据
}

const handleExport = async () => {
  const response = await exportOffering({})
  download(response, '服务数据')
}
</script>
```

### 2. ExcelImport 组件属性

| 属性 | 类型 | 说明 | 必填 |
|------|------|------|------|
| url | string | 导入接口地址 | 是 |
| title | string | 对话框标题 | 是 |
| templateCode | string | 模板代码（对应 @ExcelTemplate 的 code） | 是 |
| @success | function | 导入成功回调 | 否 |

## 五、关键配置说明

### 字段类型映射

| 字段类型 | ExcelPropertyType | 说明 |
|---------|-------------------|------|
| 主键 ID | EXPORT | 仅导出，导入时无需填写 |
| 外键 ID | EXPORT | 仅导出，导入时通过名称关联 |
| 关联名称 | ALL | 导入时通过名称查找 ID |
| 枚举字段 | ALL | 使用 DictEnumConvert + EnumSampleProvider |
| 普通字段 | ALL | 支持导入导出 |
| 系统字段 | 不标注 | 不参与导入导出 |

### 导入逻辑处理

1. **必填验证**：监听器中验证必填字段
2. **关联对象**：通过名称查找关联对象的 ID
3. **默认值**：设置默认值（如状态）
4. **去重判断**：根据业务规则判断新增或更新
5. **批量处理**：监听器自动批量提交，提升性能

### 常见业务场景

#### 场景1：树形结构导入
```java
// Entity 配置
@ExcelProperty(value = "父级分类名称")
@ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
@ExcelSample("IT设备")
@TableField(exist = false)
private String parentName;

// 监听器处理
if (StrUtil.isNotBlank(category.getParentName())) {
    CategoriesEntity parent = categoriesService.getOneSafe(
        new LambdaQueryWrapper<CategoriesEntity>()
            .eq(CategoriesEntity::getName, category.getParentName())
    );
    if (parent != null) {
        category.setParentId(parent.getId());
    }
}
```

#### 场景2：多对一关联
```java
// VO 配置
@ExcelProperty(value = "供应商名称")
@ExcelPropertyType(type = ExcelPropertyType.TypeEnum.ALL)
@ExcelSample("阿里云")
private String supplierName;

// 监听器处理
SuppliersEntity supplier = suppliersService.getOneSafe(
    new LambdaQueryWrapper<SuppliersEntity>()
        .eq(SuppliersEntity::getName, offering.getSupplierName())
);
if (supplier != null) {
    offering.setSupplierId(supplier.getId());
}
```

#### 场景3：更新策略
```java
// 策略1：根据 ID 更新
if (offering.getId() != null && existing != null && offering.getId().equals(existing.getId())) {
    // 更新记录
    offeringService.updateById(entity);
}

// 策略2：根据唯一键更新
OfferingEntity existing = offeringService.getOneSafe(
    new LambdaQueryWrapper<OfferingEntity>()
        .eq(OfferingEntity::getName, offering.getName())
        .eq(OfferingEntity::getSupplierId, offering.getSupplierId())
);
if (existing != null) {
    entity.setId(existing.getId());
    offeringService.updateById(entity);
} else {
    offeringService.save(entity);
}
```

## 六、完整实现清单

实现一个完整的导入导出功能需要以下步骤：

- [ ] 1. **实体类配置**：添加 @ExcelTemplate, @ExcelProperty, @ExcelPropertyType, @ExcelSample
- [ ] 2. **枚举类配置**：实现 IDictEnum 接口，枚举字段使用 EnumSampleProvider
- [ ] 3. **VO 类配置**（可选）：添加关联对象的名称字段
- [ ] 4. **Service 接口**：添加 importData 方法
- [ ] 5. **导入监听器**：继承 SseProgressExcelListener，实现 processData
- [ ] 6. **Controller 接口**：添加 importData 和 export 方法
- [ ] 7. **前端按钮**：添加 ExcelImport 组件
- [ ] 8. **权限配置**：添加导入导出权限点

## 七、调试技巧

1. **查看生成的模板**：通过导出功能查看模板格式是否正确
2. **日志输出**：监听器中添加日志，跟踪导入过程
3. **异常提示**：在监听器中抛出 BusinessException，前端会显示详细错误信息
4. **进度监控**：通过 SSE 查看实时导入进度
5. **数据校验**：在监听器中添加详细的数据验证逻辑

---

## 附录：提供给 AI 的提示词模板

```
请帮我为 [实体名称] 实现 Excel 导入导出功能。

业务需求：
- 实体类：[实体类路径]
- 需要导入的字段：[字段列表]
- 关联对象：[关联关系说明]
- 业务规则：[去重规则、更新策略等]

参考文档：[Excel导入导出配置指南.md]

请提供：
1. 实体类/VO 的完整注解配置
2. 导入监听器的实现代码
3. Service 和 Controller 的方法实现
4. 前端按钮的集成代码
```

使用此文档，AI 可以快速理解系统架构并生成符合规范的代码。
