package atemos.everse.api.service;

import atemos.everse.api.domain.AlarmType;
import atemos.everse.api.dto.AlarmDto;
import atemos.everse.api.dto.IotStatusHistoryDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 업체의 에너지 사용량과 요금 등의 데이터를 조회하고 엑셀로 제공하는 기능을 제공하는 서비스 구현 클래스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final EnergyService energyService;
    private final IotStatusHistoryService iotStatusHistoryService;
    private final AlarmService alarmService;

    /**
     * 특정 기간 내 업체의 에너지 사용량 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 우선 월별 - 일별 - 시간별로 나열하고 최하단에 전체 데이터를 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량 등을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    public void reportEnergyUsage(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response) {
        if (endDate == null) {
            endDate = startDate;
        }
        var summaryResponse = energyService.readEnergy(companyId, startDate, endDate);
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(startDate + " ~ " + endDate);
            sheet.setColumnWidth(0, 2 * 256);
            // 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(33);
            var titleCell = titleRow.createCell(1);
            var title = "Energy Usage Report (" + (startDate.equals(endDate) ? startDate : startDate + " ~ " + endDate) + ")";
            titleCell.setCellValue(title);
            // 타이틀 스타일
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 22);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6)); // 타이틀 셀 병합
            // 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{
                    "Reference time", "Usage", "AI Forecast Usage", "Usage Difference", "Deviation Rate", "Forecast Accuracy"
            };
            // 헤더 스타일
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 셀 스타일 설정
            var dataStyle = workbook.createCellStyle();
            var format = workbook.createDataFormat();
            dataStyle.setDataFormat(format.getFormat("#,##0"));
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월 스타일 설정
            var monthStyle = workbook.createCellStyle();
            var monthFont = workbook.createFont();
            monthFont.setBold(true);
            monthStyle.setFont(monthFont);
            monthStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            monthStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            monthStyle.setAlignment(HorizontalAlignment.CENTER);
            monthStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 일 스타일 설정
            var dayStyle = workbook.createCellStyle();
            var dayFont = workbook.createFont();
            dayFont.setBold(true);
            dayStyle.setFont(dayFont);
            dayStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            dayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dayStyle.setAlignment(HorizontalAlignment.CENTER);
            dayStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // Summary 스타일 설정
            var summaryStyle = workbook.createCellStyle();
            var summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setColor(IndexedColors.WHITE.getIndex());
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setAlignment(HorizontalAlignment.CENTER);
            summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월별 데이터 삽입
            int rowNum = 2; // 데이터 시작 행
            for (var monthlyEntry : summaryResponse.getMonthlyResponse().entrySet()) {
                String monthKey = monthlyEntry.getKey();
                var monthlyResponse = monthlyEntry.getValue();
                // 월별 행 생성
                var monthlyRow = sheet.createRow(rowNum++);
                monthlyRow.createCell(1).setCellValue("Month: " + monthKey);
                monthlyRow.getCell(1).setCellStyle(monthStyle);
                // 월별 데이터 삽입
                monthlyRow.createCell(2).setCellValue(monthlyResponse.getMonthlyUsage().toPlainString());
                monthlyRow.getCell(2).setCellStyle(monthStyle);
                monthlyRow.createCell(3).setCellValue(monthlyResponse.getMonthlyForecastUsage().toPlainString());
                monthlyRow.getCell(3).setCellStyle(monthStyle);
                monthlyRow.createCell(4).setCellValue(monthlyResponse.getMonthlyActualAndForecastUsageDifference().toPlainString());
                monthlyRow.getCell(4).setCellStyle(monthStyle);
                monthlyRow.createCell(5).setCellValue(monthlyResponse.getMonthlyDeviationRate().toPlainString() + "%");
                monthlyRow.getCell(5).setCellStyle(monthStyle);
                monthlyRow.createCell(6).setCellValue(monthlyResponse.getMonthlyForecastAccuracy().toPlainString() + "%");
                monthlyRow.getCell(6).setCellStyle(monthStyle);
                // 일별 데이터 삽입
                for (var dailyEntry : monthlyResponse.getDailyResponse().entrySet()) {
                    String dayKey = dailyEntry.getKey();
                    var dailyResponse = dailyEntry.getValue();
                    var dailyRow = sheet.createRow(rowNum++);
                    dailyRow.createCell(1).setCellValue("Day: " + dayKey);
                    dailyRow.getCell(1).setCellStyle(dayStyle);
                    dailyRow.createCell(2).setCellValue(dailyResponse.getDailyUsage().toPlainString());
                    dailyRow.getCell(2).setCellStyle(dayStyle);
                    dailyRow.createCell(3).setCellValue(dailyResponse.getDailyForecastUsage().toPlainString());
                    dailyRow.getCell(3).setCellStyle(dayStyle);
                    dailyRow.createCell(4).setCellValue(dailyResponse.getDailyActualAndForecastUsageDifference().toPlainString());
                    dailyRow.getCell(4).setCellStyle(dayStyle);
                    dailyRow.createCell(5).setCellValue(dailyResponse.getDailyDeviationRate().toPlainString() + "%");
                    dailyRow.getCell(5).setCellStyle(dayStyle);
                    dailyRow.createCell(6).setCellValue(dailyResponse.getDailyForecastAccuracy().toPlainString() + "%");
                    dailyRow.getCell(6).setCellStyle(dayStyle);
                    // 시간별 데이터 삽입
                    for (var hourlyResponse : dailyResponse.getHourlyResponse()) {
                        var hourlyRow = sheet.createRow(rowNum++);
                        hourlyRow.createCell(1).setCellValue(hourlyResponse.getReferenceTime().toString());
                        hourlyRow.createCell(2).setCellValue(hourlyResponse.getUsage().toPlainString());
                        hourlyRow.createCell(3).setCellValue(hourlyResponse.getForecastUsage().toPlainString());
                        hourlyRow.createCell(4).setCellValue(hourlyResponse.getActualAndForecastUsageDifference().toPlainString());
                        hourlyRow.createCell(5).setCellValue(hourlyResponse.getDeviationRate().toPlainString() + "%");
                        hourlyRow.createCell(6).setCellValue(hourlyResponse.getForecastAccuracy().toPlainString() + "%");
                    }
                }
            }
            // 요약 데이터 삽입
            var summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(1).setCellValue("[Summary]");
            summaryRow.getCell(1).setCellStyle(summaryStyle);
            summaryRow.createCell(2).setCellValue(summaryResponse.getSummaryUsage().toPlainString());
            summaryRow.getCell(2).setCellStyle(summaryStyle);
            summaryRow.createCell(3).setCellValue(summaryResponse.getSummaryForecastUsage().toPlainString());
            summaryRow.getCell(3).setCellStyle(summaryStyle);
            summaryRow.createCell(4).setCellValue(summaryResponse.getSummaryUsageForecastDifference().toPlainString());
            summaryRow.getCell(4).setCellStyle(summaryStyle);
            summaryRow.createCell(5).setCellValue(summaryResponse.getSummaryDeviationRate().toPlainString() + "%");
            summaryRow.getCell(5).setCellStyle(summaryStyle);
            summaryRow.createCell(6).setCellValue(summaryResponse.getSummaryForecastAccuracy().toPlainString() + "%");
            summaryRow.getCell(6).setCellStyle(summaryStyle);
            // 엑셀 파일 다운로드 설정
            var fileName = "Energy_usage_report_" + startDate + (startDate.equals(endDate) ? "" : "_to_" + endDate) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file", e);
        }
    }

    /**
     * 특정 기간 내 업체의 에너지 요금 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 우선 월별 - 일별 - 시간별로 나열하고 최하단에 전체 데이터를 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    public void reportEnergyBill(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response) {
        if (endDate == null) {
            endDate = startDate;
        }
        var summaryResponse = energyService.readEnergy(companyId, startDate, endDate);
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(startDate + " ~ " + endDate);
            sheet.setColumnWidth(0, 2 * 256);
            // 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(33);
            var titleCell = titleRow.createCell(1);
            var title = "Energy Bill Report (" + (startDate.equals(endDate) ? startDate : startDate + " ~ " + endDate) + ")";
            titleCell.setCellValue(title);
            // 타이틀 스타일
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 22);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6)); // 타이틀 셀 병합
            // 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{
                    "Reference time", "Bill", "Forecast Bill", "Bill Difference", "Deviation Rate", "Forecast Accuracy"
            };
            // 헤더 스타일
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 셀 스타일 설정
            var dataStyle = workbook.createCellStyle();
            var format = workbook.createDataFormat();
            dataStyle.setDataFormat(format.getFormat("#,##0"));
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월 스타일 설정
            var monthStyle = workbook.createCellStyle();
            var monthFont = workbook.createFont();
            monthFont.setBold(true);
            monthStyle.setFont(monthFont);
            monthStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            monthStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            monthStyle.setAlignment(HorizontalAlignment.CENTER);
            monthStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 일 스타일 설정
            var dayStyle = workbook.createCellStyle();
            var dayFont = workbook.createFont();
            dayFont.setBold(true);
            dayStyle.setFont(dayFont);
            dayStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            dayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dayStyle.setAlignment(HorizontalAlignment.CENTER);
            dayStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // Summary 스타일 설정
            var summaryStyle = workbook.createCellStyle();
            var summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setColor(IndexedColors.WHITE.getIndex());
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setAlignment(HorizontalAlignment.CENTER);
            summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월별 데이터 삽입
            int rowNum = 2; // 데이터 시작 행
            for (var monthlyEntry : summaryResponse.getMonthlyResponse().entrySet()) {
                String monthKey = monthlyEntry.getKey();
                var monthlyResponse = monthlyEntry.getValue();
                // 월별 행 생성
                var monthlyRow = sheet.createRow(rowNum++);
                monthlyRow.createCell(1).setCellValue("Month: " + monthKey);
                monthlyRow.getCell(1).setCellStyle(monthStyle);
                // 월별 데이터 삽입
                monthlyRow.createCell(2).setCellValue(monthlyResponse.getMonthlyBill().toPlainString());
                monthlyRow.getCell(2).setCellStyle(monthStyle);
                monthlyRow.createCell(3).setCellValue(monthlyResponse.getMonthlyForecastBill().toPlainString());
                monthlyRow.getCell(3).setCellStyle(monthStyle);
                monthlyRow.createCell(4).setCellValue(monthlyResponse.getMonthlyActualAndForecastBillDifference().toPlainString());
                monthlyRow.getCell(4).setCellStyle(monthStyle);
                monthlyRow.createCell(5).setCellValue(monthlyResponse.getMonthlyDeviationRate().toPlainString() + "%");
                monthlyRow.getCell(5).setCellStyle(monthStyle);
                monthlyRow.createCell(6).setCellValue(monthlyResponse.getMonthlyForecastAccuracy().toPlainString() + "%");
                monthlyRow.getCell(6).setCellStyle(monthStyle);
                // 일별 데이터 삽입
                for (var dailyEntry : monthlyResponse.getDailyResponse().entrySet()) {
                    String dayKey = dailyEntry.getKey();
                    var dailyResponse = dailyEntry.getValue();
                    var dailyRow = sheet.createRow(rowNum++);
                    dailyRow.createCell(1).setCellValue("Day: " + dayKey);
                    dailyRow.getCell(1).setCellStyle(dayStyle);
                    dailyRow.createCell(2).setCellValue(dailyResponse.getDailyBill().toPlainString());
                    dailyRow.getCell(2).setCellStyle(dayStyle);
                    dailyRow.createCell(3).setCellValue(dailyResponse.getDailyForecastBill().toPlainString());
                    dailyRow.getCell(3).setCellStyle(dayStyle);
                    dailyRow.createCell(4).setCellValue(dailyResponse.getDailyActualAndForecastBillDifference().toPlainString());
                    dailyRow.getCell(4).setCellStyle(dayStyle);
                    dailyRow.createCell(5).setCellValue(dailyResponse.getDailyDeviationRate().toPlainString() + "%");
                    dailyRow.getCell(5).setCellStyle(dayStyle);
                    dailyRow.createCell(6).setCellValue(dailyResponse.getDailyForecastAccuracy().toPlainString() + "%");
                    dailyRow.getCell(6).setCellStyle(dayStyle);
                    // 시간별 데이터 삽입
                    for (var hourlyResponse : dailyResponse.getHourlyResponse()) {
                        var hourlyRow = sheet.createRow(rowNum++);
                        hourlyRow.createCell(1).setCellValue(hourlyResponse.getReferenceTime().toString());
                        hourlyRow.createCell(2).setCellValue(hourlyResponse.getBill().toPlainString());
                        hourlyRow.createCell(3).setCellValue(hourlyResponse.getForecastBill().toPlainString());
                        hourlyRow.createCell(4).setCellValue(hourlyResponse.getActualAndForecastBillDifference().toPlainString());
                        hourlyRow.createCell(5).setCellValue(hourlyResponse.getDeviationRate().toPlainString() + "%");
                        hourlyRow.createCell(6).setCellValue(hourlyResponse.getForecastAccuracy().toPlainString() + "%");
                    }
                }
            }
            // 요약 데이터 삽입
            var summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(1).setCellValue("[Summary]");
            summaryRow.getCell(1).setCellStyle(summaryStyle);
            summaryRow.createCell(2).setCellValue(summaryResponse.getSummaryBill().toPlainString());
            summaryRow.getCell(2).setCellStyle(summaryStyle);
            summaryRow.createCell(3).setCellValue(summaryResponse.getSummaryForecastBill().toPlainString());
            summaryRow.getCell(3).setCellStyle(summaryStyle);
            summaryRow.createCell(4).setCellValue(summaryResponse.getSummaryBillForecastDifference().toPlainString());
            summaryRow.getCell(4).setCellStyle(summaryStyle);
            summaryRow.createCell(5).setCellValue(summaryResponse.getSummaryDeviationRate().toPlainString() + "%");
            summaryRow.getCell(5).setCellStyle(summaryStyle);
            summaryRow.createCell(6).setCellValue(summaryResponse.getSummaryForecastAccuracy().toPlainString() + "%");
            summaryRow.getCell(6).setCellStyle(summaryStyle);
            // 엑셀 파일 다운로드 설정
            var fileName = "Energy_bill_report_" + startDate + (startDate.equals(endDate) ? "" : "_to_" + endDate) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file", e);
        }
    }

    /**
     * 특정 기간 내 업체의 에너지 사용량과 요금 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 시간별 데이터 < 일별 종합 < 월별 종합 < 전체 종합 형식으로 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량과 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    public void reportEnergyUsageAndBill(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response) {
        if (endDate == null) {
            endDate = startDate;
        }
        var summaryResponse = energyService.readEnergy(companyId, startDate, endDate);
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(startDate + " ~ " + endDate);
            sheet.setColumnWidth(0, 2 * 256);
            // 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(33);
            var titleCell = titleRow.createCell(1);
            var title = "Energy Usage and Bill Report (" + (startDate.equals(endDate) ? startDate : startDate + " ~ " + endDate) + ")";
            titleCell.setCellValue(title);
            // 타이틀 스타일
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 22);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 9)); // 타이틀 셀 병합
            // 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{
                    "Reference time", "Usage", "AI Forecast Usage", "Usage Difference", "Bill", "Forecast Bill", "Bill Difference", "Deviation Rate", "Forecast Accuracy"
            };
            // 헤더 스타일
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 셀 스타일 설정
            var dataStyle = workbook.createCellStyle();
            var format = workbook.createDataFormat();
            dataStyle.setDataFormat(format.getFormat("#,##0"));
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월 스타일 설정
            var monthStyle = workbook.createCellStyle();
            var monthFont = workbook.createFont();
            monthFont.setBold(true);
            monthStyle.setFont(monthFont);
            monthStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            monthStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            monthStyle.setAlignment(HorizontalAlignment.CENTER);
            monthStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 일 스타일 설정
            var dayStyle = workbook.createCellStyle();
            var dayFont = workbook.createFont();
            dayFont.setBold(true);
            dayStyle.setFont(dayFont);
            dayStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            dayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dayStyle.setAlignment(HorizontalAlignment.CENTER);
            dayStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // Summary 스타일 설정
            var summaryStyle = workbook.createCellStyle();
            var summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setColor(IndexedColors.WHITE.getIndex());
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setAlignment(HorizontalAlignment.CENTER);
            summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // 월별 데이터 삽입
            int rowNum = 2; // 데이터 시작 행
            for (var monthlyEntry : summaryResponse.getMonthlyResponse().entrySet()) {
                String monthKey = monthlyEntry.getKey();
                var monthlyResponse = monthlyEntry.getValue();
                // 월별 행 생성
                var monthlyRow = sheet.createRow(rowNum++);
                monthlyRow.createCell(1).setCellValue("Month: " + monthKey);
                monthlyRow.getCell(1).setCellStyle(monthStyle);
                // 월별 데이터 삽입
                monthlyRow.createCell(2).setCellValue(monthlyResponse.getMonthlyUsage().toPlainString());
                monthlyRow.getCell(2).setCellStyle(monthStyle);
                monthlyRow.createCell(3).setCellValue(monthlyResponse.getMonthlyForecastUsage().toPlainString());
                monthlyRow.getCell(3).setCellStyle(monthStyle);
                monthlyRow.createCell(4).setCellValue(monthlyResponse.getMonthlyActualAndForecastUsageDifference().toPlainString());
                monthlyRow.getCell(4).setCellStyle(monthStyle);
                monthlyRow.createCell(5).setCellValue(monthlyResponse.getMonthlyBill().toPlainString());
                monthlyRow.getCell(5).setCellStyle(monthStyle);
                monthlyRow.createCell(6).setCellValue(monthlyResponse.getMonthlyForecastBill().toPlainString());
                monthlyRow.getCell(6).setCellStyle(monthStyle);
                monthlyRow.createCell(7).setCellValue(monthlyResponse.getMonthlyActualAndForecastBillDifference().toPlainString());
                monthlyRow.getCell(7).setCellStyle(monthStyle);
                monthlyRow.createCell(8).setCellValue(monthlyResponse.getMonthlyDeviationRate().toPlainString() + "%");
                monthlyRow.getCell(8).setCellStyle(monthStyle);
                monthlyRow.createCell(9).setCellValue(monthlyResponse.getMonthlyForecastAccuracy().toPlainString() + "%");
                monthlyRow.getCell(9).setCellStyle(monthStyle);
                // 일별 데이터 삽입
                for (var dailyEntry : monthlyResponse.getDailyResponse().entrySet()) {
                    String dayKey = dailyEntry.getKey();
                    var dailyResponse = dailyEntry.getValue();
                    var dailyRow = sheet.createRow(rowNum++);
                    dailyRow.createCell(1).setCellValue("Day: " + dayKey);
                    dailyRow.getCell(1).setCellStyle(dayStyle);
                    dailyRow.createCell(2).setCellValue(dailyResponse.getDailyUsage().toPlainString());
                    dailyRow.getCell(2).setCellStyle(dayStyle);
                    dailyRow.createCell(3).setCellValue(dailyResponse.getDailyForecastUsage().toPlainString());
                    dailyRow.getCell(3).setCellStyle(dayStyle);
                    dailyRow.createCell(4).setCellValue(dailyResponse.getDailyActualAndForecastUsageDifference().toPlainString());
                    dailyRow.getCell(4).setCellStyle(dayStyle);
                    dailyRow.createCell(5).setCellValue(dailyResponse.getDailyBill().toPlainString());
                    dailyRow.getCell(5).setCellStyle(dayStyle);
                    dailyRow.createCell(6).setCellValue(dailyResponse.getDailyForecastBill().toPlainString());
                    dailyRow.getCell(6).setCellStyle(dayStyle);
                    dailyRow.createCell(7).setCellValue(dailyResponse.getDailyActualAndForecastBillDifference().toPlainString());
                    dailyRow.getCell(7).setCellStyle(dayStyle);
                    dailyRow.createCell(8).setCellValue(dailyResponse.getDailyDeviationRate().toPlainString() + "%");
                    dailyRow.getCell(8).setCellStyle(dayStyle);
                    dailyRow.createCell(9).setCellValue(dailyResponse.getDailyForecastAccuracy().toPlainString() + "%");
                    dailyRow.getCell(9).setCellStyle(dayStyle);
                    // 시간별 데이터 삽입
                    for (var hourlyResponse : dailyResponse.getHourlyResponse()) {
                        var hourlyRow = sheet.createRow(rowNum++);
                        hourlyRow.createCell(1).setCellValue(hourlyResponse.getReferenceTime().toString());
                        hourlyRow.createCell(2).setCellValue(hourlyResponse.getUsage().toPlainString());
                        hourlyRow.createCell(3).setCellValue(hourlyResponse.getForecastUsage().toPlainString());
                        hourlyRow.createCell(4).setCellValue(hourlyResponse.getActualAndForecastUsageDifference().toPlainString());
                        hourlyRow.createCell(5).setCellValue(hourlyResponse.getBill().toPlainString());
                        hourlyRow.createCell(6).setCellValue(hourlyResponse.getForecastBill().toPlainString());
                        hourlyRow.createCell(7).setCellValue(hourlyResponse.getActualAndForecastBillDifference().toPlainString());
                        hourlyRow.createCell(8).setCellValue(hourlyResponse.getDeviationRate().toPlainString() + "%");
                        hourlyRow.createCell(9).setCellValue(hourlyResponse.getForecastAccuracy().toPlainString() + "%");
                    }
                }
            }
            // 요약 데이터 삽입
            var summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(1).setCellValue("[Summary]");
            summaryRow.getCell(1).setCellStyle(summaryStyle);
            summaryRow.createCell(2).setCellValue(summaryResponse.getSummaryUsage().toPlainString());
            summaryRow.getCell(2).setCellStyle(summaryStyle);
            summaryRow.createCell(3).setCellValue(summaryResponse.getSummaryForecastUsage().toPlainString());
            summaryRow.getCell(3).setCellStyle(summaryStyle);
            summaryRow.createCell(4).setCellValue(summaryResponse.getSummaryUsageForecastDifference().toPlainString());
            summaryRow.getCell(4).setCellStyle(summaryStyle);
            summaryRow.createCell(5).setCellValue(summaryResponse.getSummaryBill().toPlainString());
            summaryRow.getCell(5).setCellStyle(summaryStyle);
            summaryRow.createCell(6).setCellValue(summaryResponse.getSummaryForecastBill().toPlainString());
            summaryRow.getCell(6).setCellStyle(summaryStyle);
            summaryRow.createCell(7).setCellValue(summaryResponse.getSummaryBillForecastDifference().toPlainString());
            summaryRow.getCell(7).setCellStyle(summaryStyle);
            summaryRow.createCell(8).setCellValue(summaryResponse.getSummaryDeviationRate().toPlainString() + "%");
            summaryRow.getCell(8).setCellStyle(summaryStyle);
            summaryRow.createCell(9).setCellValue(summaryResponse.getSummaryForecastAccuracy().toPlainString() + "%");
            summaryRow.getCell(9).setCellStyle(summaryStyle);
            // 엑셀 파일 다운로드 설정
            var fileName = "Energy_usage_and_bill_report_" + startDate + (startDate.equals(endDate) ? "" : "_to_" + endDate) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file", e);
        }
    }

    /**
     * 특정 기간 내 업체의 IoT 상태 이력을 엑셀 파일로 제공합니다. (조회 결과는 시간별로 집계됩니다)
     *
     * @param readIotHistoryRequestDto IoT 상태 이력을 조회하기 위한 요청 DTO입니다.
     * @param response  HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public void reportIotStatusHistory(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto, HttpServletResponse response) {
        // 종료일이 null인 경우 시작일과 동일하게 설정
        if (readIotHistoryRequestDto.getEndDate() == null) {
            readIotHistoryRequestDto.setEndDate(readIotHistoryRequestDto.getStartDate());
        }
        // IoT 상태 이력 조회 (페이징 없이 전체 조회를 위해 Pageable.unpaged() 사용)
        var iotHistoryResponse = iotStatusHistoryService.read(readIotHistoryRequestDto, Pageable.unpaged());
        // 데이터 시간순 정렬 (내림차순)
        var sortedHistoryList = iotHistoryResponse.getIotHistoryList().stream()
                .sorted(Comparator.comparing(IotStatusHistoryDto.ReadIotHistoryResponse::getCreatedDate).reversed())
                .toList();
        try (var workbook = new XSSFWorkbook()) {
            // 엑셀 시트 생성
            var sheet = workbook.createSheet(String.format("%s ~ %s",
                    readIotHistoryRequestDto.getStartDate(), readIotHistoryRequestDto.getEndDate()));
            sheet.setColumnWidth(0, 2 * 256);
            // 엑셀 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(33);
            var titleCell = titleRow.createCell(1);
            var title = String.format("IoT Status History Report %s ~ %s",
                    readIotHistoryRequestDto.getStartDate(),
                    readIotHistoryRequestDto.getEndDate().equals(readIotHistoryRequestDto.getStartDate())
                            ? readIotHistoryRequestDto.getStartDate()
                            : readIotHistoryRequestDto.getEndDate());
            titleCell.setCellValue(title);
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 22);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 5));
            // 엑셀 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{"Reference Time", "Serial Number", "Location", "Type", "Status"};
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 셀 스타일 설정
            var dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            // 엑셀 데이터 채우기
            var rowNum = 2;
            for (var history : sortedHistoryList) {
                var row = sheet.createRow(rowNum++);
                // 첫 번째 포맷: yyyy-MM-dd HH:mm:ss (분과 초는 00으로 고정)
                row.createCell(1).setCellValue(history.getCreatedDate().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00")));
                // 두 번째 포맷: yyyy-MM-dd HH시 (선택 시 주석 해제하고 사용)
                //row.createCell(1).setCellValue(history.getCreatedDate().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시")));
                row.createCell(2).setCellValue(history.getSerialNumber());
                row.createCell(3).setCellValue(history.getLocation());
                row.createCell(4).setCellValue(history.getType().toString());
                row.createCell(5).setCellValue(history.getStatus().toString());
            }
            // 파일명 설정 및 응답으로 엑셀 파일 전송
            var fileName = String.format("IoT Status History Report %s%s.xlsx",
                    readIotHistoryRequestDto.getStartDate(),
                    readIotHistoryRequestDto.getStartDate().equals(readIotHistoryRequestDto.getEndDate())
                            ? ""
                            : " ~ " + readIotHistoryRequestDto.getEndDate());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Error creating reportIotStatusHistory Excel file: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel file", e);
        }
    }

    /**
     * 기간 내 이상 탐지 관련 알람 내역 엑셀 다운로드
     * 기간 내 이상 탐지 관련 알람 내역 엑셀 다운로드 할 수 있습니다.
     *
     * @param companyId 업체 ID
     * @param startDateTime 알람 생성일시 검색 시작일
     * @param endDateTime 알람 생성일시 검색 종료일
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public void reportAnomalyAlarms(Long companyId, LocalDateTime startDateTime, LocalDateTime endDateTime, HttpServletResponse response) {
        // 알람 데이터 조회
        var readAlarmRequest = AlarmDto.ReadAlarmRequest.builder()
                .companyId(companyId)
                .type(List.of(AlarmType.MAXIMUM_ENERGY_USAGE, AlarmType.MINIMUM_ENERGY_USAGE))
                .notify(true)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .build();
        var pageable = Pageable.unpaged();
        var alarmResponse = alarmService.read(readAlarmRequest, pageable);
        try (var workbook = new XSSFWorkbook()) {
            // 엑셀 시트 생성
            var sheet = workbook.createSheet("Anomaly Alarms");
            sheet.setColumnWidth(0, 2 * 256);
            // 엑셀 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(24);
            var titleCell = titleRow.createCell(1);
            var title = "Anomaly Alarms Report " + startDateTime.toLocalDate() + " ~ " + endDateTime.toLocalDate();
            titleCell.setCellValue(title);
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 7));
            // 엑셀 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{
                    "Company Name", "Type", "Priority", "Message",
                    "Is Read", "Created Date", "Expiration Date"
            };
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 엑셀 데이터 채우기
            var rowNum = 2;
            for (var alarm : alarmResponse.getAlarmList()) {
                var row = sheet.createRow(rowNum++);
                row.createCell(1).setCellValue(alarm.getCompanyName());
                row.createCell(2).setCellValue(alarm.getType().name());
                row.createCell(3).setCellValue(alarm.getPriority().name());
                row.createCell(4).setCellValue(alarm.getMessage());
                row.createCell(5).setCellValue(alarm.getIsRead() ? "Yes" : "No");
                row.createCell(6).setCellValue(alarm.getCreatedDate().toString());
                row.createCell(7).setCellValue(alarm.getExpirationDate().toString());
            }
            // 엑셀 파일 다운로드 설정
            var fileName = "Anomaly_Alarms_Report_" + startDateTime.toLocalDate() + "_" + endDateTime.toLocalDate() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Error creating reportAnomalyAlarms Excel file: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel file", e);
        }
    }
}