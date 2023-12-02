package com.example.catsonactivity.tesutils.base

import com.example.catsonactivity.model.CatsRepository
import com.example.catsonactivity.tesutils.rules.FakeImageLoaderRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

open class BaseRobolectricTest: BaseTest() {

     @get: Rule
     val hiltRule = HiltAndroidRule(this)//for replacing dependency under control Hilt library

    @get: Rule
    val fakeImageLoaderRule = FakeImageLoaderRule()//for replacing image from network(library coil) on [ColorDrawable] with param url:String.)


    @Inject
    lateinit var catsRepository: CatsRepository

    @Before
    open fun setUp(){
        hiltRule.inject() //inject catsRepository using hiltRule
    }

}