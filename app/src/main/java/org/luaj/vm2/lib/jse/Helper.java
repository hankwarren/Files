package org.luaj.vm2.lib.jse;

/**
 * Created by hank on 9/27/17.
 *  Taken from a Stackoverflow post titled: LuaJ and Android: cannot bind class
 *
 *  LuaJ 3.0.x changed the implementation so it is not able to bind with stuff in
 *  dex files. So I am trying this shim to see if I can use the standard library.
 *  Otherwise, I have to modify that library and then carry it around forever.
 */

public class Helper {
    public static JavaClass forClass(Class c) {
        return JavaClass.forClass(c);
    }

    public Class<JavaClass> huskClass() {
        return JavaClass.class;
    }
}
