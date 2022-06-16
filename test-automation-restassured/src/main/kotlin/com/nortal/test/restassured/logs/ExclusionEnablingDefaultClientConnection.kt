/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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