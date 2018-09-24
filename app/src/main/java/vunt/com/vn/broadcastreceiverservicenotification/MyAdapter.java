package vunt.com.vn.broadcastreceiverservicenotification;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private OnClickItemSongListener mListener;

    public MyAdapter(OnClickItemSongListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(mListener, position);
    }

    @Override
    public int getItemCount() {
        return MainActivity.SONG_NAMES.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_song_name);
        }

        public void bindData(final OnClickItemSongListener listener, final int position) {
            mTextView.setText(MainActivity.SONG_NAMES[position]);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.clickItemSongListener(position);
                }
            });
        }
    }

    interface OnClickItemSongListener {
        void clickItemSongListener(int position);
    }
}
