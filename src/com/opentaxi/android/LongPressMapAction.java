package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.opentaxi.generated.mysql.tables.pojos.NewRequest;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;
import org.mapsforge.applications.android.mapbg.LocationOverlayMapViewer;
import org.mapsforge.applications.android.mapbg.TextCircle;
import org.mapsforge.applications.android.mapbg.Utils;
import org.mapsforge.core.graphics.Align;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;

import java.io.File;

/**
 * Demonstrates how to enable a LongPress on a layer, long press creates/removes
 * circles, tap on a circle toggles the colour.
 */
@EActivity
public class LongPressMapAction extends LocationOverlayMapViewer {

    @Extra
    NewRequest newRequest;

    private static final Paint GREEN = Utils.createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN), 0, Style.FILL);
    int index = -1;
    //private NewRequest address = new NewRequest();
    /*private static final Paint RED = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.RED), 0,
            Style.FILL);*/

    protected int getLayoutId() {
        return R.layout.show_address;
    }

    @Override
    protected void createLayers() {
        super.createLayers();
        Layers layers = this.layerManagers.get(0).getLayers();
        File mapFile = this.getMapFile();
        if (mapFile != null) {
            Log.i(TAG, "createLayers " + mapFile.getAbsolutePath());
            TileRendererLayer tileRendererLayer = new TileRendererLayer(
                    this.tileCache,
                    this.mapViewPositions.get(0),
                    false,
                    org.mapsforge.map.android.graphics.AndroidGraphicFactory.INSTANCE) {
                @Override
                public boolean onLongPress(LatLong tapLatLong, Point thisXY,
                                           Point tapXY) {
                    LongPressMapAction.this.onLongPress(tapLatLong);
                    return true;
                }
            };
            tileRendererLayer.setMapFile(mapFile);
            tileRendererLayer.setXmlRenderTheme(this.getRenderTheme());
            layers.add(tileRendererLayer);

            if (newRequest != null) showTextCircle(newRequest);
            else showAlert();
        }
    }

    void showAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Избор на адрес");
        alertDialogBuilder.setMessage("Моля посочете на картата и задръжте за 1 секунда върху адреса на заявката");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    protected void onLongPress(LatLong position) {
        if (this.newRequest == null) this.newRequest = new NewRequest();
        this.newRequest.setNorth(position.latitude);
        this.newRequest.setEast(position.longitude);
        showAddress(position);
        Layers layers = this.layerManagers.get(0).getLayers(); //this.mapViews.get(0).getLayerManager().getLayers()
        if (index >= 0) layers.remove(index);
        float circleSize = 8 * this.mapViews.get(0).getModel().displayModel.getScaleFactor();
        Circle circle = new Circle(position, circleSize, GREEN, null);
        layers.add(circle);
        index = layers.size() - 1;
        circle.requestRedraw();
    }

    @Background
    void showAddress(LatLong position) {
        NewRequest address = RestClient.getInstance().getAddressByCoordinates((float) position.latitude, (float) position.longitude);
        if (address != null) {
            this.newRequest = address;
            this.newRequest.setNorth(position.latitude);
            this.newRequest.setEast(position.longitude);
            showTextCircle(address);
        }
    }

    @UiThread
    void showTextCircle(NewRequest address) {
        if (address != null) {
            Layers layers = this.layerManagers.get(0).getLayers(); //this.mapViews.get(0).getLayerManager().getLayers()
            if (index >= 0) layers.remove(index);
            float circleSize = 8 * this.mapViews.get(0).getModel().displayModel.getScaleFactor();
            Paint paint = Utils.createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK), 0, Style.FILL); //android.graphics.Paint.ANTI_ALIAS_FLAG);
            paint.setTextAlign(Align.LEFT);
            paint.setTextSize(25f);
            LatLong position = new LatLong(address.getNorth(), address.getEast());
            TextCircle circle = new TextCircle(position, circleSize, address.getFullAddress(), paint, GREEN, null);
            circle.setOffsetX(10);
            layers.add(circle);
            index = layers.size() - 1;
            circle.requestRedraw();
            this.mapViews.get(0).getModel().mapViewPosition.setCenter(position);
        }
    }

    @Click
    void okButton() {
        setResult(Activity.RESULT_OK, new Intent().putExtra("newRequest", this.newRequest));
        finish();
    }

    /*@Override
    public void finish() {
        //Log.i(TAG, "Address:" + newRequest.getFullAddress());
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, new Intent().putExtra("newRequest", address));
        } else {
            getParent().setResult(Activity.RESULT_OK, new Intent().putExtra("newRequest", address));
        }
        super.finish();
    }*/
}