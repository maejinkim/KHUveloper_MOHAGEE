package com.example.maedin.mohagee.activity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ServerThread extends Thread {

    private Handler thHandler = null; // 쓰레드 핸들러
    private Handler fgHandler = null; // 프래그먼트 핸들러
    private Queue<Location_information> location;
    private String time;
    private String Lat, Lng;
    private String with_who;
    private final String cal_url = "http://163.180.116.251:8080/mohagi/recommend";
    private URL url = null;
    private BufferedReader in = null;
    private String inLine = "";
    String xml = null;

    public Handler getFgHandler() {
        return thHandler;
    }
    public ServerThread(Handler h) {
        fgHandler = h;
        location = new LinkedList<>();
    }

    public void initiate(String time, Double Lat, Double Lng, String with_who, ArrayList<Location_information> locations)
    {
        this.time = time;
        this.Lat = String.valueOf(Lat);
        this.Lng = String.valueOf(Lng);
        this.with_who = with_who;
        for(int i = 0 ; i<locations.size() ; i++)
        {
            location.add(locations.get(i));
        }
    }

    @Override
    public void run()
    {
        Looper.prepare();
        thHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                while(location.size() != 0) {
                    Location_information temp = location.poll();
                    String bigtype = temp.getBigtype();
                    String smalltype = temp.getSmalltype();
                    String theme1 = "", theme2 = "";
                    for (int i = 0; i < temp.getThemes().size(); i++) {
                        if (i == 0) {
                            theme1 = temp.getThemes().get(i);
                        }
                        if (i == 1) {
                            theme2 = temp.getThemes().get(i);
                        }
                    }

                    Message retMsg = new Message();

                    try{
                        url = new URL(cal_url + "&Lat=" + Lat + "&Lng=" + Lng + "&with_who=" + with_who + "&bigtype=" + bigtype
                        +"&smalltype=" + smalltype + "&theme1=" +theme1 + "&theme2"+theme2);
                        Log.d("Thread_in", "1");
                    }catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                        Log.d("THEREAD_ERR", "thread_error2");

                    }
                    try{
                        in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        Log.d("Thread_in", in.toString());

                    } catch(IOException e)
                    {
                        e.printStackTrace();
                        Log.d("THEREAD_ERR", "thread_error3");

                    }

                    try {
                        while ((inLine = in.readLine()) != null) {
                            xml = inLine;
                        }
                        Log.d("Thread_in", "3");

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("THEREAD_ERR", "thread_error4");
                    }

                    try {
                        in.close();
                        Log.d("Thread_in", "4");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("THREAD_ERR", "thread_error5");
                    }
                    Log.d("check_this", "hihi");
                    Log.d("XMLRET", xml + "<-");
                    //jsonArray.put(xml);
                    retMsg.obj = xml;
                    fgHandler.sendMessage(retMsg);

                }
            }

        };
        Looper.loop();
    }




}
