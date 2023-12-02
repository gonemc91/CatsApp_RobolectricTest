package com.example.catsonactivity.apps.navcomponent

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import com.example.catsonactivity.R
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.tesutils.base.BaseRobolectricTest
import com.example.catsonactivity.tesutils.extensions.containsDrawable
import com.example.catsonactivity.tesutils.extensions.requireViewHolderAt
import com.example.catsonactivity.tesutils.extensions.withActivityScope
import com.example.catsonactivity.tesutils.imageloader.FakeImageLoader
import com.example.catsonactivity.tesutils.launchNavHiltFragment
import com.example.catsonactivity.tesutils.rules.ImmediateDiffUtilRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@UninstallModules(RepositoriesModule::class)

class NavCatsListFragmentTest: BaseRobolectricTest() {

    @get: Rule
    val immediateDiffUtilRule = ImmediateDiffUtilRule()

    @RelaxedMockK
    lateinit var navController: NavController



    private val cat1 = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat1.jpg",
        description = "The first cat",
        isFavorite = false
    )

    private val cat2 = Cat(
        id = 1,
        name = "Tiger",
        photoUrl = "cat2.jpg",
        description = "The second cat",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(listOf(cat1, cat2))

    private lateinit var scenario: ActivityScenario<*>

    @Before
    override fun setUp() {
        super.setUp()
        every { catsRepository.getCats() } returns catsFlow
        scenario = launchNavHiltFragment<NavCatsListFragment>(navController)
    }

    @After
    fun tearDown(){
        scenario.close()
    }
    @Test
    fun catsAndHeadersAreDisplayedInList() = scenario.withActivityScope {
        val recyclerView = findViewById<RecyclerView>(R.id.catsRecyclerView)

        val headerHolder = recyclerView.requireViewHolderAt(0)
        val firstCatHolder = recyclerView.requireViewHolderAt(1)
        val secondCatHolder = recyclerView.requireViewHolderAt(2)

        //assert total items count (1 header + 2 cats = 3)
        Assert.assertEquals(3, recyclerView.adapter!!.itemCount)
        //assert header
        Assert.assertEquals("Cats: 1 … 2", (headerHolder.itemView as TextView).text)
        //assert the first cat
        with(firstCatHolder.itemView) {
            Assert.assertEquals(
                "Lucky",
                findViewById<TextView>(R.id.catNameTextView).text
            )
            Assert.assertEquals(
                "The first cat",
                findViewById<TextView>(R.id.catDescriptionTextView).text
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.favoriteImageView)
                    .containsDrawable(R.drawable.ic_favorite_not, R.color.action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.deleteImageView)
                    .containsDrawable(R.drawable.ic_delete, R.color.action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.catImageView)
                    .containsDrawable(
                        FakeImageLoader.createDrawable("cat1.jpg")
                    )
            )
        }
        //assert the second cat
        with(secondCatHolder.itemView) {
            Assert.assertEquals(
                "Tiger",
                findViewById<TextView>(R.id.catNameTextView).text
            )
            Assert.assertEquals(
                "The second cat", findViewById<TextView>(R.id.catDescriptionTextView).text
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.favoriteImageView)
                    .containsDrawable(R.drawable.ic_favorite, R.color.highlighted_action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.deleteImageView)
                    .containsDrawable(R.drawable.ic_delete, R.color.action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.catImageView)
                    .containsDrawable(FakeImageLoader.createDrawable("cat2.jpg"))
            )
        }
    }

    @Test
    fun clickOnCatLaunchesDetails() = scenario.withActivityScope{
        val firstCatHolder = getFirstHolderFromCatsList()
        firstCatHolder.itemView.performClick()

       val expectedDirection = NavCatsListFragmentDirections
           .actionNavCatsListFragmentToNavCatDetailsFragment(1L)
        verify {
            navController.navigate(expectedDirection)
        }
    }

    @Test
    fun clickOnFavoriteToggleFlag() = scenario.withActivityScope{
        //arrange
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            catsFlow.value = listOf(
                cat.copy(isFavorite = !cat.isFavorite),
                cat2)
        }
        //act1 - turn on a favorite flag
        getFirstHolderFromCatsList().itemView
            .findViewById<View>(R.id.favoriteImageView).performClick()

        //assert 1
        assertFavourite(R.drawable.ic_favorite, R.color.highlighted_action)

        //act2 - turn off a favourite flag
        getFirstHolderFromCatsList().itemView
            .findViewById<View>(R.id.favoriteImageView).performClick()

        //assert 2
        assertFavourite(R.drawable.ic_favorite_not, R.color.action)
    }

    @Test
    fun clickOnDeleteRemoveCatFromList() = scenario.withActivityScope{
        val recyclerView = findViewById<RecyclerView>(R.id.catsRecyclerView)
        every { catsRepository.delete(any()) } answers {
            catsFlow.value = listOf(cat2)
        }

        getFirstHolderFromCatsList().itemView
            .findViewById<View>(R.id.deleteImageView).performClick()
        Robolectric.flushForegroundThreadScheduler()  //scip scrolling animation

        // assert
        recyclerView.scrollToPosition(0)
        val headerHolder = recyclerView.requireViewHolderAt(0)
        recyclerView.scrollToPosition(1)
        val secondHolder = recyclerView.requireViewHolderAt(1)
        //assert total items count (1 header + 1 cat = 2)
        Assert.assertEquals(2, recyclerView.adapter!!.itemCount)
        //asset header
        Assert.assertEquals("Cats: 1 … 1", (headerHolder.itemView as TextView).text)
        //assert cat item
        with(secondHolder.itemView){
            Assert.assertEquals("Tiger", findViewById<TextView>(R.id.catNameTextView).text)
            Assert.assertEquals("The second cat", findViewById<TextView>(R.id.catDescriptionTextView).text
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.favoriteImageView)
                    .containsDrawable(R.drawable.ic_favorite, R.color.highlighted_action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.deleteImageView)
                    .containsDrawable(R.drawable.ic_delete, R.color.action)
            )
            Assert.assertTrue(
                findViewById<ImageView>(R.id.catImageView)
                    .containsDrawable(FakeImageLoader.createDrawable("cat2.jpg"))
            )
        }
    }

    private fun Activity.assertFavourite(expectedDrawableRes: Int, expectedTintColorRes: Int? = null){
        with(getFirstHolderFromCatsList().itemView){
            val favouriteImageView = findViewById<ImageView>(R.id.favoriteImageView)
            Assert.assertTrue(
                favouriteImageView.containsDrawable(expectedDrawableRes, expectedTintColorRes)
            )
        }
    }

    private fun Activity.getFirstHolderFromCatsList(): RecyclerView.ViewHolder{
        val recyclerView = findViewById<RecyclerView>(R.id.catsRecyclerView)
            .findViewById<RecyclerView>(R.id.catsRecyclerView)
        return recyclerView.requireViewHolderAt(1)
    }
}