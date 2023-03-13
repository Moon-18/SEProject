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

package net.micode.notes.model;

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.tool.ResourceParser.NoteBgResources;

/**
 * @version: V1.0
 * @author: Yi Huang
 * @className: WorkingNote
 * @packageName: model
 * @description: 描述当前工作便签的类
 * @data: 2023-03-14
 **/
public class WorkingNote {
    // Note for the working note
    private Note mNote;
    // Note Id
    private long mNoteId;
    // Note content
    private String mContent;
    // Note mode
    private int mMode;

    private long mAlertDate;

    private long mModifiedDate;

    private int mBgColorId;

    private int mWidgetId;

    private int mWidgetType;

    private long mFolderId;

    private Context mContext;

    private static final String TAG = "WorkingNote";

    private boolean mIsDeleted;

    private NoteSettingChangedListener mNoteSettingStatusListener;

    public static final String[] DATA_PROJECTION = new String[] {
            DataColumns.ID,
            DataColumns.CONTENT,
            DataColumns.MIME_TYPE,
            DataColumns.DATA1,
            DataColumns.DATA2,
            DataColumns.DATA3,
            DataColumns.DATA4,
    };

    public static final String[] NOTE_PROJECTION = new String[] {
            NoteColumns.PARENT_ID,
            NoteColumns.ALERTED_DATE,
            NoteColumns.BG_COLOR_ID,
            NoteColumns.WIDGET_ID,
            NoteColumns.WIDGET_TYPE,
            NoteColumns.MODIFIED_DATE
    };

    private static final int DATA_ID_COLUMN = 0;

    private static final int DATA_CONTENT_COLUMN = 1;

    private static final int DATA_MIME_TYPE_COLUMN = 2;

    private static final int DATA_MODE_COLUMN = 3;

    private static final int NOTE_PARENT_ID_COLUMN = 0;

    private static final int NOTE_ALERTED_DATE_COLUMN = 1;

    private static final int NOTE_BG_COLOR_ID_COLUMN = 2;

    private static final int NOTE_WIDGET_ID_COLUMN = 3;

    private static final int NOTE_WIDGET_TYPE_COLUMN = 4;

    private static final int NOTE_MODIFIED_DATE_COLUMN = 5;

    // New note construct
    /**
     * @author:  Yi Huang
     * @methodsName: WorkingNote
     * @description: 构造函数
     * @param: Context context, long noteId
     * @return:
     * @throws:
     */
    private WorkingNote(Context context, long folderId) {
        mContext = context;
        mAlertDate = 0;
        mModifiedDate = System.currentTimeMillis();
        mFolderId = folderId;
        mNote = new Note();
        mNoteId = 0;
        mIsDeleted = false;
        mMode = 0;
        mWidgetType = Notes.TYPE_WIDGET_INVALIDE;
    }


    // Existing note construct
    /**
     * @author:  Yi Huang
     * @methodsName: WorkingNote
     * @description: 构造函数
     * @param: Context context, long noteId, long folderId
     * @return:
     * @throws:
     */
    private WorkingNote(Context context, long noteId, long folderId) {
        mContext = context;
        mNoteId = noteId;
        mFolderId = folderId;
        mIsDeleted = false;
        mNote = new Note();
        loadNote();
    }

    /**
     * @author:  Yi Huang
     * @methodsName: loadNote
     * @description: 加载已有的Note
     * @param:
     * @return: void
     * @throws:
     */
    private void loadNote() {

        // 根据mNoteId在数据库中检索相应的row
        Cursor cursor = mContext.getContentResolver().query(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId), NOTE_PROJECTION, null,
                null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
                mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
                mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
                mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
                mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
                mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
            }
            cursor.close();
        } else {
            Log.e(TAG, "No note with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
        }
        loadNoteData();
    }

    /**
     * @author:  Yi Huang
     * @methodsName: loadNoteData
     * @description: 加载NoteData
     * @param:
     * @return: void
     * @throws: IllegalArgumentException
     */
    private void loadNoteData() {

        // 在数据库中检索NOTE_ID=mNoteId的row
        Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
                DataColumns.NOTE_ID + "=?", new String[] {
                        String.valueOf(mNoteId)
                }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
                    if (DataConstants.NOTE.equals(type)) { // 加载文本数据
                        mContent = cursor.getString(DATA_CONTENT_COLUMN);
                        mMode = cursor.getInt(DATA_MODE_COLUMN);
                        mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else if (DataConstants.CALL_NOTE.equals(type)) { // 加载呼叫数据
                        mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else {
                        Log.d(TAG, "Wrong note type with type:" + type);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.e(TAG, "No data with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: createEmptyNote
     * @description: 创建空的Note
     * @param: Context context, long folderId, int widgetId, int widgetType, int defaultBgColorId
     * @return: WorkingNote
     * @throws:
     */
    public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
                                              int widgetType, int defaultBgColorId) {
        WorkingNote note = new WorkingNote(context, folderId);
        note.setBgColorId(defaultBgColorId);
        note.setWidgetId(widgetId);
        note.setWidgetType(widgetType);
        return note;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: load
     * @description: 根据id加载已有的Note
     * @param: Context context, long id
     * @return: WorkingNote
     * @throws:
     */
    public static WorkingNote load(Context context, long id) {
        return new WorkingNote(context, id, 0);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: saveNote
     * @description: 保存Note到数据库；若返回True，则代表保存成功；若返回False，代表保存失败
     * @param:
     * @return: boolean
     * @throws:
     */
    public synchronized boolean saveNote() {
        if (isWorthSaving()) { // 判断是否值得保存
            if (!existInDatabase()) { // 若在数据库中不存在
                if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
                    Log.e(TAG, "Create new note fail with id:" + mNoteId);
                    return false;
                }
            }

            mNote.syncNote(mContext, mNoteId);

            // 如果存在该笔记的任何小部件，则更新小部件内容
            if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                    && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                    && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: existInDatabase
     * @description: 判断Note是否在数据库中存在；若存在，返回True；若不存在，返回False
     * @param:
     * @return: boolean
     * @throws:
     */
    public boolean existInDatabase() {
        return mNoteId > 0;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: isWorthSaving
     * @description: 判断Note是否值得保存；若值得，返回True；若不值得，返回False
     * @param:
     * @return: boolean
     * @throws:
     */
    private boolean isWorthSaving() {
        //在以下三种情况下，WorkingNote不值得保存：1）被删除；2）在数据库中不存在且文本内容为空；3）在数据库中存在但未被本地修改
        if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
                || (existInDatabase() && !mNote.isLocalModified())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setOnSettingStatusChangedListener
     * @description: 设置NoteSettingChangedListener
     * @param: NoteSettingChangedListener l
     * @return: void
     * @throws:
     */
    public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
        mNoteSettingStatusListener = l;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setAlertDate
     * @description: 设置提醒日期
     * @param: long date, boolean set
     * @return: void
     * @throws:
     */
    public void setAlertDate(long date, boolean set) {
        if (date != mAlertDate) {
            mAlertDate = date;
            mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate)); // 更新提醒日期
        }
        if (mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onClockAlertChanged(date, set);
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: markDeleted
     * @description: 删除Note
     * @param: boolean mark
     * @return: void
     * @throws:
     */
    public void markDeleted(boolean mark) {
        mIsDeleted = mark;
        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                && mWidgetType != Notes.TYPE_WIDGET_INVALIDE && mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onWidgetChanged();
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setBgColorId
     * @description: 设置BgColorId
     * @param: int id
     * @return: void
     * @throws:
     */
    public void setBgColorId(int id) {
        if (id != mBgColorId) {
            mBgColorId = id;
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onBackgroundColorChanged();
            }
            mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setCheckListMode
     * @description: 设置文本数据的MODE；若MODE=1，表示在check list；若MODE=0，表示不在check list
     * @param: int mode
     * @return: void
     * @throws:
     */
    public void setCheckListMode(int mode) {
        if (mMode != mode) {
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
            }
            mMode = mode;
            mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setWidgetType
     * @description: 设置WidgetType
     * @param: int type
     * @return: void
     * @throws:
     */
    public void setWidgetType(int type) {
        if (type != mWidgetType) {
            mWidgetType = type;
            mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(mWidgetType));
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setWidgetId
     * @description: 设置WidgetId
     * @param: int id
     * @return: void
     * @throws:
     */
    public void setWidgetId(int id) {
        if (id != mWidgetId) {
            mWidgetId = id;
            mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(mWidgetId));
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setWorkingText
     * @description: 设置文本
     * @param: String text
     * @return: void
     * @throws:
     */
    public void setWorkingText(String text) {
        if (!TextUtils.equals(mContent, text)) {
            mContent = text;
            mNote.setTextData(DataColumns.CONTENT, mContent);
        }
    }

    /**
     * @author:  Yi Huang
     * @methodsName: convertToCallNote
     * @description: 转化为通话数据
     * @param: String phoneNumber, long callDate
     * @return: void
     * @throws:
     */
    public void convertToCallNote(String phoneNumber, long callDate) {
        mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate)); // 设置通话日期
        mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber); // 设置通话号码
        mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER)); // 将Note存到专门存放通话数据的文件夹中
    }

    /**
     * @author:  Yi Huang
     * @methodsName: hasClockAlert
     * @description: 判断是否有闹钟提醒；若有，返回True；若没有，则返回False
     * @param:
     * @return: boolean
     * @throws:
     */
    public boolean hasClockAlert() {
        return (mAlertDate > 0 ? true : false);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getContent
     * @description: 获取文本
     * @param:
     * @return: String
     * @throws:
     */
    public String getContent() {
        return mContent;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getAlertDate
     * @description: 获取提醒日期
     * @param:
     * @return: long
     * @throws:
     */
    public long getAlertDate() {
        return mAlertDate;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getModifiedDate
     * @description: 获取修改日期
     * @param:
     * @return: long
     * @throws:
     */
    public long getModifiedDate() {
        return mModifiedDate;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getBgColorResId
     * @description: 获取便签背景颜色资源ID
     * @param:
     * @return: int
     * @throws:
     */
    public int getBgColorResId() {
        return NoteBgResources.getNoteBgResource(mBgColorId);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getBgColorId
     * @description: 获取便签背景颜色ID
     * @param:
     * @return: int
     * @throws:
     */
    public int getBgColorId() {
        return mBgColorId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getTitleBgResId
     * @description: 获取标题背景颜色ID
     * @param:
     * @return: int
     * @throws:
     */
    public int getTitleBgResId() {
        return NoteBgResources.getNoteTitleBgResource(mBgColorId);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getCheckListMode
     * @description: 获取CheckListMode；若Note在check list中，返回True；若Note不在check list中，返回False
     * @param:
     * @return: int
     * @throws:
     */
    public int getCheckListMode() {
        return mMode;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getNoteId
     * @description: 获取Note对应的ID
     * @param:
     * @return: long
     * @throws:
     */
    public long getNoteId() {
        return mNoteId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getFolderId
     * @description: 获取Note所在文件夹的ID
     * @param:
     * @return: long
     * @throws:
     */
    public long getFolderId() {
        return mFolderId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getWidgetId
     * @description: 获取WidgetId
     * @param:
     * @return: int
     * @throws:
     */
    public int getWidgetId() {
        return mWidgetId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getWidgetType
     * @description: 获取WidgetType
     * @param:
     * @return: int
     * @throws:
     */
    public int getWidgetType() {
        return mWidgetType;
    }

    /**
     * @version: V1.0
     * @author: Yi Huang
     * @className: NoteSettingChangedListener
     * @packageName: model
     * @description: 监视便签的属性设置是否改变
     * @data: 2023-03-07
     **/
    public interface NoteSettingChangedListener {
        /**
         * Called when the background color of current note has just changed
         */
        /**
         * @author:  Yi Huang
         * @methodsName: onBackgroundColorChanged
         * @description: 在当前笔记的背景颜色刚刚改变时被调用
         * @param:
         * @return: void
         * @throws:
         */
        void onBackgroundColorChanged();

        /**
         * Called when user set clock
         */
        /**
         * @author:  Yi Huang
         * @methodsName: onClockAlertChanged
         * @description: 在用户设置闹钟时被调用
         * @param:
         * @return: void
         * @throws:
         */
        void onClockAlertChanged(long date, boolean set);

        /**
         * Call when user create note from widget
         */
        /**
         * @author:  Yi Huang
         * @methodsName: onWidgetChanged
         * @description: 当用户从小部件创建笔记时被调用
         * @param:
         * @return: void
         * @throws:
         */
        void onWidgetChanged();

        /**
         * Call when switch between check list mode and normal mode
         * @param oldMode is previous mode before change
         * @param newMode is new mode
         */
        /**
         * @author:  Yi Huang
         * @methodsName: onCheckListModeChanged
         * @description: 清单模式和普通模式切换时被调用
         * @param: int oldMode, int newMode
         * @return: void
         * @throws:
         */
        void onCheckListModeChanged(int oldMode, int newMode);
    }
}
