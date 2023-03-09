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
/**
 * @analyst: lwq
 */

package net.micode.notes.gtask.data;

import android.database.Cursor;

import org.json.JSONObject;

/**
 * 同步操作所使用到的数据类型及方法
 */
public abstract class Node {
    /**
     * 以下静态常量代表不同的同步状态
     * local：本地，remote：云端
     */
    public static final int SYNC_ACTION_NONE = 0;   // 本地及云端无可执行操作

    public static final int SYNC_ACTION_ADD_REMOTE = 1; // 云端需添加内容

    public static final int SYNC_ACTION_ADD_LOCAL = 2;  // 本地需添加内容

    public static final int SYNC_ACTION_DEL_REMOTE = 3; // 云端需删除内容

    public static final int SYNC_ACTION_DEL_LOCAL = 4;  // 本地需删除内容

    public static final int SYNC_ACTION_UPDATE_REMOTE = 5;  // 云端需更新内容

    public static final int SYNC_ACTION_UPDATE_LOCAL = 6;   // 本地需更新内容

    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7;    // 同步更新过程中发生冲突

    public static final int SYNC_ACTION_ERROR = 8;  // 同步过程发生错误

    private String mGid;    // 消息id

    private String mName;   // 消息名称

    private long mLastModified; // 最后一次修改时间

    private boolean mDeleted;   // 代表当前消息是否被删除

    /**
     * 消息节点对象
     */
    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }

    /**
     * 以下方法为抽象类，此处仅做声明，需在子类中具体实现
     */
    public abstract JSONObject getCreateAction(int actionId);   // 给定actionId，创建并返回当前Node中的JSONObject对象

    public abstract JSONObject getUpdateAction(int actionId);   // 更新JSONObject

    public abstract void setContentByRemoteJSON(JSONObject js); // 给定远程Node的JSONObject，更新当前Node的内容

    public abstract void setContentByLocalJSON(JSONObject js);  // 给定本地Node的JSONObject，更新当前Node的内容

    public abstract JSONObject getLocalJSONFromContent();

    public abstract int getSyncAction(Cursor c);

    /**
     * 以下方法为常规属性设置方法，此处不再标记
     */
    public void setGid(String gid) {
        this.mGid = gid;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public String getGid() {
        return this.mGid;
    }

    public String getName() {
        return this.mName;
    }

    public long getLastModified() {
        return this.mLastModified;
    }

    public boolean getDeleted() {
        return this.mDeleted;
    }

}
