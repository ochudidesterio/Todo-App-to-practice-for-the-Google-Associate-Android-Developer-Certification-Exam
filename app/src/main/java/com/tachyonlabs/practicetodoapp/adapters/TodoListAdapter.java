package com.tachyonlabs.practicetodoapp.adapters;

import com.tachyonlabs.practicetodoapp.R;
import com.tachyonlabs.practicetodoapp.data.TodoListContract;
import com.tachyonlabs.practicetodoapp.models.TodoTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoListAdapterViewHolder> {
    private final static String TAG = TodoListAdapter.class.getSimpleName();
    private final TodoListAdapterOnClickHandler mClickHandler;
    private Context mContext;
    private Drawable[] priorityStars;
    private Drawable completedStar;
    private Cursor mCursor;
    private int mDescriptionIndex;
    private int mPriorityIndex;
    private int mDueDateIndex;
    private int m_IDIndex;
    private int mCompletedIndex;
    private ColorStateList completedCheckboxColors;
    private ColorStateList unCompletedCheckboxColors;

    public TodoListAdapter(Context context, TodoListAdapterOnClickHandler todoListAdapterOnClickHandler) {
        mClickHandler = todoListAdapterOnClickHandler;
        mContext = context;
        Resources res = context.getResources();
        // not really the place for this, but I had too much trouble trying to read them from @arrays
        priorityStars = new Drawable[]{res.getDrawable(R.drawable.ic_star_red_24dp), res.getDrawable(R.drawable.ic_star_orange_24dp), res.getDrawable(R.drawable.ic_star_yellow_24dp)};
        completedStar = res.getDrawable(R.drawable.ic_star_grey_24dp);

        completedCheckboxColors = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked},
                },
                new int[]{
                        Color.DKGRAY,
                        mContext.getResources().getColor(R.color.colorCompleted),
                });

        unCompletedCheckboxColors = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked},
                },
                new int[]{
                        Color.DKGRAY,
                        mContext.getResources().getColor(R.color.colorAccent),
                });
    }

    @Override
    public TodoListAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_todo_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        TodoListAdapterViewHolder viewHolder = new TodoListAdapterViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull TodoListAdapter.TodoListAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.cbTodoDescription.setText(mCursor.getString(mDescriptionIndex));
//        // ditto for not propagating a red/overdue date
//        holder.tvTodoDueDate.setTextColor(holder.tvTodoPriority.getCurrentTextColor());

        String dueDateString;
        long dueDate = mCursor.getLong(mDueDateIndex);
        Log.d(TAG, mCursor.getString(mDescriptionIndex) + " " + dueDate);
        if (dueDate == TodoTask.NO_DUE_DATE) {
            dueDateString = mContext.getString(R.string.no_due_date);
        } else {
            dueDateString = TodoTask.formatDueDate(mContext, dueDate);
        }

        int priority = mCursor.getInt(mPriorityIndex);
        holder.tvTodoDueDate.setText(dueDateString);
        holder.tvTodoPriority.setText(mContext.getResources().getStringArray(R.array.priorities)[priority]);
        int isCompleted = mCursor.getInt(mCompletedIndex);
        holder.cbTodoDescription.setChecked(isCompleted == TodoTask.TASK_COMPLETED);

        if (isCompleted == TodoTask.TASK_COMPLETED) {
            // if the task is completed, we want everything grey
            holder.clTodoListItem.setBackground(mContext.getResources().getDrawable(R.drawable.list_item_completed_touch_selector));
            holder.cbTodoDescription.setTextColor(mContext.getResources().getColor(R.color.colorCompleted));
            holder.cbTodoDescription.setSupportButtonTintList(completedCheckboxColors);
            holder.tvTodoPriority.setText(mContext.getResources().getString(R.string.completed));
            holder.ivTodoPriorityStar.setBackground(completedStar);
        } else {
            holder.clTodoListItem.setBackground(mContext.getResources().getDrawable(R.drawable.list_item_touch_selector));
            holder.cbTodoDescription.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            holder.cbTodoDescription.setSupportButtonTintList(unCompletedCheckboxColors);
            holder.tvTodoPriority.setText(mContext.getResources().getStringArray(R.array.priorities)[priority]);
            holder.ivTodoPriorityStar.setBackground(priorityStars[priority]);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (mCursor != null) {
            mDescriptionIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_DESCRIPTION);
            mPriorityIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_PRIORITY);
            mDueDateIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_DUE_DATE);
            m_IDIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_ID);
            mCompletedIndex = mCursor.getColumnIndex(TodoListContract.TodoListEntry.COLUMN_COMPLETED);
        }
        notifyDataSetChanged();
    }

    public interface TodoListAdapterOnClickHandler {
        void onClick(TodoTask todoTask, View view);
    }

    public class TodoListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final AppCompatCheckBox cbTodoDescription;
        final TextView tvTodoDueDate;
        final TextView tvTodoPriority;
        final ImageView ivTodoPriorityStar;
        final ConstraintLayout clTodoListItem;

        public TodoListAdapterViewHolder(View itemView) {
            super(itemView);
            cbTodoDescription = itemView.findViewById(R.id.cb_todo_description);
            tvTodoDueDate = itemView.findViewById(R.id.tv_todo_due_date);
            tvTodoPriority = itemView.findViewById(R.id.tv_todo_priority);
            ivTodoPriorityStar = itemView.findViewById(R.id.iv_todo_priority_star);
            clTodoListItem = (ConstraintLayout) itemView;
            itemView.setOnClickListener(this);
            cbTodoDescription.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mCursor.moveToPosition(getAdapterPosition());
            TodoTask todoTask = new TodoTask(mCursor.getString(mDescriptionIndex),
                    mCursor.getInt(mPriorityIndex),
                    mCursor.getLong(mDueDateIndex),
                    mCursor.getInt(m_IDIndex),
                    mCursor.getInt(mCompletedIndex));
            mClickHandler.onClick(todoTask, view);
        }
    }
}
