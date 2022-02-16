package com.app.medialoader.manager;

import com.app.medialoader.tinyhttpd.request.Request;
import com.app.medialoader.tinyhttpd.response.Response;
import com.app.medialoader.tinyhttpd.response.ResponseException;

import java.io.IOException;

/**
 * Media业务接口
 *
 * @author vincanyang
 */
public interface MediaManager {

    void responseByRequest(Request request, Response response) throws ResponseException, IOException;

    void pauseDownload(String url);

    void resumeDownload(String url);

    void destroy();
}
