package com.example.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TxtToCsv {

    private static final String currentPath = System.getProperty("user.dir");
    private static final String readFilePath = currentPath+"/酿酒酵母蛋白质.txt";
    private static final String targetFilePath = currentPath + "/酿酒酵母蛋白质.csv";

    public static void main(String[] args) throws IOException {

        String txt = readFileAll(readFilePath);
        String result = txtToCsvFormatString(txt);
        System.out.println(result);
        write(result, targetFilePath);
    }

    /**
     * 正则转换csv
     *
     * @param sourceTxt 源文件文本内容
     * @return 处理后的文本内容
     */
    public static String txtToCsvFormatString(String sourceTxt) {

        String tempStr = sourceTxt.replaceFirst(">", "").replaceAll("\r\n", "");
        List<String> list = Arrays.asList(tempStr.split(">"));
        String reg = "([A-Za-z]+)([0-9]+)([A-Za-z]+)";
        Pattern pattern = Pattern.compile(reg);

        List<String> resultList = list.stream().map(str -> {
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group(1) + matcher.group(2) + "," + matcher.group(3);
            } else return "";
        }).collect(Collectors.toList());

        return resultList.stream().reduce("", ((s, s2) -> s.trim() + "\r\n" + s2));
    }

    public static String readFileAll(String filePath) throws IOException {
        return readFileAll(new File(filePath), StandardCharsets.UTF_8);
    }

    public static String readFileAll(File file) throws IOException {
        return readFileAll(file, StandardCharsets.UTF_8);
    }

    /**
     * @param file    读取的文件
     * @param charset 字符集
     */
    public static String readFileAll(File file, Charset charset) throws IOException {
        Path path = file.toPath();
        //一个文本文件如果已经大于int最大值，这种文件一般来说很少见有可能是log文件
        if (file.length() <= Integer.MAX_VALUE - 8) {
            //使用nio提高读取速度
            try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ)) {
                ByteBuffer byteBuffer = ByteBuffer.allocate((int) in.size());
                in.read(byteBuffer);
                return new String(byteBuffer.array(), charset);
            }
        }
        StringBuilder msg = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            for (; ; ) {
                String line = reader.readLine();
                if (line == null)
                    break;
                msg.append(line);
            }
        }
        return msg.toString();
    }

    /**
     * 写文件
     *
     * @param txt            文本内容
     * @param targetFilePath 目标文件路径
     */
    public static void write(String txt, String targetFilePath) throws IOException {
        Files.writeString(Paths.get(targetFilePath), txt, StandardCharsets.UTF_8);
    }
}
