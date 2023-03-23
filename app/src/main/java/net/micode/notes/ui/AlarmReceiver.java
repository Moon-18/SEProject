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

package net.micode.notes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* @author: gmy
* @className:     AlarmReceiver
* @packageName: ui
* @description: 是一个Android应用程序中的广播接收器（BroadcastReceiver），其作用是在接收到特定广播时启动AlarmAlertActivity活动。
* @date: 2023年3月21日10:27:58
**/
public class AlarmReceiver extends BroadcastReceiver {
    /**
         * @author: gmy
         * @methodsName: onReceive
         * @description: 具体来说，当应用程序中的闹钟到达指定时间时，系统会发送一个广播。此时，AlarmReceiver类将被调用，并且onReceive()方法将被执行。在这个方法内部，
            它会创建一个新的Intent对象并将其目标Activity设置为AlarmAlertActivity。接着，通过addFlags()方法给Intent对象添加了FLAG_ACTIVITY_NEW_TASK标志，
            来指示系统启动该Activity需要在新的任务栈中运行。最后，使用startActivity()方法来启动AlarmAlertActivity。
         * @param: Context context, Intent intent
         * @return: void 无返回值
         * @throws:
         */
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, AlarmAlertActivity.class);
        //启动AlarmAlertActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //activity要存在于activity的栈中，而非activity的途径启动activity时必然不存在一个activity的栈
        //所以要新起一个栈装入启动的activity
        context.startActivity(intent);
    }
}
