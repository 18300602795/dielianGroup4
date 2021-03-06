package com.etsdk.app.huov7.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etsdk.app.huov7.R;
import com.etsdk.app.huov7.down.DownloadHelper;
import com.etsdk.app.huov7.down.TasksManager;
import com.etsdk.app.huov7.down.TasksManagerModel;
import com.etsdk.app.huov7.ui.DownloadingManagerActivity;
import com.etsdk.app.huov7.ui.GameDetailV2Activity;
import com.etsdk.app.huov7.view.DownloadingListItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 2017/5/6.
 */

public class DownloadingListAdapterNew extends RecyclerView.Adapter {
    private int TITLE = 0;
    private int LIST = 1;

    private List<TasksManagerModel> modelList;
    private boolean ifShowEdit = false;
    SelectListener selectListener;

    public DownloadingListAdapterNew(List<TasksManagerModel> modelList, SelectListener selectListener) {
        this.modelList = modelList;
        this.selectListener = selectListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TITLE) {
            return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_download, parent, false));
        } else {
            return new ListViewHolder(new DownloadingListItem(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == LIST) {
            final TasksManagerModel tasksManagerModel = modelList.get(position-1);
            ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.setModel(tasksManagerModel);
            ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.showCheck(ifShowEdit);
            ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.setCheck(tasksManagerModel.isSelected);
            noticeSelectChange();

            ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.tvDownStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tasksManagerModel == null) {
                        return;
                    }
                    if(((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.isEdit){//编辑状态，
                        ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.setCheck(!tasksManagerModel.isSelected);
                        tasksManagerModel.isSelected = !tasksManagerModel.isSelected;
                        noticeSelectChange();
                    }else {//未处于编辑状态
                        DownloadHelper.onClick(tasksManagerModel.getGameId(), ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem);
                    }
                }
            });
            ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tasksManagerModel == null) {
                        return;
                    }
                    if(((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.isEdit){//编辑状态，
                        ((DownloadingListAdapterNew.ListViewHolder) holder).downloadItem.setCheck(!tasksManagerModel.isSelected);
                        tasksManagerModel.isSelected = !tasksManagerModel.isSelected;
                        noticeSelectChange();
                    }else {//未处于编辑状态
                        GameDetailV2Activity.start(v.getContext(), tasksManagerModel.getGameId());
                    }
                }
            });
        } else {
            ((TitleViewHolder) holder).tvTitle.setText("正在下载");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TITLE;
        } else {
            return LIST;
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size() + 1;
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        DownloadingListItem downloadItem;

        ListViewHolder(DownloadingListItem view) {
            super(view);
            downloadItem = view;
        }
    }

    private void noticeSelectChange() {
        if(selectListener!=null){
            if(ifNotSelectAll()){
                selectListener.onSelect(false);
            }else {
                selectListener.onSelect(true);
            }
        }
    }

    /**
     * 显示编辑
     */
    public void showEdit(){
        ifShowEdit = true;
        notifyDataSetChanged();
    }

    public void hideEdit(){
        ifShowEdit = false;
        notifyDataSetChanged();
    }

    /**
     * 切换是否编辑
     */
    public void togleShowEdit(){
        if(ifShowEdit){
            hideEdit();
        }else{
            showEdit();
        }
    }

    public boolean isShowEdit(){
        return ifShowEdit;
    }

    public void togleSelectAll(){
        if(ifNotSelectAll()){
            for(TasksManagerModel model: modelList){
                model.isSelected = true;
            }
        }else{
            for(TasksManagerModel model: modelList){
                model.isSelected = false;
            }
        }
        notifyDataSetChanged();
    }

    private boolean ifNotSelectAll(){
        boolean b = false;
        for(TasksManagerModel model: modelList){
            if(model.isSelected == false){
                b = true;
                break;
            }
        }
        return b;
    }

    public void deleteSelected(){
        List<TasksManagerModel> remove = new ArrayList<>();
        for(TasksManagerModel model: modelList){
            if(model.isSelected){
                TasksManager.getImpl().deleteTaskByModel(model);
                remove.add(model);
            }
        }
        modelList.removeAll(remove);
        notifyDataSetChanged();
    }

    public int getSelectedSize(){
        List<TasksManagerModel> remove = new ArrayList<>();
        for(TasksManagerModel model: modelList){
            if(model.isSelected){
                remove.add(model);
            }
        }
        return remove.size();
    }

    public void release(){
        selectListener = null;
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface SelectListener{
        /**
         * 全选的时候能够回调
         * @param isSelectAll
         */
        void onSelect(boolean isSelectAll);
    }

}
