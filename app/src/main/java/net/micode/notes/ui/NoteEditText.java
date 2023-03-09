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

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.widget.EditText;

import net.micode.notes.R;

import java.util.HashMap;
import java.util.Map;
/**
 * @version: V1.0
 * @author: gmy
 * @className: NoteEditText
 * @packageName: ui
 * @description: 这是便签编辑类，继承自EditText，设置便签文本框
 * @date: 2023年3月4日19:46:45
 **/
public class NoteEditText extends EditText {
    private static final String TAG = "NoteEditText";
    private int mIndex;//序号
    private int mSelectionStartBeforeDelete;//删之前的序号?

    private static final String SCHEME_TEL = "tel:" ;//电话
    private static final String SCHEME_HTTP = "http:" ;//网络协议
    private static final String SCHEME_EMAIL = "mailto:" ;//邮箱
    //建立一个字符-整数 的map，用于标记电话，网站，还有邮箱
    private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();
    static {
        sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);
        sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);
        sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);
    }

    /**
     * Call by the {@link NoteEditActivity} to delete or add edit text
     * @description 在NoteEditActivity中删除或添加文本的操作，可以认为是一个文本是否被变的标记
     */
    public interface OnTextViewChangeListener {
        /**
         * Delete current edit text when {@link KeyEvent#KEYCODE_DEL} happens
         * and the text is null
         * 处理删除按键时的操作
         */
        void onEditTextDelete(int index, String text);

        /**
         * Add edit text after current edit text when {@link KeyEvent#KEYCODE_ENTER}
         * happen
         * 处理进入按键操作
         */
        void onEditTextEnter(int index, String text);

        /**
         * Hide or show item option when text change
         * 处理
         */
        void onTextChange(int index, boolean hasText);
    }

    private OnTextViewChangeListener mOnTextViewChangeListener;

    /**
     * @author:  gmy
     * @methodsName: NoteEditText
     * @description: 根据context设置文本
     * @param:  context
     * @return: 构造函数,无返回值
     * @throws:
     */
    public NoteEditText(Context context) {
        super(context, null);
        mIndex = 0;
    }
    /**
     * @author:  gmy
     * @methodsName: setIndex
     * @description: 设置当前光标
     * @param:  index
     * @return: void
     * @throws:
     */
    public void setIndex(int index) {
        mIndex = index;
    }
    /**
     * @author:  gmy
     * @methodsName: setOnTextViewChangeListener
     * @description: 初始化文本修改标记
     * @param:  OnTextViewChangeListener
     * @return: void
     * @throws:
     */
    public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
        mOnTextViewChangeListener = listener;
    }

    /**
     * @author:  gmy
     * @methodsName: NoteEditText
     * @description: 维护便签动态变化的属性
     * @param:  Context context 文本内容, AttributeSet attrs 自定义空控件属性
     * @return: 构造函数
     * @throws:
     */
    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     * @author:  gmy
     * @methodsName: NoteEditText
     * @description: 根据defstyle完成初始化
     * @param:  Context context, AttributeSet attrs, int defStyle 定义的样式
     * @return: 构造函数
     * @throws:
     */
    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    /**
     * @author:  gmy
     * @methodsName: onTouchEvent
     * @description: 处理手机屏幕的触摸事件
     * @param:  MotionEvent event 参数event为手机屏幕触摸事件封装类的对象，其中封装了该事件的所有信息，
     * 例如触摸的位置、触摸的类型以及触摸的时间等。该对象会在用户触摸手机屏幕时被创建
     * @return: boolean
     * @throws:
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://当屏幕被按下
                //更新坐标
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();
                //用布局控件layout根据x,y的新值设置新的位置
                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                Selection.setSelection(getText(), off);//更新光标新的位置
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * @author:  gmy
     * @methodsName: onKeyDown
     * @description: 处理用户按下一个键盘按键时会触发的事件
     * @param:  int keyCode 对应的按键编码, KeyEvent event
     * @return: boolean
     * @throws:
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {//根据按键对应的int值来处理
            case KeyEvent.KEYCODE_ENTER://"进入"按键
                if (mOnTextViewChangeListener != null) {
                    return false;
                }
                break;
            case KeyEvent.KEYCODE_DEL://"删除按键"
                mSelectionStartBeforeDelete = getSelectionStart();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);//链式调用,交给父类继续处理
    }

    /**
     * @author:  gmy
     * @methodsName: onKeyUp
     * @description: 和上一个函数对应,处理用户松开一个按键时会触发的事件
     * @param:  int keyCode 对应的按键编码, KeyEvent event
     * @return: boolean
     * @throws:
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode) {//根据键值进行分支操作
            case KeyEvent.KEYCODE_DEL://松开删除
                if (mOnTextViewChangeListener != null) {//如果被修改过
                    if (0 == mSelectionStartBeforeDelete && mIndex != 0) {//之前有修改且光标位置不为0(文档不为空)
                        //利用上文OnTextViewChangeListener对KEYCODE_DEL按键情况的删除函数进行删除
                        mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                        return true;
                    }
                } else {
                    //其他情况报错，改动监听器并没有建立
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;
            case KeyEvent.KEYCODE_ENTER://松开进入
                if (mOnTextViewChangeListener != null) {
                    int selectionStart = getSelectionStart();//当前位置
                    String text = getText().subSequence(selectionStart, length()).toString();//获取当前文本
                    setText(getText().subSequence(0, selectionStart));//根据获取的文本设置当前文本
                    mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                } else {
                    //同上,其他情况报错，改动监听器并没有建立
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);//同上,父类继续处理
    }

    /**
     * @author:  gmy
     * @methodsName: onFocusChanged
     * @description: 当焦点发生变化时，会自动调用该方法来处理焦点改变的事件
     * @param: focused表示触发该事件的View是否获得了焦点，获得焦点时，focused等于true，否则等于false。
     *            direction表示焦点移动的方向，用数值表示
     *            Rect：表示在触发事件的View的坐标系中，前一个获得焦点的矩形区域，即表示焦点是从哪里来的。如果不可用则为null
     * @return: void
     * @throws:
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (mOnTextViewChangeListener != null) {////若监听器不为空(已经建立)
            if (!focused && TextUtils.isEmpty(getText())) {//获取到焦点并且文本不为空
                mOnTextViewChangeListener.onTextChange(mIndex, false);//mOnTextViewChangeListener子函数，置false隐藏事件选项
            } else {
                mOnTextViewChangeListener.onTextChange(mIndex, true);//mOnTextViewChangeListener子函数，置true显示事件选项
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);//链式调用,父类继续处理
    }

    /**
     * @author:  gmy
     * @methodsName: onKeyUp
     * @description: 生成上下文菜单
     * @param:  ContextMenu menu
     * @return: void
     * @throws:
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (getText() instanceof Spanned) {//有文本
            int selStart = getSelectionStart();//记录开始位置和结束位置
            int selEnd = getSelectionEnd();

            int min = Math.min(selStart, selEnd);//获取开始到结束的最小值
            int max = Math.max(selStart, selEnd);

            final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);//设置url的范围值
            if (urls.length == 1) {
                int defaultResId = 0;
                for(String schema: sSchemaActionResMap.keySet()) {//遍历计划表中所有的key
                    if(urls[0].getURL().indexOf(schema) >= 0) {//若url可以添加则在添加后将defaultResId置为key所映射的值
                        defaultResId = sSchemaActionResMap.get(schema);
                        break;
                    }
                }

                if (defaultResId == 0) {//defaultResId == 0则说明url并没有添加任何东西，所以置为连接其他SchemaActionResMap的值
                    defaultResId = R.string.note_link_other;
                }

                menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener( //建立菜单
                        new OnMenuItemClickListener() {//new一个按键监听器
                            public boolean onMenuItemClick(MenuItem item) {
                                // goto a new intent
                                urls[0].onClick(NoteEditText.this); //根据相应的文本设置菜单的按键
                                return true;
                            }
                        });
            }
        }
        super.onCreateContextMenu(menu);//链式调用,父类继续处理
    }
}
