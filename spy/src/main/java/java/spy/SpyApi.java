package java.spy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpyApi {

    public static Map<String,String> map = new ConcurrentHashMap<String, String>();

    public static Map<String,Object> map2 = new ConcurrentHashMap<String, Object>();

    static {
        map.put("asd","123");
    }

    public static void put(String key,String value){
        map.put(key,value);
    }

    public static Set getKeySet(){
        return map.keySet();
    }

    public static int getSize(){
        return map.size();
    }

    public static void put2(String key,Object value){
        map2.put(key,value);
    }
    public static Object getObject(){
        return map2.get("object");
    }

    public static Collection<String> getValueSet(){
        return map.values();
    }
}

