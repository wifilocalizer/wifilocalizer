package de.kk.wifilocalizer.ui.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.core.CoreManager;
import de.kk.wifilocalizer.core.helper.Position;
import de.kk.wifilocalizer.ui.activities.MainActivity;

/**
 * Draws Bitmaps on screen and handles LongPress events
 */
public class MapView extends View {
    private final GestureDetectorCompat mDetector;
    private static Vibrator mVibrator;

    private Bitmap mBitmapMeasurement;
    private Bitmap mBitmapEgoPosition;
    private Bitmap mMap, mInitMap, mOriginalMap;

    private List<Position> mMeasurePositions;
    private Point mMapTopLeft;
    private Point mEgoPosition;
    private int mMapWidth, mMapHeight;
    private int mViewWidth, mViewHeight;

    private String mMapname;
    private boolean mShowPoints = true;
    private boolean mMapAvailable = false;

    /**
     * Initializes variables and allocates drawables to bitmaps
     * 
     * @param context
     * @param attrs
     */
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
        mMapTopLeft = new Point(0, 0);
        mEgoPosition = new Point(-1, -1);
        mMeasurePositions = new ArrayList<Position>();
        mDetector = new GestureDetectorCompat(context, new MyGestureListener());
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mBitmapMeasurement = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_marker_measurement);
        mBitmapEgoPosition = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_marker);
        mInitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_init_map);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    /**
     * Converts filepath and initializes image for later use
     * 
     * @param context
     * @param mapname
     * @param filepath
     */
    public void setUpMap(Context context, String mapname, String filepath) {
        mMapAvailable = true;
        mMapname = mapname;
        Uri uri = Uri.parse(filepath);
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(uri);
            mOriginalMap = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invalidates view if Show/Hide button in menubar is pressed
     */
    public void toggleShowHidePoints() {
        mShowPoints = !mShowPoints;
        invalidate();
    }

    /**
     * Invalidates view
     */
    public void invalidateView() {
        invalidate();
    }

    /**
     * Called from Localizing thread in LocalizationFragment<br />
     * Sets EgoPosition and invalidates view
     * 
     * @param pointF
     */
    public void drawEgoPosition(final Position pos) {
        PointF point = transformCoordinatesToDraw(pos);
        mEgoPosition.x = (int) point.x;
        mEgoPosition.y = (int) point.y;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw != w || oldh != h) {
            mViewWidth = w;
            mViewHeight = h;
            if (mMapAvailable) {
                float mapRatio = (float) mOriginalMap.getWidth() / mOriginalMap.getHeight();
                float viewRatio = (float) mViewWidth / mViewHeight;
                if (mapRatio >= viewRatio) {
                    // adapt image to fit viewWidth
                    float scaleFactor = (float) mViewWidth / (float) mOriginalMap.getWidth();
                    mMapHeight = (int) (scaleFactor * mOriginalMap.getHeight());
                    mMapWidth = mViewWidth;
                    mMapTopLeft.x = 0;
                    mMapTopLeft.y = mViewHeight / 2 - mMapHeight / 2;
                } else {
                    // adapt image to fit viewHeight
                    float scaleFactor = (float) mViewHeight / (float) mOriginalMap.getHeight();
                    mMapWidth = (int) (scaleFactor * mOriginalMap.getWidth());
                    mMapHeight = mViewHeight;
                    mMapTopLeft.x = mViewWidth / 2 - mMapWidth / 2;
                    mMapTopLeft.y = 0;
                }
                // scaled image ready to draw
                mMap = Bitmap.createScaledBitmap(mOriginalMap, mMapWidth, mMapHeight, true);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMapAvailable) {
            // Draw scaled map
            canvas.drawBitmap(mMap, mMapTopLeft.x, mMapTopLeft.y, null);
            // Draw EgoPosition
            if (mEgoPosition.x >= 0 || mEgoPosition.y >= 0)
                canvas.drawBitmap(mBitmapEgoPosition, mEgoPosition.x - mBitmapEgoPosition.getWidth() / 2, mEgoPosition.y
                        - mBitmapEgoPosition.getHeight(), null);
            // If requested show measured points
            if (mShowPoints) {
                if (CoreManager.hasMap(mMapname)) {
                    mMeasurePositions.clear();
                    mMeasurePositions = CoreManager.getMeasuredPositions(mMapname);
                    for (int i = 0; i < mMeasurePositions.size(); i++) {
                        PointF pointF = transformCoordinatesToDraw(mMeasurePositions.get(i));
                        canvas.drawBitmap(mBitmapMeasurement, pointF.x - mBitmapMeasurement.getWidth() / 2, pointF.y
                                - mBitmapMeasurement.getHeight(), null);
                    }
                }
            }
        } else {
            // Draw init map
            canvas.drawBitmap(mInitMap, mViewWidth / 2 - mInitMap.getWidth() / 2,
                    mViewHeight / 2 - mInitMap.getHeight() / 2, null);
        }
    }

    /**
     * Transforms PointF to Position<br />
     * 
     * @param pointF
     * @return transformed Position
     */
    private PointF transformCoordinatesToNorm(final PointF pointF) {
        PointF point = new PointF((pointF.x - mMapTopLeft.x) / mMapWidth, (pointF.y - mMapTopLeft.y) / mMapHeight);
        return point;
    }

    /**
     * Transforms Position to PointF
     * 
     * @param pointF
     *            value range 0.0 - 1.0
     * @return transformed PointF
     */
    private PointF transformCoordinatesToDraw(final Position pos) {
        PointF point = new PointF(pos.getX() * mMapWidth + mMapTopLeft.x, pos.getY() * mMapHeight + mMapTopLeft.y);
        return point;
    }

    /**
     * Listens to LongPress events and shows dialogs according to pressed position<br />
     * Dialogs: confirmation dialog for setPosition, no map selected, WiFi fetching off, LongPress out of map area
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            mVibrator.vibrate(50);
            if (mMapAvailable) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                if (sp.getBoolean(MainActivity.WIFI_FETCHING_TOGGLE_ON, false)) {
                    boolean isInBoundsX = (event.getX() >= mMapTopLeft.x && event.getX() <= mMapTopLeft.x + mMapWidth);
                    boolean isInBoundsY = (event.getY() >= mMapTopLeft.y && event.getY() <= mMapTopLeft.y + mMapHeight);
                    if (isInBoundsX && isInBoundsY) {
                        Log.d("MapView", "Point absolute: (" + event.getX() + ", " + event.getY() + ")");
                        final PointF pointF = transformCoordinatesToNorm(new PointF(event.getX(), event.getY()));
                        Log.d("MapView", "Point relative: (" + pointF.x + ", " + pointF.y + ")");
                        // Dialog: confirmation dialog for setPosition
                        new AlertDialog.Builder(getContext()).setTitle(R.string.mapview_alert_dialog_measuring_title)
                                .setMessage(R.string.mapview_alert_dialog_measuring_message)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Position pos = new Position(pointF.x, pointF.y);
                                        CoreManager.setPosition(pos);
                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close dialog
                                    }
                                }).show();
                    } else {
                        // Dialog: LongPress was out of area
                        new AlertDialog.Builder(getContext()).setTitle(R.string.mapview_alert_dialog_out_of_area_title)
                                .setMessage(R.string.mapview_alert_dialog_out_of_area_message)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close dialog
                                    }
                                }).show();
                    }
                } else {
                    // Dialog: for correct measurement WiFi has to be turned on
                    new AlertDialog.Builder(getContext()).setTitle(R.string.mapview_alert_dialog_wifi_fetching_off_title)
                            .setMessage(R.string.mapview_alert_dialog_wifi_fetching_off_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Close dialog
                                }
                            }).show();
                }

            } else {
                // Dialog: no map selected
                new AlertDialog.Builder(getContext()).setTitle(R.string.mapview_alert_dialog_no_map_title)
                        .setMessage(R.string.mapview_alert_dialog_no_map_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close dialog
                            }
                        }).show();
            }
            postInvalidate();
        }
    }
}
