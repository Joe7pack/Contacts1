package com.guzzardo.contacts;

import android.graphics.Bitmap;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {

    private final String[] names;
    private final String[] company;
    private final String[] favorite;
    private final Integer[] imageId;
    private MainPresenter presenter;
    private MyApplication mMyApplication;
    private int mOtherContactStartingPosition = 0;

    public CustomList(MyApplication myApplication, Activity context,
                      String[] names, String[] company, Integer[] imageId, String[] favorite) {
        super(context, R.layout.list_single, names);
        this.names = names;
        this.company = company;
        this.imageId = imageId;
        this.favorite = favorite;
        mMyApplication = myApplication;
        presenter = mMyApplication.getPresenter();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = mMyApplication.getCustomList(position);

        TextView txtContactType = (TextView) rowView.findViewById(R.id.contact_type);
        txtContactType.setText("FAVORITE CONTACTS");

        ImageView imageViewFavorite = (ImageView) rowView.findViewById(R.id.favorite_contact);
        if (favorite[position].equals("true")) {
            imageViewFavorite.setImageResource(R.drawable.favorite_icon_true);
        } else {
            imageViewFavorite.setImageResource(R.drawable.favorite_icon_false_invisible);
            txtContactType.setText("OTHER CONTACTS");
            if (mOtherContactStartingPosition == 0) {
                mOtherContactStartingPosition = position;
            }
        }

        if (position == 0 | position == mOtherContactStartingPosition) {
            txtContactType.setVisibility(View.VISIBLE);
        } else {
            txtContactType.setVisibility(View.INVISIBLE);
            TableLayout.LayoutParams parms = (TableLayout.LayoutParams)txtContactType.getLayoutParams();
            parms.height = 0;
            txtContactType.setLayoutParams(parms);
        }

        if (mOtherContactStartingPosition != 0 && position >= mOtherContactStartingPosition) {
            imageViewFavorite.setVisibility(View.INVISIBLE);

            Object xxx = imageViewFavorite.getLayoutParams();
            TableRow.LayoutParams parms = (TableRow.LayoutParams)imageViewFavorite.getLayoutParams();
            parms.width = 10;
        }

        TextView txtName = (TextView) rowView.findViewById(R.id.contact_name);
        TextView txtCompany = (TextView) rowView.findViewById(R.id.contact_company);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtName.setText(names[position]);
        txtCompany.setText(company[position]);
        Repository.Employee employee = presenter.getModel().getEmployee(Integer.toString(position));
        String employeeId = mMyApplication.getEmployeeIdByPosition(Integer.toString(position));
        Bitmap bitmap = mMyApplication.getSmallIconBitmapMap(employeeId);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 40, 40, false));

        return rowView;
    }
}
