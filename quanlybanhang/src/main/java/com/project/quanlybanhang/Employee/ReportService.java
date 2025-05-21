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
                Files.writeString(srcReportJsonFilePath, "[]", StandardOpenOption.CREATE);
            }
            if (!Files.exists(targetReportJsonFilePath) && Files.exists(srcReportJsonFilePath.getParent())) {
                Files.createDirectories(targetReportJsonFilePath.getParent()); // Ensure target parent dir exists
                Files.writeString(targetReportJsonFilePath, "[]", StandardOpenOption.CREATE);
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
        if (Files.exists(srcReportJsonFilePath)) {
            if (Files.size(srcReportJsonFilePath) > 0) {
                try (InputStream is = Files.newInputStream(srcReportJsonFilePath)) {
                    return mapper.readValue(is, new TypeReference<List<ReportData>>() {});
                }
            } else {
                return new ArrayList<>(); // File rỗng
            }
        }
        // Nếu src không có, hoặc có lỗi, thử target (nhưng target nên là bản copy của src)
        else if (Files.exists(targetReportJsonFilePath)) {
            if (Files.size(targetReportJsonFilePath) > 0) {
                try (InputStream is = Files.newInputStream(targetReportJsonFilePath)) {
                    return mapper.readValue(is, new TypeReference<List<ReportData>>() {});
                }
            } else {
                return new ArrayList<>();
            }
        }
        // Nếu cả hai đều không tồn tại (dù constructor đã cố tạo)
        return new ArrayList<>();
    }


    private synchronized void saveReportsToFile(List<ReportData> reports) throws IOException {
        ensureReportDirectoriesExist();
        // Ghi vào src
        try (OutputStream osSrc = Files.newOutputStream(srcReportJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(osSrc, reports);
        } catch (IOException e) {
            System.err.println("[ReportService] CRITICAL: Failed to write report.json to src path: " + srcReportJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            throw e;
        }
        // Ghi vào target
        try (OutputStream osTarget = Files.newOutputStream(targetReportJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(osTarget, reports);
        } catch (IOException e) {
            System.err.println("[ReportService] WARNING: Failed to write report.json to target path: " + targetReportJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
        }
    }

    public synchronized void addReport(ReportData reportData) throws IOException {
        List<ReportData> reports = getAllReports();
        reports.add(reportData);
        saveReportsToFile(reports);
    }
}