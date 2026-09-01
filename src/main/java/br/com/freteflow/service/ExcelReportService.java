package br.com.freteflow.service;

import br.com.freteflow.dto.report.ExpenseSummaryDTO;
import br.com.freteflow.dto.report.FreightSummaryDTO;
import br.com.freteflow.dto.report.VehicleProfitReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExcelReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateVehicleProfitExcel(VehicleProfitReportDTO report) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Styles styles = new Styles(workbook);

            buildSummarySheet(workbook, report, styles);
            buildFreightsSheet(workbook, report, styles);
            buildExpensesSheet(workbook, report, styles);

            workbook.setActiveSheet(0);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------- ABA RESUMO (DASHBOARD) ----------

    private void buildSummarySheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Resumo");
        sheet.setDisplayGridlines(false);


        for (int r = 0; r <= 40; r++) {
            Row bgRow = sheet.createRow(r);
            for (int c = 0; c <= 21; c++) {
                Cell cell = bgRow.createCell(c);
                cell.setCellStyle(styles.dashboardBg);
            }
        }


        for (int r = 1; r <= 4; r++) {
            Row bgRow = sheet.getRow(r);
            for (int c = 0; c <= 8; c++) {
                bgRow.getCell(c).setCellStyle(styles.topbarBg);
            }
        }

        sheet.setColumnWidth(0, 10000);
        sheet.setColumnWidth(1, 2000);
        sheet.setColumnWidth(2, 9000);
        sheet.setColumnWidth(3, 2000);
        sheet.setColumnWidth(4, 9000);


        Row brand = sheet.getRow(1);
        brand.setHeightInPoints(30);
        Cell brandCell = brand.getCell(0);
        brandCell.setCellValue("🚚 FreteFlow ");
        brandCell.setCellStyle(styles.brand);

        Row title = sheet.getRow(2);
        Cell titleCell = title.getCell(0);
        titleCell.setCellValue("Relatório Financeiro — Veículo " + report.licensePlate());
        titleCell.setCellStyle(styles.title);

        Row period = sheet.getRow(3);
        Cell periodLabel = period.getCell(0);
        periodLabel.setCellValue("Período: " + report.startDate().format(DATE_FMT) + " até " + report.endDate().format(DATE_FMT));
        periodLabel.setCellStyle(styles.labelValue);


        writeCard(sheet, 6, 0, "TOTAL EM FRETES (BRUTO)", report.totalFreightValue(), styles.cardHeaderNeutral, styles.cardValueNeutral);
        writeCard(sheet, 6, 2, "TOTAL EM DESPESAS", report.totalExpenses(), styles.cardHeaderNegative, styles.cardValueNegative);
        writeCard(sheet, 6, 4, "LUCRO LÍQUIDO", report.netProfit(), styles.cardHeaderPositive, styles.cardValuePositive);

        int chart1DataRow = 100;
        Row hRow1 = sheet.createRow(chart1DataRow);
        hRow1.createCell(0).setCellValue("Despesas");
        hRow1.createCell(1).setCellValue(report.totalExpenses().doubleValue());

        Row hRow2 = sheet.createRow(chart1DataRow + 1);
        hRow2.createCell(0).setCellValue("Lucro Líquido");
        hRow2.createCell(1).setCellValue(report.netProfit().doubleValue());

        String chart1Title = String.format("Composição do Frete (Total Bruto: R$ %,.2f)", report.totalFreightValue());
        byte[][] colors1 = {
                {(byte)194, (byte)24, (byte)21},  // #c21815 (Vermelho)
                {(byte)25, (byte)166, (byte)0}   // #19a600 (Verde)
        };
        addPieChart(sheet, 0, 9, 3, 24, chart1Title, chart1DataRow, chart1DataRow + 1, colors1);

        buildExpenseBreakdown(sheet, report, 9);
    }

    private void writeCard(XSSFSheet sheet, int startRow, int col, String label, BigDecimal value,
                           CellStyle headerStyle, CellStyle valueStyle) {
        Row headerRow = sheet.getRow(startRow) != null ? sheet.getRow(startRow) : sheet.createRow(startRow);
        headerRow.setHeightInPoints(20);
        Cell header = headerRow.getCell(col) != null ? headerRow.getCell(col) : headerRow.createCell(col);
        header.setCellValue(label);
        header.setCellStyle(headerStyle);

        Row valueRow = sheet.getRow(startRow + 1) != null ? sheet.getRow(startRow + 1) : sheet.createRow(startRow + 1);
        valueRow.setHeightInPoints(35);
        Cell valueCell = valueRow.getCell(col) != null ? valueRow.getCell(col) : valueRow.createCell(col);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }

    private void buildExpenseBreakdown(XSSFSheet sheet, VehicleProfitReportDTO report, int startRow) {
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (ExpenseSummaryDTO e : report.expenses()) {
            byCategory.merge(e.description(), e.amount(), BigDecimal::add);
        }

        if (byCategory.isEmpty()) return;

        int hiddenRowExp = 105;
        int currentRow = hiddenRowExp;
        for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
            Row r = sheet.createRow(currentRow++);
            r.createCell(0).setCellValue(entry.getKey());
            r.createCell(1).setCellValue(entry.getValue().doubleValue());
        }

        String chart2Title = String.format("Despesas por Categoria (Total: R$ %,.2f)", report.totalExpenses());

        byte[][] colors2 = {
                {(byte)0, (byte)61, (byte)92},
                {(byte)98, (byte)117, (byte)135},
                {(byte)179, (byte)179, (byte)179}
        };

        addPieChart(sheet, 4, startRow, 9, startRow + 15, chart2Title, hiddenRowExp, currentRow - 1, colors2);
    }

    private void addPieChart(XSSFSheet sheet, int col1, int row1, int col2, int row2,
                             String title, int firstDataRow, int lastDataRow, byte[][] colors) {
        XSSFDrawing drawing = sheet.getDrawingPatriarch() != null ? sheet.getDrawingPatriarch() : sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(firstDataRow, lastDataRow, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(firstDataRow, lastDataRow, 1, 1));

        XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
        XDDFChartData.Series series = data.addSeries(categories, values);

        series.setTitle("", null);
        chart.plot(data);

        try {
            org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart ctPie = chart.getCTChart().getPlotArea().getPieChartArray(0);
            org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer ctSer = ctPie.getSerArray(0);

            org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls dLbls = ctSer.addNewDLbls();
            dLbls.addNewShowVal().setVal(true);
            dLbls.addNewShowCatName().setVal(true);
            dLbls.addNewShowLegendKey().setVal(false);
            dLbls.addNewShowPercent().setVal(false);
            dLbls.addNewShowSerName().setVal(false);

            dLbls.addNewNumFmt().setFormatCode("\"R$\" #,##0.00");
            dLbls.getNumFmt().setSourceLinked(false);

            for (int i = 0; i <= (lastDataRow - firstDataRow); i++) {
                org.openxmlformats.schemas.drawingml.x2006.chart.CTDPt pt = ctSer.addNewDPt();
                pt.addNewIdx().setVal(i);
                byte[] color = colors[i % colors.length];
                pt.addNewSpPr().addNewSolidFill().addNewSrgbClr().setVal(color);
            }
        } catch (Exception e) {}
    }

    // ---------- ABA FRETES ----------

    private void buildFreightsSheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Fretes");

        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(2, 25 * 256);
        sheet.setColumnWidth(3, 40 * 256);
        sheet.setColumnWidth(4, 20 * 256);

        String[] columns = {"Data", "Placa", "Motorista", "Loja/Rota", "Valor"};
        writeTableHeader(sheet, columns, styles);

        int rowIdx = 1;
        boolean zebra = false;
        for (FreightSummaryDTO f : report.freights()) {
            Row row = sheet.createRow(rowIdx++);
            writeCell(row, 0, f.date().format(DATE_FMT), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 1, report.licensePlate(), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 2, f.driverName(), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 3, f.storeName(), zebra ? styles.rowZebra : styles.rowPlain);

            Cell valueCell = row.createCell(4);
            valueCell.setCellValue(f.value().doubleValue());
            valueCell.setCellStyle(zebra ? styles.currencyZebra : styles.currencyPlain);
            zebra = !zebra;
        }

        Row totalRow = sheet.createRow(rowIdx);
        Cell totalLabel = totalRow.createCell(3);
        totalLabel.setCellValue("TOTAL FILTRADO:");
        totalLabel.setCellStyle(styles.tableHeader);

        Cell totalValue = totalRow.createCell(4);
        totalValue.setCellFormula(String.format("SUBTOTAL(109, E2:E%d)", rowIdx));
        totalValue.setCellStyle(styles.currencyPlain);

        finalizeTable(sheet, columns.length, rowIdx + 1);
    }

    // ---------- ABA DESPESAS ----------

    private void buildExpensesSheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Despesas");

        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 50 * 256);
        sheet.setColumnWidth(2, 20 * 256);

        String[] columns = {"Data", "Descrição", "Valor"};
        writeTableHeader(sheet, columns, styles);

        int rowIdx = 1;
        boolean zebra = false;
        for (ExpenseSummaryDTO e : report.expenses()) {
            Row row = sheet.createRow(rowIdx++);
            writeCell(row, 0, e.date().format(DATE_FMT), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 1, e.description(), zebra ? styles.rowZebra : styles.rowPlain);

            Cell valueCell = row.createCell(2);
            valueCell.setCellValue(e.amount().doubleValue());
            valueCell.setCellStyle(zebra ? styles.currencyZebra : styles.currencyPlain);
            zebra = !zebra;
        }

        Row totalRow = sheet.createRow(rowIdx);
        Cell totalLabel = totalRow.createCell(1);
        totalLabel.setCellValue("TOTAL FILTRADO:");
        totalLabel.setCellStyle(styles.tableHeader);

        Cell totalValue = totalRow.createCell(2);
        totalValue.setCellFormula(String.format("SUBTOTAL(109, C2:C%d)", rowIdx));
        totalValue.setCellStyle(styles.currencyPlain);

        finalizeTable(sheet, columns.length, rowIdx + 1);
    }

    private void writeTableHeader(Sheet sheet, String[] columns, Styles styles) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(22);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(styles.tableHeader);
        }
    }

    private void writeCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void finalizeTable(Sheet sheet, int columnCount, int lastRowExclusive) {
        if (lastRowExclusive > 1) {
            sheet.setAutoFilter(new CellRangeAddress(0, lastRowExclusive - 1, 0, columnCount - 1));
        }
        sheet.createFreezePane(0, 1);
    }

    // ---------- ESTILOS VISUAIS ----------

    private static class Styles {
        final CellStyle brand, title, labelValue, tableHeader, rowPlain, rowZebra;
        final CellStyle currencyPlain, currencyZebra, dashboardBg, topbarBg;
        final CellStyle cardHeaderNeutral, cardHeaderPositive, cardHeaderNegative;
        final CellStyle cardValueNeutral, cardValuePositive, cardValueNegative;

        Styles(XSSFWorkbook wb) {
            DataFormat currencyFormat = wb.createDataFormat();
            short fmt = currencyFormat.getFormat("R$ #,##0.00");

            dashboardBg = wb.createCellStyle();
            dashboardBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            dashboardBg.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            byte[] blackBg = new byte[]{(byte)0, (byte)0, (byte)0};
            topbarBg = wb.createCellStyle();
            ((XSSFCellStyle) topbarBg).setFillForegroundColor(new XSSFColor(blackBg, null));
            topbarBg.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            brand = createCustomStyleWithRgb(wb, 18, true, new byte[]{(byte)255, (byte)255, (byte)255}, blackBg);
            title = createCustomStyleWithRgb(wb, 14, true, new byte[]{(byte)220, (byte)220, (byte)220}, blackBg);
            labelValue = createCustomStyleWithRgb(wb, 11, false, new byte[]{(byte)220, (byte)220, (byte)220}, blackBg);

            tableHeader = wb.createCellStyle();
            tableHeader.setFont(createFont(wb, 11, true, IndexedColors.WHITE.getIndex()));
            tableHeader.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(tableHeader);

            rowPlain = wb.createCellStyle();
            rowPlain.setFont(createFont(wb, 11, false, IndexedColors.BLACK.getIndex()));
            border(rowPlain);

            rowZebra = wb.createCellStyle();
            rowZebra.setFont(createFont(wb, 11, false, IndexedColors.BLACK.getIndex()));
            rowZebra.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            rowZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(rowZebra);

            currencyPlain = wb.createCellStyle();
            currencyPlain.setDataFormat(fmt);
            currencyPlain.setFont(createFont(wb, 11, false, IndexedColors.BLACK.getIndex()));
            border(currencyPlain);

            currencyZebra = wb.createCellStyle();
            currencyZebra.setDataFormat(fmt);
            currencyZebra.setFont(createFont(wb, 11, false, IndexedColors.BLACK.getIndex()));
            currencyZebra.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            currencyZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(currencyZebra);

            byte[] customRed = new byte[]{(byte)194, (byte)24, (byte)21};
            byte[] customGreen = new byte[]{(byte)25, (byte)166, (byte)0};
            byte[] black = new byte[]{(byte)0, (byte)0, (byte)0};

            cardHeaderNeutral = customCardHeader(wb, black);
            cardHeaderPositive = customCardHeader(wb, customGreen);
            cardHeaderNegative = customCardHeader(wb, customRed);

            cardValueNeutral = customCardValue(wb, fmt, black);
            cardValuePositive = customCardValue(wb, fmt, customGreen);
            cardValueNegative = customCardValue(wb, fmt, customRed);
        }

        private Font createFont(XSSFWorkbook wb, int size, boolean bold, short color) {
            Font font = wb.createFont();
            font.setFontName("Segoe UI");
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(color);
            return font;
        }

        private XSSFFont createCustomFont(XSSFWorkbook wb, int size, boolean bold, byte[] rgb) {
            XSSFFont font = wb.createFont();
            font.setFontName("Segoe UI");
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(new XSSFColor(rgb, null));
            return font;
        }

        private XSSFCellStyle createCustomStyleWithRgb(XSSFWorkbook wb, int size, boolean bold, byte[] fontRgb, byte[] bgRgb) {
            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(createCustomFont(wb, size, bold, fontRgb));
            style.setFillForegroundColor(new XSSFColor(bgRgb, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        }

        private XSSFCellStyle customCardHeader(XSSFWorkbook wb, byte[] fontColor) {
            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(createCustomFont(wb, 10, true, fontColor));
            style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            border(style);
            return style;
        }

        private XSSFCellStyle customCardValue(XSSFWorkbook wb, short fmt, byte[] fontColor) {
            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(createCustomFont(wb, 16, true, fontColor));
            style.setDataFormat(fmt);
            style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            border(style);
            return style;
        }

        private void border(CellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        }
    }
}