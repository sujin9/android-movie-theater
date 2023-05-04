package woowacourse.movie.ui.seat

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import woowacourse.movie.R
import woowacourse.movie.ui.alarm.ReservationAlarmManager
import woowacourse.movie.ui.moviedetail.MovieDetailActivity
import woowacourse.movie.ui.setting.SettingFragment
import woowacourse.movie.ui.ticket.MovieTicketActivity
import woowacourse.movie.uimodel.MovieTicketModel
import woowacourse.movie.uimodel.PeopleCountModel
import woowacourse.movie.uimodel.ReservationModel
import woowacourse.movie.uimodel.SeatModel
import woowacourse.movie.uimodel.SelectedSeatsModel
import woowacourse.movie.utils.failLoadingData
import woowacourse.movie.utils.getPreferences
import woowacourse.movie.utils.getSerializableExtraCompat
import woowacourse.movie.utils.showToast
import java.time.LocalDateTime

class SeatSelectionActivity : AppCompatActivity(), SeatSelectionContract.View {

    override lateinit var presenter: SeatSelectionPresenter

    private val priceTextView by lazy { findViewById<TextView>(R.id.seat_price) }
    private val selectButton by lazy { findViewById<TextView>(R.id.seat_confirm_button) }

    private val reservationAlarmManager by lazy { ReservationAlarmManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setPresenter()
        loadSavedData(savedInstanceState)
        initSeatTable()
        initSelectButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_SEATS, presenter.selectedSeatsModel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setPresenter() {
        val movieTitle: String = intent.getSerializableExtraCompat(MovieDetailActivity.KEY_TITLE)
            ?: return failLoadingData()
        val movieTime: LocalDateTime =
            intent.getSerializableExtraCompat(MovieDetailActivity.KEY_TIME)
                ?: return failLoadingData()
        val peopleCount: PeopleCountModel =
            intent.getSerializableExtraCompat(MovieDetailActivity.KEY_PEOPLE_COUNT)
                ?: return failLoadingData()

        presenter = SeatSelectionPresenter(this, movieTitle, movieTime, peopleCount)
    }

    private fun loadSavedData(savedInstanceState: Bundle?) {
        savedInstanceState?.getSerializableExtraCompat<SelectedSeatsModel>(KEY_SEATS)?.let {
            presenter.updateSelectedSeatsModel(it)
        }
    }

    private fun initSeatTable() {
        val seatTable = findViewById<TableLayout>(R.id.seat_table_layout)
        for (row in 1..ROW_SIZE) {
            val tableRow = TableRow(this)
            for (column in 1..COLUMN_SIZE) {
                tableRow.addView(getSeatView(row, column))
            }
            seatTable.addView(tableRow)
        }
    }

    override fun initMovieTitleView(title: String) {
        findViewById<TextView>(R.id.seat_movie_title).text = title
    }

    private fun initSelectButton() {
        selectButton.setOnClickListener {
            makeDialog().show()
        }
    }

    private fun getSeatView(row: Int, column: Int): View {
        val seat = SeatModel(row, column)
        return seat.getView(
            this,
            presenter.isSelected(seat),
        ) {
            clickSeat(seat, this)
        }
    }

    private fun clickSeat(seat: SeatModel, seatView: View) {
        if (!canSelectMoreSeat(seatView)) {
            showToast("이미 인원수만큼 좌석이 선택되었습니다")
            return
        }

        seatView.isSelected = !seatView.isSelected
        presenter.clickSeat(seat, seatView.isSelected)
        updateBackgroundColor(seatView)
    }

    private fun makeDialog(): AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(getString(R.string.seat_dialog_title))
        .setMessage(getString(R.string.seat_dialog_message))
        .setPositiveButton(getString(R.string.seat_dialog_submit_button)) { _, _ ->
            MovieTicketModel(
                title = presenter.movieTitle,
                time = presenter.movieTime,
                peopleCount = presenter.peopleCountModel,
                seats = presenter.selectedSeatsModel,
            ).apply {
                setReservationData(this)
                makeAlarm()
                moveToTicketActivity(this)
            }
        }
        .setNegativeButton(getString(R.string.seat_dialog_cancel_button)) { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)

    private fun canSelectMoreSeat(seatView: View) =
        !(!seatView.isSelected && presenter.isSelectionDone())

    private fun updateBackgroundColor(seatView: View) {
        when (seatView.isSelected) {
            true -> {
                seatView.setBackgroundColor(getColor(R.color.seat_selected_background))
            }
            false -> {
                seatView.setBackgroundColor(getColor(R.color.seat_unselected_background))
            }
        }
    }

    override fun updatePriceText(price: Int) {
        priceTextView.text = getString(R.string.price_with_unit, price)
    }

    override fun updateButtonEnablement(isSelectionDone: Boolean) {
        selectButton.isEnabled = isSelectionDone
    }

    private fun setReservationData(ticket: MovieTicketModel) {
        ReservationModel.add(ticket)
    }

    private fun MovieTicketModel.makeAlarm() {
        if (this@SeatSelectionActivity.getPreferences()
                .getBoolean(SettingFragment.KEY_SWITCH, false)
        ) {
            reservationAlarmManager.makeAlarm(this)
        }
    }

    private fun moveToTicketActivity(ticket: MovieTicketModel) {
        startActivity(MovieTicketActivity.getIntent(ticket, this))
        finish()
    }

    companion object {
        const val ROW_SIZE = 5
        const val COLUMN_SIZE = 4
        private const val KEY_SEATS = "seats"
    }
}
