package com.example.advanced_android.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.advanced_android.PlantWateringService;
import com.example.advanced_android.R;
import com.example.advanced_android.provider.PlantContract;
import com.example.advanced_android.utils.PlantUtils;

import static com.example.advanced_android.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.advanced_android.provider.PlantContract.PATH_PLANTS;

public class PlantDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SINGLE_LOADER_ID = 200;
    public static final String EXTRA_PLANT_ID = "com.example.android.mygarden.extra.PLANT_ID";
    long mPlantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);
        mPlantId = getIntent().getLongExtra(EXTRA_PLANT_ID, PlantContract.INVALID_PLANT_ID);
        // This activity displays single plant information that is loaded using a cursor loader
        getSupportLoaderManager().initLoader(SINGLE_LOADER_ID, null, this);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    public void onWaterButtonClick(View view) {
        PlantWateringService.startActionWaterPlant(this, mPlantId);
        //check if already dead then can't water

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), mPlantId);
        return new CursorLoader(this, SINGLE_PLANT_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) return;
        cursor.moveToFirst();
        int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int planTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        int plantType = cursor.getInt(planTypeIndex);
        long createdAt = cursor.getLong(createTimeIndex);
        long wateredAt = cursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();

        int plantImgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);

        ((ImageView) findViewById(R.id.plant_detail_image)).setImageResource(plantImgRes);
        ((TextView) findViewById(R.id.plant_detail_name)).setText(String.valueOf(mPlantId));
        ((TextView) findViewById(R.id.plant_age_number)).setText(
                String.valueOf(PlantUtils.getDisplayAgeInt(timeNow - createdAt))
        );
        ((TextView) findViewById(R.id.plant_age_unit)).setText(
                PlantUtils.getDisplayAgeUnit(this, timeNow - createdAt)
        );
        ((TextView) findViewById(R.id.last_watered_number)).setText(
                String.valueOf(PlantUtils.getDisplayAgeInt(timeNow - wateredAt))
        );
        ((TextView) findViewById(R.id.last_watered_unit)).setText(
                PlantUtils.getDisplayAgeUnit(this, timeNow - wateredAt)
        );
        int waterPercent = 100 - ((int) (100 * (timeNow - wateredAt) / PlantUtils.MAX_AGE_WITHOUT_WATER));
        ((WaterLevelView) findViewById(R.id.water_level)).setValue(waterPercent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onCutButtonClick(View view) {
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), mPlantId);
        getContentResolver().delete(SINGLE_PLANT_URI, null, null);
        PlantWateringService.startActionUpdatePlantsWidgets(this);
        finish();
    }
}