package com.guzzardo.contacts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MvpView, View.OnClickListener {

    private MainPresenter presenter;
    private ListView mListView;
    private int mEmployeeSelected;
    private static boolean listViewLoaded;
    private MyApplication myApplication;
    static private Map<String, Drawable> employeeSmallIcons;
    static private LayoutInflater inflater;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list);
        mListView.setClickable(true);

        myApplication = ((MyApplication) this.getApplication());
        myApplication.setListView(mListView);
        myApplication.setContext(this);
        mContext = this;

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object o = mListView.getItemAtPosition(position);
                mEmployeeSelected = position;
                myApplication.setEmployeeId(Integer.toString(position));
                showEmployeeDetail();
            }
        });

        presenter = new MainPresenter(this);
        presenter.setContext(this);
        employeeSmallIcons = new HashMap<String, Drawable>();
        inflater = this.getLayoutInflater();

        ((MyApplication) this.getApplication()).setPresenter(presenter);
        new DownloadFilesTask(this).execute("one", "two", "three");
    }

    private void showEmployeeDetail() {
        Intent i = new Intent(this, EmployeeDetailActivity.class);
        i.putExtra(EmployeeDetailActivity.ID, Integer.toString(mEmployeeSelected));
        startActivity(i);
    }

    Drawable loadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable bitmapDrawable = Drawable.createFromStream(is, "src name");
            Drawable bitmap = bitmapDrawable.getCurrent();
            return bitmapDrawable;
        } catch (Exception e) {
            System.out.println("error in loadImageFromWebOperations: " +e);
            return mContext.getResources().getDrawable(R.drawable.user_icon_small);
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, Void> {
        private ProgressBar dialog;
        ProgressDialog progressBar;
        private List<Repository.Employee> employeeList;

        public DownloadFilesTask(MainActivity activity) {
            dialog = new ProgressBar(activity);
        }

        @Override
        protected void onPreExecute() {
            //dialog.setProgress(0);
            dialog.setMax(100);
            int progressbarstatus = 0;
            //dialog.setProgress();

            //dialog.setMe .setM        .setMessage("Loading data, please wait.");
           // dialog.sh .show();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (int i = 0; i < 30; i++) {
                try {
                    if (presenter.getDataLoaded()) {
                        employeeList = presenter.getEmployeeList();
                        if (listViewLoaded) {
                            return null;
                        }
                        loadListView(employeeList);
                    }
                    publishProgress(i);
                    dialog.incrementProgressBy(10);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            mListView.setAdapter(myApplication.getmAdapter());

            /*
            if (dialog.   .isShowing()) {
                dialog.dismiss();
            }
            */
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }


        //load the small icon bitmaps...
        private void loadListView(List<Repository.Employee> employeeList) {
            String[] listNames = new String[employeeList.size()];
            String[] listCompany = new String[employeeList.size()];
            Integer[] listImages = new Integer[employeeList.size()];
            String[] listFavorites = new String[employeeList.size()];
            List<View> customList = new ArrayList<View>();
            Map<String, Bitmap> bitmapMap = new HashMap<String, Bitmap>();

            int imageId = getResources().getIdentifier("user_icon_small", "drawable", getPackageName());

            if (employeeSmallIcons.size() == 0) {
                for (int x = 0; x < employeeList.size(); x++) {
                    Repository.Employee employee = employeeList.get(x);
                    listNames[x] = employee.name;
                    listCompany[x] = employee.companyName;

                    listImages[x] = imageId;
                    listFavorites[x] = employee.isFavorite;
                    myApplication.setEmployeeIdByPosition(Integer.toString(x), employee.id);

                    String smallIconUrl = employee.smallImageURL;
                    Drawable contactSmallIcon = loadImageFromWebOperations(smallIconUrl);
                    employeeSmallIcons.put(employee.id, contactSmallIcon);
                    Bitmap bitmap = ((BitmapDrawable) contactSmallIcon).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                    bitmapMap.put(employee.id, bitmap2);
                }
            }

            for (int x=0; x<employeeList.size(); x++) {
                View rowView = inflater.inflate(R.layout.list_single, null, false);
                customList.add(rowView);
                String employeeId = myApplication.getEmployeeIdByPosition(Integer.toString(x));
                Drawable contactSmallIcon = employeeSmallIcons.get(employeeId);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
                Bitmap bm = ((BitmapDrawable) contactSmallIcon).getBitmap();
                imageView.setImageBitmap(bm);
            }

            myApplication.setCustomList(customList);
            myApplication.setSmallIconBitmapMap(bitmapMap);
            CustomList adapter = new CustomList(myApplication,MainActivity.this, listNames, listCompany, listImages, listFavorites);
            myApplication.setListViewAdapter(adapter);
            listViewLoaded = true;
        }
    }

    public void loadListView2(List<Repository.Employee> employeeList) {
        String[] listNames = new String[employeeList.size()];
        String[] listCompany = new String[employeeList.size()];
        Integer[] listImages = new Integer[employeeList.size()];
        String[] listFavorites = new String[employeeList.size()];
        List<View> customList = new ArrayList<View>();

        int imageId = getResources().getIdentifier("user_icon_small", "drawable", getPackageName());

        for (int x=0; x<employeeList.size(); x++) {
            Repository.Employee employee = employeeList.get(x);
            listNames[x] = employee.name;
            listCompany[x] = employee.companyName;

            listImages[x] = imageId;
            listFavorites[x] = employee.isFavorite;
            myApplication.setEmployeeIdByPosition(Integer.toString(x), employee.id);

            View rowView = inflater.inflate(R.layout.list_single, null, false);
            customList.add(rowView);
        }
        myApplication.setCustomList(customList);
        CustomList adapter = new CustomList(myApplication,MainActivity.this, listNames, listCompany, listImages, listFavorites);
        myApplication.setListViewAdapter(adapter);
        mListView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.name)
            showEmployeeDetail();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mListView.setAdapter(myApplication.getmAdapter());
    }

}
