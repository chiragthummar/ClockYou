package com.bnyro.clock.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R
import com.bnyro.clock.ui.components.ClickableIcon
import com.bnyro.clock.ui.model.SettingsModel
import com.bnyro.clock.ui.model.TimerModel
import com.bnyro.clock.ui.prefs.ButtonGroupPref
import com.bnyro.clock.ui.prefs.IconPreference
import com.bnyro.clock.ui.prefs.SettingsCategory
import com.bnyro.clock.ui.prefs.SwitchPref
import com.bnyro.clock.util.IntentHelper
import com.bnyro.clock.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClickBack: () -> Unit,
    settingsModel: SettingsModel,
    timerModel: TimerModel
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    ClickableIcon(imageVector = Icons.Default.ArrowBack) {
                        onClickBack.invoke()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(pv)
                .verticalScroll(scrollState)
        ) {
            SettingsCategory(stringResource(R.string.appearance))
            ButtonGroupPref(
                preferenceKey = Preferences.themeKey,
                title = stringResource(R.string.theme),
                options = listOf(R.string.system, R.string.light, R.string.dark).map {
                    stringResource(it)
                },
                values = listOf("system", "light", "dark"),
                defaultValue = "system"
            ) {
                settingsModel.themeMode = it
            }
            Divider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(title = stringResource(R.string.behavior))
            SwitchPref(
                prefKey = Preferences.showSecondsKey,
                title = stringResource(R.string.show_seconds),
                defaultValue = true
            )
            SwitchPref(
                prefKey = Preferences.timerUsePickerKey,
                title = stringResource(R.string.timer_use_time_picker),
                defaultValue = false
            ) {
                // reset the timer model state to prevent issues when changing the time picker layout
                timerModel.timePickerFakeUnits = 0
                timerModel.timePickerSeconds = 0
            }
            SwitchPref(
                prefKey = Preferences.timerShowExamplesKey,
                title = stringResource(R.string.show_timer_quick_selection),
                defaultValue = true
            )
            Divider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(stringResource(R.string.about))
            IconPreference(
                title = stringResource(R.string.source_code),
                summary = stringResource(R.string.source_code_summary),
                imageVector = Icons.Default.OpenInNew
            ) {
                IntentHelper.openUrl(context, "https://github.com/you-apps/ClockYou")
            }
            IconPreference(
                title = stringResource(R.string.app_name),
                summary = stringResource(
                    R.string.version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ),
                imageVector = Icons.Default.History
            ) {
                IntentHelper.openUrl(
                    context,
                    "https://github.com/you-apps/ClockYou/releases/latest"
                )
            }
        }
    }
}
