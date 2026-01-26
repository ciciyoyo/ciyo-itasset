package com.ciyocloud.itam.service.impl;

import com.ciyocloud.envconfig.service.SysEnvConfigService;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.service.DevicePrintService;
import com.ciyocloud.itam.service.DeviceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.pdf.pdfbox.core.base.Document;
import org.dromara.pdf.pdfbox.core.base.Page;
import org.dromara.pdf.pdfbox.core.base.PageSize;
import org.dromara.pdf.pdfbox.core.component.*;
import org.dromara.pdf.pdfbox.core.enums.BarcodeType;
import org.dromara.pdf.pdfbox.core.enums.HorizontalAlignment;
import org.dromara.pdf.pdfbox.core.enums.VerticalAlignment;
import org.dromara.pdf.pdfbox.handler.PdfHandler;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;

/**
 * 设备打印Service业务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class DevicePrintServiceImpl implements DevicePrintService {

    // 标签尺寸 60mm x 40mm (适配主流热敏打印机) -> 转 points (1mm ≈ 2.83pt)
    private static final float LABEL_WIDTH = 170F;  // ~60mm
    private static final float LABEL_HEIGHT = 113F; // ~40mm
    private final DeviceService deviceService;
    private final SysEnvConfigService configService;

    @Override
    public void printLabels(List<Long> ids, HttpServletResponse response) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择设备");
        }
        List<DeviceEntity> devices = deviceService.listByIds(ids);
        if (devices.isEmpty()) {
            throw new RuntimeException("设备不存在");
        }

        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"device-labels.pdf\"");

            // 创建文档
            Document document = PdfHandler.getDocumentHandler().create();
            document.setMargin(0F);

            for (DeviceEntity device : devices) {
                // 创建新页面，设定尺寸 (60mm x 40mm)
                Page page = new Page(document, PageSize.create(LABEL_WIDTH, LABEL_HEIGHT));
                page.setMargin(2.5F);

                // 主表格：2列布局 (左边二维码，右边设备信息)
                Table mainTable = new Table(page);

                // 左列宽度约40%用于二维码，右列宽度约60%用于信息
                float qrCodeWidth = 70F;
                float infoWidth = LABEL_WIDTH - qrCodeWidth - 5F; // 减去边距 (2.5 * 2 = 5)
                mainTable.setCellWidths(qrCodeWidth, infoWidth);

                mainTable.setIsBorder(true);
                mainTable.setBorderColor(Color.BLACK);
                mainTable.setBorderLineWidth(0.5F);

                // 创建唯一的行
                TableRow mainRow = new TableRow(mainTable);
                mainRow.setHeight(LABEL_HEIGHT - 5F); // 设置行高为标签高度减去边距

                // === 左侧单元格：二维码 ===
                TableCell qrCodeCell = new TableCell(mainRow);
                qrCodeCell.setContentHorizontalAlignment(HorizontalAlignment.CENTER);
                qrCodeCell.setContentVerticalAlignment(VerticalAlignment.CENTER);
                qrCodeCell.setIsBorder(true);
                qrCodeCell.setBorderColor(Color.BLACK);
                qrCodeCell.setBorderLineWidth(0.5F);

                // 生成二维码
                Barcode qrCode = new Barcode(page);
                qrCode.setCodeType(BarcodeType.QR_CODE);
                qrCode.setContent(device.getDeviceNo());
                qrCode.setWidth(85);
                qrCode.setHeight(85);

                qrCodeCell.addComponents(qrCode);

                // === 右侧单元格：设备信息表格 ===
                TableCell infoCell = new TableCell(mainRow);
                infoCell.setIsBorder(true);
                infoCell.setBorderColor(Color.BLACK);
                infoCell.setBorderLineWidth(0.5F);

                // 右侧嵌套表格，用于显示设备信息
                Table infoTable = new Table(page);
                infoTable.setCellWidths(35F, infoWidth - 35F);
                infoTable.setIsBorder(true);
                infoTable.setBorderColor(Color.BLACK);
                infoTable.setBorderLineWidth(0.5F);

                // 信息行1：资产名称
                TableRow nameRow = new TableRow(infoTable);
                // 剩余高度分配给资产名称，以支持换行显示
                // 总高度 ~108F, 其他4行共 16F*4 = 64F, 剩余 ~44F
                nameRow.setHeight(40F);

                TableCell nameLabel = new TableCell(nameRow);
                nameLabel.setContentVerticalAlignment(VerticalAlignment.CENTER);
                nameLabel.setIsBorder(true);
                nameLabel.setBorderColor(Color.BLACK);
                nameLabel.setBorderLineWidth(0.5F);
                Textarea nameLabelText = new Textarea(page);
                nameLabelText.setText("资产名称");
                nameLabelText.setFontSize(7F);
                nameLabelText.setMarginLeft(2F);
                nameLabelText.setIsWrap(true);
                nameLabel.addComponents(nameLabelText);

                TableCell nameValue = new TableCell(nameRow);
                nameValue.setContentVerticalAlignment(VerticalAlignment.CENTER);
                nameValue.setIsBorder(true);
                nameValue.setBorderColor(Color.BLACK);
                nameValue.setBorderLineWidth(0.5F);
                Textarea nameValueText = new Textarea(page);
                String rawName = device.getName() != null ? device.getName() : "";
                // Manual splice every 8 characters for wrapping
                nameValueText.setText(splitString(rawName,16));
                nameValueText.setFontSize(7F);
                nameValueText.setMarginLeft(2F);
                nameValueText.setIsWrap(true);
                nameValue.addComponents(nameValueText);

                nameRow.addCells(nameLabel, nameValue);
                infoTable.addRows(nameRow);

                // Common height for standard single-line rows
                float standardRowHeight = 16F;

                // 信息行2：设备编号
                TableRow deviceNoRow = new TableRow(infoTable);
                deviceNoRow.setHeight(standardRowHeight);

                TableCell deviceNoLabel = new TableCell(deviceNoRow);
                deviceNoLabel.setContentVerticalAlignment(VerticalAlignment.CENTER);
                deviceNoLabel.setIsBorder(true);
                deviceNoLabel.setBorderColor(Color.BLACK);
                deviceNoLabel.setBorderLineWidth(0.5F);
                Textarea deviceNoLabelText = new Textarea(page);
                deviceNoLabelText.setText("设备编号");
                deviceNoLabelText.setFontSize(7F);
                deviceNoLabelText.setMarginLeft(2F);
                deviceNoLabel.addComponents(deviceNoLabelText);

                TableCell deviceNoValue = new TableCell(deviceNoRow);
                deviceNoValue.setContentVerticalAlignment(VerticalAlignment.CENTER);
                deviceNoValue.setIsBorder(true);
                deviceNoValue.setBorderColor(Color.BLACK);
                deviceNoValue.setBorderLineWidth(0.5F);
                Textarea deviceNoValueText = new Textarea(page);
                deviceNoValueText.setText(device.getDeviceNo() != null ? device.getDeviceNo() : "");
                deviceNoValueText.setFontSize(7F);
                deviceNoValueText.setMarginLeft(2F);
                deviceNoValue.addComponents(deviceNoValueText);

                deviceNoRow.addCells(deviceNoLabel, deviceNoValue);
                infoTable.addRows(deviceNoRow);

                // 信息行3：序列号 (新增)
                TableRow serialRow = new TableRow(infoTable);
                serialRow.setHeight(standardRowHeight);

                TableCell serialLabel = new TableCell(serialRow);
                serialLabel.setContentVerticalAlignment(VerticalAlignment.CENTER);
                serialLabel.setIsBorder(true);
                serialLabel.setBorderColor(Color.BLACK);
                serialLabel.setBorderLineWidth(0.5F);
                Textarea serialLabelText = new Textarea(page);
                serialLabelText.setText("序列号");
                serialLabelText.setFontSize(7F);
                serialLabelText.setMarginLeft(2F);
                serialLabel.addComponents(serialLabelText);

                TableCell serialValue = new TableCell(serialRow);
                serialValue.setContentVerticalAlignment(VerticalAlignment.CENTER);
                serialValue.setIsBorder(true);
                serialValue.setBorderColor(Color.BLACK);
                serialValue.setBorderLineWidth(0.5F);
                Textarea serialValueText = new Textarea(page);
                serialValueText.setText(device.getSerial() != null ? device.getSerial() : "");
                serialValueText.setFontSize(7F);
                serialValueText.setMarginLeft(2F);
                serialValue.addComponents(serialValueText);

                serialRow.addCells(serialLabel, serialValue);
                infoTable.addRows(serialRow);

                // 信息行4：购入日期
                TableRow dateRow = new TableRow(infoTable);
                dateRow.setHeight(standardRowHeight);

                TableCell dateLabel = new TableCell(dateRow);
                dateLabel.setContentVerticalAlignment(VerticalAlignment.CENTER);
                dateLabel.setIsBorder(true);
                dateLabel.setBorderColor(Color.BLACK);
                dateLabel.setBorderLineWidth(0.5F);
                Textarea dateLabelText = new Textarea(page);
                dateLabelText.setText("购入日期");
                dateLabelText.setFontSize(7F);
                dateLabelText.setMarginLeft(2F);
                dateLabel.addComponents(dateLabelText);

                TableCell dateValue = new TableCell(dateRow);
                dateValue.setContentVerticalAlignment(VerticalAlignment.CENTER);
                dateValue.setIsBorder(true);
                dateValue.setBorderColor(Color.BLACK);
                dateValue.setBorderLineWidth(0.5F);
                Textarea dateValueText = new Textarea(page);
                dateValueText.setText(device.getPurchaseDate() != null ? device.getPurchaseDate().toString() : "");
                dateValueText.setFontSize(7F);
                dateValueText.setMarginLeft(2F);
                dateValue.addComponents(dateValueText);

                dateRow.addCells(dateLabel, dateValue);
                infoTable.addRows(dateRow);

                // 信息行5：维保日期 (新增)
                TableRow warrantyRow = new TableRow(infoTable);
                // Calculate remaining height: Total - (Name Row + 3 * Standard Row)
                // Name Row = 40F
                // Standard Row = 16F (DeviceNo, Serial, Date)
                // Height should be 108F - (40 + 16*3) = 20F
                float remainingHeight = (LABEL_HEIGHT - 5F) - (40F + standardRowHeight * 3);
                warrantyRow.setHeight(remainingHeight);

                TableCell warrantyLabel = new TableCell(warrantyRow);
                warrantyLabel.setContentVerticalAlignment(VerticalAlignment.CENTER);
                warrantyLabel.setIsBorder(true);
                warrantyLabel.setBorderColor(Color.BLACK);
                warrantyLabel.setBorderLineWidth(0.5F);
                warrantyLabel.setIsBorderBottom(false);
                Textarea warrantyLabelText = new Textarea(page);
                warrantyLabelText.setText("维保日期");
                warrantyLabelText.setFontSize(7F);
                warrantyLabelText.setMarginLeft(2F);
                warrantyLabel.addComponents(warrantyLabelText);

                TableCell warrantyValue = new TableCell(warrantyRow);
                warrantyValue.setContentVerticalAlignment(VerticalAlignment.CENTER);
                warrantyValue.setIsBorder(true);
                warrantyValue.setIsBorderBottom(false);
                warrantyValue.setBorderColor(Color.BLACK);
                warrantyValue.setBorderLineWidth(0.5F);
                Textarea warrantyValueText = new Textarea(page);
                warrantyValueText.setText(device.getWarrantyDate() != null ? device.getWarrantyDate().toString() : "");
                warrantyValueText.setFontSize(7F);
                warrantyValueText.setMarginLeft(2F);
                warrantyValue.addComponents(warrantyValueText);

                warrantyRow.addCells(warrantyLabel, warrantyValue);
                infoTable.addRows(warrantyRow);

                // 将信息表格添加到右侧单元格
                infoCell.addComponents(infoTable);

                // 将两个单元格添加到主行
                mainRow.addCells(qrCodeCell, infoCell);
                mainTable.addRows(mainRow);

                // 渲染主表格
                mainTable.render();

                // 添加页面到文档
                document.appendPage(page);
            }

            // 保存并输出
            document.save(response.getOutputStream());
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("生成标签失败", e);
        }
    }



    /**
     * Helper to split string every N characters with newlines
     */
    private String splitString(String str, int length) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i += length) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(str, i, Math.min(i + length, str.length()));
        }
        return sb.toString();
    }

}
