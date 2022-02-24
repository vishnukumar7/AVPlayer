package com.app.medialoader.data.file

import java.io.File
import java.io.RandomAccessFile

/**
 * 大文件读写RandomAccessFile性能较差，所以需要一个具备缓存能力的RandomAccessFile
 * //TODO
 *
 * @author vincanyang
 */
class BufferedRandomAccessFile(file: File?, mode: String?) : RandomAccessFile(file, mode)