/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Chris Magnussen and Elior Boukhobza
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

package io.acari.DDLC.schemes;

import com.chrisrm.ideaddlc.MTConfig;
import com.chrisrm.ideaddlc.MTThemeManager;
import com.chrisrm.ideaddlc.listeners.ConfigNotifier;
import com.chrisrm.ideaddlc.listeners.CustomConfigNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.messages.MessageBusConnection;
import io.acari.DDLC.DDLCConfig;
import org.jetbrains.annotations.NotNull;


/**
 * Component for switching DDLC Themes
 */
public final class DDLCThemesComponent implements ApplicationComponent {

  private MessageBusConnection connect;

  @Override
  public void initComponent() {

    // Activate the theme
    activateTheme();

    connect = ApplicationManager.getApplication().getMessageBus().connect();
    connect.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(MTConfig mtConfig) {
        activateTheme();
      }
    });
    connect.subscribe(CustomConfigNotifier.CONFIG_TOPIC, mtCustomThemeConfig -> activateTheme());
  }

  public static void activateTheme() {
    DDLCConfig ddlcConfig = DDLCConfig.getInstance();
    boolean firstTime = ddlcConfig.isFirstTime();
    if (MTThemeManager.isDDLCActive() || firstTime) {
      MTThemeManager.activate();
      if (firstTime) {
        ddlcConfig.setFirstTime(false);
      }
    }
  }

  @Override
  public void disposeComponent() {
    connect.disconnect();
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "DDLCThemesComponent";
  }
}
