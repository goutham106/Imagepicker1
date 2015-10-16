package com.gm.imagepicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gm.imgpick.ImagePicker;

import java.io.IOException;

public class Main3Activity extends AppCompatActivity {

    Uri selectedImageUri;
    String selectedPath = "";
    Button b, bCam;
    ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        b = (Button) findViewById(R.id.bGallery);
        bCam = (Button) findViewById(R.id.bCamera);
        preview = (ImageView) findViewById(R.id.preview);
        bCam.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 100);
            }
        });


        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openGallery(10);
            }
        });
    }


    public void openGallery(int req_code) {

        Intent intent = new Intent();

        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select file to upload "), req_code);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data.getData() != null) {
                selectedImageUri = data.getData();
            } else {
                Log.d("selectedPath1 : ", "Came here its null !");
                Toast.makeText(getApplicationContext(), "failed to get Image!", Toast.LENGTH_SHORT).show();
            }

            if (requestCode == 100 && resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                preview.setImageBitmap(photo);
                Log.d("selectedPath1 : ", selectedPath);

            }

            if (requestCode == 10)

            {
                // ImagePicker imagePicker = new ImagePicker();

                try {
                    Uri e = ImagePicker.getInstance().storeImage(ImagePicker.getInstance().getThumbnail(selectedImageUri));
                    Log.e("PATH", ImagePicker.getInstance().getPath(e));
                    Log.e("PATH", ImagePicker.getInstance().getPath(selectedImageUri));
                    preview.setImageBitmap(ImagePicker.getInstance(this).getThumbnail(selectedImageUri));
                   // preview.setImageURI(e);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }
    }
        public void clear(View v){

            ImagePicker.getInstance(this).clearCatch();

    }



}
