package com.simplemobiletools.clock.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import com.simplemobiletools.clock.databinding.WidgetConfigVerticalDigitalBinding
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.MyVerticalDigitalTimeWidgetProvider
import com.simplemobiletools.clock.helpers.SIMPLE_PHONE
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.FeatureLockedDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS

class WidgetVerticalDigitalConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mTextColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mFeatureLockedDialog: FeatureLockedDialog? = null
    private val binding: WidgetConfigVerticalDigitalBinding by viewBinding(WidgetConfigVerticalDigitalBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(binding.root)
        initVariables()

        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (!config.wasInitialWidgetSetUp && Build.BRAND.equals(SIMPLE_PHONE, true)) {
            saveConfig()
            config.wasInitialWidgetSetUp = true
            return
        }

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        binding.configDigitalSave.setOnClickListener { saveConfig() }
        binding.configDigitalBgColor.setOnClickListener { pickBackgroundColor() }
        binding.configDigitalTextColor.setOnClickListener { pickTextColor() }

        val primaryColor = getProperPrimaryColor()
        binding.configDigitalBgSeekbar.setColors(mTextColor, primaryColor, primaryColor)

        if (!isCustomizingColors && !isOrWasThankYouInstalled()) {
            mFeatureLockedDialog = FeatureLockedDialog(this) {
                if (!isOrWasThankYouInstalled()) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mFeatureLockedDialog != null && isOrWasThankYouInstalled()) {
            mFeatureLockedDialog?.dismissDialog()
        }
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        mBgAlpha = Color.alpha(mBgColor) / 255.toFloat()
        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))

        binding.configDigitalBgSeekbar.setOnSeekBarChangeListener(bgSeekbarChangeListener)
        binding.configDigitalBgSeekbar.progress = (mBgAlpha * 100).toInt()
        updateBackgroundColor()

        mTextColor = config.widgetTextColor
        if (mTextColor == resources.getColor(com.simplemobiletools.commons.R.color.default_widget_text_color) && config.isUsingSystemTheme) {
            mTextColor = resources.getColor(com.simplemobiletools.commons.R.color.you_primary_color, theme)
        }

        updateTextColor()
    }

    private fun saveConfig() {
        storeWidgetColors()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetColors() {
        config.apply {
            widgetBgColor = mBgColor
            widgetTextColor = mTextColor
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColor = color
                updateTextColor()
            }
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyVerticalDigitalTimeWidgetProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateTextColor() {
        binding.configDigitalTextColor.setFillWithStroke(mTextColor, mTextColor)
        binding.configDigitalTimeHour.setTextColor(mTextColor)
        binding.configDigitalTimeMinute.setTextColor(mTextColor)
        binding.configDigitalDate.setTextColor(mTextColor)
        binding.configDigitalSave.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun updateBackgroundColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        binding.configDigitalBgColor.setFillWithStroke(mBgColor, mBgColor)
        binding.configDigitalBackground.applyColorFilter(mBgColor)
        binding.configDigitalSave.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private val bgSeekbarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mBgAlpha = progress.toFloat() / 100.toFloat()
            updateBackgroundColor()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }
}
