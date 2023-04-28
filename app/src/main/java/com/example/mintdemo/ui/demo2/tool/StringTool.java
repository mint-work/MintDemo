package com.example.mintdemo.ui.demo2.tool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StringTool {
    /**
     * 将文本字符串转化为每行字符串
     * @param s 文本代码
     * @return List<CodeVo>
     */
    public static List<String> getCodeVoList(String s) {
        if (Objects.isNull(s)) {
            return Collections.emptyList();
        }
        List<String> vos = new ArrayList<>();
        try (InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
             BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                vos.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vos;
    }
}


