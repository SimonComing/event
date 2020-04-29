package com.gowild.eremite.am;

import android.content.ComponentName;
import android.content.Context;

import com.gowild.eremite.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 解析EreActivityManifest.xml
 *
 * @author Simon
 * @data: 2017/6/15 10:16
 * @version: V1.0
 */


public class EreActivityParser {


    private final String MANIFEST = "manifest";
    private final String EREACTIVITY = "eraActivity";
    private final String LEVEL = "level";
    private final String ACTION = "action";
    private Context mContext;

    EreActivityParser(Context context){
        mContext = context;
    }

    void parseBaseInfo(HashMap<ComponentName, EreActivityInfo> activities, HashMap<String, EreActivityInfo> actionsActivities) {
        try {
            XmlPullParserFactory xmlPactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullparser = xmlPactory.newPullParser();
            xmlPullparser.setInput(mContext.getResources().openRawResource(R.raw.ereactivitymanifest), "UTF-8");
            int event = xmlPullparser.getEventType();
            EreActivityInfo ereActivityInfo = null;
            String pkg = "";
            ArrayList<String> actions = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullparser.getName();

                switch (event) {
                    // 开始文档
                    case XmlPullParser.START_DOCUMENT: {
                        break;
                    }
                    // 标签开始
                    case XmlPullParser.START_TAG: {
                        if (MANIFEST.equals(nodeName)) {
                            pkg = xmlPullparser.getAttributeValue(0);
                        } else if (EREACTIVITY.equals(nodeName)) {
                            ereActivityInfo = new EreActivityInfo();
                            String name = xmlPullparser.getAttributeValue(0);
                            ereActivityInfo.name = name;
                            ereActivityInfo.packageName = pkg;
//                            activities.put(new ComponentName(pkg, name), ereActivityInfo);
                        } else if (LEVEL.equals(nodeName)) {
                            ereActivityInfo.launchLevel = Integer.parseInt(xmlPullparser.nextText());
                        } else if (ACTION.equals(nodeName)) {
                            String action = xmlPullparser.getAttributeValue(0);
                            ereActivityInfo.addAction(action);
                            actions.add(action);
                        }
                        break;
                    }
                    // 标签结束
                    case XmlPullParser.END_TAG: {
                        if(EREACTIVITY.equals(nodeName)){
                            activities.put(new ComponentName(pkg, ereActivityInfo.name), ereActivityInfo);
                            for(String action : actions){
                                actionsActivities.put(action, new EreActivityInfo(ereActivityInfo));
                            }
                            actions.clear();
                        }
                        break;
                    }
                }
                // 下一个标签
                event = xmlPullparser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
