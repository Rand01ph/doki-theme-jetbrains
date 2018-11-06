package io.acari.DDLC.ui.chibi

import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Forged in the flames of battle by alex.
 */
class DDLCChibiPane(private val rootPane: JFrame) : JPanel() {

    init {
        isOpaque = false
        isVisible = false
        layout = null
        DDLCChibiPainters.initEditorPainters(this)
        DDLCChibiPainters.initFramePainters(this)
    }
}

object DDLCChibiPainters {
    fun initFramePainters(ddlcChibiPane: DDLCChibiPane) {

    }
    fun initEditorPainters(ddlcChibiPane: DDLCChibiPane) {

    }
}