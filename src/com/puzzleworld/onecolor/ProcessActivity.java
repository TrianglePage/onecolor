package com.puzzleworld.onecolor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;

public class ProcessActivity extends Activity {

	private ScaleImageView ivProcess;
	private ImageButton btnRestore;
	private ImageButton btnUndo;
	private ImageButton btnRedo;
	private ImageButton btnConfirm;
	private ImageView btnPickanother;
	private Bitmap showBitmap = null;
	private ImageView ivSubtraction;
	private ImageView ivAdd;
	private SeekBar seekBar;
	// private TextView textView;
	// private TextView textView1;
	private int align;
	private boolean picSelected = false;
	private Thread myThread;
	private int seekbarLevel = 0;
	private Handler mHandler;
	private boolean selectedNew = false;
	private CheckBox cbIsBlur;

	private GradientDrawable bgShape;
	private View currentSelectedColor = null;
	private backgroundColor_e bgColor;
	private int isBlur;
	final float PIC_MAX_WIDTH = 1920;
	final float PIC_MAX_HEIGHT = 1080;
	final int seekbarMaxLevel = 100;

	private int statusLevel = 0;
	private int statusBgColor = 0;
	private int statusIsBlur = 0;

	private ViewPager mPager;// 页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private TextView t1, t2, t3, t4;// 页卡头标
	private int currIndex = 0;// 当前页卡编号

	private enum textView_e {
		TV_SELECT_PIC, TV_CHANGE_LEVEL, TV_TOUCH_POINT, TV_CONFIRM, TV_MAX_NUM
	};

	private enum backgroundColor_e {
		BG_GRAY, BG_GREEN, BG_BLUE, BG_YELLOW, BG_PINK
	};
	
	private void setBlurBackground() {
		//背景是虚化的，这里设置处理级别为1，背景颜色是0，是否虚化是1
		ivProcess.setParameters(1, 0, 1);
		int processPointXY[] = {1,1};
		Bitmap bkpic = ivProcess.getProcessedPicture(processPointXY, 1);
		LinearLayout bklayout = (LinearLayout) findViewById(R.id.layoutProcessPic);
		bklayout.setBackground(new BitmapDrawable(bkpic));
	}

	private void switchColorStatus(View tempColor) {
		if (currentSelectedColor == null) {
			currentSelectedColor = tempColor;
			bgShape = (GradientDrawable) currentSelectedColor.getBackground();
			bgShape.setStroke(3, Color.argb(200, 255, 255, 255));
			currentSelectedColor.setBackground(bgShape);
		} else {
			bgShape = (GradientDrawable) currentSelectedColor.getBackground();
			bgShape.setStroke(0, Color.argb(255, 255, 0, 0));
			currentSelectedColor.setBackground(bgShape);

			if (currentSelectedColor == tempColor) {
				currentSelectedColor = null;
			} else {
				bgShape = (GradientDrawable) tempColor.getBackground();
				bgShape.setStroke(3, Color.argb(200, 255, 255, 255));
				tempColor.setBackground(bgShape);
				currentSelectedColor = tempColor;
			}
		}

		getParameters();
	}

	private void getParameters() {
		if (currentSelectedColor != null) {
			switch (currentSelectedColor.getId()) {
			case R.id.bgColorGray:
				bgColor = backgroundColor_e.BG_GRAY;
				break;
			case R.id.bgColorGreen:
				bgColor = backgroundColor_e.BG_GREEN;
				break;
			case R.id.bgColorBlue:
				bgColor = backgroundColor_e.BG_BLUE;
				break;
			case R.id.bgColorYellow:
				bgColor = backgroundColor_e.BG_YELLOW;
				break;
			case R.id.bgColorPink:
				bgColor = backgroundColor_e.BG_PINK;
				break;
			default:
				bgColor = backgroundColor_e.BG_GRAY;
			}
		}

		if (cbIsBlur != null) {
			isBlur = cbIsBlur.isChecked() ? 1 : 0;
		}
	}

	private boolean statusChanged() {
		return (statusLevel != seekbarLevel) || (statusBgColor != bgColor.ordinal()) || (statusIsBlur != isBlur);
	}

	private void updateStatus() {
		statusLevel = seekbarLevel;
		statusBgColor = bgColor.ordinal();
		statusIsBlur = isBlur;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process);
		InitTextView();
		InitViewPager();
		InitImageView();

		picSelected = false;
		ivProcess = (ScaleImageView) findViewById(R.id.ivProcess);
		btnRestore = (ImageButton) findViewById(R.id.btnCancel1);
		btnUndo = (ImageButton) findViewById(R.id.btnUndo1);
		btnRedo = (ImageButton) findViewById(R.id.btnRedo1);
		btnConfirm = (ImageButton) findViewById(R.id.btnConfirm1);
		btnPickanother = (ImageView) findViewById(R.id.ivChoosepic);
		cbIsBlur = (CheckBox) listViews.get(2).findViewById(R.id.cbBlur);
		bgColor = backgroundColor_e.BG_GRAY;

		// textView = (TextView) findViewById(R.id.textView);
		// textView1 = (TextView) findViewById(R.id.textView1);

		// textView 点击更新
		// textView.getPaint().setFakeBoldText(true);
		// textView.setTextColor(Color.rgb(255, 255, 255));
		// textView_e tv_0 = textView_e.TV_SELECT_PIC;
		// fresh_textView(tv_0);

		listViews.get(2).findViewById(R.id.bgColorGray).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchColorStatus(v);
			}
		});

		listViews.get(2).findViewById(R.id.bgColorGreen).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchColorStatus(v);
			}
		});

		listViews.get(2).findViewById(R.id.bgColorBlue).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchColorStatus(v);
			}
		});

		listViews.get(2).findViewById(R.id.bgColorYellow).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchColorStatus(v);
			}
		});

		listViews.get(2).findViewById(R.id.bgColorPink).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchColorStatus(v);
			}
		});

		//未选择图片时这里设置背景
		Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.background);
		LinearLayout bklayout = (LinearLayout) findViewById(R.id.layoutProcessPic);
		bklayout.setBackground(new BitmapDrawable(image));

		// 滑动条
		seekBar = (SeekBar) listViews.get(0).findViewById(R.id.seekBar1);

		seekBar.setMax(seekbarMaxLevel);
		ivSubtraction = (ImageView) listViews.get(0).findViewById(R.id.ivSubtraction1);
		ivAdd = (ImageView) listViews.get(0).findViewById(R.id.ivAdd1);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				System.out.println("kevin Start Tracking Touch-->");
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				System.out.println("kevin Stop Tracking Touch-->");
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				System.out.println("kevin progress changed-->" + progress);
				// textView1.getPaint().setFakeBoldText(true);
				// textView1.setTextColor(Color.rgb(255, 255, 255));
				// textView 点击更新
				// textView_e tv_2 = textView_e.TV_TOUCH_POINT;
				// fresh_textView(tv_2);

				// textView1.setText(String.format("色彩保留等级: %d", progress));
				seekbarLevel = progress;
			}
		});

		ivSubtraction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (seekbarLevel > 0) {
					seekBar.setProgress(--seekbarLevel);
				}
			}
		});

		ivAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (seekbarLevel < seekbarMaxLevel) {
					seekBar.setProgress(++seekbarLevel);
				}
			}
		});

		// 判断是否已经选好图片执行不同操作
		ivProcess.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// textView_e tv_3 = textView_e.TV_CONFIRM;
				// fresh_textView(tv_3);
				if (picSelected) {
					// 已经选择了图片，处理图片在ScaleImageView中的监听函数中。
				} else {
					//pick_another_picture();
				}
			}
		});

		cbIsBlur.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getParameters();
			}
		});

		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// textView 点击更新
				// textView_e tv_1 = textView_e.TV_CHANGE_LEVEL;
				// fresh_textView(tv_1);

				ivProcess.setImageBitmapEx(showBitmap, true);
			}
		});

		btnUndo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ivProcess.undo();
			}
		});
		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Intent intent_preview = new Intent();

				intent_preview.setClass(ProcessActivity.this, MoodPreviewActivity.class);
				Bitmap image = ((BitmapDrawable) ivProcess.getDrawable()).getBitmap();
				BitmapStore.setBitmapProcessed(image);
				// ProcessActivity.this.startActivity(intent_preview);
				startActivityForResult(intent_preview, 1);
			}
		});

		 btnPickanother.setOnClickListener(new OnClickListener() {
		 @Override
		 public void onClick(View v) {
		 pick_another_picture();
		 }
		 });

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					// Log.i("chz", "process----------------");
					getParameters();
					ivProcess.setParameters(seekbarLevel, bgColor.ordinal(), isBlur);
					ivProcess.processPicture();
				}
				super.handleMessage(msg);
			}
		};

		myThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(400);
						if (statusChanged()) {
							Log.i("chz", "status changed");
							Message msg = mHandler.obtainMessage();
							msg.what = 1;
							msg.sendToTarget();
							updateStatus();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		myThread.start();
	}

	private void pick_another_picture() {
		// textView 点击更新
		textView_e tv_1 = textView_e.TV_CHANGE_LEVEL;
		// fresh_textView(tv_1);

		// TODO Auto-generated method stub
		Intent intent = new Intent();
		/* 开启Pictures画面Type设定为image */
		intent.setType("image/*");
		/* 使用Intent.ACTION_GET_CONTENT这个Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* 取得相片后返回本画面 */
		startActivityForResult(intent, 1);
	}

	// private void fresh_textView(textView_e tv_type) {
	// textView.getPaint().setFakeBoldText(true);
	// textView.setTextColor(Color.rgb(255, 255, 255));
	//
	// switch (tv_type) {
	// case TV_CONFIRM:
	// textView.setText(String.format("请点击确认按钮"));
	// break;
	// case TV_TOUCH_POINT:
	// textView.setText(String.format("请点击需要突出显示的区域可以双手放大、缩小操作区域"));
	// break;
	// case TV_CHANGE_LEVEL:
	// textView.setText(String.format("请选择处理强度级别"));
	// break;
	// case TV_SELECT_PIC:
	// default:
	// textView.setText(String.format("请选择一张图片"));
	// break;
	// }
	// }

	protected void onDestroy() {
		super.onDestroy();
		if (currentSelectedColor != null) {
			bgShape = (GradientDrawable) currentSelectedColor.getBackground();
			bgShape.setStroke(0, Color.argb(255, 255, 0, 0));
			currentSelectedColor.setBackground(bgShape);
			currentSelectedColor = null;
		}
	}

	protected void onResume() {
		super.onResume();
	};

	// 在这里设置imageview的图片，因为这时候imageview的大小才能获取到，oncreat的时候获取不到。
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (selectedNew) {
			Bitmap currentBitmap = BitmapStore.getBitmapOriginal();
			ivProcess.setImageBitmapEx(currentBitmap, picSelected);
			selectedNew = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			// Log.i("uri", uri.toString());
			ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
				align = 2 << (seekbarLevel + 1);
				showBitmap = scaleAndAlignBitmap(bm, align);
				BitmapStore.setBitmapOriginal(showBitmap);
				picSelected = true;
				selectedNew = true;
				setBlurBackground();
				// Log.i("PickpicActivity", "pick up picture ok!");
				// ivProcess.setImageBitmap(showBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("PickpicActivity", "pick up picture failed!");
		}
	}

	/*
	 * 压缩和对齐图片，便于算法处理
	 */
	private Bitmap scaleAndAlignBitmap(Bitmap bgimage, int align) {
		int alignedWidth = bgimage.getWidth();
		int alignedHeight = bgimage.getHeight();
		Matrix matrix = null;
		Bitmap scaledBitmap = bgimage;
		// 如果图片过大，压缩处理
		if (bgimage.getWidth() > PIC_MAX_WIDTH || bgimage.getHeight() > PIC_MAX_HEIGHT) {
			float wRatio = PIC_MAX_WIDTH / (float) (bgimage.getWidth());
			float hRatio = PIC_MAX_HEIGHT / (float) (bgimage.getHeight());
			float scaleRatio = wRatio > hRatio ? hRatio : wRatio;
			matrix = new Matrix();
			matrix.postScale(scaleRatio, scaleRatio);
			Log.wtf("chz", "w=" + bgimage.getWidth() + ",h=" + bgimage.getHeight() + ",ratio=" + scaleRatio);
			scaledBitmap = Bitmap.createBitmap(bgimage, 0, 0, bgimage.getWidth(), bgimage.getHeight(), matrix, true);
		}

		// 对齐
		alignedWidth = (scaledBitmap.getWidth() / align) * align;
		alignedHeight = (scaledBitmap.getHeight() / align) * align;

		// Log.wtf("chz", "w=" + alignedWidth + ",h=" + alignedHeight);
		return Bitmap.createBitmap(scaledBitmap, 0, 0, alignedWidth, alignedHeight, null, true);
	}

	//// 图像压缩-质量压缩法-只支持JPG
	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		int i = 0;

		// Log.d("kevin", "before compressImage Byte Count = " +
		// baos.toByteArray().length + "Bytes");
		while (baos.toByteArray().length / 1024 > 31) { // 循环判断如果压缩后图片是否大于31kb,大于继续压缩
			// Log.d("kevin", "图像质量压缩处理" + i + " 次。");
			++i;
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		// Log.d("kevin", "after compressImage Byte Count = " +
		// baos.toByteArray().length + "Bytes");

		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		// Log.d("kevin", "after compressImage bitmap Byte Count = " +
		// bitmap.getByteCount() + "Bytes");
		return bitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		showDialog(this, item.getItemId());
		return true;
	}

	private void showDialog(Context context, int ItemId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		switch (ItemId) {
		case R.id.action_about:
			builder.setTitle("关于");
			builder.setMessage("声明：\nCopyright © 2016 TrianglePage.\n All Rights Reserved.\n三角页工作室 版权所有");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// setTitle("你懂了");
				}
			});
			// builder.setNeutralButton("确定", new
			// DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog, int whichButton) {
			// // setTitle("你真懂了");
			// }
			// });
			builder.show();
			break;
		case R.id.action_update:
			builder.setTitle("更新");
			builder.setMessage("目前已是最新版本");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			// builder.setNeutralButton("下载", new
			// DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog, int whichButton) {
			// }
			// });
			builder.show();
			break;
		default:
			break;
		}
	}

	/****************************************
	 * tab list
	 **************************************************/
	boolean isHidden[] = {true,true,true,true};
	int currentTab = 0;
	
	/**
	 * 初始化头标
	 */
	private void InitTextView() {
		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);
		t3 = (TextView) findViewById(R.id.text3);
		t4 = (TextView) findViewById(R.id.text4);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
		t4.setOnClickListener(new MyOnClickListener(3));
		currentTab = 0;
	}
	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}
		
		private void switchRightButtonVisible(int bottomTabIndex) {
			float fromX = 0;
			float toX = 0;

			int isVisible = 0;
			if(isHidden[bottomTabIndex]) {
				//当前底边栏不可见，如果右边栏可见切出
				if(btnUndo.getVisibility() == View.VISIBLE) {
					toX = 2.0f;
					isVisible = View.INVISIBLE;
				}
			} else {
				//当前底边栏可见，如果右边栏不可见则切入
				if(btnUndo.getVisibility() == View.INVISIBLE) {
					fromX = 2.0f;
					isVisible = View.VISIBLE;
				}
			}

	        TranslateAnimation rightSwitchAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromX,
	        		Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_SELF,
	        		0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	        
	        rightSwitchAction.setDuration(300);
			btnRedo.startAnimation(rightSwitchAction);
			btnRedo.setVisibility(isVisible);
			btnUndo.startAnimation(rightSwitchAction);
			btnUndo.setVisibility(isVisible);
			btnConfirm.startAnimation(rightSwitchAction);
			btnConfirm.setVisibility(isVisible);
			btnRestore.startAnimation(rightSwitchAction);
			btnRestore.setVisibility(isVisible);

		}
		
		private void switchToolBarVisible(int index) {
			//底部工具动画坐标
			float fromY = 0;
			float toY = 0;

			int isVisible = 0;
			if(isHidden[index]) {
				fromY = 1.0f;
				isHidden[index] = false;
				isVisible = View.VISIBLE;
			} else {
				toY = 1.0f;
				isHidden[index] = true;
				isVisible = View.INVISIBLE;
			}
	        TranslateAnimation bottomSwitchAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
	        		Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
	        		fromY, Animation.RELATIVE_TO_SELF, toY);
	        
	        bottomSwitchAction.setDuration(300);
			listViews.get(index).startAnimation(bottomSwitchAction);
			listViews.get(index).setVisibility(isVisible);
			switchRightButtonVisible(index);
		}

		@Override
		public void onClick(View v) {         
			mPager.setCurrentItem(index);
			if(index == currentTab) {
				switchToolBarVisible(index);
			} else {
				if(isHidden[index]) {
					switchToolBarVisible(index);
				} else {
					switchRightButtonVisible(index);
				}
			}
			currentTab = index;
		}
	};

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		listViews.add(mInflater.inflate(R.layout.tab_card1, null));
		listViews.add(mInflater.inflate(R.layout.tab_card2, null));
		listViews.add(mInflater.inflate(R.layout.tab_card3, null));
		listViews.add(mInflater.inflate(R.layout.tab_card4, null));
		for(int i = 0; i<3; i++) {
			listViews.get(i).setVisibility(View.INVISIBLE);
		}
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		Matrix matrix = new Matrix();
		matrix.postTranslate(0, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		WindowManager wm = ProcessActivity.this.getWindowManager();
		int screanWidth = wm.getDefaultDisplay().getWidth();
		int step = screanWidth / 4;
		int fromX = 0;
		int toX = 0;

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			fromX = step * currIndex;
			toX = step * arg0;
			Log.i("chz", "fromX."+fromX+",toX."+toX);
			animation = new TranslateAnimation(fromX, toX, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			cursor.setAnimation(animation);
			/** 开始动画 */
			animation.startNow();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
}
