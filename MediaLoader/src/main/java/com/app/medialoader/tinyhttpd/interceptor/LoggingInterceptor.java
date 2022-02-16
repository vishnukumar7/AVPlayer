package com.app.medialoader.tinyhttpd.interceptor;

import com.app.medialoader.tinyhttpd.request.Request;
import com.app.medialoader.tinyhttpd.response.Response;
import com.app.medialoader.tinyhttpd.response.ResponseException;
import com.app.medialoader.utils.LogUtil;
import com.app.medialoader.utils.Util;

import java.io.IOException;

/**
 * 日志拦截器
 *
 * @author wencanyang
 */
public class LoggingInterceptor implements Interceptor {

    @Override
    public void intercept(Chain chain) throws ResponseException,IOException {
        long t1 = System.nanoTime();
        Request request = chain.request();
        Response response = chain.response();
                LogUtil.e(String.format("Sending request %s with headers %n%s", Util.decode(request.url()), request.headers()));
        chain.proceed(request,response);
        long t2 = System.nanoTime();
        LogUtil.e(String.format("Received response for %s in %.1fms with headers %n%s", Util.decode(request.url()), (t2 - t1) / 1e6d, response.headers()));
    }
}
