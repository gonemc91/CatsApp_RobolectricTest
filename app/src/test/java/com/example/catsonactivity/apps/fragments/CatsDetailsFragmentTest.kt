package com.example.catsonactivity.apps.fragments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.activities.CatsListActivity
import com.example.catsonactivity.apps.fragments.di.FragmentRouterModule
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.tesutils.base.BaseRobolectricTest
import com.example.catsonactivity.tesutils.extensions.containsDrawable
import com.example.catsonactivity.tesutils.extensions.withActivityScope
import com.example.catsonactivity.tesutils.imageloader.FakeImageLoader
import com.example.catsonactivity.tesutils.launchHiltFragment
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@UninstallModules(RepositoriesModule::class, FragmentRouterModule::class)


class CatsDetailsFragmentTest : BaseRobolectricTest() {



    private lateinit var scenario:ActivityScenario<*>

    @BindValue @RelaxedMockK
    lateinit var router: FragmentRouter


    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(cat)

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCatById(any()) } returns catsFlow
        scenario = launchHiltFragment{
            CatDetailsFragment.newInstance(cat.id)
        }
    }

    @After
    fun tearDown(){
        scenario.close()
    }

    @Test
    fun catsDisplayed() = scenario.withActivityScope {
        assertEquals(
            "Lucky",
          findViewById<TextView>(R.id.catNameTextView).text
        )
        assertEquals(
            "Meow-meow",
            findViewById<TextView>(R.id.catDescriptionTextView).text
        )
        assertTrue(
            findViewById<ImageView>(R.id.favoriteImageView)
                .containsDrawable(R.drawable.ic_favorite, R.color.highlighted_action)
        )
        assertTrue(
            findViewById<ImageView>(R.id.catImageView)
                .containsDrawable(FakeImageLoader.createDrawable(cat.photoUrl))
        )
    }
        @Test
        fun toggleFavouriteToggleFlag() = scenario.withActivityScope {
            //arrange
            every { catsRepository.toggleIsFavorite(any()) } answers {
                val cat = firstArg<Cat>()
                val newCat = cat.copy(isFavorite = !cat.isFavorite)
                catsFlow.value = newCat
            }
            val favouriteImageView = findViewById<ImageView>(R.id.favoriteImageView)

            //act 1 - turn off favourite flag
            favouriteImageView.performClick()
            //assert
            assertTrue(favouriteImageView
                .containsDrawable(R.drawable.ic_favorite_not, R.color.action)
            )

            //act 2 - turn on favourite flag
            favouriteImageView.performClick()
            //assert
            assertTrue(
                favouriteImageView.containsDrawable(
                    R.drawable.ic_favorite,
                    R.color.highlighted_action
                )
            )
        }


    @Test
    fun clickOnBackFinishesActivity() = scenario.withActivityScope {
        findViewById<View>(R.id.goBackButton).performClick()
        verify {
            router.goBack()
        }
    }

}