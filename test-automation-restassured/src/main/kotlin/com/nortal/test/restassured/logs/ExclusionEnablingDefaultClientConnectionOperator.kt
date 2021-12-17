package com.nortal.test.restassured.logs

import com.nortal.test.restassured.logs.ExclusionEnablingDefaultClientConnection
import org.apache.http.conn.OperatedClientConnection
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.conn.DefaultClientConnectionOperator

class ExclusionEnablingDefaultClientConnectionOperator(schemes: SchemeRegistry?) : DefaultClientConnectionOperator(schemes) {
    override fun createConnection(): OperatedClientConnection {
        return ExclusionEnablingDefaultClientConnection()
    }
}