package com.app.medialoader.tinyhttpd.request;

import com.app.medialoader.tinyhttpd.HttpHeaders;
import com.app.medialoader.tinyhttpd.HttpVersion;

/**
 * 请求接口
 *
 * @author vincanyang
 */
public interface Request {

    HttpMethod method();

    String url();

    HttpVersion protocol();

    HttpHeaders headers();

    String getParam(String name);
}
