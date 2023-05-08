package woowacourse.movie.ui.setting

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import woowacourse.movie.data.alarm.AlarmStateRepository

internal class SettingPresenterTest {
    private lateinit var presenter: SettingPresenter
    private lateinit var view: SettingContract.View

    @Before
    fun setUp() {
        view = mockk()

        val alarmStateRepository = object : AlarmStateRepository {
            private var isAlarmOn: Boolean = false

            override fun getData(): Boolean {
                return isAlarmOn
            }

            override fun saveData(dataSource: Boolean) {
                isAlarmOn = dataSource
            }
        }

        presenter = SettingPresenter(view, alarmStateRepository)
    }

    @Test
    fun 토글을_클릭하여_ON_한다() {
        val slot = slot<Boolean>()
        every { view.setToggleButton(capture(slot)) } just Runs
        presenter.clickSwitch(true)

        assertEquals(true, slot.captured)
        verify { view.setToggleButton(slot.captured) }
    }

    @Test
    fun 토글을_클릭하여_OFF_한다() {
        val slot = slot<Boolean>()
        every { view.setToggleButton(capture(slot)) } just Runs
        presenter.clickSwitch(false)

        assertEquals(false, slot.captured)
        verify { view.setToggleButton(slot.captured) }
    }
}