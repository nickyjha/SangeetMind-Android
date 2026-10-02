package com.sangeetmind.core.network

import java.net.Inet4Address
import java.net.InetAddress
import okhttp3.Dns

/**
 * Puts IPv4 addresses before IPv6 ones. OkHttp 4 tries a host's addresses in order and
 * waits a full connect timeout on each, so a network whose IPv6 path silently drops
 * packets (common on Indian ISPs and home Wi-Fi) made every request hang. IPv6 is
 * still tried when IPv4 fails.
 */
class Ipv4FirstDns(private val delegate: Dns = Dns.SYSTEM) : Dns {
    override fun lookup(hostname: String): List<InetAddress> = ipv4First(delegate.lookup(hostname))

    companion object {
        fun ipv4First(addresses: List<InetAddress>): List<InetAddress> =
            addresses.sortedBy { if (it is Inet4Address) 0 else 1 }
    }
}
