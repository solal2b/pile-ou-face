package com.example.pileface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Une pièce dessinée au Canvas. Le lancer fait monter la pièce, la fait tourner
 * sur elle-même (nombre entier de tours), puis la fait retomber avec deux
 * petits rebonds. Comme le nombre de demi-tours est toujours pair, la pièce
 * retombe systématiquement sur PILE.
 */
public class CoinView extends View {

    public interface Listener {
        void onLanded();
    }

    private static final long TOSS_MS = 2200;
    private static final long BOUNCE1_MS = 280;
    private static final long BOUNCE2_MS = 160;
    private static final long TOTAL_MS = TOSS_MS + BOUNCE1_MS + BOUNCE2_MS;

    private final Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Random random = new Random();

    /** Rotation autour de l'axe horizontal, en degrés. 0 (ou 360, 720...) = PILE visible. */
    private float flipDegrees = 0f;
    /** Hauteur de la pièce, de 0 (au sol) à 1 (apogée). */
    private float lift = 0f;
    private boolean flying = false;
    private ValueAnimator animator;
    private Listener listener;

    public CoinView(Context context) {
        super(context);

        edgePaint.setColor(0xFFB8860B);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(0xFFB8860B);

        textPaint.setColor(0xFF8A6508);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        shadowPaint.setColor(0xFF000000);

        setContentDescription("Pièce de monnaie");
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isFlying() {
        return flying;
    }

    private float radius() {
        return Math.min(getWidth(), getHeight() * 0.6f) * 0.30f;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float r = radius();
        if (r <= 0f) {
            return;
        }
        // Les coordonnées du dégradé sont relatives au centre de la pièce (on y translate le canvas).
        facePaint.setShader(new RadialGradient(
                -r * 0.3f, -r * 0.3f, r * 1.4f,
                0xFFFFE9A0, 0xFFD4A017, Shader.TileMode.CLAMP));
        ringPaint.setStrokeWidth(r * 0.06f);
    }

    /** Lance la pièce. Sans effet si elle est déjà en l'air. */
    public void toss() {
        if (flying) {
            return;
        }
        flying = true;

        // 5 à 8 tours complets : multiple de 360° => on retombe toujours sur PILE.
        final float totalFlip = 360f * (5 + random.nextInt(4));

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(TOTAL_MS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            float ms = a.getAnimatedFraction() * TOTAL_MS;
            if (ms < TOSS_MS) {
                float u = ms / TOSS_MS;
                lift = 4f * u * (1f - u);
                flipDegrees = totalFlip * (1f - (1f - u) * (1f - u));
            } else if (ms < TOSS_MS + BOUNCE1_MS) {
                float v = (ms - TOSS_MS) / BOUNCE1_MS;
                lift = 0.14f * 4f * v * (1f - v);
                flipDegrees = totalFlip;
            } else {
                float v = (ms - TOSS_MS - BOUNCE1_MS) / BOUNCE2_MS;
                lift = 0.05f * 4f * v * (1f - v);
                flipDegrees = totalFlip;
            }
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                flipDegrees = 0f; // même face que 360 * n : PILE
                lift = 0f;
                flying = false;
                invalidate();
                if (listener != null) {
                    listener.onLanded();
                }
            }
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.removeAllListeners();
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
            flying = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float r = radius();
        if (r <= 0f) {
            return;
        }

        float cx = w / 2f;
        float restY = h * 0.52f;
        float travel = h * 0.38f;
        float cy = restY - lift * travel;

        // Ombre au sol : plus petite et plus claire quand la pièce est haute.
        float shadowY = restY + r * 1.35f;
        float shadowRx = r * (1f - 0.45f * lift);
        float shadowRy = r * 0.14f * (1f - 0.45f * lift);
        shadowPaint.setAlpha((int) (80f * (1f - 0.6f * lift)));
        rect.set(cx - shadowRx, shadowY - shadowRy, cx + shadowRx, shadowY + shadowRy);
        canvas.drawOval(rect, shadowPaint);

        double rad = Math.toRadians(flipDegrees);
        float cos = (float) Math.cos(rad);
        float s = Math.abs(cos);
        boolean showingPile = cos >= 0f;

        // Tranche de la pièce, visible surtout quand elle est de profil.
        float edgeH = r * 0.14f * (1f - s);
        if (edgeH > 0.5f) {
            rect.set(cx - r, cy - edgeH / 2f, cx + r, cy + edgeH / 2f);
            canvas.drawRoundRect(rect, edgeH / 2f, edgeH / 2f, edgePaint);
        }

        // Face visible, écrasée verticalement selon l'angle.
        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(1f, Math.max(s, 0.001f));

        canvas.drawCircle(0f, 0f, r, facePaint);
        canvas.drawCircle(0f, 0f, r * 0.84f, ringPaint);

        if (showingPile) {
            drawCentered(canvas, "1", 0f, -r * 0.08f, r * 0.95f);
            drawCentered(canvas, "PILE", 0f, r * 0.50f, r * 0.26f);
        } else {
            drawCentered(canvas, "FACE", 0f, 0f, r * 0.42f);
        }

        canvas.restore();
    }

    /** Dessine un texte centré en (x, y) ; le canvas est déjà translaté au centre de la pièce. */
    private void drawCentered(Canvas canvas, String text, float x, float y, float size) {
        textPaint.setTextSize(size);
        float baseline = y - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(text, x, baseline, textPaint);
    }
}
