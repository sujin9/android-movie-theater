package woowacourse.movie.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import woowacourse.movie.R
import woowacourse.movie.ui.home.HomeFragment
import woowacourse.movie.ui.reservation.ReservationFragment
import woowacourse.movie.ui.setting.SettingFragment

class MainActivity : AppCompatActivity() {
    private var lastSelectedFragmentTag = ""

    private val bottomNavigationView: BottomNavigationView by lazy {
        findViewById(R.id.main_bottom_navigation_view)
    }

    private val fragments = mapOf(
        FRAGMENT_HOME to HomeFragment(),
        FRAGMENT_RESERVATION to ReservationFragment(),
        FRAGMENT_SETTING to SettingFragment(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragmentContainerView()
        setBottomNavigationView()
    }

    private fun initFragmentContainerView() {
        supportFragmentManager.commit {
            add(R.id.main_fragment_view, HomeFragment(), FRAGMENT_HOME)
            setReorderingAllowed(true)
        }
        lastSelectedFragmentTag = FRAGMENT_HOME
    }

    private fun changeFragment(tag: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
        }

        val fragment: Fragment = fragments[tag] ?: throw IllegalArgumentException()

        val findFragment = supportFragmentManager.findFragmentByTag(tag)

        supportFragmentManager.commit {
            supportFragmentManager.findFragmentByTag(lastSelectedFragmentTag)?.let { hide(it) }
        }

        findFragment?.let {
            supportFragmentManager.commit {
                show(it)
                lastSelectedFragmentTag = tag
            }
        } ?: kotlin.run {
            supportFragmentManager.commit {
                replace(R.id.main_fragment_view, fragment, tag)
                lastSelectedFragmentTag = tag
                setReorderingAllowed(true)
            }
        }
    }

    private fun setBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.menu_item_home
        bottomNavigationView.setOnItemSelectedListener { selectedIcon ->
            changeFragment(getTag(selectedIcon))
            true
        }
    }

    private fun getTag(item: MenuItem): String = when (item.itemId) {
        R.id.menu_item_home -> FRAGMENT_HOME
        R.id.menu_item_reservation -> FRAGMENT_RESERVATION
        R.id.menu_item_setting -> FRAGMENT_SETTING
        else -> throw IllegalArgumentException()
    }

    companion object {
        private const val FRAGMENT_HOME = "home"
        private const val FRAGMENT_RESERVATION = "reservation"
        private const val FRAGMENT_SETTING = "setting"
    }
}
