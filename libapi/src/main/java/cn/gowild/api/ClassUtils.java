package cn.gowild.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by SimonSun on 2019/5/23.
 */

public class ClassUtils {

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";


    public static Set<String> getClassNameInPackage(Context context, final String packageName) throws PackageManager
            .NameNotFoundException, IOException {
        final Set<String> classNames = new HashSet<>();

        // 获取dex文件地址
        List<String> dexFilePaths = getSourcePaths(context);

        for (String path : dexFilePaths) {

            DexFile dexFile = null;
            try {

                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexFile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexFile = new DexFile(path);
                }

                Enumeration<String> dexEntries = dexFile.entries();
                while (dexEntries.hasMoreElements()) {
                    String className = dexEntries.nextElement();
                    if (className.startsWith(packageName)) {
                        classNames.add(className);
                    }
                }

            } finally {
                if (dexFile != null) {
                    dexFile.close();
                }
            }


        }

        return classNames;
    }

    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); //add the default apk path

        return sourcePaths;
    }

}
