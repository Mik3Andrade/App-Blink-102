package net.handsmidia.blink102;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FragmentBlink extends Fragment implements AdapterBlink.Callback {

    RecyclerView recyclerView;
    List<String> listaImg;
    AdapterBlink adapterBlink;
    private DatabaseReference mDatabase;

    public FragmentBlink() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_blink, container, false);

        recyclerView = view.findViewById(R.id.recyclerImg);

        listaImg = new ArrayList<>();
        adapterBlink = new AdapterBlink(listaImg, view.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapterBlink);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String url = (String) postSnapshot.getValue();
                    listaImg.add(url);
                }

                adapterBlink.update(listaImg);

                automaticScroll();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapterBlink.setCall(this);

        return view;


    }

    private void automaticScroll() {

        if(!listaImg.isEmpty()) {

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(60000, 200 ) {
                        public void onTick(long millis) {
                            recyclerView.scrollBy(0, listaImg.size());
                        }

                        public void onFinish() {

                        }
                    }.start();
                }
            });
        }

    }

    @Override
    public void onItemChecked(String imageClicable) {
        String url = "http://www.blink102.com.br";

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        startActivity(intent);
    }
}
