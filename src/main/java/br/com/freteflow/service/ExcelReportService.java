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

            Styles styles = new Styles(workbook, report.netProfit().signum() >= 0);

            buildSummarySheet(workbook, report, styles);
            buildFreightsSheet(workbook, report, styles);
            buildExpensesSheet(workbook, report, styles);

            workbook.setActiveSheet(0);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------- ABA RESUMO ----------

    private void buildSummarySheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Resumo");
        sheet.setColumnWidth(0, 7000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);

        // Cabeçalho com "logo" textual
        Row brand = sheet.createRow(0);
        Cell brandCell = brand.createCell(0);
        brandCell.setCellValue("🚚 FreteFlow");
        brandCell.setCellStyle(styles.brand);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        Row title = sheet.createRow(1);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Relatório de Lucro — Veículo " + report.licensePlate());
        titleCell.setCellStyle(styles.title);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));

        Row period = sheet.createRow(2);
        Cell periodLabel = period.createCell(0);
        periodLabel.setCellValue("Período de apuração:");
        periodLabel.setCellStyle(styles.label);
        Cell periodValue = period.createCell(1);
        periodValue.setCellValue(report.startDate().format(DATE_FMT) + "  até  " + report.endDate().format(DATE_FMT));
        periodValue.setCellStyle(styles.labelValue);

        // Cards de indicadores (linha 4 a 6)
        writeCard(sheet, 4, 0, "TOTAL EM FRETES", report.totalFreightValue(), styles.cardHeaderPositive, styles.cardValueNeutral);
        writeCard(sheet, 4, 1, "TOTAL EM DESPESAS", report.totalExpenses(), styles.cardHeaderNegative, styles.cardValueNeutral);
        writeCard(sheet, 4, 2, "LUCRO LÍQUIDO", report.netProfit(),
                report.netProfit().signum() >= 0 ? styles.cardHeaderPositive : styles.cardHeaderNegative,
                report.netProfit().signum() >= 0 ? styles.cardValuePositive : styles.cardValueNegative);

        // Indicadores adicionais
        int qtyFreights = report.freights().size();
        int qtyExpenses = report.expenses().size();
        BigDecimal avgFreight = qtyFreights == 0
                ? BigDecimal.ZERO
                : report.totalFreightValue().divide(BigDecimal.valueOf(qtyFreights), 2, java.math.RoundingMode.HALF_UP);

        Row statsHeader = sheet.createRow(8);
        String[] statLabels = {"Qtd. de Fretes", "Qtd. de Despesas", "Ticket Médio/Frete"};
        for (int i = 0; i < statLabels.length; i++) {
            Cell c = statsHeader.createCell(i);
            c.setCellValue(statLabels[i]);
            c.setCellStyle(styles.label);
        }
        Row statsValue = sheet.createRow(9);
        Cell c0 = statsValue.createCell(0);
        c0.setCellValue(qtyFreights);
        c0.setCellStyle(styles.plainCentered);
        Cell c1 = statsValue.createCell(1);
        c1.setCellValue(qtyExpenses);
        c1.setCellStyle(styles.plainCentered);
        Cell c2 = statsValue.createCell(2);
        c2.setCellValue(avgFreight.doubleValue());
        c2.setCellStyle(styles.currencyCentered);

        // Tabela oculta de apoio para o gráfico (Fretes x Despesas)
        int chartDataRow = 12;
        Row chartHeader = sheet.createRow(chartDataRow);
        chartHeader.createCell(0).setCellValue("Categoria");
        chartHeader.createCell(1).setCellValue("Valor");
        Row rowFreight = sheet.createRow(chartDataRow + 1);
        rowFreight.createCell(0).setCellValue("Fretes");
        rowFreight.createCell(1).setCellValue(report.totalFreightValue().doubleValue());
        Row rowExpense = sheet.createRow(chartDataRow + 2);
        rowExpense.createCell(0).setCellValue("Despesas");
        rowExpense.createCell(1).setCellValue(report.totalExpenses().doubleValue());

        addComparisonChart(sheet, chartDataRow);

        // Quebra de despesas por categoria (Diesel / Pedágio / Manutenção)
        buildExpenseBreakdown(sheet, report, styles, chartDataRow + 5);
    }

    private void writeCard(XSSFSheet sheet, int startRow, int col, String label, BigDecimal value,
                           CellStyle headerStyle, CellStyle valueStyle) {
        Row headerRow = sheet.getRow(startRow) != null ? sheet.getRow(startRow) : sheet.createRow(startRow);
        Cell header = headerRow.createCell(col);
        header.setCellValue(label);
        header.setCellStyle(headerStyle);

        Row valueRow = sheet.getRow(startRow + 1) != null ? sheet.getRow(startRow + 1) : sheet.createRow(startRow + 1);
        Cell valueCell = valueRow.createCell(col);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }

    private void addComparisonChart(XSSFSheet sheet, int dataRow) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 3, 10, 20);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Fretes x Despesas");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new org.apache.poi.ss.util.CellRangeAddress(dataRow + 1, dataRow + 2, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new org.apache.poi.ss.util.CellRangeAddress(dataRow + 1, dataRow + 2, 1, 1));

        XDDFChartData data = chart.createData(ChartTypes.BAR, chart.createCategoryAxis(AxisPosition.BOTTOM),
                chart.createValueAxis(AxisPosition.LEFT));
        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle("Valor (R$)", null);
        ((XDDFBarChartData) data).setBarDirection(BarDirection.COL);
        chart.plot(data);
    }

    private void buildExpenseBreakdown(XSSFSheet sheet, VehicleProfitReportDTO report, Styles styles, int startRow) {
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (ExpenseSummaryDTO e : report.expenses()) {
            byCategory.merge(e.description(), e.amount(), BigDecimal::add);
        }

        if (byCategory.isEmpty()) {
            return;
        }

        Row header = sheet.createRow(startRow);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Despesas por Categoria");
        headerCell.setCellStyle(styles.sectionTitle);

        Row colHeader = sheet.createRow(startRow + 1);
        Cell catHeader = colHeader.createCell(0);
        catHeader.setCellValue("Categoria");
        catHeader.setCellStyle(styles.tableHeader);
        Cell valHeader = colHeader.createCell(1);
        valHeader.setCellValue("Total");
        valHeader.setCellStyle(styles.tableHeader);

        int rowIdx = startRow + 2;
        boolean zebra = false;
        for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            Cell nameCell = row.createCell(0);
            nameCell.setCellValue(entry.getKey());
            nameCell.setCellStyle(zebra ? styles.rowZebra : styles.rowPlain);
            Cell valCell = row.createCell(1);
            valCell.setCellValue(entry.getValue().doubleValue());
            valCell.setCellStyle(zebra ? styles.currencyZebra : styles.currencyPlain);
            zebra = !zebra;
        }
    }

    // ---------- ABA FRETES ----------

    private void buildFreightsSheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Fretes");
        String[] columns = {"Data", "Motorista", "Loja/Rota", "Valor"};
        writeTableHeader(sheet, columns, styles);

        int rowIdx = 1;
        boolean zebra = false;
        for (FreightSummaryDTO f : report.freights()) {
            Row row = sheet.createRow(rowIdx++);
            writeCell(row, 0, f.date().format(DATE_FMT), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 1, f.driverName(), zebra ? styles.rowZebra : styles.rowPlain);
            writeCell(row, 2, f.storeName(), zebra ? styles.rowZebra : styles.rowPlain);
            Cell valueCell = row.createCell(3);
            valueCell.setCellValue(f.value().doubleValue());
            valueCell.setCellStyle(zebra ? styles.currencyZebra : styles.currencyPlain);
            zebra = !zebra;
        }

        finalizeTable(sheet, columns.length, rowIdx);
    }

    // ---------- ABA DESPESAS ----------

    private void buildExpensesSheet(XSSFWorkbook wb, VehicleProfitReportDTO report, Styles styles) {
        XSSFSheet sheet = wb.createSheet("Despesas");
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

        finalizeTable(sheet, columns.length, rowIdx);
    }

    // ---------- HELPERS COMPARTILHADOS ----------

    private void writeTableHeader(Sheet sheet, String[] columns, Styles styles) {
        Row header = sheet.createRow(0);
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
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.createFreezePane(0, 1);
    }

    // ---------- ESTILOS ----------

    private static class Styles {
        final CellStyle brand;
        final CellStyle title;
        final CellStyle label;
        final CellStyle labelValue;
        final CellStyle sectionTitle;
        final CellStyle tableHeader;
        final CellStyle rowPlain;
        final CellStyle rowZebra;
        final CellStyle currencyPlain;
        final CellStyle currencyZebra;
        final CellStyle currencyCentered;
        final CellStyle plainCentered;
        final CellStyle cardHeaderPositive;
        final CellStyle cardHeaderNegative;
        final CellStyle cardValueNeutral;
        final CellStyle cardValuePositive;
        final CellStyle cardValueNegative;

        Styles(XSSFWorkbook wb, boolean overallPositive) {
            DataFormat currencyFormat = wb.createDataFormat();
            short fmt = currencyFormat.getFormat("R$ #,##0.00");

            brand = font(wb, 16, true, IndexedColors.DARK_BLUE.getIndex(), null);
            title = font(wb, 13, true, IndexedColors.GREY_80_PERCENT.getIndex(), null);
            label = font(wb, 10, true, IndexedColors.GREY_50_PERCENT.getIndex(), null);
            labelValue = font(wb, 10, false, IndexedColors.BLACK.getIndex(), null);
            sectionTitle = font(wb, 12, true, IndexedColors.DARK_BLUE.getIndex(), null);

            tableHeader = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            tableHeader.setFont(headerFont);
            tableHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(tableHeader);

            rowPlain = wb.createCellStyle();
            border(rowPlain);

            rowZebra = wb.createCellStyle();
            rowZebra.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            rowZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(rowZebra);

            currencyPlain = wb.createCellStyle();
            currencyPlain.setDataFormat(fmt);
            border(currencyPlain);

            currencyZebra = wb.createCellStyle();
            currencyZebra.setDataFormat(fmt);
            currencyZebra.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            currencyZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(currencyZebra);

            currencyCentered = wb.createCellStyle();
            currencyCentered.setDataFormat(fmt);
            currencyCentered.setAlignment(HorizontalAlignment.CENTER);

            plainCentered = wb.createCellStyle();
            plainCentered.setAlignment(HorizontalAlignment.CENTER);

            cardHeaderPositive = cardHeader(wb, IndexedColors.LIGHT_GREEN.getIndex());
            cardHeaderNegative = cardHeader(wb, IndexedColors.ROSE.getIndex());

            cardValueNeutral = cardValue(wb, fmt, IndexedColors.BLACK.getIndex());
            cardValuePositive = cardValue(wb, fmt, IndexedColors.GREEN.getIndex());
            cardValueNegative = cardValue(wb, fmt, IndexedColors.RED.getIndex());
        }

        private CellStyle font(XSSFWorkbook wb, int size, boolean bold, short color, IndexedColors fill) {
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(color);
            style.setFont(font);
            return style;
        }

        private CellStyle cardHeader(XSSFWorkbook wb, short fillColor) {
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 9);
            style.setFont(font);
            style.setFillForegroundColor(fillColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            border(style);
            return style;
        }

        private CellStyle cardValue(XSSFWorkbook wb, short fmt, short fontColor) {
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            font.setColor(fontColor);
            style.setFont(font);
            style.setDataFormat(fmt);
            style.setAlignment(HorizontalAlignment.CENTER);
            border(style);
            return style;
        }

        private void border(CellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
    }
}