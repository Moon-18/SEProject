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
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;

import java.util.ArrayList;

/**
 * @version: V1.0
 * @author: Yi Huang
 * @className: Note
 * @packageName: model
 * @description: 描述单个便签的类
 * @data: 2023-03-07
 **/

public class Note {
    private ContentValues mNoteDiffValues;
    private NoteData mNoteData;
    private static final String TAG = "Note";

    /**
     * @author:  Yi Huang
     * @methodsName: getNewNoteId
     * @description: 创建一个新的note id，用于将新的note添加到数据库
     * @param:  Context context, long folderId 文件夹ID
     * @return: long
     * @throws: IllegalStateException
     */
    public static synchronized long getNewNoteId(Context context, long folderId) {
        // Create a new note in the database
        ContentValues values = new ContentValues();
        long createdTime = System.currentTimeMillis(); // 获取当前时间
        values.put(NoteColumns.CREATED_DATE, createdTime); // 设置创建时间为当前时间
        values.put(NoteColumns.MODIFIED_DATE, createdTime); // 设置修改时间为当前时间
        values.put(NoteColumns.TYPE, Notes.TYPE_NOTE); // 设置类型为TYPE_NOTE
        values.put(NoteColumns.LOCAL_MODIFIED, 1); // 已被本地修改
        values.put(NoteColumns.PARENT_ID, folderId); // 设置PARENT_ID为文件夹ID
        Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI, values); // 向数据库插入values，返回uri
        // context.getContentResolver()返回ContentResolver类的一个对象

        long noteId = 0;
        try {
            noteId = Long.valueOf(uri.getPathSegments().get(1)); // 解析uri获取ID
        } catch (NumberFormatException e) {
            Log.e(TAG, "Get note id error :" + e.toString());
            noteId = 0;
        }
        if (noteId == -1) {
            throw new IllegalStateException("Wrong note id:" + noteId);
        }
        return noteId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: Note
     * @description: Note类的构造函数
     * @param:
     * @return:
     * @throws:
     */
    public Note() {
        mNoteDiffValues = new ContentValues();
        mNoteData = new NoteData();
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setNoteValue
     * @description: 通过修改mNoteDiffValues，来设置便签的属性
     * @param: String key 被设置的属性, String value 被设置的值
     * @return: void
     * @throws:
     */
    public void setNoteValue(String key, String value) {
        mNoteDiffValues.put(key, value);
        mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
        mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setTextData
     * @description: 修改文本数据
     * @param: String key 被设置的属性, String value 被设置的值
     * @return: void
     * @throws:
     */
    public void setTextData(String key, String value) {
        mNoteData.setTextData(key, value);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setTextDataId
     * @description: 设置文本数据的ID
     * @param: long id 要被设置的ID
     * @return: void
     * @throws:
     */
    public void setTextDataId(long id) {
        mNoteData.setTextDataId(id);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: getTextDataId
     * @description: 返回文本数据的ID
     * @param:
     * @return: long
     * @throws:
     */
    public long getTextDataId() {
        return mNoteData.mTextDataId;
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setCallDataId
     * @description: 设置通话数据的ID
     * @param: long id 要被设置的id
     * @return: void
     * @throws:
     */
    public void setCallDataId(long id) {
        mNoteData.setCallDataId(id);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: setCallData
     * @description: 修改通话数据
     * @param: String key 被设置的属性, String value 被设置的值
     * @return: void
     * @throws:
     */
    public void setCallData(String key, String value) {
        mNoteData.setCallData(key, value);
    }

    /**
     * @author:  Yi Huang
     * @methodsName: isLocalModified
     * @description: 判断便签Note是否被本地修改；若修改，返回1，否则返回0
     * @param:
     * @return: boolean
     * @throws:
     */
    public boolean isLocalModified() {
        return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified();
    }

    /**
     * @author:  Yi Huang
     * @methodsName: syncNote
     * @description: 同步便签Note到数据库；同步成功返回True，否则返回False
     * @param: Context context, long noteId
     * @return: boolean
     * @throws: IllegalArgumentException
     */
    public boolean syncNote(Context context, long noteId) {
        if (noteId <= 0) {
            throw new IllegalArgumentException("Wrong note id:" + noteId);
        }

        // 若没有被本地修改，则不用同步，直接返回True
        if (!isLocalModified()) {
            return true;
        }

        /**
         * In theory, once data changed, the note should be updated on {@link NoteColumns#LOCAL_MODIFIED} and
         * {@link NoteColumns#MODIFIED_DATE}. For data safety, though update note fails, we also update the
         * note data info
         */
        // 用mNoteDiffValues更新noteId对应的数据库中对应的row
        if (context.getContentResolver().update(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), mNoteDiffValues, null,
                null) == 0) {
            Log.e(TAG, "Update note error, should not happen");
            // Do not return, fall through
        }
        mNoteDiffValues.clear(); // 同步完成后，清空mNoteDiffValues

        // 若mNoteData被本地修改，则同步mNoteData。若同步失败，返回False
        if (mNoteData.isLocalModified()
                && (mNoteData.pushIntoContentResolver(context, noteId) == null)) {
            return false;
        }

        return true;
    }
    /**
     * @version: V1.0
     * @author: Yi Huang
     * @className: NoteData
     * @packageName: model
     * @description: 描述便签数据的类；便签数据具体包括文本数据和通话数据
     * @data: 2023-03-07
     **/
    private class NoteData {
        private long mTextDataId;

        private ContentValues mTextDataValues;

        private long mCallDataId;

        private ContentValues mCallDataValues;

        private static final String TAG = "NoteData";

        /**
         * @author:  Yi Huang
         * @methodsName: NoteData
         * @description: 构造函数，初始化类内的数据结构
         * @param:
         * @return:
         * @throws:
         */
        public NoteData() {
            mTextDataValues = new ContentValues();
            mCallDataValues = new ContentValues();
            mTextDataId = 0;
            mCallDataId = 0;
        }

        /**
         * @author:  Yi Huang
         * @methodsName: isLocalModified
         * @description: 判断便签数据是否被本地修改；若修改，返回1，否则返回0
         * @param:
         * @return: boolean
         * @throws:
         */
        boolean isLocalModified() {
            return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
        }

        /**
         * @author:  Yi Huang
         * @methodsName: setTextDataId
         * @description: 设置文本数据的ID
         * @param: long id
         * @return: void
         * @throws: IllegalArgumentException
         */
        void setTextDataId(long id) {
            if(id <= 0) {
                throw new IllegalArgumentException("Text data id should larger than 0");
            }
            mTextDataId = id;
        }

        /**
         * @author:  Yi Huang
         * @methodsName: setCallDataId
         * @description: 设置通话数据的ID
         * @param: long id
         * @return: void
         * @throws: IllegalArgumentException
         */
        void setCallDataId(long id) {
            if (id <= 0) {
                throw new IllegalArgumentException("Call data id should larger than 0");
            }
            mCallDataId = id;
        }

        /**
         * @author:  Yi Huang
         * @methodsName: setCallData
         * @description: 修改通话数据
         * @param: String key 被修改的属性, String value 要改成的目标值
         * @return: void
         * @throws:
         */
        void setCallData(String key, String value) {
            mCallDataValues.put(key, value);
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }

        /**
         * @author:  Yi Huang
         * @methodsName: setTextData
         * @description: 修改文本数据
         * @param: String key 被修改的属性, String value 要改成的目标值
         * @return: void
         * @throws:
         */
        void setTextData(String key, String value) {
            mTextDataValues.put(key, value);
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }

        /**
         * @author:  Yi Huang
         * @methodsName: pushIntoContentResolver
         * @description: 把文本数据和通话数据同步到数据库,若返回空，则说明同步失败
         * @param: Context context, long noteId
         * @return: Uri
         * @throws: IllegalArgumentException
         */
        Uri pushIntoContentResolver(Context context, long noteId) {
            /**
             * Check for safety
             */
            if (noteId <= 0) {
                throw new IllegalArgumentException("Wrong note id:" + noteId);
            }

            ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            ContentProviderOperation.Builder builder = null;

            // 同步文本数据
            if(mTextDataValues.size() > 0) {
                mTextDataValues.put(DataColumns.NOTE_ID, noteId);
                if (mTextDataId == 0) { // 如果文本数据还没有对应的ID
                    mTextDataValues.put(DataColumns.MIME_TYPE, TextNote.CONTENT_ITEM_TYPE); // 设置MIME_TYPE
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mTextDataValues); // 插入mTextDataValues
                    try {
                        setTextDataId(Long.valueOf(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new text data fail with noteId" + noteId);
                        mTextDataValues.clear();
                        return null;
                    }
                } else { // 如果文本数据已有对应的ID
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mTextDataId)); // 创建一个进行更新操作的builder对象
                    builder.withValues(mTextDataValues);
                    operationList.add(builder.build());
                }
                mTextDataValues.clear();
            }

            // 同步通话数据
            if(mCallDataValues.size() > 0) {
                mCallDataValues.put(DataColumns.NOTE_ID, noteId);
                if (mCallDataId == 0) {
                    mCallDataValues.put(DataColumns.MIME_TYPE, CallNote.CONTENT_ITEM_TYPE); // 设置MIME_TYPE
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mCallDataValues); // 插入mCallDataValues
                    try {
                        setCallDataId(Long.valueOf(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new call data fail with noteId" + noteId);
                        mCallDataValues.clear();
                        return null;
                    }
                } else {
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mCallDataId));
                    builder.withValues(mCallDataValues);
                    operationList.add(builder.build());
                }
                mCallDataValues.clear();
            }

            // ContentResolver执行operationList中的一系列操作（其实最多只有两个）
            if (operationList.size() > 0) {
                try {
                    ContentProviderResult[] results = context.getContentResolver().applyBatch(
                            Notes.AUTHORITY, operationList);
                    return (results == null || results.length == 0 || results[0] == null) ? null
                            : ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
                } catch (RemoteException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    return null;
                } catch (OperationApplicationException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    return null;
                }
            }
            return null;
        }
    }
}
