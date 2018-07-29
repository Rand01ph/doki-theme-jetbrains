/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Chris Magnussen and Elior Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.chrisrm.idea.actions.themes;

import com.chrisrm.idea.MTConfig;
import com.chrisrm.idea.tree.MTProjectViewNodeDecorator;
import com.chrisrm.idea.ui.MTButtonUI;
import com.chrisrm.idea.ui.MTTreeUI;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public abstract class MTAbstractThemeAction extends ToggleAction {

  @Override
  public void setSelected(final AnActionEvent e, final boolean state) {
    selectionActivation();
  }

  public void selectionActivation() {
    MTTreeUI.resetIcons();
    MTButtonUI.resetCache();
    MTProjectViewNodeDecorator.resetCache();
  }

  /**
   * Set button disabled if material theme is disabled
   *
   * @param e
   */
  @Override
  public void update(final AnActionEvent e) {
    e.getPresentation().setEnabled(MTConfig.getInstance().isMaterialTheme());
  }
}
