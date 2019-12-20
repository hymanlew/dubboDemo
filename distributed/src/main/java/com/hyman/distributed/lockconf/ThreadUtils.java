package com.hyman.distributed.lockconf;

import java.util.HashMap;
import java.util.Map;

public class ThreadUtils {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private Map<String, ThreadLocal> threadLocalMap = new HashMap<>();

    public static void setLockkey(String lockKey) {
        threadLocal.set(lockKey);
    }

    public static String getlockKey(String lockKey) {
        Object o = threadLocal.get();
        if(o == null){
            return null;
        }else if(lockKey.equals(o)) {
            return "1";
        }
        return null;
    }

}
