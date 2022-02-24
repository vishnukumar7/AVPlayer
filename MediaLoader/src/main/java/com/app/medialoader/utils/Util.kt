package com.app.medialoader.utils

import android.text.TextUtils
import android.util.Base64;
import android.webkit.MimeTypeMap
import com.app.medialoader.tinyhttpd.HttpConstants
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object Util {


        const val LOCALHOST = "127.0.0.1"


        const val DEFAULT_BUFFER_SIZE = 8192


        const val CHARSET_DEFAULT = "UTF-8"

        private const val MAX_EXTENSION_LENGTH = 4

        fun <T> notEmpty(`object`: T?): T {
            if (`object` is String) {
                if (TextUtils.isEmpty(`object` as String?)) {
                    throw NullPointerException(`object`.javaClass.simpleName + " can't be empty")
                }
            } else {
                if (`object` == null) {
                    throw NullPointerException(" can't be null")
                }
            }
            return `object`
        }

        fun encode(url: String?): String? {
            return try {
                URLEncoder.encode(url, CHARSET_DEFAULT)
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("Encoding not supported", e)
            }
        }

        fun decode(url: String?): String? {
            return try {
                URLDecoder.decode(url, CHARSET_DEFAULT)
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("Decoding not supported", e)
            }
        }

        fun createUrl(host: String?, port: Int, path: String?): String? {
            return java.lang.String.format(Locale.US, "http://%s:%d/%s", host, port, encode(path))
        }

        fun createUrl(host: String?, port: Int, path: String, secret: String): String? {
            val timestamp = System.currentTimeMillis().toString()
            val sign = getHmacSha1(path + timestamp, secret)
            val sb = StringBuilder("http://%s:%d/%s")
            sb.append("?").append(HttpConstants.PARAM_SIGN).append("=%s")
            sb.append("&").append(HttpConstants.PARAM_TIMESTAMP).append("=%s")
            return java.lang.String.format(
                Locale.US,
                sb.toString(),
                host,
                port,
                encode(path),
                encode(sign),
                encode(timestamp)
            )
        }

        fun getHmacSha1(s: String, keyString: String): String? {
            var hmacSha1: String? = null
            try {
                val key = SecretKeySpec(keyString.toByteArray(), "HmacSHA1")
                val mac: Mac = Mac.getInstance("HmacSHA1")
                mac.init(key)
                val bytes: ByteArray = mac.doFinal(s.toByteArray())
                hmacSha1 = Base64.encodeToString(bytes, Base64.DEFAULT).toString()
            } catch (e: InvalidKeyException) {
            } catch (e: NoSuchAlgorithmException) {
            }
            return hmacSha1
        }

        fun getMimeTypeFromUrl(url: String?): String? {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return if (TextUtils.isEmpty(extension)) "" else MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)
        }

        fun getExtensionFromUrl(url: String): String? {
            val dotIndex = url.lastIndexOf('.')
            val slashIndex = url.lastIndexOf('/')
            return if (dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length) url.substring(
                dotIndex + 1,
                url.length
            ) else ""
        }

        fun getMD5(string: String): String? {
            return try {
                val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
                val digestBytes: ByteArray = messageDigest.digest(string.toByteArray())
                bytesToHexString(digestBytes)
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException(e)
            }
        }

        private fun bytesToHexString(bytes: ByteArray): String? {
            val sb = java.lang.StringBuilder()
            for (i in bytes.indices) {
                sb.append(String.format("%02x", bytes[i])) // 以十六进制(x)输出,2为指定的输出字段的宽度.如果位数小于2,则左端补0
            }
            return sb.toString()
        }


}