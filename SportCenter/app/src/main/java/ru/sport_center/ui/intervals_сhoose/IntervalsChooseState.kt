package ru.sport_center.ui.intervals_сhoose

import ru.sport_center.data.entity.Interval
import ru.sport_center.ui.base.BaseViewState

class IntervalsChooseState(val intervals: List<Interval>? = null, error: Throwable? = null): BaseViewState<List<Interval>?>(intervals, error)