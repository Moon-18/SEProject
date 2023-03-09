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

package net.micode.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.HashMap;
/**
 * @version:v1.0
 * @author:ght
 * @classname Contact
 * @packageName:data
 * @description:使用数据库查询语言建立与数据库的联系，建立电话和联系人的映射，提供联系人的查询方法
 * @date:2023.3.28
**/
public class Contact {//联系人类
    private static HashMap<String, String> sContactCache;//cache，用于提升查询速度
    private static final String TAG = "Contact";
    //定义字符串CALLER_ID_SELECTION，使用字符串拼接出数据库查询语言
    private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
    + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
    + " AND " + Data.RAW_CONTACT_ID + " IN "
            + "(SELECT raw_contact_id "
            + " FROM phone_lookup"
            + " WHERE min_match = '+')";
    //获取联系人,传入context内容和电话号码，返回对应联系人的姓名
    public static String getContact(Context context, String phoneNumber) {
        //如果cache为空则新建
        if(sContactCache == null) {
            sContactCache = new HashMap<String, String>();
        }
        // 如果cache命中则返回结果
        if(sContactCache.containsKey(phoneNumber)) {
            return sContactCache.get(phoneNumber);
        }
        //否则进入数据库查询信息
        String selection = CALLER_ID_SELECTION.replace("+",
                PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));
        Cursor cursor = context.getContentResolver().query(//数据库查询
                Data.CONTENT_URI,
                new String [] { Phone.DISPLAY_NAME },
                selection,
                new String[] { phoneNumber },
                null);
        // 如果查询成功得到结果name则更新cache并返回答案
        // 如果查询失败则给出提示信息
        //moveToFirst返回第一条
        if (cursor != null && cursor.moveToFirst()) {
            try {
            //找到相关信息
                String name = cursor.getString(0);
                sContactCache.put(phoneNumber, name);
                return name;
            //异常
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, " Cursor get string error " + e.toString());
                return null;
            } finally {
                cursor.close();
            }
            //未找到相关信息
        } else {
            Log.d(TAG, "No contact matched with number:" + phoneNumber);
            return null;
        }
    }
}
