package java.spy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpyApi {

    public static Map<String,String> map = new ConcurrentHashMap<String, String>();

    static {
        map.put("asd","123");
    }

    public static void put(String key,String value){
        map.put(key,value);
    }

    public static Set getKeySet(){
        return map.keySet();
    }

    public static Collection<String> getValueSet(){
        return map.values();
    }
}

