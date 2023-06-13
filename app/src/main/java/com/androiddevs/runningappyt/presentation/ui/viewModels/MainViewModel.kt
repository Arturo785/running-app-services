package com.androiddevs.runningappyt.presentation.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.domain.model.RunDomain
import com.androiddevs.runningappyt.domain.repositories.MainRepository
import com.androiddevs.runningappyt.domain.use_case.GetRunUseCases
import com.androiddevs.runningappyt.domain.utils.RunOrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val runUseCases: GetRunUseCases
) : ViewModel() {

    private val _runsSortedBy = MutableStateFlow<List<RunDomain>>(emptyList())
    val runsSortedBy = _runsSortedBy.asLiveData()

    init {
        getRunsSorted(RunOrderType.ByDate)
    }

    fun insertRun(run: RunDomain) {
        viewModelScope.launch {
            runUseCases.insertRunUseCase(run)
        }
    }

    fun getRunsSorted(sortOrder: RunOrderType) {
        runUseCases.getRunSortedByUseCase(sortOrder).onEach { sortedRuns ->
            _runsSortedBy.emit(sortedRuns)
        }.launchIn(viewModelScope)
        // onEach is not a terminal flow operator like collect so we need the launchIn
        // without the launchIn the flow does not return value, only another flow
        /** launchIn
         * Terminal flow operator that [launches][launch] the [collection][collect] of the given flow in the [scope].
         * It is a shorthand for `scope.launch { flow.collect() }`.
         *
         * This operator is usually used with [onEach], [onCompletion] and [catch] operators to process all emitted values
         * handle an exception that might occur in the upstream flow or during processing, for example:
         *
         * ```
         * flow
         *     .onEach { value -> updateUi(value) }
         *     .onCompletion { cause -> updateUi(if (cause == null) "Done" else "Failed") }
         *     .catch { cause -> LOG.error("Exception: $cause") }
         *     .launchIn(uiScope)
         * ```
         *
         * Note that the resulting value of [launchIn] is not used and the provided scope takes care of cancellation.
         */
    }
}