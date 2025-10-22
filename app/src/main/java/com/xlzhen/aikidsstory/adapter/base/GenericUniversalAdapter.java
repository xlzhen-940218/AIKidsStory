package com.xlzhen.aikidsstory.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

/**
 * 泛型通用适配器，适用于所有数据类型 (T) 的 ListView。
 *
 * @param <T> 列表项的数据类型
 */
public abstract class GenericUniversalAdapter<T> extends BaseAdapter {

    protected Context context;
    protected List<T> dataList;
    private final int layoutResId;
    private final LayoutInflater layoutInflater;

    /**
     * 构造函数
     *
     * @param context Context
     * @param dataList 要显示的数据列表
     * @param layoutResId 列表项布局的资源ID (R.layout.xxx)
     */
    public GenericUniversalAdapter(Context context, List<T> dataList, int layoutResId) {
        this.context = context;
        this.dataList = dataList;
        this.layoutResId = layoutResId;
        this.layoutInflater = LayoutInflater.from(context);
    }

    // --- BaseAdapter 核心方法实现 ---

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. 获取或创建 ViewHolder
        ViewHolder viewHolder;
        if (convertView == null) {
            // 首次创建 View
            convertView = layoutInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            // 重用 View
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 2. 获取当前位置的数据项
        T item = getItem(position);

        // 3. 抽象方法：将数据绑定到 View 上
        // 这一步由使用者（继承者）实现，从而实现 View 的定制化
        bindView(viewHolder, item, position);

        return convertView;
    }

    /**
     * 抽象方法：供子类实现，用于将数据绑定到视图上。
     * * @param viewHolder 视图持有者，用于查找子视图
     * @param item 当前位置的数据项
     * @param position 当前位置
     */
    protected abstract void bindView(ViewHolder viewHolder, T item, int position);

    // --- 数据操作方法 ---

    /**
     * 更新适配器的数据，并通知 ListView 刷新。
     * @param newDataList 新的数据列表
     */
    public void updateData(List<T> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    // --- 通用 ViewHolder ---

    /**
     * 通用 ViewHolder 类，用于缓存 View，优化性能。
     * 它通过 View 的 ID 缓存子 View，避免重复调用 findViewById()。
     */
    public static class ViewHolder {
        private final View convertView;

        public ViewHolder(View convertView) {
            this.convertView = convertView;
        }

        /**
         * 根据 ID 查找并返回子 View。
         *
         * @param viewId 子 View 的资源ID
         * @param <V> 子 View 的具体类型
         * @return 查找到的子 View
         */
        public <V extends View> V findViewById(int viewId) {
            return convertView.findViewById(viewId);
        }
    }
}