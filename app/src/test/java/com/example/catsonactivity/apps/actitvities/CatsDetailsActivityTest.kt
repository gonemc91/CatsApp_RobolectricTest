package com.example.catsonactivity.apps.actitvities

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.example.catsonactivity.apps.activities.CatDetailsActivity
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.tesutils.base.BaseRobolectricTest
import com.example.catsonactivity.tesutils.extensions.withActivityScope
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.catsonactivity.R
import com.example.catsonactivity.tesutils.extensions.containsDrawable
import com.example.catsonactivity.tesutils.imageloader.FakeImageLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue


@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@UninstallModules(RepositoriesModule::class)


class CatsDetailsActivityTest: BaseRobolectricTest() {

    private lateinit var scenario: ActivityScenario<CatDetailsActivity>

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
        scenario = ActivityScenario.launch(
            CatDetailsActivity::class.java,
            bundleOf(
                CatDetailsActivity.EXTRA_CAT_ID to 1L
            )
        )
        scenario.moveToState(Lifecycle.State.RESUMED)
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
            assertTrue(favouriteImageView.containsDrawable(R.drawable.ic_favorite_not, R.color.action))

            //act 2 - turn on favourite flag
            favouriteImageView.performClick()
            //assert
            assertTrue(favouriteImageView.containsDrawable(R.drawable.ic_favorite, R.color.highlighted_action))

    }
        @Test
        fun clickOnBackFinishesActivity() = scenario.withActivityScope {
            findViewById<View>(R.id.goBackButton).performClick()
            assertTrue(isFinishing)

    }
}