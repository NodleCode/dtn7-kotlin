package io.nodle.dtn

import org.junit.*
import org.junit.Assert.assertNotNull
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class BpUtilTest {
    // TODO Class
    private val bpUtil = BpUtil()

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun call(){
        /* When */
        bpUtil.call()

        /* Then */
        assertNotNull(bpUtil)
    }
}