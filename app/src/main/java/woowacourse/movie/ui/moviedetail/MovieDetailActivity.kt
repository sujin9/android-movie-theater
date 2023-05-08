package woowacourse.movie.ui.moviedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import woowacourse.movie.R
import woowacourse.movie.databinding.ActivityMovieDetailBinding
import woowacourse.movie.ui.seat.SeatSelectionActivity
import woowacourse.movie.uimodel.MovieListModel
import woowacourse.movie.uimodel.PeopleCountModel
import woowacourse.movie.utils.failLoadingData
import woowacourse.movie.utils.getParcelableCompat
import woowacourse.movie.utils.getSerializableExtraCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MovieDetailActivity : AppCompatActivity(), MovieDetailContract.View {

    override val presenter = MovieDetailPresenter(this)

    private lateinit var binding: ActivityMovieDetailBinding

    private lateinit var timeSpinnerAdapter: ArrayAdapter<LocalTime>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val movie: MovieListModel.MovieModel =
            intent.getParcelableCompat(KEY_MOVIE) ?: return failLoadingData()

        setMovieInfo(movie)
        initSpinner(movie)
        initPeopleCountController()
        initBookingButton(movie)
        loadSavedData(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState.putInt(KEY_DATE_POSITION, binding.detailDateSpinner.selectedItemPosition)
        outState.putInt(KEY_TIME_POSITION, binding.detailTimeSpinner.selectedItemPosition)
        outState.putSerializable(KEY_PEOPLE_COUNT, presenter.peopleCountModel)
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

    private fun setMovieInfo(movie: MovieListModel.MovieModel) {
        binding.detailPoster.setImageResource(movie.poster)
        binding.detailTitle.text = movie.title
        binding.detailDate.text =
            getString(R.string.screening_date, movie.startDate.format(), movie.endDate.format())
        binding.detailRunningTime.text = getString(R.string.running_time, movie.runningTime)
        binding.detailDescription.text = movie.description
    }

    private fun LocalDate.format(): String =
        format(DateTimeFormatter.ofPattern(getString(R.string.date_format)))

    private fun initSpinner(movie: MovieListModel.MovieModel) {
        setDateSpinner(movie)
        setTimeSpinner()
    }

    private fun setDateSpinner(movie: MovieListModel.MovieModel) {
        val dateSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            presenter.getDatesBetweenTwoDates(movie),
        )
        dateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.detailDateSpinner.adapter = dateSpinnerAdapter
        binding.detailDateSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    presenter.updateTimesByDate(binding.detailDateSpinner.selectedItem as LocalDate)
                    binding.detailTimeSpinner.setSelection(0)
                    timeSpinnerAdapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun setTimeSpinner() {
        presenter.updateTimesByDate(binding.detailDateSpinner.selectedItem as LocalDate)
        timeSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            presenter.times,
        )
        timeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.detailTimeSpinner.adapter = timeSpinnerAdapter
    }

    private fun initPeopleCountController() {
        setMinusButton()
        setPlusButton()
        setPeopleCountView(presenter.peopleCountModel.count)
    }

    private fun setMinusButton() {
        binding.detailMinusButton.setOnClickListener {
            presenter.minusCount()
        }
    }

    private fun setPlusButton() {
        binding.detailPlusButton.setOnClickListener {
            presenter.addCount()
        }
    }

    override fun setPeopleCountView(count: Int) {
        binding.detailPeopleCount.text = "$count"
    }

    private fun initBookingButton(movie: MovieListModel.MovieModel) {
        binding.detailBookingButton.setOnClickListener {
            moveToSeatSelectionActivity(movie)
        }
    }

    private fun moveToSeatSelectionActivity(movie: MovieListModel.MovieModel) {
        val intent = Intent(this, SeatSelectionActivity::class.java).apply {
            putExtra(KEY_TITLE, movie.title)
            putExtra(
                KEY_TIME,
                LocalDateTime.of(
                    binding.detailDateSpinner.selectedItem as LocalDate,
                    binding.detailTimeSpinner.selectedItem as LocalTime,
                ),
            )
            putExtra(KEY_PEOPLE_COUNT, presenter.peopleCountModel)
        }
        startActivity(intent)
        finish()
    }

    private fun loadSavedData(savedInstanceState: Bundle?) {
        binding.detailDateSpinner.setSelection(savedInstanceState?.getInt(KEY_DATE_POSITION) ?: 0)
        binding.detailTimeSpinner.setSelection(savedInstanceState?.getInt(KEY_TIME_POSITION) ?: 0)
        presenter.updatePeopleCount(
            savedInstanceState?.getSerializableExtraCompat<PeopleCountModel>(KEY_PEOPLE_COUNT)?.count
                ?: 1,
        )
    }

    companion object {
        const val KEY_MOVIE = "movie"
        private const val KEY_DATE_POSITION = "date_position"
        private const val KEY_TIME_POSITION = "time_position"
        const val KEY_TITLE = "title"
        const val KEY_TIME = "time"
        const val KEY_PEOPLE_COUNT = "count"

        fun getIntent(movie: MovieListModel.MovieModel, context: Context): Intent {
            return Intent(context, MovieDetailActivity::class.java).apply {
                putExtra(KEY_MOVIE, movie)
            }
        }
    }
}
