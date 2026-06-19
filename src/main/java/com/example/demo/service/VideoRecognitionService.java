package com.example.demo.service;

import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListAdvanceRequest;
import com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListResponse;
import com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListResponseBody;
import com.aliyun.viapi20230117.models.GetAsyncJobResultRequest;
import com.aliyun.viapi20230117.models.GetAsyncJobResultResponse;
import com.aliyun.viapi20230117.models.GetAsyncJobResultResponseBody;
import com.example.demo.config.AliyunConfig;
import com.example.demo.text.GetAsyncJobResult;
import com.example.demo.text.RecognizeVideoCastCrewList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VideoRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(VideoRecognitionService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + File.separator + "video-demo-upload";

    private final AliyunConfig aliyunConfig;

    public VideoRecognitionService(AliyunConfig aliyunConfig) {
        this.aliyunConfig = aliyunConfig;
    }

    /**
     * 上传视频并提交识别任务，立即返回 jobId
     */
    public Map<String, Object> submitVideoFromFile(MultipartFile file) throws Exception {
        log.info("提交视频识别任务, fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        Path uploadDir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        String tempFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File tempFile = uploadDir.resolve(tempFileName).toFile();
        file.transferTo(tempFile);
        log.info("视频已保存到临时文件: {}", tempFile.getAbsolutePath());

        try {
            String jobId = submitRecognitionByFile(tempFile);
            log.info("任务提交成功, jobId={}", jobId);

            Map<String, Object> result = new HashMap<>();
            result.put("jobId", jobId);
            result.put("status", "SUBMITTED");
            result.put("message", "任务已提交");
            return result;
        } finally {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (Exception e) {
                log.warn("清理临时文件失败: {}", tempFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 查询异步任务结果，所有错误封装在返回 Map 中，不抛异常
     */
    public Map<String, Object> queryResult(String jobId) {
        try {
            com.aliyun.viapi20230117.Client client = GetAsyncJobResult.createClient(
                    aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());

            GetAsyncJobResultRequest request = new GetAsyncJobResultRequest().setJobId(jobId);
            RuntimeOptions runtime = new RuntimeOptions();
            GetAsyncJobResultResponse response = client.getAsyncJobResultWithOptions(request, runtime);
            GetAsyncJobResultResponseBody body = response.getBody();
            com.aliyun.viapi20230117.models.GetAsyncJobResultResponseBody.GetAsyncJobResultResponseBodyData data = body.getData();

            if (data == null) {
                log.warn("阿里云返回 Data 为空, jobId={}, requestId={}", jobId, body.getRequestId());
                return errorResult(jobId, "INVALID_RESPONSE", "阿里云返回数据为空，可能 jobId 无效", null);
            }

            String status = data.getStatus();
            String errorCode = data.getErrorCode();
            String errorMessage = data.getErrorMessage();
            String resultJson = data.getResult();

            // === INFO 级别打印真实任务状态 ===
            log.info("轮询结果 —— jobId={} | status={} | errorCode={} | errorMessage={} | hasResult={}",
                    jobId, status, errorCode, errorMessage, resultJson != null && !resultJson.isEmpty());

            if (status == null || status.isEmpty()) {
                // 阿里云返回的 Data 中没有 Status → 可能 jobId 不存在
                log.warn("阿里云返回 Status 为空, jobId={}, requestId={}", jobId, body.getRequestId());
                return errorResult(jobId, "INVALID_RESPONSE", "阿里云未返回任务状态，可能 jobId 无效", errorCode);
            }

            switch (status) {
                case "PROCESS_SUCCESS":
                    return handleSuccess(jobId, resultJson);
                case "PROCESS_FAILED":
                    log.warn("阿里云处理失败, jobId={}, errorCode={}, errorMessage={}", jobId, errorCode, errorMessage);
                    return failedResult(jobId, status, errorCode, errorMessage);
                case "QUEUING":
                    log.info("任务排队中, jobId={}", jobId);
                    return statusResult(jobId, status, "QUEUING", "任务排队中");
                case "PROCESSING":
                    return statusResult(jobId, status, "PROCESSING", "处理中");
                default:
                    log.info("未知状态, jobId={}, status={}", jobId, status);
                    return statusResult(jobId, status != null ? status : "UNKNOWN", "UNKNOWN", "未知状态: " + status);
            }

        } catch (TeaException e) {
            String code = e.getCode() != null ? e.getCode() : "";
            log.error("阿里云SDK异常, jobId={}, code={}, message={}", jobId, code, e.getMessage());

            if (isAuthError(code, e.getMessage())) {
                return errorResult(jobId, "AUTH_ERROR", "鉴权失败: " + e.getMessage(), code);
            }
            if (isRateLimitError(code, e.getMessage())) {
                return errorResult(jobId, "RATE_LIMITED", "请求过于频繁，稍后重试", code);
            }
            return errorResult(jobId, "RETRYABLE", "SDK异常: " + e.getMessage(), code);

        } catch (Exception e) {
            log.error("查询异常, jobId={}", jobId, e);
            return errorResult(jobId, "RETRYABLE", "系统异常: " + e.getMessage(), null);
        }
    }

    // ==================== 结果构建 ====================

    private Map<String, Object> handleSuccess(String jobId, String resultJson) {
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("status", "PROCESS_SUCCESS");
        result.put("message", "识别完成");

        if (resultJson != null && !resultJson.isEmpty()) {
            parseSubtitles(resultJson, result);
        }

        log.info("识别成功, jobId={}, chineseUrl={}, englishUrl={}",
                jobId, result.get("chineseSubtitleUrl"), result.get("englishSubtitleUrl"));
        return result;
    }

    private void parseSubtitles(String resultJson, Map<String, Object> result) {
        try {
            JsonNode resultNode = objectMapper.readTree(resultJson);
            if (resultNode.has("subtitlesResults") && resultNode.get("subtitlesResults").isArray()
                    && resultNode.get("subtitlesResults").size() > 0) {
                JsonNode subtitles = resultNode.get("subtitlesResults").get(0);
                if (subtitles.has("subtitlesChineseResultsUrl")) {
                    result.put("chineseSubtitleUrl", subtitles.get("subtitlesChineseResultsUrl").asText());
                }
                if (subtitles.has("subtitlesEnglishResultsUrl")) {
                    result.put("englishSubtitleUrl", subtitles.get("subtitlesEnglishResultsUrl").asText());
                }
            }
            result.put("rawResponse", resultJson);
        } catch (Exception e) {
            log.warn("解析字幕地址失败: {}", e.getMessage());
        }
    }

    private Map<String, Object> statusResult(String jobId, String status, String frontendStatus, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("status", frontendStatus);    // 前端用
        result.put("aliyunStatus", status);       // 阿里云原始值
        result.put("message", msg);
        return result;
    }

    private Map<String, Object> failedResult(String jobId, String status, String errorCode, String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("status", "PROCESS_FAILED");
        result.put("aliyunStatus", status);
        result.put("errorCode", errorCode != null ? errorCode : "");
        result.put("errorMessage", errorMessage != null ? errorMessage : "");
        result.put("message", buildFailMessage(errorCode, errorMessage));
        return result;
    }

    private Map<String, Object> errorResult(String jobId, String errorCode, String errorMessage, String aliyunCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("status", "ERROR");
        result.put("errorCode", errorCode);
        result.put("aliyunCode", aliyunCode != null ? aliyunCode : "");
        result.put("message", errorMessage);
        return result;
    }

    private String buildFailMessage(String errorCode, String errorMessage) {
        String msg = "识别处理失败";
        if (errorCode != null && !errorCode.isEmpty()) {
            msg += " (" + errorCode + ")";
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            msg += ": " + errorMessage;
        }
        return msg;
    }

    private boolean isAuthError(String code, String msg) {
        String lower = (code + " " + (msg != null ? msg : "")).toLowerCase();
        return lower.contains("invalidaccesskeyid") || lower.contains("signature")
                || lower.contains("forbidden") || lower.contains("notpurchase")
                || lower.contains("unauthorized") || lower.contains("authfailure");
    }

    private boolean isRateLimitError(String code, String msg) {
        String lower = (code + " " + (msg != null ? msg : "")).toLowerCase();
        return lower.contains("throttling") || lower.contains("requestlimitexceeded")
                || lower.contains("flowcontrol");
    }

    // ==================== 提交任务 ====================

    private String submitRecognitionByFile(File videoFile) throws Exception {
        com.aliyun.videorecog20200320.Client client = RecognizeVideoCastCrewList.createClient(
                aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        RuntimeOptions runtime = new RuntimeOptions();

        try (InputStream inputStream = new FileInputStream(videoFile)) {
            RecognizeVideoCastCrewListAdvanceRequest request = new RecognizeVideoCastCrewListAdvanceRequest()
                    .setVideoUrlObject(inputStream);
            RecognizeVideoCastCrewListResponse response = client.recognizeVideoCastCrewListAdvance(request, runtime);
            RecognizeVideoCastCrewListResponseBody body = response.getBody();

            log.info("提交识别任务返回 —— requestId={}, dataExist={}",
                    body.getRequestId(), body.getData() != null);

            // 阿里云异步 API 用 requestId 作为 jobId
            return body.getRequestId();
        } catch (TeaException e) {
            log.error("提交失败, code={}, message={}", e.getCode(), e.getMessage());
            throw new RuntimeException("提交失败: " + e.getMessage(), e);
        }
    }
}
