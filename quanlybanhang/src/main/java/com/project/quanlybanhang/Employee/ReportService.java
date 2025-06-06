package com.project.quanlybanhang.Employee; // Hoặc com.project.quanlybanhang.Report

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private static final String REPORT_DATA_RELATIVE_PATH = "static/report/report.json";
    private final ObjectMapper mapper;
    private final Path srcReportJsonFilePath;
    private final Path targetReportJsonFilePath;

    public ReportService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String projectDir = System.getProperty("user.dir");
        this.srcReportJsonFilePath = Paths.get(projectDir, "src", "main", "resources", REPORT_DATA_RELATIVE_PATH);
        this.targetReportJsonFilePath = Paths.get(projectDir, "target", "classes", REPORT_DATA_RELATIVE_PATH);

        try {
            ensureReportDirectoriesExist();
            // Initialize file if it doesn't exist
            if (!Files.exists(srcReportJsonFilePath)) {
                Files.writeString(srcReportJsonFilePath, "[]", StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
           
            if (Files.exists(srcReportJsonFilePath.getParent())) { 
                if (!Files.exists(targetReportJsonFilePath.getParent())) { 
                    Files.createDirectories(targetReportJsonFilePath.getParent()); 
                }
                if (!Files.exists(targetReportJsonFilePath)) { 
                    Files.writeString(targetReportJsonFilePath, "[]", StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            }

        } catch (IOException e) {
            System.err.println("[ReportService] Error initializing report.json: " + e.getMessage());
        }
    }

    private void ensureReportDirectoriesExist() throws IOException {
        Path srcParentDir = srcReportJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
        }
        Path targetParentDir = targetReportJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
        }
    }

    public synchronized List<ReportData> getAllReports() throws IOException {
        ensureReportDirectoriesExist();
        // Ưu tiên đọc từ src
        Path pathToRead = srcReportJsonFilePath;

        

        if (!Files.exists(pathToRead) || Files.size(pathToRead) == 0) {
            
            if (Files.exists(targetReportJsonFilePath) && Files.size(targetReportJsonFilePath) > 0) {
                System.out.println("[ReportService] Warning: srcReportJsonFilePath is empty or missing, attempting to read from targetReportJsonFilePath.");
                pathToRead = targetReportJsonFilePath;
            } else {
               
                if (!Files.exists(srcReportJsonFilePath)) { // Nếu src chưa được tạo, cố gắng tạo file rỗng
                    Files.writeString(srcReportJsonFilePath, "[]", StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
                if (!Files.exists(targetReportJsonFilePath) && Files.exists(targetReportJsonFilePath.getParent())) {
                    Files.writeString(targetReportJsonFilePath, "[]", StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
                return new ArrayList<>();
            }
        }

        try (InputStream is = Files.newInputStream(pathToRead)) {
            return mapper.readValue(is, new TypeReference<List<ReportData>>() {});
        } catch (IOException e) {
            System.err.println("[ReportService] Error reading reports from " + pathToRead.toAbsolutePath() + ": " + e.getMessage());
           
            return new ArrayList<>(); 
        }
    }


    private synchronized void saveReportsToFile(List<ReportData> reports) throws IOException {
        ensureReportDirectoriesExist();
        // Ghi vào src
        try (OutputStream osSrc = Files.newOutputStream(srcReportJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            mapper.writeValue(osSrc, reports);
            System.out.println("[ReportService] Successfully wrote to src: " + srcReportJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ReportService] CRITICAL: Failed to write report.json to src path: " + srcReportJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            throw e; // Ném lại lỗi để controller có thể xử lý
        }


        if (targetReportJsonFilePath.getParent() != null && !Files.exists(targetReportJsonFilePath.getParent())) {
            Files.createDirectories(targetReportJsonFilePath.getParent());
        }
        try (OutputStream osTarget = Files.newOutputStream(targetReportJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            mapper.writeValue(osTarget, reports);
            System.out.println("[ReportService] Successfully wrote to target: " + targetReportJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
           
            System.err.println("[ReportService] WARNING: Failed to write report.json to target path: " + targetReportJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
        }
    }

    public synchronized void addReport(ReportData reportData) throws IOException {
        List<ReportData> reports = getAllReports(); 
        reports.add(reportData);
        saveReportsToFile(reports);
    }


    public synchronized void clearAllReports() throws IOException {
        List<ReportData> emptyList = new ArrayList<>();
        saveReportsToFile(emptyList); 
        System.out.println("[ReportService] All reports have been cleared from src and target.");
    }

}