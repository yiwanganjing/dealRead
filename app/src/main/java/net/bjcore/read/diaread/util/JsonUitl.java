package net.bjcore.read.diaread.util;


import android.content.Context;
import android.support.annotation.RawRes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUitl {

    public static String getJson(Context context,@RawRes int id){
        InputStream is = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  sb.toString();
    }
}
