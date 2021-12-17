package com.nortal.test.restassured.logs

import com.nortal.test.restassured.logs.ExclusionEnablingDefaultClientConnectionOperator
import org.apache.http.conn.ClientConnectionOperator
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.conn.BasicClientConnectionManager

class ExclusionEnablingBasicClientConnectionManager : BasicClientConnectionManager() {
    override fun createConnectionOperator(schreg: SchemeRegistry): ClientConnectionOperator {
        return ExclusionEnablingDefaultClientConnectionOperator(schreg)
    }
}