package com.nortal.test.restassured.logs

import java.io.IOException
import com.nortal.test.restassured.logs.LoggingExclusionFilter
import org.apache.commons.logging.LogFactory
import org.apache.http.impl.conn.DefaultClientConnection
import org.apache.http.impl.conn.LoggingSessionInputBuffer
import org.apache.http.impl.conn.LoggingSessionOutputBuffer
import org.apache.http.impl.conn.Wire
import org.apache.http.impl.io.SocketInputBuffer
import org.apache.http.impl.io.SocketOutputBuffer
import org.apache.http.io.SessionInputBuffer
import org.apache.http.io.SessionOutputBuffer
import org.apache.http.params.HttpParams
import org.apache.http.params.HttpProtocolParams
import java.net.Socket

class ExclusionEnablingDefaultClientConnection : DefaultClientConnection() {
    private val wireLog = LogFactory.getLog("org.apache.http.wire")
    @Throws(IOException::class)
    override fun createSessionInputBuffer(
        socket: Socket,
        buffersize: Int,
        params: HttpParams
    ): SessionInputBuffer {
        var inbuffer: SessionInputBuffer = SocketInputBuffer(
            socket,
            if (buffersize > 0) buffersize else 8192,
            params
        )
        if (wireLog.isDebugEnabled
            && params.getBooleanParameter(LoggingExclusionFilter.SHOULD_EXLUDE_BODY_LOGGING, true)
        ) {
            inbuffer = LoggingSessionInputBuffer(
                inbuffer,
                Wire(wireLog),
                HttpProtocolParams.getHttpElementCharset(params)
            )
        }
        return inbuffer
    }

    @Throws(IOException::class)
    override fun createSessionOutputBuffer(
        socket: Socket,
        buffersize: Int,
        params: HttpParams
    ): SessionOutputBuffer {
        var outbuffer: SessionOutputBuffer = SocketOutputBuffer(
            socket,
            if (buffersize > 0) buffersize else 8192,
            params
        )
        if (wireLog.isDebugEnabled
            && params.getBooleanParameter(LoggingExclusionFilter.SHOULD_EXLUDE_BODY_LOGGING, true)
        ) {
            outbuffer = LoggingSessionOutputBuffer(
                outbuffer,
                Wire(wireLog),
                HttpProtocolParams.getHttpElementCharset(params)
            )
        }
        return outbuffer
    }
}