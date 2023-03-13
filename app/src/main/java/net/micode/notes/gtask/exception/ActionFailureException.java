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

package net.micode.notes.gtask.exception;

/**
 * 操作执行失败时抛出的异常
 */
public class ActionFailureException extends RuntimeException {
    private static final long serialVersionUID = 4425249765923293627L;  // 序列码

    public ActionFailureException() {
        super();
    }   // 构造函数

    public ActionFailureException(String paramString) {
        super(paramString);
    }   // 抛出相关异常

    public ActionFailureException(String paramString, Throwable paramThrowable) {   // 抛出相关异常
        super(paramString, paramThrowable);
    }
}
