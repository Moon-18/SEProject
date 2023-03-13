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

package net.micode.notes.gtask.data;

import android.database.Cursor;
import android.util.Log;

import net.micode.notes.tool.GTaskStringUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class MetaData extends Task {
    private final static String TAG = MetaData.class.getSimpleName();   // 将当前类名存入字符串

    private String mRelatedGid = null;

    /**
     * 设置元数据
     */
    public void setMeta(String gid, JSONObject metaInfo) {
        try {
            metaInfo.put(GTaskStringUtils.META_HEAD_GTASK_ID, gid); // 将信息放入JSON对象
        } catch (JSONException e) {
            Log.e(TAG, "failed to put related gid");
        }
        setNotes(metaInfo.toString());  // 对当前类对象设置信息
        setName(GTaskStringUtils.META_NOTE_NAME);   // 设置消息名
    }

    /**
     * 返回关联的Gid
     */
    public String getRelatedGid() {
        return mRelatedGid;
    }

    /**
     * 返回消息内容是否为空，是否需要保存
     */
    @Override
    public boolean isWorthSaving() {
        return getNotes() != null;
    }

    /**
     * 重载，给定远程Node的JSONObject，更新当前Node的内容
     * 在此基础上依据note信息生成并设置元数据内容
     */
    @Override
    public void setContentByRemoteJSON(JSONObject js) {
        super.setContentByRemoteJSON(js);
        if (getNotes() != null) {
            try {   // 生成元数据并设置
                JSONObject metaInfo = new JSONObject(getNotes().trim());
                mRelatedGid = metaInfo.getString(GTaskStringUtils.META_HEAD_GTASK_ID);
            } catch (JSONException e) {
                Log.w(TAG, "failed to get related gid");
                mRelatedGid = null;
            }
        }
    }

    /**
     * 使用本地JSON对象设置元数据，调用时抛出异常
     */
    @Override
    public void setContentByLocalJSON(JSONObject js) {
        // this function should not be called
        throw new IllegalAccessError("MetaData:setContentByLocalJSON should not be called");
    }

    /**
     * 从元数据中获取本地JSON对象，调用时抛出异常
     */
    @Override
    public JSONObject getLocalJSONFromContent() {
        throw new IllegalAccessError("MetaData:getLocalJSONFromContent should not be called");
    }

    /**
     * 获取同步状态，调用时抛出异常
     */
    @Override
    public int getSyncAction(Cursor c) {
        throw new IllegalAccessError("MetaData:getSyncAction should not be called");
    }

}
