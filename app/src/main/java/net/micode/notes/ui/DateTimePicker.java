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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import net.micode.notes.R;

/* @author: gmy
* @className:     DateTimePicker
* @packageName: ui
* @description: 用来定义时间选择器
* @date: 2023年3月21日11:27:58
**/
public class DateTimePicker extends FrameLayout {

    //定义一些常量，如一天24小时，一小时60分钟等等
    private static final boolean DEFAULT_ENABLE_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOURS_IN_ALL_DAY = 24;
    private static final int DAYS_IN_ALL_WEEK = 7;
    private static final int DATE_SPINNER_MIN_VAL = 0;
    private static final int DATE_SPINNER_MAX_VAL = DAYS_IN_ALL_WEEK - 1;
    private static final int HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW = 0;
    private static final int HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW = 23;
    private static final int HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW = 1;
    private static final int HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW = 12;
    private static final int MINUT_SPINNER_MIN_VAL = 0;
    private static final int MINUT_SPINNER_MAX_VAL = 59;
    private static final int AMPM_SPINNER_MIN_VAL = 0;
    private static final int AMPM_SPINNER_MAX_VAL = 1;
    // 初始化控件
    // NumberPicker是数字选择器
    // 这里定义的四个变量全部是在设置闹钟时需要选择的变量（如日期、时、分、上午或者下午）
    
    private final NumberPicker mDateSpinner;
    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mAmPmSpinner;
    private Calendar mDate;
    // 定义了Calendar类型的变量mDate，用于操作时间
    private String[] mDateDisplayValues = new String[DAYS_IN_ALL_WEEK];

    private boolean mIsAm;

    private boolean mIs24HourView;

    private boolean mIsEnabled = DEFAULT_ENABLE_STATE;

    private boolean mInitialising;

    private OnDateTimeChangedListener mOnDateTimeChangedListener;
    // 这段代码的作用是将mOnDateChangedListener注册到NumberPicker控件中，以便监听用户对该控件进行数值更改时所需执行的操作。
    private NumberPicker.OnValueChangeListener mOnDateChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mDate.add(Calendar.DAY_OF_YEAR, newVal - oldVal);
            updateDateControl();
            onDateTimeChanged();
        }
    };
    
    // 这段代码的作用是将mOnHourChangedListener注册到NumberPicker控件中，以便监听用户对该控件进行小时数更改时所需执行的操作。
    //这段代码定义了一个名为mOnHourChangedListener的私有成员变量，它是一个NumberPicker . OnValueChangeListener接口的实例对象。该接口用于监听NumberPicker控件值的变化，并在值发生变化时触发相应的操作。
    // 具体来说，当用户改变NumberPicker控件上的小时数时，onValueChange()方法将被调用。该方法接受三个参数：picker表示当前被改变数值的NumberPicker对象；oldVal表示改变前的数值；newVal表示改变后的数值。
    // 在onValueChange()方法内部，首先通过判断mIs24HourView和mIsAm属性的值，来确定当前时间是否需要加上或减去一天。如果需要，则使用Calendar类中的add()方法将日期加上或减去一天，并更新isDateChanged标志。然后根据当前时间是否为24小时制和上午/下午状态，计算出新的小时数并设置到mDate对象中。
    // 接着调用onDateTimeChanged()方法通知监听器日期时间已经发生改变。如果isDateChanged标志为true，则调用setCurrentYear()、setCurrentMonth()和setCurrentDay()方法更新日期控件。
    private NumberPicker.OnValueChangeListener mOnHourChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            boolean isDateChanged = false;
            Calendar cal = Calendar.getInstance();
            if (!mIs24HourView) {
                if (!mIsAm && oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    isDateChanged = true;
                } else if (mIsAm && oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    isDateChanged = true;
                }
                if (oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY ||
                        oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                }
            } else {
                if (oldVal == HOURS_IN_ALL_DAY - 1 && newVal == 0) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    isDateChanged = true;
                } else if (oldVal == 0 && newVal == HOURS_IN_ALL_DAY - 1) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    isDateChanged = true;
                }
            }
            int newHour = mHourSpinner.getValue() % HOURS_IN_HALF_DAY + (mIsAm ? 0 : HOURS_IN_HALF_DAY);
            mDate.set(Calendar.HOUR_OF_DAY, newHour);
            onDateTimeChanged();
            if (isDateChanged) {
                setCurrentYear(cal.get(Calendar.YEAR));
                setCurrentMonth(cal.get(Calendar.MONTH));
                setCurrentDay(cal.get(Calendar.DAY_OF_MONTH));
            }
        }
    };
    /*
     这段代码定义了一个名为mOnMinuteChangedListener的私有成员变量，它是一个NumberPicker . OnValueChangeListener接口的实例对象。该接口用于监听NumberPicker控件值的变化，并在值发生变化时触发相应的操作。

    具体来说，当用户改变NumberPicker控件上的分钟数时，onValueChange()方法将被调用。该方法接受三个参数：picker表示当前被改变数值的NumberPicker对象；oldVal表示改变前的数值；newVal表示改变后的数值。

    在onValueChange()方法内部，首先获取当前分钟数允许的最小和最大值。然后根据oldVal和newVal计算出新旧分钟数之间的偏移量offset。

    接着判断offset是否不等于0。如果不等于0，则调用Calendar类中的add()方法将日期加上或减去一小时，并通过getCurrentHourOfDay()方法获取当前小时数。根据当前小时数是否超过12来判断当前是否为下午，并更新mIsAm属性和amPmSpinner控件。

    最后将新的分钟数设置到mDate对象中，并调用onDateTimeChanged()方法通知监听器日期时间已经发生改变。

    这段代码的作用是将mOnMinuteChangedListener注册到NumberPicker控件中，以便监听用户对该控件进行分钟数更改时所需执行的操作。
  
*/
    private NumberPicker.OnValueChangeListener mOnMinuteChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            int minValue = mMinuteSpinner.getMinValue();
            int maxValue = mMinuteSpinner.getMaxValue();
            int offset = 0;
            if (oldVal == maxValue && newVal == minValue) {
                offset += 1;
            } else if (oldVal == minValue && newVal == maxValue) {
                offset -= 1;
            }
            if (offset != 0) {
                mDate.add(Calendar.HOUR_OF_DAY, offset);
                mHourSpinner.setValue(getCurrentHour());
                updateDateControl();
                int newHour = getCurrentHourOfDay();
                if (newHour >= HOURS_IN_HALF_DAY) {
                    mIsAm = false;
                    updateAmPmControl();
                } else {
                    mIsAm = true;
                    updateAmPmControl();
                }
            }
            mDate.set(Calendar.MINUTE, newVal);
            onDateTimeChanged();
        }
    };
/*
    这段代码定义了一个名为mOnAmPmChangedListener的私有成员变量，它是一个NumberPicker.OnValueChangeListener接口的实例对象。该接口用于监听NumberPicker控件值的变化，并在值发生改变时触发相应的操作。

    具体来说，当用户改变amPmSpinner控件上的上午/下午状态时，onValueChange()方法将被调用。该方法接受三个参数：picker表示当前被改变数值的NumberPicker对象；oldVal表示改变前的数值；newVal表示改变后的数值。

在onValueChange()方法内部，首先通过将mIsAm属性取反来更新当前时间是上午还是下午。然后根据当前时间是否为下午，使用Calendar类中的add()方法将日期加上或减去12小时。

最后更新amPmSpinner控件，并调用onDateTimeChanged()方法通知监听器日期时间已经发生改变。

这段代码的作用是将mOnAmPmChangedListener注册到amPmSpinner控件中，以便监听用户对该控件进行上午/下午状态更改时所需执行的操作。
  
*/
    private NumberPicker.OnValueChangeListener mOnAmPmChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mIsAm = !mIsAm;
            if (mIsAm) {
                mDate.add(Calendar.HOUR_OF_DAY, -HOURS_IN_HALF_DAY);
            } else {
                mDate.ad Calendar.HOUR_OF_DAY, HOURS_IN_HALF_DAY);
            } 
            updateAmPmControl();
            onDateTimeChanged();
        }
    };

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month,
                int dayOfMonth, int hourOfDay, int minute);
    } 
        /**
         * @author: gmy
         * @methodsName: DateTimePicker
         * @description: 根据content信息初始化
         * @param: Context context
         * @return: 构造函数 无返回值
         * @throws:
         */
    public DateTimePicker(Context context) {
        this(context, System.currentTimeMillis());
    }
        /**
         * @author: gmy
         * @methodsName: DateTimePicker
         * @description: 作用和上面的差不多，但是参数不同，重载了方法，根据content信息，long date初始化
         * @param: Context context，long date
         * @return: 构造函数 无返回值
         * @throws:
         */
    public DateTimePicker(Context context, long date) {
        this(context, date, DateFormat.is24HourFormat(context));
    }
        /**
         * @author: gmy
         * @methodsName: DateTimePicker
         * @description: 作用和上面的差不多，但是参数不同，重载了方法，根据不同参数规范化之后，再进行初始化
         * @param: Context context, long date, boolean is24HourView
         * @return: 构造函数 无返回值
         * @throws:
         */
    public DateTimePicker(Context context, long date, boolean is24HourView) {
        super(context);
        mDate = Calendar.getInstance();
        mInitialising = true;
        mIsAm = getCurre HourOfDay() >= HOURS_IN_HALF_DAY;
        inflate(context, .layout.datetime_picker, this);
 
        mDateSpinner = (NumberPicker) findViewById(R.id.date);
        mDateSpinner.setMinValue(DATE_SPINNER_MIN_VAL);
        mDateSpinner.setMaxValue(DATE_SPINNER_MAX_VAL);
        mDateSpinner.setOnValueChangedListener(mOnDateChangedListener);

        mHourSpinner = (NumberPicker) findViewById(R.id.hour);
        mHourSpinner.setOnValueChangedListener(mOnHourChangedListener);
        mMinuteSpinner =  (NumberPicker) findViewById(R.id.minute);
        mMinuteSpinner.setMinValue(MINUT_SPINNER_MIN_VAL);
        mMinuteSpinner.setMaxValue(MINUT_SPINNER_MAX_VAL);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener);

        String[] stringsForAmPm = new DateFormatSymbols().getAmPmStrings();
        mAmPmSpinner = (NumberPicker) findViewById(R.id.amPm);
        mAmPmSpinner.setMinValue(AMPM_SPINNER_MIN_VAL);
        mAmPmSpinner.setMaxValue(AMPM_SPINNER_MAX_VAL);
        mAmPmSpinner.setDisplayedValues(stringsForAmPm);
        mAmPmSpinner.setOnValueChangedListener(mOnAmPmChangedListener);

        // update controls to initial state
        updateDateControl();
        updateHourControl();
        updateAmPmControl();

        set24HourView(is24HourView);

        // set to current time
        setCurrentDate(date);

        setEnabled(isEnabled());
       
        // set the       ontent descriptions
        mInitialising = false;
    }  
     
    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mDateSpinner.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
        mHourSpinn       r.setEnabled(enabled);
        mAmPmSpinne      .setEnabled(enabled);
        mIsEnabled = enabled;
    }  
     
    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    /**       
     * Get the curr      nt date in millis
     *
     * @return the curr  nt date in millis
     */     
    public long getCurrentDateInTimeMillis() {
        return mDate.getTimeInMillis();
    }

    /**       
     * Set the curr      nt date
     *
     * @param date The   urrent date in millis
     */     
    public void se       CurrentDate(long date) {
        Calendar c              = Calendar.getInstance();
        cal.setTim             nMillis(date);
        setCurrentD      te(  al.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.      et(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }       
     
    /**
     * Set the current date
     *
     * @param year The current year
     * @param month The current month
     * @param dayOfMonth The current dayOfMonth
     * @param hourOfDay The current hourOfDay
     * @param minute The current minute
     */
    public void setCurrentDate(int year, int month,
            int dayOfMonth, int hourOfDay, int minute) {
        setCurrentYear(year);
        setCurrentMonth(month);
        setCurrentDay(dayOfMonth);
        setCurrentHour(hourOfDay);
        setCurrentMinute(minute);
    }

    /**
     * Get current year
     *
     * @return The current year
     */
    public int getCurrentYear() {
        return mDate.get(Calendar.YEAR);
    }

    /**
     * Set current year
     *
     * @param year The current year
     */
    public void setCurrentYear(int year) {
        if (!mInitialising && year == getCurrentYear()) {
            return;
        }
        mDate.set(Calendar.YEAR, year);
        updateDateControl();
        onDateTimeChanged();
    }

    /**
     * 
     * Get current month in the year
     *
     * @return The current month in the year
     */
    public int getCurrentMonth() {
        return mDate.get(Calendar.MONTH);
    }
 
    /**
     * Set current month in the year
     *
     * @param month The month in the year
     */
    pu 
     * lic void setCurrentMonth(int month) {
        if (!mInitialising && month == getCurrentMonth()) {
            return;
        }
        mDate.set(Calendar.MONTH, month);
        updateDateControl();
        onDateTimeChanged();
    } 

    /**
     * 
     * Get current day of the month
     *
     * @return The day of the month
     */
    public int getCurrentDay() {
        return mDate.get(Calendar.DAY_OF_MONTH);
    }
 
    /**
     * 
     * Set current day of the month
     *
     * @param dayOfMonth The day of the month
     */
    public void setCurrentDay(int dayOfMonth) {
       
     * 
     *  
     *  if (!mInitialising && dayOfMonth == getCurrentDay()) {
            return;
        }
        mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateControl();
        onDateTimeChanged();
    }
   
    /**
     * Get current hour in 24 hour mode, in the range (0~23)
     * @return The current hour in 24 hour mode
     */
    public int getCurrentHourOfDay() {
        return mDate.get(Calendar.HOUR_OF_DAY);
    }

    private int getCurrentHour() {
        if (mIs24HourView){
            return getCurrentHourOfDay();
        } else {
            int hour = getCurrentHourOfDay();
            if (hour > HOURS_IN_HALF_DAY) {
                return hour - HOURS_IN_HALF_DAY;
            } else {
                return hour == 0 ? HOURS_IN_HALF_DAY : hour;
            }
        }
    }

    /**
     * Set current hour in 24 hour mode, in the range (0~23)
     *
     * @param hourOfDay
     */
    public void setCurrentHour(int hourOfDay) {
        if (!mInitialising && hourOfDay == getCurrentHourOfDay()) {
            return;
        }
        mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        if (!mIs24HourView) {
            if (hourOfDay >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (hourOfDay >HOURS_IN_HALF_DAY) {
                    hourOfDay -= HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (hourOfDay == 0) {
                    hourOfDay = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourSpinner.setValue(hurOfDay);
        onDateTimeChanged();
    }

    /**
     * Get currentMinute
     *
     * @return The Current Minute
     */
    public int getCurrentMinute() {
        return mDate.get(Calendr.MINUTE);
    }

    /**
     * Set current minute
     */
    public void setCurrentMinute(int minute) {
        if (!mInitialising && minute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setValue(minute);
        mDate.set(Calendar.MINUTE, minute);
        onDateTimeChanged();
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView () {
        return mIs24HourView;
    }

    /* 
     * 
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True for 24 hour mode. False for AM/PM mode.
     */
    public void set24HourView(boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        mAmPmSpinner.setVisibility(is24HourView ? View.GONE : View.VISIBLE);
        int hour = getCurrentHourOfDay();
        updateHourControl();
        setCurrentHour(hour);
       
     *  updateAmPmControl();
    }

    private void updateDateControl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_ALL_WEEK / 2 - 1);
        mDateSpinner.setDisplayedValues(null);
        for (int i = 0; i < DAYS_IN_ALL_WEEK; ++i) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            mDateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
       
     *  }
        mDateSpinner.setDisplayedValues(mDateDisplayValues);
        mDateSpinner.setValue(DAYS_IN_ALL_WEEK / 2);
        mDateSpinner.invalidate();
    }

    private void updateAmPmControl() {
        if (mIs24HourView) {
            mAmPmSpinner.setVisibility(View.GONE);
        } else {
       
     *      int index = mIsAm ? Calendar.AM : Calendar.PM;
            mAmPmSpinner.setValue(index);
            mAmPmSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void updateHourControl() {
        if (mIs24HourView) {
       
     * 
     * 
     *      mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW);
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW);
        } else {
            mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW);
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW);
        }
    }

    /**
     * Set the callback that indicates the 'Set' button has been pressed.
     * @param callback the callback, if null will do nothing
     */
    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener.onDateTimeChanged(this, getCurrentYear(),
                    getCurrentMonth(), getCurrentDay(), getCurrentHourOfDay(), getCurrentMinute());
        }
    }
}
