package com.example.catsonactivity.tesutils.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catsonactivity.tesutils.rules.TestDispatcherRule
import io.mockk.junit4.MockKRule
import org.junit.Rule

open class BaseTest {

    @get:Rule
    val testDispatcherRule = TestDispatcherRule()//replacing coroutines dispatcher for test scope(package->app.test-utils)

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()//replacing dispatcher for live data (package->implementation arch.core)

    @get:Rule
    val mockkRule = MockKRule(this)
}