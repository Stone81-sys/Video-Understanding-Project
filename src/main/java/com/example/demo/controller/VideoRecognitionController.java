package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.service.VideoRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoRecognitionController {

    private static final Logger log = LoggerFactory.getLogger(VideoRecognitionController.class);
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    private final VideoRecognitionService videoRecognitionService;

    public VideoRecognitionController(VideoRecognitionService videoRecognitionService) {
        this.videoRecognitionService = videoRecognitionService;
    }

    /**
     * 上传视频，提交异步识别任务
     */
    @PostMapping(value = "/recognize-cast-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> submitTask(@RequestParam("video") MultipartFile file) {
        log.info("接收视频上传, fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "上传的文件为空"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "文件大小超过限制（最大100MB）"));
        }

        try {
            Map<String, Object> result = videoRecognitionService.submitVideoFromFile(file);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("提交失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "提交失败: " + e.getMessage()));
        }
    }

    /**
     * 查询异步任务结果
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<ApiResponse<?>> queryResult(@PathVariable String jobId) {
        Map<String, Object> result = videoRecognitionService.queryResult(jobId);
        String status = (String) result.get("status");

        if ("PROCESS_SUCCESS".equals(status)) {
            String chineseUrl = (String) result.get("chineseSubtitleUrl");
            String englishUrl = (String) result.get("englishSubtitleUrl");
            String rawJson = (String) result.get("rawResponse");
            return ResponseEntity.ok(ApiResponse.success(result, chineseUrl, englishUrl, rawJson));
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
