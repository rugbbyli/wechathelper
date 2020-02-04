package cc.oldrivers.groupliker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Demo implements IXposedHookLoadPackage {

    boolean hookedTL = false;
    boolean hookedBi = false;
    BaseAdapter snsActivityAdapter;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        //XposedBridge.log("[groupliker]load package: " + lpparam.packageName + ", progress: " + lpparam.processName);
        String pkgName = lpparam.packageName;
        String wxPkgName = "com.tencent.mm";
        if(!pkgName.equals(wxPkgName) || !lpparam.processName.equals(wxPkgName)) {
            return;
        }
        hookedBi = false;
        hookedTL = false;

        XposedBridge.log("[groupliker] opening wx :" + pkgName + ", process: " + lpparam.processName);

        final String snsTimeLineUIClass = "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI";
        final String biClass = "com.tencent.mm.plugin.sns.ui.bi";

        findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) return;

                Class<?> cls = (Class<?>) param.getResult();
                String name = cls.getName();

                if(name.startsWith("com.tencent.mm.plugin.sns")) {
                    //Log.i("[groupliker]", "load class:" + name);
                }

                if (!hookedTL && snsTimeLineUIClass.equals(name)) {
                    //hook timeLineActivity onResume method
                    XC_MethodHook.Unhook ret = findAndHookMethod(cls, "onResume", new XC_MethodHook() {
                        /*@Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("[groupliker] before opening wx group: " + param.method);
                        }*/

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.i("[groupliker]", " opening wx group?");
                            try {
                                snsActivityAdapter = GetFieldOf(param.thisObject, param.thisObject.getClass(), "udO");
                                //Object list = GetFieldOf(adapter, adapter.getClass(), "ujY");

                                Log.i("[groupliker]", " opening wx group hook working finish");
                            } catch (Exception e) {
                                Log.e("[groupliker]", "opening wx group hook working error:" + e.getLocalizedMessage(), e);
                            }
                        }
                    });
                    Log.i("[groupliker]", " hook finish 1, result: " + ret.getHookedMethod().getName());
//
//                    ret = findAndHookMethod(cls, "ec", View.class, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            Log.i("[groupliker]", "invoke ec");
//                        }
//                    });
//                    Log.i("[groupliker]", " hook finish 2, result: " + ret.getHookedMethod().getName());


                    //liker view id: 2131893971
                    //action view id: 2131886608
                    //toggle action view id: 2131893889

//                    ret = findAndHookMethod(cls, "a", biClass, View.class, View.class, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            int length = param.args.length;
//                            if(length == 3 && param.args[length - 1].getClass().isInstance(LinearLayout.class)) {
//                                LinearLayout actionView = (LinearLayout) param.args[length - 1];
//                                TextView textView = new TextView(actionView.getContext());
//                                textView.setText("已阅");
//                                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                                textView.setTextSize(20);
//                                textView.setPadding(5, 0, 5, 0);
//                                actionView.addView(textView, 0);
//                            }
//                            else {
//                                Log.i("[groupliker]", "invoke bi.a, param count: " + length + ", last param: " + param.args[length - 1].getClass().getName());
//                            }
//
//                        }
//                    });
//
//                    Log.i("[groupliker]", " hook finish 3, result: " + ret.getHookedMethod().getName());

                    hookedTL = true;
                }

                if(!hookedBi && biClass.equals(name)) {
                    Log.i("[groupliker]", "start hook bi.a");
                    XC_MethodHook.Unhook ret = findAndHookMethod(cls, "a", biClass, View.class, View.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            int length = param.args.length;
                            Object lastParam = param.args[length - 1];
                            //Log.i("[groupliker]", "length:" + length + ",last param is linearLayout:" + (lastParam instanceof LinearLayout));
                            final View snsActionToggleBtn = (View)param.args[length - 2];
                            Log.i("[groupliker]", "second param name:" + snsActionToggleBtn.getTag().toString());
                            if(length == 3 && (lastParam instanceof LinearLayout)) {
                                LinearLayout actionView = (LinearLayout) lastParam;
                                //Log.i("[groupliker]", "actionView:" + actionView.getId());
                                TextView textView = new TextView(actionView.getContext());
                                textView.setText(" 已 阅 ");
                                textView.setLayoutParams(new LinearLayout.LayoutParams(140, ViewGroup.LayoutParams.WRAP_CONTENT));
                                textView.setTextSize(16);
                                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                textView.setTextColor(Color.WHITE);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Runnable logger = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Object firstItem = snsActivityAdapter.getItem(0);
                                                    Log.i("[groupliker]", "adapter item count: " + snsActivityAdapter.getCount());
                                                    PrintWxObject(firstItem, null, "adapter item");
                                                    Log.i("[groupliker]", "print finish inside thread");
                                                } catch (Exception e) {
                                                    Log.e("[groupliker]", "error: " + e.getLocalizedMessage(), e);
                                                }
                                            }
                                        };
                                        new Thread(logger).run();
//                                        try {
//                                            Object snsItemViewHolder = ((View)snsActionToggleBtn).getTag();
//                                            Class cls = snsItemViewHolder.getClass();
//                                            while (!cls.getSimpleName().equals("BaseViewHolder")) cls = cls.getSuperclass();
//                                            //PrintWxObject(snsItemViewHolder, cls);
//
//                                            LinearLayout snsLikesView = GetFieldOf(snsItemViewHolder, cls, "uox");
//                                            Object timeLineObject = GetFieldOf(snsItemViewHolder, cls, "timeLineObject");
//                                            PrintWxObject(timeLineObject, null);
//
//                                            /*f = snsLikesView.getClass().getDeclaredField("tYP");
//                                            f.setAccessible(true);
//                                            TextView likesText = (TextView)f.get(snsLikesView);*/
//
//                                            if(snsLikesView == null) {
//                                                Log.i("[groupliker]", "error, snsLikesView is null");
//                                                return;
//                                            }
//                                            TextView likesText = (TextView)snsLikesView.findViewById(2131893971);
//                                            if(likesText == null) {
//                                                //no one like it..
//                                                Log.i("[groupliker]", "error, likesText is null");
//                                                return;
//                                            }
//                                            SpannableString rawStr = (SpannableString)likesText.getText();
//
//                                            Object[] spans = rawStr.getSpans(rawStr.length() - 3, rawStr.length() - 1, Object.class);
//                                            Log.i("[groupliker]", "raw span length:" + spans.length);
//
//                                            SpannableString appendStr = new SpannableString(", " + String.join(", ", RandomPick(names, 10)));
//                                            appendStr.setSpan(new StyleSpan(Typeface.BOLD), 0, appendStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                            for (Object span : spans) {
//                                                appendStr.setSpan(span, 0, appendStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                                Log.i("[groupliker]","apply span: " + span.toString());
//                                            }
//                                            likesText.append(appendStr);
//
//                                            /*try{
//                                                Method setOriginFunc = GetClass(likesText.getClass(), "SnsTextView").getDeclaredMethod("setOriginText", String.class);
//                                                setOriginFunc.invoke(likesText, likesText.getText().toString());
//                                            } catch(Exception e) {
//                                                Log.e("[groupliker]", "find method setOriginText error.");
//                                            }*/
//
//                                            /*CharSequence rawStr = likesText.getText();
//                                            boolean hasLikers = rawStr.length() > 0;
//                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(rawStr);
//                                            if(hasLikers) spannableStringBuilder.append(",");
//                                            spannableStringBuilder.append(" 习大大");
//                                            likesText.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);*/
//
//
//                                        /*} catch (NoSuchFieldException e) {
//                                            Log.e("[groupliker]", e.getLocalizedMessage(), e);
//                                        } catch (IllegalAccessException e) {
//                                            Log.e("[groupliker]", e.getLocalizedMessage(), e);*/
//                                        } catch (Exception e) {
//                                            Log.e("[groupliker]", e.getLocalizedMessage(), e);
//                                        }
                                    }
                                });
                                actionView.addView(textView);
                                Log.i("[groupliker]", "work finish");
                            }
                            else {
                                Log.i("[groupliker]", "check error. param count: " + length + ", last param: " + param.args[length - 1].getClass().getName());
                            }

                        }
                    });

                    Log.i("[groupliker]", " hook finish 4, result: " + ret.getHookedMethod().getName());

                    hookedBi = true;
                }
            }
        });

    }

    private final String wxClassPrefix = "com.tencent.mm";
    private void PrintWxObject(Object object, Class cls, String tag) {
        if(object == null) {
            Log.i("[groupliker]", tag + ": null" );
            return;
        }

        if(cls == null) cls = object.getClass();

        if(object instanceof Collection) {
            Collection collection = (Collection)object;
            Optional firstItem = collection.stream().findFirst();
            PrintWxObject(firstItem.orElse(null), null, tag + "[0]");
        }
        else if(object instanceof Map) {
            Map map = (Map)object;
            //Optional firstItem = map.values().stream().findFirst();
            Optional<Map.Entry> firstItem = map.entrySet().stream().findFirst();
            if(firstItem.isPresent()) {
                PrintWxObject(firstItem.get().getValue(), null, tag + "[" + firstItem.get().getKey() + "]");
            }
            else {
                PrintWxObject(null, null, tag);
            }
        }
        else if(cls.getName().startsWith(wxClassPrefix)) {
            Log.i("[groupliker]", "=============>>>>> print wxobject begin: [" + cls.getName() + "] -> " + tag);
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    PrintWxObject(value, null, field.getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            Log.i("[groupliker]", "=============>>>>> print wxobject end -> " + tag);
        }
        else {
            Log.i("[groupliker]", tag + "[" + cls.getName() + "]: " + object.toString());
        }

    }

    private Collection toCollection(Object object) {
        return object instanceof Collection ? (Collection)object : null;
    }

    private Class GetClass(Class cls, String shortName) {
        while (cls != null && cls.getSimpleName() != shortName) cls = cls.getSuperclass();
        return cls;
    }

    private <T> T GetFieldOf(Object object, Class cls, String field) {
        Field f = null;
        try {
            f = cls.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }

        f.setAccessible(true);

        try {
            return (T)f.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> List RandomPick(T[] list, int count) {

        List rdList = Arrays.asList(list);
        Collections.shuffle(rdList);

        return rdList.subList(0, count);
    }

    private final String[] names = {
            "何足道","鹤笔翁","郭靖","莫声谷","阳顶天","赵敏","小昭","郭破虏","韦一笑","周芷若",
            "俞岱岩","灭绝师太","张无忌","郭襄","黄衫女子","胡青牛","杨不悔","裘千丈","穆念慈","刘瑛",
            "梅超风","华筝","周伯通","黄蓉","独孤求败","欧阳峰","洪七公","裘千仞","武三通","杨康",
            "黄药师","玄苦","段正明","虚竹","慕容复","李秋水","段誉","阿紫","钟灵","阿朱",
            "丁春秋","玄难","天山童姥","王语嫣","木婉清","乔峰","左冷禅","向问天","莫大","蔡子峰",
            "林平之",
            "杨过","小龙女","李莫愁"
    };
}
//com.tencent.wcdb.database.SQLiteDatabase.insert
//com.tencent.wcdb.database.SQLiteDatabase.update
//com.tencent.mm.ui.MMActivity.onResume
//com.tencent.mm.plugin.sns.ui.SnsTimeLineUI.onResume