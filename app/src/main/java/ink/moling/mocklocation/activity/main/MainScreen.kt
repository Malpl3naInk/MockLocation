package ink.moling.mocklocation.activity.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R
import ink.moling.mocklocation.activity.main.views.PageMainView
import ink.moling.mocklocation.activity.main.views.PageSimulationView
import ink.moling.mocklocation.activity.main.views.SheetPointsView
import ink.moling.mocklocation.activity.main.views.SheetRoutesView
import ink.moling.mocklocation.data.local.repository.MockServiceState
import ink.moling.mocklocation.data.local.repository.MockServiceStatusRepository
import ink.moling.mocklocation.ui.dialog.DeleteConfirmDialog
import ink.moling.mocklocation.ui.dialog.ErrorDialog
import ink.moling.mocklocation.ui.dialog.HelpButtonConfig
import ink.moling.mocklocation.utils.HelpMessages
import ink.moling.mocklocation.utils.MockMode
import kotlinx.coroutines.launch

/**
 * 主屏幕 Composable
 * 
 * @param viewModel 主屏幕 ViewModel
 * @param appName 应用名称
 * @param onStartMockLocation 开始模拟位置回调
 * @param onStopMockLocation 停止模拟位置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    appName: String,
    onStartMockLocation: () -> Unit,
    onStopMockLocation: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 从 ViewModel 收集状态
    val mockStatus by viewModel.mockStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Scaffold 和 Pager 状态（纯 UI 状态，保留在 Composable 中）
    val pagerState = rememberPagerState { 2 }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val sheetState = scaffoldState.bottomSheetState
    val scaffoldExpanded by remember {
        derivedStateOf {
            (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded &&
            sheetState.targetValue == SheetValue.Expanded) ||
                    sheetState.currentValue == SheetValue.Expanded
        }
    }
    
    // 收集 UI 事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MainUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MainUiEvent.CollapseBottomSheet -> {
                    scaffoldState.bottomSheetState.expand()
                }
                is MainUiEvent.HideBottomSheet -> {
                    scaffoldState.bottomSheetState.hide()
                }
            }
        }
    }
    
    // 显示错误对话框（优先显示 UI State 中的错误）
    uiState.errorState?.let { error ->
        ErrorDialog(
            title = error.title,
            text = error.message,
            stackTrace = error.stackTrace,
            helpButton = (
                if (HelpMessages.isExists(error.title))
                    HelpButtonConfig(
                        stringResource(HelpMessages.getHelpMessage(error.title))
                    )
                else
                    error.helpButton
            ),
            onDismiss = {
                viewModel.clearError()
                onStopMockLocation()
            }
        )
    } ?: run {
        // 向后兼容：如果 UI State 中没有错误，则检查 MockService 状态
        if (mockStatus is MockServiceState.Error) {
            val mockError = mockStatus as MockServiceState.Error
            ErrorDialog(
                title = mockError.type,
                text = mockError.msg,
                stackTrace = mockError.stackTrace,
                helpButton = null,
                onDismiss = {
                    onStopMockLocation()
                    MockServiceStatusRepository.state.value = MockServiceState.Disabled
                }
            )
        }
    }

    // 确认删除点对话框
    if (uiState.showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            text = stringResource(R.string.main_dialog_delete_point, uiState.pointName),
            onConfirm = { viewModel.confirmDeletePoint() },
            onDismiss = { viewModel.dismissDeletePointDialog() }
        )
    }
    
    // 确认删除路线对话框
    if (uiState.showDeleteRouteConfirmDialog) {
        DeleteConfirmDialog(
            text = stringResource(R.string.main_dialog_delete_route, uiState.routeName),
            onConfirm = { viewModel.confirmDeleteRoute() },
            onDismiss = { viewModel.dismissDeleteRouteDialog() }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        containerColor = MaterialTheme.colorScheme.background,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                when (uiState.selectedSimulation) {
                    MockMode.POINT -> SheetPointsView(viewModel)
                    MockMode.ROUTE -> SheetRoutesView(viewModel)
                }
            }
        }
    ) {
        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !scaffoldExpanded && !uiState.editingSimPoint
            ) { page ->
                when (page) {
                    0 -> PageMainView(viewModel, appName, onStartMockLocation, onStopMockLocation)
                    1 -> PageSimulationView(viewModel, scaffoldState)
                }
            }

            Column {
                Spacer(Modifier.weight(1f))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            Modifier
                                .size(18.dp)
                                .padding(4.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary,
                                    CircleShape
                                )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = scaffoldExpanded,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                )
            }
        }
    }
}
