package com.app.nao.photorecon.ui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateManager {
    public static String getLocalDate() {
        LocalDateTime nowDate = LocalDateTime.now();
        // System.out.println(nowDate); //2020-12-20T13:32:48.293

        DateTimeFormatter dtf1 =
                DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return dtf1.format(nowDate);
    }

    public static String getLocalDateFormatH() {
        LocalDateTime nowDate = LocalDateTime.now();
        // System.out.println(nowDate); //2020-12-20T13:32:48.293

        DateTimeFormatter dtf1 =
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return dtf1.format(nowDate);
    }
}
//        // 現在日時を取得
//        LocalDateTime nowDate = LocalDateTime.now();
//       //  System.out.println(nowDate); //2020-12-20T13:32:48.293
//
//        // 表示形式を指定
//        DateTimeFormatter dtf1 =
//                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"); // ①
//        String formatNowDate = dtf1.format(nowDate); // ②
//        // System.out.println(formatNowDate); // 2020/12/20 13:32:48.293
//
//        // 表示形式を指定
//        DateTimeFormatter dtf2 =
//                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分ss秒 E曜日");
//        String formatNowDate = dtf2.format(nowDate);
//        // System.out.println(formatNowDate); // 2020年12月20日 13時32分48秒 日曜日
//
//        // 表示形式を指定
//        DateTimeFormatter dtf3 =
//                DateTimeFormatter.ofPattern("yyyyMMddHHmm");
//        String formatNowDate = dtf3.format(nowDate);
//        // System.out.println(formatNowDate); // 202012201332
