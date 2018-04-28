package com.bilkentazure.evenu.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bilkentazure.evenu.MainActivity;
import com.bilkentazure.evenu.R;
import com.bilkentazure.evenu.models.Event;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Aziz Utku Kağıtcı on 17/04/2018
 * Fragment for Favorite Events
 * @author Aziz Utku Kağıtcı
 * @version 28/04/2018
 */
public class FavFragment extends Fragment {


	//Firebase
	private FirebaseFirestore db;
	private FirebaseAuth mAuth;
	private FirebaseUser mUser;
	private FirestoreRecyclerAdapter mFirestoreRecyclerAdapter;

	private RecyclerView mRecyclerEvent;

	public FavFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {


		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_fav, container, false);

		db = FirebaseFirestore.getInstance();
		mAuth = FirebaseAuth.getInstance();
		mUser = mAuth.getCurrentUser();

		mRecyclerEvent = view.findViewById(R.id.fragment_fav_recycler);

		mRecyclerEvent.setHasFixedSize(true);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(container.getContext());
		mRecyclerEvent.setLayoutManager(layoutManager);
		mRecyclerEvent.setItemAnimator(new DefaultItemAnimator());
		mRecyclerEvent.addItemDecoration(new DividerItemDecoration(container.getContext(), LinearLayoutManager.VERTICAL));

		Query query = db.collection("_events")
				.orderBy("from",Query.Direction.ASCENDING);

		FirestoreRecyclerOptions<Event> options =  new FirestoreRecyclerOptions.Builder<Event>()
				.setQuery(query, Event.class)
				.build();

		mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Event, HomeFragment.EventHolder>(options) {
			@Override
			public void onBindViewHolder(final HomeFragment.EventHolder holder, int position, final Event event) {


				final String id = event.getId();
				String club_id = event.getClub_id();
				String name = event.getName();
				String image = event.getImage();
				String description = event.getDescription();
				String location = event.getLocation();
				Date from = event.getFrom();
				Date to = event.getTo();
				int ge_point = event.getGe_point();
				ArrayList<String> tags = event.getTags();
				ArrayList<String> keywords = event.getKeywords();
				String qr_id = event.getQr_id();
				String spreadsheet = event.getSpreadsheet();
				String security_check = event.getSecurity_check();

				holder.setTitle(name);
				holder.setInfo(description);
				holder.setImage(image);
				holder.setLocation(location);
				holder.setDate(from);


				if(MainActivity.userModel != null){

					ArrayList<String> favoriteEvents = MainActivity.userModel.getFavoriteEvents();
					boolean isPresent = false;

					for(String event_id: favoriteEvents){
						if(event_id.equals(id)){
							isPresent = true;
							holder.btnFav.setImageResource(R.drawable.ic_favorite_selected);
						}
					}

					if(!isPresent){

						holder.hide();
					}

				}


				holder.btnFav.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						if(MainActivity.userModel != null){
							ArrayList<String> favoriteEvents = MainActivity.userModel.getFavoriteEvents();

							for (Iterator<String> iterator = favoriteEvents.iterator(); iterator.hasNext(); ) {
								String value = iterator.next();

								if(value.equals(id)){

									iterator.remove();
									MainActivity.userModel.setFavoriteEvents(favoriteEvents);
									holder.hide();

									db.collection("users").document(mAuth.getCurrentUser().getUid()).update("favoriteEvents", favoriteEvents);

								}

							}

						}

					}
				});

				holder.btnShare.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						holder.btnShare.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy\nH:m");
								String date = dateFormat.format(event.getFrom());
								Intent intent = new Intent(android.content.Intent.ACTION_SEND);
								intent.setType("text/plain");
								intent.putExtra(android.content.Intent.EXTRA_SUBJECT, event.getName());
								intent.putExtra(android.content.Intent.EXTRA_TEXT, "We want to see you among us! Do you want to attend \""
										+ event.getName()
										+ "\" event?\n\nEvent Description: "
										+ event.getDescription()
										+ "\n\nDate: " + date
										+ "\n\nLocation: "
										+ event.getLocation());
								startActivity(Intent.createChooser(intent, "Share event!"));
							}
						});
					}
				});

				//TODO Add btnNotify onClickListener to buttons


			}

			@Override
			public HomeFragment.EventHolder onCreateViewHolder(ViewGroup group, int i) {
				// Create a new instance of the ViewHolder, in this case we are using a custom
				// layout called R.layout.message for each item
				View view = LayoutInflater.from(group.getContext())
						.inflate(R.layout.event_list_item, group, false);

				return new HomeFragment.EventHolder(view);
			}
		};

		mRecyclerEvent.setAdapter(mFirestoreRecyclerAdapter);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		mFirestoreRecyclerAdapter.startListening();
	}

	@Override
	public void onStop() {
		super.onStop();
		mFirestoreRecyclerAdapter.stopListening();
	}

}
