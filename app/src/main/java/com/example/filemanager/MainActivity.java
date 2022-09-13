package com.example.filemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layouts);

    }

    class TextAdapter extends BaseAdapter {

        private List<String> data = new ArrayList<>();
        private boolean[] select;


        public void setData(List<String> data) {
            if (data != null) {
                this.data.clear();
                if (data.size() > 0) {
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        public void setSelect(boolean[] select){
            if(select!=null){
                this.select = new boolean[select.length];
                for(int i=0; i<select.length;i++){
                    this.select[i]=select[i];
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
            if(select!=null){
              if(select[position]){
                  holder.info.setBackgroundColor(Color.GREEN);
              }else{
                  holder.info.setBackgroundColor(Color.WHITE);
              }


            }
            return convertView;
        }

        class ViewHolder {
            TextView info;

            ViewHolder(TextView info) {
                this.info = info;
            }
        }

    }

    private static final int REQUEST_PERMISSIONS = 1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied() {

        int p = 0;
        while (p < PERMISSIONS_COUNT) {
            if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            p++;
        }

        return false;
    }

    private boolean isFileManagerInitialized = false;

    private boolean[] select;

    private File[] files;

    private List<String>filesList;
    private int filesFoundCount;
    private int selectedItemIndex;
    private Button refreshButton;




    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {

                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
                return;

        }
        if(!isFileManagerInitialized){
            final String rootPath = String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            final File dir = new File(rootPath);
            files = dir.listFiles();
            final TextView pathOutput = findViewById(R.id.pathOutput);
            pathOutput.setText(rootPath.substring(rootPath.lastIndexOf('/')+1));
            filesFoundCount = files.length;
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter tA1 = new TextAdapter();
            listView.setAdapter(tA1);

             filesList = new ArrayList<>();
            for (int i = 0; i < filesFoundCount; i++) {
                filesList.add(String.valueOf(files[i].getAbsolutePath()));
            }
            tA1.setData(filesList);


            select = new boolean[files.length];

           refreshButton = findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    files = dir.listFiles();
                    filesFoundCount = files.length;
                    filesList.clear();
                    for (int i = 0; i < filesFoundCount; i++) {
                        filesList.add(String.valueOf(files[i].getAbsolutePath()));
                    }
                    tA1.setData(filesList);

                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    select[position]=!select[position];
                    tA1.setSelect(select);
                    int selectCount = 0;
                    for (boolean b : select) {
                        if (b) {
                            selectCount++;
                        }
                    }
                    if(selectCount>0){
                        if(selectCount==1){
                            selectedItemIndex = position;
                            findViewById(R.id.rename).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(R.id.rename).setVisibility(View.GONE);
                        }
                        findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.bottomBar).setVisibility(View.GONE);
                    }
                    return false;
                }
            });



            final Button bn1 = findViewById(R.id.bn1);


            bn1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder delDialog = new AlertDialog.Builder(MainActivity.this);
                    delDialog.setTitle("Delete");
                    delDialog.setMessage("Are you sure you want to delete this?");
                    delDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int w) {
                            for(int i = 0; i<files.length; i++){
                                if(select[i]){
                                    deleteFileOrFolder(files[i]);
                                }
                            }
                            refreshButton.callOnClick();

                        }

                    });
                    delDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    delDialog.show();
                }
            });

            final Button renameButton = findViewById(R.id.rename);
            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder renameDialog =
                            new AlertDialog.Builder(MainActivity.this);
                    renameDialog.setTitle("Rename:");
                    final EditText input = new EditText(MainActivity.this);
                    final String renamePath = files[selectedItemIndex].getAbsolutePath();
                    input.setText(renamePath.substring(renamePath.lastIndexOf('/')));
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    renameDialog.setView(input);
                    renameDialog.setPositiveButton("Rename",
                            new DialogInterface.OnClickListener() {
                               @Override
                                public void onClick(DialogInterface dialog, int w) {
                                   String store = new File(renamePath).getParent() + "/"+ input.getText();
                                   File newFile = new File(store);
                                   new File(renamePath).renameTo(newFile);
                                   refreshButton.callOnClick();
                                   select = new boolean[files.length];
                                   tA1.setSelect(select);

                               }
                            }
                    );
                    renameDialog.show();

                }
            });
            isFileManagerInitialized=true;
        }else{
            refreshButton.callOnClick();
        }


    }

    private void deleteFileOrFolder(File fileOrFolder){
      if(fileOrFolder.isDirectory()){
          if(fileOrFolder.list().length == 0){
              fileOrFolder.delete();
          }else{
              String files[] = fileOrFolder.list();
              for(String tmp:files){
                  File fileToDelete = new File(fileOrFolder, tmp);
                  deleteFileOrFolder(fileToDelete);
              }
              if(fileOrFolder.list().length==0){
                  fileOrFolder.delete();
              }
          }
      }else{
          fileOrFolder.delete();
      }

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String[] permissions, final int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_PERMISSIONS && grantResults.length > 0) {
          if(arePermissionsDenied()){
              ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
              recreate();
          }else{
              onResume();
          }
        }
    }
}
