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
class BpCreateTest {
    // TODO Class
    private val bpCreate = BpCreate()

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun call(){
        /* When */
        bpCreate.call()

        /* Then */
        assertNotNull(bpCreate)
    }
}