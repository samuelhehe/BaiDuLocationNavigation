package com.samuelnoes.bdmaps.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;

import com.samuelnotes.bdmaps.R;

public class NaviPointView extends View {

//	private static int w;
//	private static int h;
	
	private int  mBitmapH ;
	
	public static  Bitmap mBitmap;

	public NaviPointView(Context context) {
		super(context);
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_gcoding);
		mBitmapH = mBitmap.getHeight();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int w = this.getWidth() / 2 - mBitmap.getWidth() / 2;
		int h = this.getHeight() / 2 - mBitmap.getHeight();
//		w = this.getWidth() / 2 - mBitmap.getWidth() / 2;
//		h = this.getHeight() / 2 - mBitmap.getHeight() / 2;
		canvas.drawBitmap(mBitmap, w, h, null);
	}

	/**
	 * @return the mBitmapH
	 */
	public int getmBitmapH() {
		return mBitmapH;
	}

	/**
	 * @param mBitmapH the mBitmapH to set
	 */
	public void setmBitmapH(int mBitmapH) {
		this.mBitmapH = mBitmapH;
	}

}
