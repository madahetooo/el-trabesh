package com.wether.exchat;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class EditProfile extends Fragment implements View.OnClickListener {
    TextView txEditFullName, txEditPhoneNumber, txEditNewPassword, txEditOldPassword, txAddImage;
    EditText etEditFullName, etEditPhoneNumber, etEditOldPassword, etEditNewPassword;
    Button bUpdate;
    CircleImageView edit_image;
    FirebaseUser mUser;
    StorageReference Xstorage;
    private Uri filePath = null;
    public static final int GALLERY_REQUEST = 1;
    //    public static final int CAMERA_REQUEST = 2;
//    public static final int REQUEST_CROP = 3;
    String mCurrentPhotoPath;
    DatabaseReference Xreference;
    Bitmap bitmap;
    String image_url, imgaePath;
    ProgressDialog progressDialog;
//    File mediaFile, mediaStorageDir;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_edit_profile, container, false);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        txEditFullName = (TextView) mView.findViewById(R.id.txEditFullName);
        txEditPhoneNumber = (TextView) mView.findViewById(R.id.txEditPhoneNumber);
        txEditOldPassword = (TextView) mView.findViewById(R.id.txEditOldPassword);
        txEditNewPassword = (TextView) mView.findViewById(R.id.txEditNewPassword);
        etEditFullName = (EditText) mView.findViewById(R.id.etEditFullName);
        etEditPhoneNumber = (EditText) mView.findViewById(R.id.etEditPhoneNumber);
        etEditOldPassword = (EditText) mView.findViewById(R.id.etEditOldPassword);
        etEditNewPassword = (EditText) mView.findViewById(R.id.etEditNewPassword);
        edit_image = (CircleImageView) mView.findViewById(R.id.edit_image);
        txAddImage = (TextView) mView.findViewById(R.id.txAddImage);
        bUpdate = (Button) mView.findViewById(R.id.bUpdate);
        txAddImage.setOnClickListener(this);
        bUpdate.setOnClickListener(this);
        Xreference = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Xstorage = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        Xreference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                etEditFullName.setText(dataSnapshot.getValue(UserModel.class).getName());
                etEditPhoneNumber.setText(dataSnapshot.getValue(UserModel.class).getPhoneNumber());
                etEditOldPassword.setText(dataSnapshot.getValue(UserModel.class).getPassword());
                image_url = dataSnapshot.child("imgs").getValue().toString();
                if (image_url != null && !image_url.equals("")) {
                    Picasso.with(getActivity()).load(image_url).into(edit_image);

                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        return mView;
    }

    public boolean validate() {
        boolean valid = true;
        if (etEditFullName.getText().toString().isEmpty()) {
            etEditFullName.setError("Please Enter Full Name");
            valid = false;
        }
        if (etEditPhoneNumber.getText().toString().isEmpty()) {
            etEditPhoneNumber.setError("Please Enter Phone Number");
            valid = false;
        }
        if (!isValidPassword (etEditNewPassword.getText().toString())) {
            etEditNewPassword.setError("Please Enter Phone Number");
            valid = false;
        }
        return valid;
    }

    private void UploadFile() {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        if (filePath!=null) {


            StorageReference riversRef = Xstorage.child("images").child(filePath.getLastPathSegment());
            Picasso.with(getActivity())
                    .load(filePath)
                    .resize(200, 200)
                    .fit().centerCrop();
            riversRef.putFile(filePath)

                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            Xreference.child("imgs").setValue(downloadUri.toString());
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(filePath)
                                    .build();
                            user.updateProfile(profileChangeRequest);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(((int) progress) + "% Uploaded...");
                        }
                    });
        }
    }

    private void GalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            edit_image.setImageBitmap(bitmap);
        }


    }

    @Override
    public void onClick(View view) {
        if (view == txAddImage) {
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_menu_gallery)
                    .setTitle("Upload Image")
                    .setMessage("Upload Image From Gallery")
                    .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GalleryIntent();
                        }
                    }).show();
        } else if (view == bUpdate) {
            if (validate()) {
                if (!etEditNewPassword.getText().toString().trim().equals("")&& !etEditOldPassword.getText().toString().trim().equals("")) {
                    Xreference.child("Name").setValue(etEditFullName.getText().toString().trim());
                    Xreference.child("PhoneNumber").setValue(etEditPhoneNumber.getText().toString().trim());
                    Xreference.child("Password").setValue(etEditNewPassword.getText().toString().trim());
                    mUser.updatePassword(etEditNewPassword.getText().toString().trim());

                }
                UploadFile();
                Toast.makeText(getActivity(), "Uploading image...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Please Enter Valid Data", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isValidPassword(String pass) {// validation for password
        boolean valid = false;
        String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pass);
        if (matcher.matches()) valid = true;
        return valid;
    }
}


//        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//        File photoFile = null;


//                isChooseImage=false;
//                if(isChooseImage==false) {
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
//                    uriPath = mediaFile.getAbsolutePath().toString();//To capture Image with all android devices {By Hussein 06/08/2017}
//                }


//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "JPEG_" + timeStamp + "_";
//           // storageDir = new File(Environment.getExternalStorageDirectory(),"Images");
//            File path = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES);
//            storageDir= new File(path, "DemoPicture.jpg");
//            storageDir.mkdir();

   /*         if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("Directory :: ", "Problem creating Image folder");
                }
            }
            image = File.createTempFile(
                    imageFileName,
                    ".jpeg",
                    storageDir
            );*/


//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,
//                ".jpeg",
//                storageDir
//        );
//        mCurrentPhotoPath = image.getAbsolutePath();
//        return image;
//    }


//    public void cropImage(Uri uri) {
//        getActivity().grantUriPermission("com.android.camera", uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", 720);
//        intent.putExtra("outputY", 720);
//        intent.putExtra("outputFormat", "JPEG");
//        intent.putExtra("noFaceDetection", true);
//        intent.putExtra("return-data", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        filePath = uri;
//        startActivityForResult(intent, REQUEST_CROP);
//    }


//String path=mediaStorageDir.toString()+"/"+"IMG_"+timeStamp+".jpeg";


//        if (photoFile != null) {
//                /*Uri uri = FileProvider.getUriForFile(getActivity(),
//                        "com.wether.exchat",
//                        photoFile);*/
//            //intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//            //filePath = uri;
//        }




    /* bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                edit_image.setImageBitmap(bitmap);*/


//        if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
//                edit_image.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


//    private void CameraIntent() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        try {
//            mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "El-Trapesh");
//            if (!mediaStorageDir.exists()) {
//                mediaStorageDir.mkdirs();
//            }
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
//                    Locale.getDefault()).format(new Date());
//            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//
//            mediaFile = new File(dir, "IMG_" + timeStamp);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
//
//
//            mediaFile = File.createTempFile(
//                    "IMG_" + timeStamp,  /* prefix */
//                    ".jpeg",         /* suffix */
//                    mediaStorageDir      /* directory */
//            );
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
//            imgaePath = mediaFile.getAbsolutePath().toString();
//            startActivityForResult(intent, CAMERA_REQUEST);
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//    }


//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            try {
//                filePath = Uri.parse(imgaePath);
//                edit_image.setImageURI(filePath);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }