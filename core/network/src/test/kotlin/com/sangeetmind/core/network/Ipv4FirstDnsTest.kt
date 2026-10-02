package com.sangeetmind.core.network

import java.net.InetAddress
import okhttp3.Dns
import org.junit.Assert.assertEquals
import org.junit.Test

class Ipv4FirstDnsTest {
    private val v6 = InetAddress.getByName("2a09:8280:1::8e:73e1:0")
    private val v4 = InetAddress.getByName("66.241.124.210")

    @Test fun ipv4ComesFirst() {
        val dns = Ipv4FirstDns(object : Dns {
            override fun lookup(hostname: String) = listOf(v6, v4)
        })
        assertEquals(listOf(v4, v6), dns.lookup("example.test"))
    }

    @Test fun ipv6OnlyStillReturned() {
        assertEquals(listOf(v6), Ipv4FirstDns.ipv4First(listOf(v6)))
    }
}
