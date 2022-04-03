package com.prashantbarahi.hateoasdemo

import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class FakeNetworkDelayFilter : Filter {
  override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
    Thread.sleep(100 + ThreadLocalRandom.current().nextLong(1200))
    chain!!.doFilter(request, response)
  }
}