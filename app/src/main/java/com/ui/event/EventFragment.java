package com.ui.event;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.LoginActivity;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ui.placeholder.EventData;
import com.ui.placeholder.PlaceholderContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class EventFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    public static MyEventRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EventFragment newInstance(int columnCount) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference eventsRef = db.collection("events");

            List<EventData> ITEMS = new ArrayList<>();
            eventsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    int id = 1;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("----tag----", document.getId());
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String imageUrl = document.getString("image");
                        if (LoginActivity.isGuestMode == true)
                            ITEMS.add(new EventData(Integer.toString(id), title, description, imageUrl, document.getId(), "", false));
                        else {
                            ITEMS.add(new EventData(Integer.toString(id), title, description, imageUrl, document.getId(), mAuth.getCurrentUser().getUid(), false));
                        }
                        id = id + 1;
                    }
                    if (LoginActivity.isGuestMode == false){
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("myevents").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    ArrayList<String> eventlist = (ArrayList<String>) documentSnapshot.get("eventlist");

                                    for(String event : eventlist){
                                        Log.d("firebase----",event );
                                        for(EventData Item : ITEMS) {
                                            Log.d("firebase----",Item.getDocId() );
                                            if (event.equals(Item.getDocId())) {
                                                Item.setShare(true);
                                                Log.d("firebase----",Item.getName() );
                                            }
                                        }
                                    }
                                }
                                adapter = new MyEventRecyclerViewAdapter(ITEMS);
                                recyclerView.setAdapter(adapter);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("firebase", "Error getting document", e);
                            }
                        });
                    }
                    else {
                        adapter = new MyEventRecyclerViewAdapter(ITEMS);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@androidx.annotation.NonNull Exception e) {

                }
            });

        }
        return view;
    }
}