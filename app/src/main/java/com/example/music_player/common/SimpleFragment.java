package com.example.music_player.common;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.R;
import com.example.music_player.common.adapter.BaseAdapter;
import com.example.music_player.common.adapter.SingleTypeAdapter;
import com.example.music_player.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;


public class SimpleFragment extends Fragment {
    private static final String ARG_TAB_NAME = "tab_name";
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private Context mContext;
    private String mTabName;
    private ArrayList<String> mTabNames = new ArrayList<>();


    public SimpleFragment() {
        // Required empty public constructor
    }

    public static SimpleFragment newInstance(String tabName) {
        SimpleFragment fragment = new SimpleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TAB_NAME, tabName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTabName = getArguments().getString(ARG_TAB_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_simple, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (int i = 0; i < 20; i++) {
            mTabNames.add(mTabName);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        SimpleAdapter adapter = new SimpleAdapter(mTabNames, R.layout.item_music_sheet);

        mRecyclerView.setAdapter(adapter);
        adapter.setHandleClickListener(new BaseAdapter.HandleClickListener() {
            @Override
            public void handleClick(BaseAdapter.ViewHolder holder) {
                final TextView mItem = holder.getItemView().findViewById(R.id.item_music_sheet_music_name);
//                final TextView mItem = ButterKnife.findById(holder.getItemView(), R.id.item);
                mItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtils.show(mItem.getText().toString());
                    }
                });
            }
        });
    }

    static class SimpleAdapter extends SingleTypeAdapter<String> {
        public SimpleAdapter(List<String> list, int layoutResId) {
            super(list, layoutResId);
        }

        @Override
        public void bindView(ViewHolder holder, int position, View itemView) {
            TextView mItem = itemView.findViewById(R.id.item_music_sheet_music_name);
//            TextView mItem = ButterKnife.findById(itemView, R.id.item);
            mItem.setText(getDataList().get(holder.getAdapterPosition()) + "-" + holder.getAdapterPosition());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        ButterKnife.unbind(this);
    }
}
