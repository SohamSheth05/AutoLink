package com.autolink.shared

import androidx.compose.runtime.Composable
import com.autolink.core.designsystem.AutoLinkTheme
import com.autolink.feature.vehicle.presentation.DashboardRoot

/** Single screen for the POC; navigation arrives with the history / sync screens. */
@Composable
fun App() {
    AutoLinkTheme {
        DashboardRoot()
    }
}
