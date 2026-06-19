package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应封装
 *
 * @param <T> data 的具体类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 状态码，200 表示成功 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 中文字幕下载地址（从异步结果中解析） */
    private String chineseSubtitleUrl;

    /** 英文字幕下载地址（从异步结果中解析） */
    private String englishSubtitleUrl;

    /** 阿里云原始响应 JSON 字符串，供前端校验 */
    private String rawResponse;

    private ApiResponse() {}

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = "success";
        resp.data = data;
        return resp;
    }

    /**
     * 成功响应（含字幕下载链接）
     */
    public static <T> ApiResponse<T> success(T data, String chineseSubtitleUrl, String englishSubtitleUrl, String rawResponse) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = "success";
        resp.data = data;
        resp.chineseSubtitleUrl = chineseSubtitleUrl;
        resp.englishSubtitleUrl = englishSubtitleUrl;
        resp.rawResponse = rawResponse;
        return resp;
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = code;
        resp.message = message;
        return resp;
    }

    // ====== getters & setters ======

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getChineseSubtitleUrl() { return chineseSubtitleUrl; }
    public void setChineseSubtitleUrl(String chineseSubtitleUrl) { this.chineseSubtitleUrl = chineseSubtitleUrl; }

    public String getEnglishSubtitleUrl() { return englishSubtitleUrl; }
    public void setEnglishSubtitleUrl(String englishSubtitleUrl) { this.englishSubtitleUrl = englishSubtitleUrl; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
