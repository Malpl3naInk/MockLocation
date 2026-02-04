package ink.moling.mocklocation.ui.views

/**
 * 点位模式视图组件
 */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PointModeView(
//    viewModel: MainViewModel,
//    onAddPointClick: () -> Unit
//) {
//    var isExpanded by remember { mutableStateOf(false) }
//    val points by viewModel.points.collectAsState()
//    val mockStatus by viewModel.mockStatus.collectAsState()
//    val isMockDisabled = mockStatus != MockServiceState.Enabled
//
//    LaunchedEffect(Unit) {
//        viewModel.loadPoints()
//    }
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        ExposedDropdownMenuBox(
//            expanded = isExpanded,
//            onExpandedChange = {
//                if (isMockDisabled) isExpanded = it
//            },
//            modifier = Modifier
//                .weight(1f)
//                .padding(end = 8.dp)
//        ) {
//            @Suppress("DEPRECATION")
//            OutlinedTextField(
//                value = viewModel.selectedMockPoint?.name ?: "",
//                onValueChange = {},
//                readOnly = true,
//                enabled = isMockDisabled,
//                label = { Text("Select point") },
//                trailingIcon = {
//                    ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
//                },
//                modifier = Modifier
//                    .menuAnchor()
//                    .fillMaxWidth()
//            )
//
//            ExposedDropdownMenu(
//                expanded = isExpanded,
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                onDismissRequest = { isExpanded = false }
//            ) {
//                if (points.isEmpty()) {
//                    DropdownMenuItem(
//                        text = { Text("No saved points") },
//                        onClick = { isExpanded = false },
//                        enabled = false
//                    )
//                } else {
//                    points.forEach { point ->
//                        LongPressDeleteMenuItem(
//                            text = {
//                                Column {
//                                    Text(point.name)
//                                    Text(
//                                        text = "%.8f, %.8f".format(point.lat, point.longitude),
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colorScheme.onSecondary
//                                    )
//                                }
//                            },
//                            onClick = {
//                                viewModel.selectedMockPoint = point
//                                isExpanded = false
//                            },
//                            onDelete = {
//                                if (point.name == viewModel.selectedMockPoint?.name) {
//                                    viewModel.selectedMockPoint = null
//                                }
//                                viewModel.deletePoint(point.id)
//                                isExpanded = false
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // 添加点位按钮
//        IconButton(
//            modifier = Modifier
//                .size(56.dp)
//                .padding(top = 4.dp),
//            onClick = onAddPointClick
//        ) {
//            Icon(
//                Icons.Outlined.Add,
//                contentDescription = "Add point",
//                // tint = Color.White
//            )
//        }
//    }
//}

