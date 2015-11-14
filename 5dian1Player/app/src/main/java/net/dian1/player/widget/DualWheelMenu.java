package net.dian1.player.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;

import net.dian1.player.R;
import net.dian1.player.widget.wheel_view.KeyValue;
import net.dian1.player.widget.wheel_view.OnWheelChangedListener;
import net.dian1.player.widget.wheel_view.Reason;
import net.dian1.player.widget.wheel_view.WheelView;
import net.dian1.player.widget.wheel_view.adapters.ArrayWheelAdapter;

import java.util.List;

/**
 * Created by Desmond on 2015/8/25.
 */
public class DualWheelMenu extends Dialog implements OnWheelChangedListener, View.OnClickListener{

    private Context mContext;
    //private View rootView;
    private WheelView mWheelView1;
    private WheelView mWheelView2;
    private KeyValueAdapter wheelAdapter1;
    private KeyValueAdapter wheelAdapter2;
    private OnDualWheelChangedListener changedListener;
    private WheelValue mWheelValue;
    private KeyValue[] wheelList1;
    private KeyValue[] wheelList2;

    public DualWheelMenu(Context context, final List<?> list1, final List<?> list2) {
        super(context, R.style.dialog_fullscreen);
        mContext = context;
        mWheelValue = new WheelValue();
        this.wheelList1 = (list1 == null ? null : getKeyValue(list1));
        this.wheelList2 = (list2 == null ? null : getKeyValue(list2));
    }

    private KeyValue[] getKeyValue(List<?> objects) {
        if(objects != null) {
            KeyValue[] results = new KeyValue[objects.size()];
            for(int i=0; i < results.length; i++) {
                if(objects.get(i) instanceof KeyValue) {
                    results[i] = (KeyValue) objects.get(i);
                } else {
                    results[i] = new Reason("", objects.get(i).toString());
                }
            }
            return results;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDialog();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dual_wheel_menu);
        initView();
    }

    private void initDialog() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        //window.setWindowAnimations(R.style.dual_wheel_anim);  //添加动画
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.verticalMargin = 0;
        lp.horizontalMargin = 0;
        window.setAttributes(lp);
    }

    private void initView() {
        mWheelView1 = (WheelView) findViewById(R.id.wheel_view_1);
        mWheelView1.setCyclic(false);
        mWheelView1.setInterpolator(new AnticipateOvershootInterpolator());
        mWheelView1.addChangingListener(this);
        mWheelView2 = (WheelView) findViewById(R.id.wheel_view_2);
        mWheelView2.setInterpolator(new AnticipateOvershootInterpolator());
        mWheelView2.addChangingListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);

        wheelAdapter1 = new KeyValueAdapter(mContext, wheelList1);
        wheelAdapter1.setItemResource(R.layout.dual_wheel_item_layout);
        mWheelView1.setViewAdapter(wheelAdapter1);
        mWheelView1.setCurrentItem(0, true);

        mWheelView2.setVisibility(isSingleWheel() ? View.GONE : View.VISIBLE);
        if(!isSingleWheel()) {
            wheelAdapter2 = new ColorKeyValueAdapter(mContext, wheelList2);
            wheelAdapter2.setItemResource(R.layout.dual_wheel_item_layout);
            mWheelView2.setViewAdapter(wheelAdapter2);
            mWheelView2.setCurrentItem(0, true);
        }
    }

    /**
     * 设置选中条目
     *
     * @param value
     */
    public void setCurrentValue(String... value) {
        if(value != null) {
            int index1 = wheelAdapter1.indexOf(value[0]);
            if(index1 != -1) {
                mWheelView1.setCurrentItem(index1, true);
            }
            if(value.length == 2) {
                int index2 = wheelAdapter1.indexOf(value[1]);
                if(index2 != -1) {
                    mWheelView2.setCurrentItem(index2, true);
                }
            }
        }
    }

    private boolean isSingleWheel() {
        return wheelList2 == null || wheelList2.length == 0;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if(wheel.equals(mWheelView1)) {
            mWheelValue.firstOldValue = oldValue;
            mWheelValue.firstNewValue = newValue;
        } else {
            mWheelValue.secondOldValue = oldValue;
            mWheelValue.secondNewValue = newValue;
        }
    }

    /**
     * Adds wheel clicking listener
     * @param listener the listener
     */
    public void setOnChangeListener(OnDualWheelChangedListener listener) {
        changedListener = listener;
    }

    private void notifyChangeListener() {
        if(changedListener != null) {
            changedListener.onChanged(mWheelValue.firstOldValue, mWheelValue.firstNewValue,
                    mWheelValue.secondOldValue, mWheelValue.secondNewValue);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                notifyChangeListener();
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }

    private class WheelValue {
        int firstOldValue = 0;
        int firstNewValue = 0;
        int secondOldValue = 0;
        int secondNewValue = 0;
    }

    public class KeyValueAdapter extends ArrayWheelAdapter<KeyValue> {

        /**
         * Constructor
         *
         * @param context the current context
         * @param items   the items
         */
        public KeyValueAdapter(Context context, KeyValue[] items) {
            super(context, items);
        }

        @Override
        public CharSequence getItemText(int index) {
            if (index >= 0 && index < items.length) {
                KeyValue item = items[index];
                return item.getValue();
            }
            return null;
        }

        /**
         * 根据value查找在列表中的index
         *
         * @param value
         * @return
         */
        public int indexOf(String value) {
            for(int i = 0; i < items.length; i++) {
                KeyValue keyValue = items[i];
                if(keyValue != null && keyValue.getValue().equals(value)) {
                    return i;
                }
            }
            return -1;
        }

    }

    public class ColorKeyValueAdapter extends KeyValueAdapter{

        /**
         * Constructor
         *
         * @param context the current context
         * @param items   the items
         */
        public ColorKeyValueAdapter(Context context, KeyValue[] items) {
            super(context, items);
        }

//        @Override
//        public View getItem(int index, View convertView, ViewGroup parent) {
//            View itemView = super.getItem(index, convertView, parent);
//            if(itemView instanceof TextView) {
//                String period = getItemText(index).toString();
//                long curServerTime = BasicDataManager.getInstance().getServerTime();
//                boolean timePassed = !StringUtils.timeBeforePeriod(curServerTime, period);
//                 ((TextView) itemView).setTextColor(timePassed ? mContext.getResources().getColor(R.color.gray) : DEFAULT_TEXT_COLOR);
//            }
//            return itemView;
//        }
    }

    public interface OnDualWheelChangedListener {

        void onChanged(int firstOldValue, int firstNewValue, int secondOldValue, int secondNewValue);

    }
}
