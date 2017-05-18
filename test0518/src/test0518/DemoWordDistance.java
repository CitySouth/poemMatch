/**
 * Created by Leo on 2017/5/18.
 */
/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/9 13:49</create-date>
 *
 * <copyright file="DemoWordDistance.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package test0518;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 语义距离
 * @author hankcs
 */
public class DemoWordDistance
{

    private static JSONObject datajson;
    private static ArrayList titlelist;
    private static double[] numarray;
    private static ArrayList resultlist;
    public static void main(String[] args)
    {

        load_json();
        numarray = new double[titlelist.size()];

        String[] wordArray = new String[]
                {
                        "大自然",
                        "没有人",
                        "叶子",
                        "花",
                        "种粮",
                        "植物",
                        "精致的",
                        "户外",
                        "支",
                        "明亮",
                        "樱桃木",
                        "花瓣",
                        "树",
                        "夏季",
                        "舒适的天气",
                        "纯度",
                        "花园",
                        "模糊",
                        "芽",
                        "果园",

                };
        System.out.printf("%-5s\t%-5s\t%-10s\t%-5s\n", "词A", "词B", "语义距离", "语义相似度");
        for (String a : wordArray)
        {
            for (int i = 0; i < titlelist.size(); i++)
            {
                numarray[i] += CoreSynonymDictionary.similarity(a, titlelist.get(i).toString());
                System.out.printf("%-5s\t%-5s\t%-15d\t%-5.10f\n", a, titlelist.get(i).toString(), CoreSynonymDictionary.distance(a, titlelist.get(i).toString()), CoreSynonymDictionary.similarity(a, titlelist.get(i).toString()));

            }
        }

        int[] index = SearchMaxWithIndex(numarray);

        for (int i = 0; i < index.length; i++){
            if (index[i] == -1) break;
            System.out.println(titlelist.get(index[i]));
        }

        double[] test = new double[]{2.3, 2.4, 4.6, 5.5, 2.4, 4.2, 5.5, 3,3};
        int[] ti = SearchMaxWithIndex(test);
        for (int i = 0; i < ti.length; i++){
            if (ti[i] == -1) break;
            System.out.println(ti[i]);
        }
        ArrayList results = GetResuleFromJson(index);
        for (int i = 0; i < results.size(); i++){
            System.out.println(results.get(i));
        }

    }

    private static void load_json() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/json/poem.json"));
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = br.readLine()) != null) {
                builder.append(s);
            }
            br.close();
            datajson = new JSONObject(builder.toString());
            titlelist = new ArrayList();
            for (Iterator<String> iterator = datajson.keys(); iterator.hasNext();) {
                String key = iterator.next();
                titlelist.add(key);
            }

        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] SearchMaxWithIndex(double[] arr) {
        int[] pos = new int[arr.length];
        int position = 0;
        int j = 1;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[position]) {
                position = i;
                j = 1;
            }
            else if (arr[i] == arr[position])
                pos[j++] = i;
        }
        pos[0] = position;

        if (j < arr.length) pos[j] = -1;
        return pos;
    }

    private static ArrayList GetResuleFromJson(int[] pos) {
        resultlist = new ArrayList();
        for (int i = 0; i < pos.length; i++){
            if (pos[i] == -1) break;
            JSONArray jsonArray = new JSONArray();
            jsonArray = datajson.getJSONArray(titlelist.get(pos[i]).toString());
            for (int j = 0; j < jsonArray.length(); j++){
                resultlist.add(jsonArray.getString(j));
            }
        }
        return resultlist;
    }

}
