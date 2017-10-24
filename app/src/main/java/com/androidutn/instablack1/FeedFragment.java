package com.androidutn.instablack1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidutn.instablack1.model.Post;
import com.androidutn.instablack1.viewholders.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedFragment extends Fragment {

    @BindView(R.id.feed_list) RecyclerView mList;
    private FeedFragmentListener mListener;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            Query query = FirebaseDatabase.getInstance().getReference("Feed").child(uid).orderByChild("fechaRev");
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
            FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                    .setIndexedQuery(query, postsRef, Post.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

                @Override
                public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                    return new PostViewHolder(itemView);
                }

                @Override
                protected void onBindViewHolder(PostViewHolder holder, int position, Post model) {
                    holder.setModel(model);
                }
            };

            mList.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof FeedFragmentListener) {
//            mListener = (FeedFragmentListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement FeedFragmentListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FeedFragmentListener {
    }
}
