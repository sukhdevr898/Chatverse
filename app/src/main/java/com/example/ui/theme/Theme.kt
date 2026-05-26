package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.content.ContextWrapper
import android.content.Context

private val ChatVerseDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = ElectricPurple,
    tertiary = SuccessGreen,
    background = DeepBlack,
    surface = Graphite,
    surfaceVariant = SoftDark,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = SoftGray,
    error = ErrorRed
)

private val ChatVerseLightColorScheme = lightColorScheme(
    primary = Color(0xFF006A60),
    secondary = Color(0xFF008f82),
    tertiary = Color(0xFF23C16B),
    background = Color(0xFFF4F7F6),
    surface = Color(0xFFFBFDF9),
    surfaceVariant = Color(0xFFDAE5E1),
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = Color(0xFF191C1B),
    onSurface = Color(0xFF191C1B),
    onSurfaceVariant = Color(0xFF56605E),
    error = ErrorRed
)

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ChatVerseDarkColorScheme else ChatVerseLightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = view.context.findActivity()?.window
      if (window != null) {
          window.statusBarColor = android.graphics.Color.TRANSPARENT
          window.navigationBarColor = android.graphics.Color.TRANSPARENT
          val insetsController = WindowCompat.getInsetsController(window, view)
          insetsController.isAppearanceLightStatusBars = !darkTheme
          insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
