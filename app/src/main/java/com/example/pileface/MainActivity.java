package com.example.pileface;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int GOLD = 0xFFFFD35A;
    private static final int WHITE = 0xFFFFFFFF;

    private CoinView coin;
    private TextView result;
    private TextView hint;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF14172B);

        coin = new CoinView(this);
        root.addView(coin, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText("Pile ou Face");
        title.setTextColor(WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        titleLp.topMargin = dp(32);
        root.addView(title, titleLp);

        result = new TextView(this);
        result.setTextColor(GOLD);
        result.setTextSize(36);
        result.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams resultLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        resultLp.bottomMargin = dp(120);
        root.addView(result, resultLp);

        hint = new TextView(this);
        hint.setText("Touchez l'écran pour lancer la pièce");
        hint.setTextColor(0xFFB8BCD6);
        hint.setTextSize(16);
        FrameLayout.LayoutParams hintLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        hintLp.bottomMargin = dp(48);
        root.addView(hint, hintLp);

        // Android 15 force l'affichage bord à bord : on compense avec les marges système.
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());
            return insets;
        });

        coin.setListener(() -> {
            result.setText("PILE !");
            hint.setText("Touchez pour relancer");
        });

        root.setOnClickListener(v -> launchCoin());
        setContentView(root);
    }

    private void launchCoin() {
        if (coin.isFlying()) {
            return;
        }
        result.setText("");
        hint.setText("…");
        coin.toss();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
