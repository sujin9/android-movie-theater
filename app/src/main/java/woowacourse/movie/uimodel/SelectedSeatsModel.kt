package woowacourse.movie.uimodel

import java.io.Serializable

data class SelectedSeatsModel(
    val seats: Set<SeatModel> = emptySet(),
) : Serializable {
    override fun toString(): String {
        return seats.joinToString(",")
    }
}