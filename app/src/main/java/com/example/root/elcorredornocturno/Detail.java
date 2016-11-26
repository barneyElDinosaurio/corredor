package com.example.root.elcorredornocturno;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class Detail extends AppCompatActivity {

    String choosedDetail,choosed, img , title,descripcion;
    FirebaseDatabase database;//Instance
    DatabaseReference myRef;//Parent Reference
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FLOATING BTN
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        //GET DATA FROM MY MAIN CLASS

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            System.out.println(choosed = extras.getString("choosedCategory"));
            System.out.println(choosedDetail = extras.getString("choosedDetail"));

            //FIREBASE

        }else Toast.makeText(getApplicationContext(),"Informacion no cargada desde la DB",Toast.LENGTH_SHORT).show();

        database = FirebaseDatabase.getInstance();//Instance
        myRef = database.getReference(choosed+"/"+choosedDetail);//Reference


        //READING INFO FROM MY DB

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //System.out.println(dataSnapshot.getValue());
                //for (DataSnapshot postSnapshot : dataSnapshot.getChildren() ){
                //System.out.println(rePostSnapshot.getKey() +" : "+rePostSnapshot.child("lat").getValue());


                //Toast.makeText(getApplicationContext(),dataSnapshot.child("descripcion").getValue().toString(),Toast.LENGTH_SHORT).show();
                //titulo = postSnapshot.getKey().toString();
                img = dataSnapshot.child("img").getValue().toString();


                //Image Detail

                ImageView imgV = (ImageView) findViewById(R.id.imgDetail);
                Picasso.with(getApplicationContext()).load(img).into(imgV);

                //Text Detail
                TextView textV = (TextView) findViewById(R.id.textDetail);
                textV.setText(dataSnapshot.child("descripcion").getValue().toString());
                //   }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();




    }
}
