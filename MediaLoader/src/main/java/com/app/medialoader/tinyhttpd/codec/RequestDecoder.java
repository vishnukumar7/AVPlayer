package com.app.medialoader.tinyhttpd.codec;

import com.app.medialoader.tinyhttpd.request.Request;
import com.app.medialoader.tinyhttpd.response.ResponseException;

/**
 * {@link Request}的解码器
 *
 * @author vincanyang
 */
public interface RequestDecoder<T extends Request> {

    T decode(byte[] bytes) throws ResponseException;
}
