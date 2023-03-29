/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.ResourceParser;

/**
 * @version: V1.0
 * @author: Yi Huang
 * @className: NoteWidgetProvider_2x
 * @packageName: widget
 * @description: 2倍大小的便签小组件
 * @data: 2023-03-29
 **/
public class NoteWidgetProvider_2x extends NoteWidgetProvider {
    @Override
    /**
     * @author:  Yi Huang
     * @methodsName: onUpdate
     * @description: 更新
     * @param: Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds
     * @return: void
     * @throws:
     */
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    @Override
    /**
     * @author:  Yi Huang
     * @methodsName: getLayoutId
     * @description: 获取布局ID
     * @param:
     * @return: int
     * @throws:
     */
    protected int getLayoutId() {
        return R.layout.widget_2x;
    }

    @Override
    /**
     * @author:  Yi Huang
     * @methodsName: getBgResourceId
     * @description: 根据背景ID获取背景资源ID
     * @param: int bgId
     * @return: int
     * @throws:
     */
    protected int getBgResourceId(int bgId) {
        return ResourceParser.WidgetBgResources.getWidget2xBgResource(bgId);
    }

    @Override
    /**
     * @author:  Yi Huang
     * @methodsName: getLayoutId
     * @description: 获取小组件类型
     * @param:
     * @return: int
     * @throws:
     */
    protected int getWidgetType() {
        return Notes.TYPE_WIDGET_2X;
    }
}
