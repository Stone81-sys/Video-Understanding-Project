package com.example.demo.model;

/**
 * 视频识别请求 DTO，前端传入的视频 URL
 */
public class VideoRecognitionRequest {

    /** 待识别的视频 URL 地址 */
    private String videoUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
