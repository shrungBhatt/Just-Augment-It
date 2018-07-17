/*
 * !***************************************************************************
 * @File GLSurfaceView/PVRShellView.java
 * @Title GLSurfaceView/PVRShellView.java
 * @Date 10/02/2010
 * @Copyright Copyright (C) by Imagination Technologies Limited.
 * @Platform Android
 * @Description Handles the GL and EGL initialisation
 * ***************************************************************************
 */
package com.projects.shrungbhatt.blitzzardemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class CustomSurfaceView extends GLSurfaceView {

    public static final String TAG = "WTGLSurfaceView";
    private GLRenderer mRenderer;
    private float mDisplayMetricsDensity;
    private float mPreviousX;
    private float mPreviousY;

    public enum TargetRenderingAPI{
        OPENGL_ES_2, OPENGL_ES_3
    }

    public CustomSurfaceView(final Context context, final GLRenderer renderer,final float displayMetricsDensity, final TargetRenderingAPI... targetRenderingAPIs) {
        this(context, renderer, null, targetRenderingAPIs);

        mDisplayMetricsDensity  = displayMetricsDensity;
    }

    public CustomSurfaceView(final Context context, final GLRenderer renderer, final AttributeSet attrs, final TargetRenderingAPI... targetRenderingAPIs) {
        super(context, attrs);

        if (CustomSurfaceView.this.getContext() == null || CustomSurfaceView.this.getContext() instanceof Activity && ((Activity) CustomSurfaceView.this.getContext()).isFinishing()) {
            return;
        }

        List<TargetRenderingAPI> targetRenderingAPIList = Arrays.asList(targetRenderingAPIs);

        mRenderer = renderer;

        final boolean tryOpenGlEs3_0 = targetRenderingAPIList.contains(TargetRenderingAPI.OPENGL_ES_3);
        final boolean tryOpenGlEs2_0 = targetRenderingAPIList.contains(TargetRenderingAPI.OPENGL_ES_2);

        setEGLContextFactory(new EGLContextFactory() {
            private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            private final float GL_VERSION_3_0 = 3.0f;
            private final float GL_VERSION_2_0 = 2.0f;

            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                /** OpenGLES 3.0 was introduced with android 4.3(18). */
                if (tryOpenGlEs3_0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    EGLContext gles3Context = createOpenGL3Context(egl, display, eglConfig);

                    if (gles3Context.equals(EGL10.EGL_NO_CONTEXT) && tryOpenGlEs2_0) {
                        return createOpenGL2Context(egl, display, eglConfig);
                    }
                    else {
                        if (gles3Context.equals(EGL10.EGL_NO_CONTEXT)) {
                            Log.e(TAG, "createContext: OpenGL ES 3 context could not be created.");
                        }
                        return gles3Context;
                    }
                } else {
                    return createOpenGL2Context(egl, display, eglConfig);
                }
            }

            private EGLContext createOpenGL3Context(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                Log.i(TAG, "creating OpenGL ES " + GL_VERSION_3_0 + " context");
                int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, (int) GL_VERSION_3_0, EGL10.EGL_NONE };
                return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            }
            private EGLContext createOpenGL2Context(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                Log.i(TAG, "creating OpenGL ES " + GL_VERSION_2_0 + " context");
                int[] attrib_list_2_0 = {EGL_CONTEXT_CLIENT_VERSION, (int) GL_VERSION_2_0, EGL10.EGL_NONE };
                return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_2_0);
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                egl.eglDestroyContext(display, context);
            }
        });
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (mRenderer != null) {
                    float deltaX = (x - mPreviousX) / mDisplayMetricsDensity / 2f;
                    float deltaY = (y - mPreviousY) / mDisplayMetricsDensity / 2f;

                    mRenderer.mDeltaX += deltaX;
                    mRenderer.mDeltaY += deltaY;
                }
            }

            mPreviousX = x;
            mPreviousY = y;

            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }

}
