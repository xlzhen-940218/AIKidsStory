package com.xlzhen.aikidsstory.adapter.base;

import android.content.Context;
import android.util.SparseArray; // 推荐使用 SparseArray 替代 HashMap 缓存 View
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

    protected final Context context; // 标记为 final
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
        // 建议对传入的 Context 进行空检查，虽然通常不会为空
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.dataList = dataList;
        this.layoutResId = layoutResId;
        this.layoutInflater = LayoutInflater.from(context);
    }

    // --- BaseAdapter 核心方法实现 ---

    @Override
    public int getCount() {
        // 避免 dataList 为 null 时抛出 NullPointerException
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList == null || position < 0 || position >= dataList.size() ? null : dataList.get(position);
    }

    // getItemId 保持不变，通常直接返回 position

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. 获取或创建 ViewHolder
        CommonViewHolder viewHolder;
        if (convertView == null) {
            // 首次创建 View
            convertView = layoutInflater.inflate(layoutResId, parent, false);
            // 使用我们重命名的 CommonViewHolder
            viewHolder = new CommonViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            // 重用 View
            viewHolder = (CommonViewHolder) convertView.getTag();
        }

        // 2. 获取当前位置的数据项
        T item = getItem(position);

        // 3. 抽象方法：将数据绑定到 View 上
        // 传入 position 更符合规范
        if (item != null) {
            bindView(viewHolder, item, position);
        }

        return convertView;
    }

    /**
     * 抽象方法：供子类实现，用于将数据绑定到视图上。
     * @param viewHolder 视图持有者，用于查找子视图
     * @param item 当前位置的数据项
     * @param position 当前位置
     */
    protected abstract void bindView(CommonViewHolder viewHolder, T item, int position); // 统一使用 CommonViewHolder

    // --- 数据操作方法 ---

    /**
     * 更新适配器的数据，并通知 ListView 刷新。
     * @param newDataList 新的数据列表
     */
    public void updateData(List<T> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }
    
    /**
     * 获取当前数据列表。
     * @return 当前数据列表
     */
    public List<T> getDataList() {
        return dataList;
    }


    // --- 通用 ViewHolder ---

    /**
     * 通用 ViewHolder 类，用于缓存 View，优化性能。
     * 优化：使用 SparseArray 替代 HashMap (在 Android 中对 int 到 Object 的映射更高效)
     */
    public static class CommonViewHolder { // 建议重命名为 CommonViewHolder 或 BaseViewHolder
        private final View convertView;
        // 使用 SparseArray 缓存已查找的子 View
        private final SparseArray<View> views = new SparseArray<>();

        public CommonViewHolder(View convertView) {
            this.convertView = convertView;
        }

        /**
         * 根据 ID 查找并返回子 View。
         * 优化：添加缓存机制，避免重复调用 findViewById()
         *
         * @param viewId 子 View 的资源ID
         * @param <V> 子 View 的具体类型
         * @return 查找到的子 View
         */
        @SuppressWarnings("unchecked")
        public <V extends View> V findViewById(int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = convertView.findViewById(viewId);
                views.put(viewId, view);
            }
            return (V) view;
        }
        
        /**
         * 获取整个 item view。
         */
         public View getConvertView() {
             return convertView;
         }
    }
}