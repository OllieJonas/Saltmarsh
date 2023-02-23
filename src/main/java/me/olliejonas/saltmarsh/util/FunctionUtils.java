package me.olliejonas.saltmarsh.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FunctionUtils {

    public void repeat(Runnable runnable, int times) {
        for (int i = 0; i < times; i++) {
            runnable.run();
        }
    }

}
