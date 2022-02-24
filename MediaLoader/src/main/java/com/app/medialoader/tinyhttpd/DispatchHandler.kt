package com.app.medialoader.tinyhttpd

import com.app.medialoader.utils.LogUtil
import com.app.medialoader.utils.Util
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class DispatchHandler @Throws(IOException::class) constructor(
    address: InetAddress,
    port: Int,
    startSignal: CountDownLatch,
    httpServer: TinyHttpd
) : Runnable {

    private var mSelector: Selector? = null

    private var mServer: ServerSocketChannel? = null

    @Volatile
    private var mIsRunning = false

    private var mHttpServer: TinyHttpd = httpServer

    private var mThreadStartSignal: CountDownLatch? = startSignal

    private val mRequestByteBuffer =
        ByteBuffer.allocateDirect(Util.DEFAULT_BUFFER_SIZE) //request最长为8192


    private val mServerExecutorService = Executors.newCachedThreadPool()

    init {
        mSelector= Selector.open()
        mServer= ServerSocketChannel.open()
        with(mServer) {
            this!!.socket().bind(InetSocketAddress(address,port))
            configureBlocking(false)
            register(mSelector,SelectionKey.OP_ACCEPT)
        }
    }
    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
       android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
        mThreadStartSignal?.countDown()
        mIsRunning=true
        while (mIsRunning){
            var key: SelectionKey?=null
            try {
                mSelector!!.select()
                val keyIterator = mSelector!!.selectedKeys().iterator()
                while (keyIterator.hasNext()) {
                    key = keyIterator.next()
                    keyIterator.remove()
                    if (!key.isValid) {
                        continue
                    }
                    if (key.isAcceptable) {
                        handleAccept(key)
                    } else if (key.isReadable) {
                        val channel = key.channel() as SocketChannel
                        val requestBytes: ByteArray? = readRequestBytes(channel)
                        if (requestBytes != null) {
                            mServerExecutorService.submit(
                                IOHandler(
                                    channel,
                                    requestBytes,
                                    mHttpServer
                                )
                            )
                        }
                    }
                }
            }catch (io: IOException){
                key?.cancel()
                LogUtil.e(io)
            }
        }
    }

    @Throws(IOException::class)
    private fun handleAccept(key: SelectionKey) {
        val serverSocketChannel = key.channel() as ServerSocketChannel
        val clientChannel = serverSocketChannel.accept()
        clientChannel.configureBlocking(false)
        clientChannel.register(mSelector, SelectionKey.OP_READ)
    }

    @Throws(IOException::class)
    private fun readRequestBytes(channel: SocketChannel): ByteArray? {
        val readCount: Int = try {
            channel.read(mRequestByteBuffer)
        } catch (e: IOException) {
            LogUtil.d("The client closed the connection", e)
            channel.close()
            throw e
        }
        if (readCount < 0) {
            LogUtil.d("The client shut the socket down")
            channel.close()
            return null
        }
        mRequestByteBuffer.flip()
        val requestBytes = ByteArray(readCount)
        mRequestByteBuffer[requestBytes]
        mRequestByteBuffer.clear()
        return requestBytes
    }

    fun getPort(): Int {
        return mServer!!.socket().localPort
    }

    fun destroy() {
        mIsRunning = false
        try {
            mServer!!.close()
        } catch (e: IOException) {
            LogUtil.e("error closing server", e)
        }
    }
}