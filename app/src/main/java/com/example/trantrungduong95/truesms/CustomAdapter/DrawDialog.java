package com.example.trantrungduong95.truesms.CustomAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.example.trantrungduong95.truesms.R;


public class DrawDialog {
	private String TAG = DrawDialog.class.getSimpleName();

	public interface OnListener {
		void onReset(DrawDialog dialog);

		void onCancel(DrawDialog dialog);

		void onOk(DrawDialog dialog, int color);
	}

	private AlertDialog dialog;
	private OnListener listener;
	private View viewHue;
	private DrawView viewColor;
	private ImageView panah;
	private View viewLama;
	private View viewBaru;
	private ImageView viewKeker;

	private float satudp;
	private int Lama;
	private int Baru;
	private float hue;
	private float sat;
	private float val;
	private float UiDp = 0x1.ep7f;
	private float UiPx; // diset di constructor

	public DrawDialog(Context context, int color, OnListener listener) {
		this.listener = listener;
		this.Lama = color;
		this.Baru = color;
		Color.colorToHSV(color, this.tmp01);
		this.hue = this.tmp01[0];
		this.sat = this.tmp01[1];
		this.val = this.tmp01[2];

		this.satudp = context.getResources().getDimension(R.dimen.ambilwarna_satudp);
		this.UiPx = this.UiDp * this.satudp;
		Log.d(TAG, "satudp = " + this.satudp);
		Log.d(TAG, "UiPx=" + this.UiPx);

		View view = LayoutInflater.from(context).inflate(R.layout.viewcolor_dialog, null);
		this.viewHue = view.findViewById(R.id.viewHue);
		this.viewColor = (DrawView) view.findViewById(R.id.viewColor);
		this.panah = (ImageView) view.findViewById(R.id.panah);
		this.viewLama = view.findViewById(R.id.Lama);
		this.viewBaru = view.findViewById(R.id.Baru);
		this.viewKeker = (ImageView) view.findViewById(R.id.keker);

		this.letakkanPanah();
		this.letakkanKeker();
		this.viewColor.setHue(this.hue);
		this.viewLama.setBackgroundColor(color);
		this.viewBaru.setBackgroundColor(color);

		this.viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY(); // dalam px, bukan dp
					if (y < 0.f) {
						y = 0.f;
					}
					if (y > DrawDialog.this.UiPx) {
						y = DrawDialog.this.UiPx - 0.001f;
					}

					DrawDialog.this.hue = 360.f - 360.f / DrawDialog.this.UiPx
							* y;
					if (DrawDialog.this.hue == 360.f) {
						DrawDialog.this.hue = 0.f;
					}

					DrawDialog.this.Baru = DrawDialog.this.hitungWarna();
					// update view
					DrawDialog.this.viewColor.setHue(DrawDialog.this.hue);
					DrawDialog.this.letakkanPanah();
					DrawDialog.this.viewBaru
							.setBackgroundColor(DrawDialog.this.Baru);

					return true;
				}
				return false;
			}
		});
		this.viewColor.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // dalam px, bukan dp
					float y = event.getY(); // dalam px, bukan dp

					if (x < 0.f) {
						x = 0.f;
					}
					if (x > DrawDialog.this.UiPx) {
						x = DrawDialog.this.UiPx;
					}
					if (y < 0.f) {
						y = 0.f;
					}
					if (y > DrawDialog.this.UiPx) {
						y = DrawDialog.this.UiPx;
					}

					DrawDialog.this.sat = (1.f / DrawDialog.this.UiPx * x);
					DrawDialog.this.val = 1.f - (1.f / DrawDialog.this.UiPx * y);

					DrawDialog.this.Baru = DrawDialog.this.hitungWarna();
					// update view
					DrawDialog.this.letakkanKeker();
					DrawDialog.this.viewBaru
							.setBackgroundColor(DrawDialog.this.Baru);

					return true;
				}
				return false;
			}
		});

		this.dialog = new AlertDialog.Builder(context)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (DrawDialog.this.listener != null) {
							DrawDialog.this.listener.onOk(DrawDialog.this,
									DrawDialog.this.Baru);
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (DrawDialog.this.listener != null) {
							DrawDialog.this.listener.onCancel(DrawDialog.this);
						}
					}
				}).setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (DrawDialog.this.listener != null) {
							DrawDialog.this.listener.onReset(DrawDialog.this);
						}
					}
				}).create();

	}

	@SuppressWarnings("deprecation")
	protected void letakkanPanah() {
		float y = this.UiPx - (this.hue * this.UiPx / 360.f);
		if (y == this.UiPx) {
			y = 0.f;
		}

		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) this.panah
				.getLayoutParams();
		layoutParams.y = (int) (y + 4);
		this.panah.setLayoutParams(layoutParams);
	}

	@SuppressWarnings("deprecation")
	protected void letakkanKeker() {
		float x = this.sat * this.UiPx;
		float y = (1.f - this.val) * this.UiPx;

		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) this.viewKeker
				.getLayoutParams();
		layoutParams.x = (int) (x + 3);
		layoutParams.y = (int) (y + 3);
		this.viewKeker.setLayoutParams(layoutParams);
	}

	float[] tmp01 = new float[3];

	private int hitungWarna() {
		this.tmp01[0] = this.hue;
		this.tmp01[1] = this.sat;
		this.tmp01[2] = this.val;
		return Color.HSVToColor(this.tmp01);
	}

	public void show() {
		this.dialog.show();
	}
}
