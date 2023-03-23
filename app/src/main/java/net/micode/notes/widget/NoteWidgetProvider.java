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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.ResourceParser;
import net.micode.notes.ui.NoteEditActivity;
import net.micode.notes.ui.NotesListActivity;

/**
 * @version: V1.0
 * @author: Yi Huang
 * @className: NoteWidgetProvider
 * @packageName: widget
 * @description: 便签小部件的提供者
 * @data: 2023-03-14
 **/
public abstract class NoteWidgetProvider extends AppWidgetProvider {
    public static final String [] PROJECTION = new String [] {
        NoteColumns.ID,
        NoteColumns.BG_COLOR_ID,
        NoteColumns.SNIPPET
    };

    public static final int COLUMN_ID           = 0;
    public static final int COLUMN_BG_COLOR_ID  = 1;
    public static final int COLUMN_SNIPPET      = 2;

    private static final String TAG = "NoteWidgetProvider";

    /**
     * @author:  Yi Huang
     * @methodsName: onDeleted
     * @description: 将widget id 在 appWidgetIds 之中的便签的 WIDGET_ID 设置为 Invalid
     * @param: Context context, int[] appWidgetIds
     * @return: void
     * @throws:
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        ContentValues values = new ContentValues();
        values.put(NoteColumns.WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        for (int i = 0; i < appWidgetIds.length; i++) {
            // 在 NOTE_URI 中查找 WIDGET_ID 在 appWidgetIds 中的便签，将其 WIDGET_ID 设置为 Invalid
            context.getContentResolver().update(Notes.CONTENT_NOTE_URI,
                    values,
                    NoteColumns.WIDGET_ID + "=?",
                    new String[] { String.valueOf(appWidgetIds[i])});
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getNoteWidgetInfo
     * @description: 获取 WIDGET_ID 等于给定值的便签的 ID，BG_COLOR_ID，SNIPPET
     * @param: Context context, int widgetId
     * @return: Cursor
     * @throws:
     */
    private Cursor getNoteWidgetInfo(Context context, int widgetId) {
        return context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                PROJECTION,
                NoteColumns.WIDGET_ID + "=? AND " + NoteColumns.PARENT_ID + "<>?",
                new String[] { String.valueOf(widgetId), String.valueOf(Notes.ID_TRASH_FOLER) },
                null);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: update
     * @description: 获取 WIDGET_ID 等于给定值的便签的 ID，BG_COLOR_ID，SNIPPET
     * @param: Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds
     * @return: void
     * @throws:
     */
    protected void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        update(context, appWidgetManager, appWidgetIds, false);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: update
     * @description: 获取 WIDGET_ID 等于给定值的便签的 ID，BG_COLOR_ID，SNIPPET
     * @param: Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, boolean privacyMode
     * @return: void
     * @throws:
     */
    private void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
            boolean privacyMode) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            if (appWidgetIds[i] != AppWidgetManager.INVALID_APPWIDGET_ID) {
                int bgId = ResourceParser.getDefaultBgId(context);
                String snippet = "";
                Intent intent = new Intent(context, NoteEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 设置intent的flags
                intent.putExtra(Notes.INTENT_EXTRA_WIDGET_ID, appWidgetIds[i]); // 把widget id加入intent，以供其他活动使用
                intent.putExtra(Notes.INTENT_EXTRA_WIDGET_TYPE, getWidgetType()); // 把widget type加入intent，以供其他活动使用

                Cursor c = getNoteWidgetInfo(context, appWidgetIds[i]);
                if (c != null && c.moveToFirst()) { // 如果cursor c不为空，就将c移动到结果集的第一行
                    if (c.getCount() > 1) { // 多条message拥有同一个widget id
                        Log.e(TAG, "Multiple message with same widget id:" + appWidgetIds[i]);
                        c.close();
                        return;
                    }
                    snippet = c.getString(COLUMN_SNIPPET); // 获取笔记片段
                    bgId = c.getInt(COLUMN_BG_COLOR_ID); // 获取笔记的背景颜色ID
                    intent.putExtra(Intent.EXTRA_UID, c.getLong(COLUMN_ID)); // 创建ID
                    intent.setAction(Intent.ACTION_VIEW); // 设置活动为ACTION_VIEW
                } else { // 若c为空
                    snippet = context.getResources().getString(R.string.widget_havenot_content); //将snippet设置为默认字符串
                    intent.setAction(Intent.ACTION_INSERT_OR_EDIT); // 设置活动为ACTION_INSERT_OR_EDIT
                }

                if (c != null) {
                    c.close(); // 关闭cursor
                }

                RemoteViews rv = new RemoteViews(context.getPackageName(), getLayoutId()); // 创建一个 RemoteViews 对象，它表示小部件的 UI 布局。
                rv.setImageViewResource(R.id.widget_bg_image, getBgResourceId(bgId)); // 使用 setImageViewResource 方法设置小部件的背景图像
                intent.putExtra(Notes.INTENT_EXTRA_BACKGROUND_ID, bgId); // 使用 putExtra 将背景颜色 ID 添加到 Intent
                /**
                 * Generate the pending intent to start host for the widget
                 */
                PendingIntent pendingIntent = null;
                if (privacyMode) { // 若处于隐私模式
                    // 创建一个 PendingIntent 以启动应用程序的 NotesListActivity，只能查看便签
                    rv.setTextViewText(R.id.widget_text,
                            context.getString(R.string.widget_under_visit_mode));
                    pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], new Intent(
                            context, NotesListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                } else { // 若不处于隐私模式
                    // 创建一个 PendingIntent 以启动应用程序的 NoteEditActivity，可以编辑便器
                    rv.setTextViewText(R.id.widget_text, snippet);
                    pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i], intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                }

                rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent); // 使用 setOnClickPendingIntent 将此 PendingIntent 设置为小部件的文本视图
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getBgResourceId
     * @description: 获取背景资源的ID
     * @param: int bgId
     * @return: int
     * @throws:
     */
    protected abstract int getBgResourceId(int bgId);

    /**
     * @author:  Yi Huang
     * @methodsName: getLayoutId
     * @description: 获取布局的ID
     * @param:
     * @return: int
     * @throws:
     */
    protected abstract int getLayoutId();

    /**
     * @author:  Yi Huang
     * @methodsName: getWidgetType
     * @description: 获取小挂件的类型
     * @param:
     * @return: int
     * @throws:
     */
    protected abstract int getWidgetType();
}
