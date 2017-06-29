package com.example.trantrungduong95.truesms.CustomAdapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import com.example.trantrungduong95.truesms.R;

public class DrawView extends View {

	Paint paint;
	Shader dalam;
	Shader luar;
	float hue;
	float satudp;
	float UiDp = 240.f;
	float UiPx; // diset di constructor
	float[] tmp00 = new float[3];

	public DrawView(Context context) {
		this(context, null);
	}

	public DrawView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.satudp = context.getResources().getDimension(R.dimen.ambilwarna_satudp);
		this.UiPx = this.UiDp * this.satudp;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (this.paint == null) {
			this.paint = new Paint();
			this.luar = new LinearGradient(0.f, 0.f, 0.f, this.UiPx, 0xffffffff, 0xff000000,
					TileMode.CLAMP);
		}

		this.tmp00[1] = this.tmp00[2] = 1.f;
		this.tmp00[0] = this.hue;
		int rgb = Color.HSVToColor(this.tmp00);

		this.dalam = new LinearGradient(0.f, 0.f, this.UiPx, 0.f, 0xffffffff, rgb, TileMode.CLAMP);
		ComposeShader shader = new ComposeShader(this.luar, this.dalam, PorterDuff.Mode.MULTIPLY);

		this.paint.setShader(shader);

		canvas.drawRect(0.f, 0.f, this.UiPx, this.UiPx, this.paint);
	}

	void setHue(float hue) {
		this.hue = hue;
		this.invalidate();
	}
}
