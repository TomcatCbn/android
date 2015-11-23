package cbn.cn.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by boning on 15/11/21.
 */
public class ViewHolder {
    private SparseArray<View> mViews = null;
    private int mPosition;
    private View mConvertView;

    public ViewHolder(Context context, ViewGroup parent, int layoutID, int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<>();
        mConvertView = LayoutInflater.from(context).inflate(layoutID, null);

        mConvertView.setTag(this);

    }

    public static ViewHolder get(Context context, View convertView, ViewGroup parent, int position, int layoutID) {
        if (convertView == null) {
            return new ViewHolder(context, parent, layoutID, position);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        mViews.put(viewId, view);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
        }
        return (T) view;
    }

    public View getConvertView() {
        return mConvertView;
    }
}
