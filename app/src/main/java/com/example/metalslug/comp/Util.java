package com.example.metalslug.comp;

import java.util.Random;

/**
 * Created by iceberg on 2016/3/17.
 */
public class Util {
    public static Random random = new Random();

    // 返回一个 0~range 的随机数
    public static int rand(int range) {
        // 如果 range 为0，直接返回0
        if (range == 0) {
            return 0;
        }
        // 获取一个 0~range 之间的随机数
        return Math.abs(random.nextInt() % range);
    }
}
