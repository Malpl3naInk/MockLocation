package ink.moling.mocklocation.activity.osslicense

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.models.OssLicense
import ink.moling.mocklocation.data.repository.OssLicenseManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicenseScreen() {
    val context = LocalContext.current
    var licenses by remember { mutableStateOf(listOf<OssLicense>()) }

    LaunchedEffect(Unit) {
        licenses = OssLicenseManager.loadLicenses(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            item {
                Text(
                    modifier = Modifier.padding(top = 88.dp, bottom = 32.dp, start = 24.dp),
                    text = stringResource(R.string.oss_license_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(vertical = 6.dp),
                    text = stringResource(R.string.oss_license_section_libraries),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column {
                        licenses.forEachIndexed { index, license ->
                            LicenseItem(
                                license = license,
                                onUrlClick = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                    context.startActivity(intent)
                                }
                            )
                            if (index < licenses.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseItem(
    license: OssLicense,
    onUrlClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = license.project,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val licenseName = license.licenses.firstOrNull()?.license
                    ?: stringResource(R.string.oss_license_unknown)
                Text(
                    text = licenseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp, start = 32.dp)
            ) {
                // Version
                license.version?.let { version ->
                    InfoRow(
                        label = stringResource(R.string.oss_license_version),
                        value = version
                    )
                }

                // Description
                license.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        InfoRow(
                            label = stringResource(R.string.oss_license_description),
                            value = desc
                        )
                    }
                }

                // Developers
                if (license.developers.isNotEmpty()) {
                    InfoRow(
                        label = stringResource(R.string.oss_license_developers),
                        value = license.developers.joinToString(", ")
                    )
                }

                // URL
                license.url?.let { url ->
                    if (url.isNotBlank()) {
                        ClickableInfoRow(
                            label = stringResource(R.string.oss_license_project_url),
                            value = url,
                            onClick = { onUrlClick(url) }
                        )
                    }
                }

                // License URL
                license.licenses.firstOrNull()?.licenseUrl?.let { licenseUrl ->
                    if (licenseUrl.isNotBlank()) {
                        ClickableInfoRow(
                            label = stringResource(R.string.oss_license_license_url),
                            value = licenseUrl,
                            onClick = { onUrlClick(licenseUrl) }
                        )
                    }
                }

                // Dependency
                license.dependency?.let { dep ->
                    InfoRow(
                        label = stringResource(R.string.oss_license_dependency),
                        value = dep,
                        isMonoSpace = true
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMonoSpace: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Text(
            text = value,
            style = if (isMonoSpace) {
                MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ClickableInfoRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable(onClick = onClick)
        )
    }
}
