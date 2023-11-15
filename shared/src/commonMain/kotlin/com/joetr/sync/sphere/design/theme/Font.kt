package com.joetr.sync.sphere.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

val openSansRegular: FontFamily
    @Composable
    get() {
        return FontFamily(
            font(
                "Poppins",
                "poppins_regular",
                FontWeight.Normal,
                FontStyle.Normal,
            ),
        )
    }

val AppTypography: Typography
    @Composable
    get() {
        val copy = Typography()
        return Typography(
            displayLarge = copy.displayLarge.copy(
                fontFamily = openSansRegular,
            ),
            displayMedium = copy.displayMedium.copy(
                fontFamily = openSansRegular,
            ),
            displaySmall = copy.displaySmall.copy(
                fontFamily = openSansRegular,
            ),
            headlineLarge = copy.headlineLarge.copy(
                fontFamily = openSansRegular,
            ),
            headlineMedium = copy.headlineMedium.copy(
                fontFamily = openSansRegular,
            ),
            headlineSmall = copy.headlineSmall.copy(
                fontFamily = openSansRegular,
            ),
            titleLarge = copy.titleLarge.copy(
                fontFamily = openSansRegular,
            ),
            titleMedium = copy.titleMedium.copy(
                fontFamily = openSansRegular,
            ),
            titleSmall = copy.titleSmall.copy(
                fontFamily = openSansRegular,
            ),
            bodyLarge = copy.bodyLarge.copy(
                fontFamily = openSansRegular,
            ),
            bodyMedium = copy.bodyMedium.copy(
                fontFamily = openSansRegular,
            ),
            bodySmall = copy.bodySmall.copy(
                fontFamily = openSansRegular,
            ),
            labelLarge = copy.labelLarge.copy(
                fontFamily = openSansRegular,
            ),
            labelMedium = copy.labelMedium.copy(
                fontFamily = openSansRegular,
            ),
            labelSmall = copy.labelSmall.copy(
                fontFamily = openSansRegular,
            ),
        )
    }
