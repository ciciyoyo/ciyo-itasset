package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.mapper.*;
import com.ciyocloud.itam.service.AssetsReportService;
import com.ciyocloud.itam.vo.AssetsReportVO;
import com.ciyocloud.itam.vo.AssetsSummaryVO;
import com.ciyocloud.itam.vo.SupplierFailureStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 资产报表统计 Service 实现类
 *
 * @author codeck
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
public class AssetsReportServiceImpl implements AssetsReportService {

    private final AssetsMonthlyStatsMapper assetsMonthlyStatsMapper;
    private final DeviceMapper deviceMapper;
    private final LicensesMapper licensesMapper;
    private final AccessoriesMapper accessoriesMapper;
    private final ConsumablesMapper consumablesMapper;
    private final OfferingMapper offeringMapper;
    private final FailuresMapper failuresMapper;

    @Override
    public List<AssetsReportVO> getYearlyAssetsValueStats() {
        int year = LocalDate.now().getYear();
        String yearPrefix = String.valueOf(year);

        QueryWrapper<AssetsMonthlyStatsEntity> queryWrapper = new QueryWrapper<>();
        // 统计当前年份内，按月份和类型分组的总资产价值
        queryWrapper.select("stats_month", "assets_type", "SUM(current_value) as totalValue")
                .likeRight("stats_month", yearPrefix)
                .groupBy("stats_month", "assets_type")
                .orderByAsc("stats_month", "assets_type");

        List<Map<String, Object>> list = assetsMonthlyStatsMapper.selectMaps(queryWrapper);
        List<AssetsReportVO> result = new ArrayList<>();

        for (Map<String, Object> map : list) {
            AssetsReportVO vo = new AssetsReportVO();
            vo.setStatsMonth(getStringValue(map, "stats_month"));

            String typeCode = getStringValue(map, "assets_type");
            if (typeCode != null) {
                for (AssetType type : AssetType.values()) {
                    if (type.getCode().equals(typeCode)) {
                        vo.setAssetsType(type);
                        vo.setAssetsTypeDesc(type.getDesc());
                        break;
                    }
                }
            }

            Object totalValueObj = getValueCaseInsensitive(map, "totalValue");
            if (totalValueObj != null) {
                if (totalValueObj instanceof BigDecimal) {
                    vo.setTotalValue((BigDecimal) totalValueObj);
                } else {
                    vo.setTotalValue(new BigDecimal(totalValueObj.toString()));
                }
            } else {
                vo.setTotalValue(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<AssetsReportVO> getAssetsDistribution() {
        // 1. 获取最新统计月份
        QueryWrapper<AssetsMonthlyStatsEntity> latestMonthQuery = new QueryWrapper<>();
        latestMonthQuery.select("MAX(stats_month) as latestMonth");
        Map<String, Object> latestMonthMap = assetsMonthlyStatsMapper.selectMaps(latestMonthQuery)
                .stream().findFirst().orElse(null);

        Object latestMonthObj = null;
        if (latestMonthMap != null) {
            latestMonthObj = getValueCaseInsensitive(latestMonthMap, "latestMonth");
        }

        if (latestMonthObj == null) {
            return new ArrayList<>();
        }

        String latestMonth = latestMonthObj.toString();

        // 2. 统计该月份下各类型的总价值
        QueryWrapper<AssetsMonthlyStatsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("assets_type", "SUM(current_value) as totalValue")
                .eq("stats_month", latestMonth)
                .groupBy("assets_type");

        List<Map<String, Object>> list = assetsMonthlyStatsMapper.selectMaps(queryWrapper);
        List<AssetsReportVO> result = new ArrayList<>();

        for (Map<String, Object> map : list) {
            AssetsReportVO vo = new AssetsReportVO();
            vo.setStatsMonth(latestMonth);

            String typeCode = getStringValue(map, "assets_type");
            if (typeCode != null) {
                for (AssetType type : AssetType.values()) {
                    if (type.getCode().equals(typeCode)) {
                        vo.setAssetsType(type);
                        vo.setAssetsTypeDesc(type.getDesc());
                        break;
                    }
                }
            }

            Object totalValueObj = getValueCaseInsensitive(map, "totalValue");
            if (totalValueObj != null) {
                if (totalValueObj instanceof BigDecimal) {
                    vo.setTotalValue((BigDecimal) totalValueObj);
                } else {
                    vo.setTotalValue(new BigDecimal(totalValueObj.toString()));
                }
            } else {
                vo.setTotalValue(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<AssetsReportVO> getMonthlyTrend(AssetType assetsType) {
        LocalDate now = LocalDate.now();
        List<String> last12Months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            last12Months.add(String.format("%d-%02d", date.getYear(), date.getMonthValue()));
        }

        QueryWrapper<AssetsMonthlyStatsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("stats_month", "SUM(current_value) as totalValue")
                .eq("assets_type", assetsType.getCode())
                .in("stats_month", last12Months)
                .groupBy("stats_month")
                .orderByAsc("stats_month");

        List<Map<String, Object>> list = assetsMonthlyStatsMapper.selectMaps(queryWrapper);
        List<AssetsReportVO> result = new ArrayList<>();

        // 初始化近 12 个月份的数据，确保折线图完整
        for (String month : last12Months) {
            AssetsReportVO vo = new AssetsReportVO();
            vo.setStatsMonth(month);
            vo.setAssetsType(assetsType);
            vo.setAssetsTypeDesc(assetsType.getDesc());
            vo.setTotalValue(BigDecimal.ZERO);

            // 如果查询结果中有该月份数据，则更新
            for (Map<String, Object> map : list) {
                String statsMonth = getStringValue(map, "stats_month");
                if (month.equals(statsMonth)) {
                    vo.setTotalValue(getBigDecimalValue(map, "totalValue"));
                    break;
                }
            }
            result.add(vo);
        }

        return result;
    }

    @Override
    public AssetsSummaryVO getAssetsSummary() {
        AssetsSummaryVO summary = new AssetsSummaryVO();

        // 1. 获取最新统计月份的总金额
        QueryWrapper<AssetsMonthlyStatsEntity> latestMonthQuery = new QueryWrapper<>();
        latestMonthQuery.select("MAX(stats_month) as latestMonth");
        Map<String, Object> latestMonthMap = assetsMonthlyStatsMapper.selectMaps(latestMonthQuery)
                .stream().findFirst().orElse(null);

        if (latestMonthMap != null) {
            Object latestMonthObj = getValueCaseInsensitive(latestMonthMap, "latestMonth");
            if (latestMonthObj != null) {
                String latestMonth = latestMonthObj.toString();
                QueryWrapper<AssetsMonthlyStatsEntity> sumQuery = new QueryWrapper<>();
                sumQuery.select("SUM(current_value) as totalAmount")
                        .eq("stats_month", latestMonth);
                Map<String, Object> sumMap = assetsMonthlyStatsMapper.selectMaps(sumQuery)
                        .stream().findFirst().orElse(null);
                if (sumMap != null) {
                    summary.setTotalAssetAmount(getBigDecimalValue(sumMap, "totalAmount"));
                }
            }
        }

        // 2. 统计各项数量
        // 总设备数
        Long devices = deviceMapper.selectCount(new QueryWrapper<DeviceEntity>().eq("deleted", 0));
        summary.setTotalDevices(devices);

        // 总软件 (许可证) 数 - 统计总授权数
        QueryWrapper<LicensesEntity> licenseQuery = new QueryWrapper<>();
        licenseQuery.select("SUM(total_seats) as total").eq("deleted", 0);
        Map<String, Object> licenseMap = licensesMapper.selectMaps(licenseQuery).stream().findFirst().orElse(null);
        Long software = getLongValue(licenseMap, "total");
        summary.setTotalSoftware(software);

        // 总配件数 - 统计数量
        QueryWrapper<AccessoriesEntity> accessoriesQuery = new QueryWrapper<>();
        accessoriesQuery.select("SUM(quantity) as total").eq("deleted", 0);
        Map<String, Object> accessoriesMap = accessoriesMapper.selectMaps(accessoriesQuery).stream().findFirst().orElse(null);
        Long accessories = getLongValue(accessoriesMap, "total");
        summary.setTotalAccessories(accessories);

        // 总服务数
        Long services = offeringMapper.selectCount(null);
        summary.setTotalServices(services);

        // 总耗材数 - 统计当前库存数量
        QueryWrapper<ConsumablesEntity> consumablesQuery = new QueryWrapper<>();
        consumablesQuery.select("SUM(quantity) as total").eq("deleted", 0);
        Map<String, Object> consumablesMap = consumablesMapper.selectMaps(consumablesQuery).stream().findFirst().orElse(null);
        Long consumables = getLongValue(consumablesMap, "total");
        summary.setTotalConsumables(consumables);

        // 总资产数 (设备 + 软件 + 配件 + 服务 + 耗材)
        summary.setTotalAssetCount(devices + software + accessories + services + consumables);

        return summary;
    }

    @Override
    public List<SupplierFailureStatsVO> getSupplierFailureStats() {
        return failuresMapper.countSupplierFailures();
    }

    @Override
    public BigDecimal getLatestAssetValue(AssetType assetsType) {
        if (assetsType == null) {
            return BigDecimal.ZERO;
        }

        // 1. 获取该资产类型的最新统计月份
        QueryWrapper<AssetsMonthlyStatsEntity> latestMonthQuery = new QueryWrapper<>();
        latestMonthQuery.select("MAX(stats_month) as latestMonth")
                .eq("assets_type", assetsType.getCode());
        Map<String, Object> latestMonthMap = assetsMonthlyStatsMapper.selectMaps(latestMonthQuery)
                .stream().findFirst().orElse(null);

        if (latestMonthMap == null) {
            return BigDecimal.ZERO;
        }

        Object latestMonthObj = getValueCaseInsensitive(latestMonthMap, "latestMonth");
        if (latestMonthObj == null) {
            return BigDecimal.ZERO;
        }

        String latestMonth = latestMonthObj.toString();

        // 2. 统计该月份下该类型的总价值
        QueryWrapper<AssetsMonthlyStatsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(current_value) as totalValue")
                .eq("stats_month", latestMonth)
                .eq("assets_type", assetsType.getCode());

        Map<String, Object> resultMap = assetsMonthlyStatsMapper.selectMaps(queryWrapper)
                .stream().findFirst().orElse(null);

        return getBigDecimalValue(resultMap, "totalValue");
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object val = getValueCaseInsensitive(map, key);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        if (map == null) return 0L;
        Object val = getValueCaseInsensitive(map, key);
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = getValueCaseInsensitive(map, key);
        return val != null ? val.toString() : null;
    }

    private Object getValueCaseInsensitive(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        if (map.containsKey(key.toLowerCase())) {
            return map.get(key.toLowerCase());
        }
        return null;
    }
}
