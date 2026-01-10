package ink.moling.mocklocation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ink.moling.mocklocation.data.db.AppDatabase
import ink.moling.mocklocation.data.db.MockPointEntity
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.RouteItem
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.data.repository.MockServiceStatusRepository
import ink.moling.mocklocation.service.MockLocationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // =====================================================
    // 1. Mock Service / 生命周期相关
    // =====================================================

    val mockStatus = MockServiceStatusRepository.state

    val serviceBinder =
        MutableStateFlow<MockLocationService.MockLocationServiceBinder?>(null)

    private val _needStartService = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val needStartService = _needStartService.asSharedFlow()

    private var pendingAction: (() -> Unit)? = null

    fun onServiceBinderReady(
        binder: MockLocationService.MockLocationServiceBinder
    ) {
        serviceBinder.value = binder
        pendingAction?.invoke()
        pendingAction = null
    }

    // =====================================================
    // 2. UI 状态（Dialog / 页面）
    // =====================================================

    private val _isImportExportDialogOpen = MutableStateFlow(false)
    val isImportExportDialogOpen = _isImportExportDialogOpen.asStateFlow()

    private val _isAddPointDialogOpen = MutableStateFlow(false)
    val isAddPointDialogOpen = _isAddPointDialogOpen.asStateFlow()

    // 当前选中的 Mock 目标（UI Selection）
    var selectedMockPoint: MockPointEntity? = null
    var selectedMockRoute: RouteItem? = null

    fun setImportExportDialogOpen(open: Boolean) {
        _isImportExportDialogOpen.value = open
    }

    fun setAddPointDialogOpen(open: Boolean) {
        _isAddPointDialogOpen.value = open
    }

    // =====================================================
    // 3. 定位候选（GPS / Network）
    // =====================================================

    private val _gpsFlow = MutableStateFlow<CandidateLocation?>(null)
    val gpsFlow: StateFlow<CandidateLocation?> = _gpsFlow.asStateFlow()

    private val _netFlow = MutableStateFlow<CandidateLocation?>(null)
    val netFlow: StateFlow<CandidateLocation?> = _netFlow.asStateFlow()

    private var lastGpsAltitude: Double? = null

    fun updateGpsCandidate(c: CandidateLocation) {
        _gpsFlow.value = c
        c.alt?.let { lastGpsAltitude = it }
    }

    fun updateNetCandidate(c: CandidateLocation) {
        _netFlow.value = c
    }

    // =====================================================
    // 4. 定位融合（页面最终只看这个）
    // =====================================================

    private fun chooseBest(
        gps: CandidateLocation?,
        net: CandidateLocation?
    ): CandidateLocation? {
        return when {
            gps == null -> net
            net == null -> gps
            gps.accuracy <= net.accuracy -> gps
            else -> net
        }
    }

    private fun finalizeAltitude(
        chosen: CandidateLocation
    ): CandidateLocation {
        val alt = when (chosen.source) {
            Source.GPS -> chosen.alt
            Source.NETWORK -> lastGpsAltitude
        }
        return chosen.copy(alt = alt)
    }

    val fusedLocation: StateFlow<CandidateLocation?> =
        combine(gpsFlow, netFlow) { gps, net ->
            chooseBest(gps, net)
        }
            .map { chosen ->
                chosen?.let { finalizeAltitude(it) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    // =====================================================
    // 5. Mock 行为（命令 Service）
    // =====================================================

    fun setMockPosition(lat: Double, lng: Double, alt: Double) {
        val binder = serviceBinder.value
        if (binder == null) {
            pendingAction = {
                serviceBinder.value?.setStaticPoint(lat, lng, alt)
            }
            _needStartService.tryEmit(Unit)
            return
        }
        binder.setStaticPoint(lat, lng, alt)
    }

    // =====================================================
    // 6. 本地路径点存储
    // =====================================================

    private val mockPointDao by lazy {
        AppDatabase
            .getInstance(getApplication())
            .mockPointDao()
    }

    private val _points = MutableStateFlow<List<MockPointEntity>>(emptyList())
    val points: StateFlow<List<MockPointEntity>> = _points.asStateFlow()

    fun loadPoints() {
        viewModelScope.launch {
            _points.value = mockPointDao.getAll()
        }
    }

    fun addPoint(
        name: String,
        latitude: Double,
        longitude: Double,
        altitude: Double
    ) {
        viewModelScope.launch {
            mockPointDao.insert(
                MockPointEntity(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude
                )
            )
            _points.value = mockPointDao.getAll()
        }
    }

    fun deletePoint(id: Long) {
        viewModelScope.launch {
            mockPointDao.deleteById(id)
            _points.value = mockPointDao.getAll()
        }
    }

    fun clearAllPoints() {
        viewModelScope.launch {
            mockPointDao.clearAll()
            _points.value = emptyList()
        }
    }
}
