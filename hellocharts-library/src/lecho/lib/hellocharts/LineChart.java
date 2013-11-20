package lecho.lib.hellocharts;

import lehco.lib.hellocharts.model.LineChartData;
import lehco.lib.hellocharts.model.LineSeries;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class LineChart extends View {
	protected LineChartData mData;
	protected Bitmap mBitmap;
	protected Canvas mCanvas;
	protected Paint mLinePaint = new Paint();
	protected Path mLinePath = new Path();
	protected Paint mPointPaint = new Paint();
	protected Xfermode mXfermode = new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR);
	protected float mLineWidth = 4.0f;
	protected float mPointRadius = 12.0f;
	protected float mPointRadiusBig = mPointRadius + mLineWidth;
	protected float mPointRadiusSmall = mPointRadius - mLineWidth;
	protected float minXValue = Float.MAX_VALUE;
	protected float maxXValue = Float.MIN_VALUE;
	protected float minYValue = Float.MAX_VALUE;
	protected float maxYValue = Float.MIN_VALUE;
	protected float mXMultiplier;
	protected float mYMultiplier;

	public LineChart(Context context) {
		super(context);
		initPaint();
	}

	public LineChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public LineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	protected void initPaint() {
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.BLACK);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setStrokeWidth(mLineWidth);

		mPointPaint.setAntiAlias(true);
		mPointPaint.setColor(Color.BLACK);
		mPointPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		super.onSizeChanged(width, height, oldWidth, oldHeight);
		mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		mXMultiplier = availableWidth / (maxXValue - minXValue);
		mYMultiplier = availableHeight / (maxYValue - minYValue);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		long time = System.nanoTime();
		for (LineSeries lineSeries : mData.series) {
			mLinePaint.setColor(lineSeries.color);
			int valueIndex = 0;
			for (Float valueX : mData.domain) {
				float rawValueX = calculateX(valueX);
				float rawValueY = calculateY(lineSeries.values.get(valueIndex));
				if (valueIndex == 0) {
					mLinePath.moveTo(rawValueX, rawValueY);
				} else {
					mLinePath.lineTo(rawValueX, rawValueY);
				}
				++valueIndex;
			}
			mCanvas.drawPath(mLinePath, mLinePaint);
			mLinePath.reset();
		}

		for (LineSeries lineSeries : mData.series) {
			int valueIndex = 0;
			for (Float valueX : mData.domain) {
				float rawValueX = calculateX(valueX);
				float rawValueY = calculateY(lineSeries.values.get(valueIndex));
				mPointPaint.setXfermode(mXfermode);
				mPointPaint.setColor(Color.TRANSPARENT);
				mCanvas.drawCircle(rawValueX, rawValueY, mPointRadiusBig, mPointPaint);
				mPointPaint.setXfermode(null);
				mPointPaint.setColor(lineSeries.color);
				mCanvas.drawCircle(rawValueX, rawValueY, mPointRadius, mPointPaint);
				mPointPaint.setXfermode(mXfermode);
				mPointPaint.setColor(Color.TRANSPARENT);
				mCanvas.drawCircle(rawValueX, rawValueY, mPointRadiusSmall, mPointPaint);
				++valueIndex;
			}
		}
		Log.v("TAG", "Narysowane w [ms]: " + (System.nanoTime() - time) / 1000000);
		canvas.drawBitmap(mBitmap, 0, 0, null);
		Log.v("TAG", "Wyświetlone w [ms]: " + (System.nanoTime() - time) / 1000000);
	}

	private float calculateX(float valueX) {
		return getPaddingLeft() + (valueX - minXValue) * mXMultiplier;
	}

	private float calculateY(float valueY) {
		return getHeight() - getPaddingBottom() - (valueY - minYValue) * mYMultiplier;
	}

	public void setData(LineChartData data) {
		mData = data;
		calculateRanges();
		postInvalidate();
	}

	private void calculateRanges() {
		for (Float value : mData.domain) {
			if (value < minXValue) {
				minXValue = value;
			} else if (value > maxXValue) {
				maxXValue = value;
			}
		}
		for (LineSeries lineSeries : mData.series) {
			for (Float value : lineSeries.values) {
				if (value < minYValue) {
					minYValue = value;
				} else if (value > maxYValue) {
					maxYValue = value;
				}
			}
		}
	}

}
