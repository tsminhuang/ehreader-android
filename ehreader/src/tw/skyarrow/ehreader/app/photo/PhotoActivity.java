package tw.skyarrow.ehreader.app.photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.util.ActionBarHelper;

public class PhotoActivity extends ActionBarActivity implements View.OnSystemUiVisibilityChangeListener {
    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_PAGE = "page";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private static final boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static final boolean IS_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    private static final int UI_HIDE_DELAY = 3000;

    private View decorView;
    private Handler systemUIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);

        // Set up toolbar
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this);
        systemUIHandler = new SystemUIHandler(this);

        // Read arguments
        Bundle args = getIntent().getExtras();
        long galleryId = args.getLong(EXTRA_GALLERY_ID);
        int galleryPage = args.getInt(EXTRA_PAGE);

        // Attach fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment lastFragment = fm.findFragmentByTag(PhotoFragment.TAG);

        if (lastFragment == null){
            ft.replace(R.id.frame, PhotoFragment.newInstance(galleryId, galleryPage), PhotoFragment.TAG);
        } else {
            ft.attach(lastFragment);
        }

        ft.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ActionBarHelper.upNavigation(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    public void hideSystemUI(){
        systemUIHandler.removeMessages(0);

        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;

        if (IS_JELLY_BEAN){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (IS_KITKAT){
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    @SuppressLint("NewApi")
    public void showSystemUI(){
        int uiOptions = 0;

        if (IS_JELLY_BEAN){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        if (IS_KITKAT){
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        decorView.setSystemUiVisibility(uiOptions);
        delayedHideSystemUI(UI_HIDE_DELAY);
    }

    public void toggleUIVisibility(){
        if (isUIVisible()){
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    @SuppressLint("NewApi")
    public boolean isUIVisible(){
        if (IS_JELLY_BEAN){
            return (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
        } else {
            return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
        }
    }

    private void delayedHideSystemUI(int delay){
        systemUIHandler.removeMessages(0);
        systemUIHandler.sendEmptyMessageDelayed(0, delay);
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        //
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus){
            delayedHideSystemUI(UI_HIDE_DELAY);
        } else {
            systemUIHandler.removeMessages(0);
        }
    }

    private static class SystemUIHandler extends Handler {
        private final WeakReference<PhotoActivity> mActivity;

        private SystemUIHandler(PhotoActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PhotoActivity activity = mActivity.get();
            if (activity == null) return;

            activity.hideSystemUI();
        }
    }
}